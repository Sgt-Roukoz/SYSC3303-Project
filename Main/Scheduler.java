package Main;

import com.sun.nio.sctp.PeerAddressChangeNotification;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Garrison Su
 * @author Marwan Zeid
 * @version 2024-02-24
 */

public class Scheduler implements Runnable {
    private DatagramSocket sendReceiveSocket;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;
    private SchedulerStoreInt store;
    private Map<Integer, LinkedList<Integer>> sourceFloors;
    private Map<Integer, LinkedList<Integer>> destFloors;
    private Map<Integer, String> lastKnownDirection;
    private Map<Integer, Map<Integer, Integer>> srcDestPairs;
    private  Map<Integer, Map<Integer, Integer>> srcErrorPairs;

    /**
     * Scheduler class constructor
     *
     */
    public Scheduler(SchedulerStoreInt store) {

        sourceFloors = new HashMap<>();
        destFloors = new HashMap<>();
        lastKnownDirection = new HashMap<>();
        srcDestPairs = new HashMap<>();

        try {
            this.store =  store;
            sendReceiveSocket = new DatagramSocket();
            System.out.println(store.getElevators());
            for(Integer i : store.getElevators().keySet()){
                System.out.println("Elevator Found");
                sourceFloors.put(i, new LinkedList<>());
                destFloors.put(i, new LinkedList<>());
                srcDestPairs.put(i, new HashMap<>());
                srcErrorPairs.put(i, new HashMap<>());
                lastKnownDirection.put(i, "IDLE");
            }
        } catch (SocketException | RemoteException se) {
            se.printStackTrace();
            System.exit(1);
        }
        System.out.println(sourceFloors);
    }

    /**
     * Main Scheduler subsystem run loop
     */
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                removeOutOfOrderElevators();
                checkArrivedAtAnyFloor();
                readFloorRequest();
                if (floorRequestToBeProcessed != null) {
                    System.out.println("Scheduler is processing: " + floorRequestToBeProcessed);
                    findClosest(floorRequestToBeProcessed.getSourceFloor(), floorRequestToBeProcessed.getDestFloor());
                }

                Thread.sleep(100);

            } catch (InterruptedException | RemoteException e) {
                e.printStackTrace();
                //Thread.currentThread().interrupt();
                System.out.println("Scheduler was interrupted.");
            }
        }
    }

    /**
     * Checks if any elevator that was saved is out of order, and removes them from the queue
     */
    private void removeOutOfOrderElevators() throws RemoteException {
        for (Map.Entry<Integer, ArrayList<Serializable>> entry : store.getElevators().entrySet())
        {
            if ((int)entry.getValue().get(3) == 4)
            {
                sourceFloors.remove(entry.getKey()); //remove corresponding sourceFloors queue
                for (Map.Entry<Integer, Integer> subEntry: srcDestPairs.get(entry.getKey()).entrySet()) //reassign unserviced requests
                {
                    if (!destFloors.get(entry.getKey()).contains(subEntry.getValue())) // if the elevator has not reached the source floor of a request
                    {
                        findClosest(subEntry.getKey(), subEntry.getValue()); //reassign request
                    }
                }
                destFloors.remove(entry.getKey()); //remove corresponding destination floors queue
                srcDestPairs.remove(entry.getKey());
                store.removeElevator(entry.getKey());
            }
        }
    }

    /**
     * Read an ElevatorEvent from the floor request queue in store
     */
    private void readFloorRequest() throws RemoteException {
        floorRequestToBeProcessed = store.getFloorRequest();
    }

    /**
     * Checks if an elevator has arrived at any of its assigned source floors/destination floors
     * @throws RemoteException
     * @throws InterruptedException
     */
    private void checkArrivedAtAnyFloor() throws RemoteException, InterruptedException {
        Map<Integer, ArrayList<Serializable>> sourceElevs = store.getElevators();
        for (Integer key : sourceElevs.keySet())
        {
            if ((int)sourceElevs.get(key).get(3) == 0) //is elevator idle?
            {
                if (!sourceFloors.get(key).isEmpty() && contains(sourceFloors.get(key),(Integer)sourceElevs.get(key).get(2))) //is it at a source floor?
                {
                    destFloors.get(key).add(srcDestPairs.get(key).remove((Integer) sourceElevs.get(key).get(2)));
                    sourceFloors.get(key).removeFirstOccurrence(sourceElevs.get(key).get(2));
                    Thread.sleep(200);
                    sendToClosest(key);
                } else if (!destFloors.get(key).isEmpty() && contains(destFloors.get(key),(Integer)sourceElevs.get(key).get(2))) //is it at a destination floor?
                {
                    destFloors.get(key).removeFirstOccurrence(sourceElevs.get(key).get(2));
                    Thread.sleep(200);
                    sendToClosest(key);
                }
            }
        }
    }

    /**
     * Check if list contains value
     * @param list list being searched
     * @param value value searched for
     * @return Returns true if list contains value
     */
    private boolean contains(LinkedList<Integer> list, Integer value)
    {
        for (Integer i : list)
        {
            if (i.equals(value)) return true;
        }
        return false;
    }

    /**
     * Create UDP message to send to elevator
     * @param elevID target elevator
     * @param floor floor to go to
     * @return Message to be sent
     * @throws RemoteException
     */
    private String createMessage(int elevID, int floor) throws RemoteException {
        String msg = "03";
        if(floor > (int) store.getElevators().get(elevID).get(2)){
            msg += "UP,";
            lastKnownDirection.put(elevID,"UP");
        }else{
            msg += "DN,";
            lastKnownDirection.put(elevID,"DN");
        }

        if (floor <= 9) msg += "0";
        msg+= floor;
        msg += "0";
        return msg;
    }

    /**
     * Send command to elevator
     * @param elevID target elevator
     * @param floor floor to move to
     */
    private void sendCommand(int elevID, int floor)
    {
        byte[] acknowledged;
        int msgLen;
        DatagramPacket receivePacket;

        try {
            byte[] toSend = HelperFunctions.generateMsg(createMessage(elevID, floor));
            DatagramPacket destPacket;
            InetAddress ipAddress = (InetAddress) store.getElevators().get(elevID).get(0);
            destPacket = new DatagramPacket(toSend, toSend.length,
                    ipAddress, (int) store.getElevators().get(elevID).get(1));
            sendReceiveSocket.send(destPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        acknowledged = new byte[100];
        receivePacket = new DatagramPacket(acknowledged, acknowledged.length);

        try {
            // Block until Elevator acknowledges the destination packet
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        msgLen = receivePacket.getLength();
        System.out.println("Scheduler: Destination ACK received:");
        HelperFunctions.printDataInfo(acknowledged, msgLen);
    }

    /**
     * Find next floor to go to based on direction
     * @param floor current floor
     * @param destfloors list of destination floors
     * @param srcFloors list of source floors
     * @param direction current direction
     * @return Returns next floor
     */
    private int findNextClosest(int floor, LinkedList<Integer> destfloors, LinkedList<Integer> srcFloors, String direction){
        int closest = 23;
        int chosenFloor = 0;
        LinkedList<Integer> combined = new LinkedList<>();
        combined.addAll(destfloors);
        combined.addAll(srcFloors);

        if (direction.equals("UP")) {
            for (Integer i : combined) {
                if ((i - floor) >= 0 && (i - floor) < closest)
                {
                    closest = i - floor;
                    chosenFloor = i;
                }
            }
            if (closest == 23) {
                return Collections.max(combined);
            }
        }
        else if (direction.equals("DN"))
        {
            for (Integer i : combined) {
                if ((floor - i) >= 0 && (floor - i) < closest)
                {
                    closest = floor - i;
                    chosenFloor = i;
                }
            }
            if (closest == 23) {
                return Collections.min(combined);
            }
        }

        return chosenFloor;
    }

    /**
     * Send elevator to next destination
     * @param elevID Target elevator
     */
    private void sendToClosest(Integer elevID)
    {
        if (sourceFloors.get(elevID).isEmpty() && !destFloors.get(elevID).isEmpty())
        {
            int floorToGo = findNextClosest(elevID, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

            sendCommand(elevID, floorToGo);
        }
        else if (!sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            int floorToGo = findNextClosest(elevID, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

            sendCommand(elevID, floorToGo);
        }
        else if (sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            lastKnownDirection.put(elevID, "IDLE");
        }
        else
        {
            if (lastKnownDirection.get(elevID).equals("UP"))
            {
                int floorToGo = findNextClosest(elevID, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

                sendCommand(elevID, floorToGo);
            }
            else if (lastKnownDirection.get(elevID).equals("DN"))
            {
                int floorToGo = findNextClosest(elevID, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

                sendCommand(elevID, floorToGo);
            }
        }
    }

    /**
     * Iterate over elevators to find which is the closest for a given request.
     *
     * @param sourceFloor The source floor of a request being processed
     * @param destFloor The destination floor of a request being processed
     */
    public void findClosest(int sourceFloor, int destFloor) {
        Map<Integer, ArrayList<Serializable>> elevators;
        try {
            elevators = store.getElevators();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        int closestID = 0;
        int previousFloor = (int) elevators.get(1).get(2);
        int check = 22;

        //if source floor less than destfloor
        if (sourceFloor < destFloor)
        {
            //check if there are idle elevators beneath source:
            for (Integer key: elevators.keySet())
            {
                ArrayList<Serializable> beingChecked = elevators.get(key);
                if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) <= sourceFloor)
                {
                    int tempCheck = (int) elevators.get(key).get(2) - sourceFloor;
                    if (tempCheck < check) {
                        check = tempCheck;
                        closestID = key;
                    }
                }
            }

            //check if there is an idle elevator above it with no destinations above it
            if (closestID == 0){
                for (Integer key: elevators.keySet())
                {
                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) > sourceFloor)
                    {
                        int tempCheck = Math.abs((int) elevators.get(key).get(2) - sourceFloor);
                        if (tempCheck < check) {
                            check = tempCheck;
                            closestID = key;
                        }
                    }
                }
            }

            //check if there are any moving elevators moving upwards that will pass by the source floor
            if(closestID == 0){
                int closestSoFar = 22;
                for (Integer key: elevators.keySet())
                {

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(2) < sourceFloor && (int) beingChecked.get(4) > sourceFloor)
                    {
                        if(closestSoFar > (sourceFloor - (int) elevators.get(key).get(2))){
                            closestSoFar = sourceFloor - (int) elevators.get(key).get(2);
                            closestID = key;
                        }
                    }
                }
            }

            //check if there are elevators with closest highest value in destination array
            if(closestID == 0){
                int closestHighest = 0;
                for (Integer key: elevators.keySet())
                {

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(4) > sourceFloor)
                    {
                        if((int) beingChecked.get(4) > closestHighest){
                            closestHighest = (int) beingChecked.get(4);
                            closestID = key;
                        }
                    }
                }
            }

        }else{

            //first find if there are elevators above src that are idle (with no destinations)
            int shortestDistance = 22;
            for (Integer key: elevators.keySet())
            {
                ArrayList<Serializable> beingChecked = elevators.get(key);
                if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) > sourceFloor)
                {
                    if(sourceFloor - (int) beingChecked.get(2) < shortestDistance){
                        closestID = key;
                        shortestDistance = sourceFloor - (int) beingChecked.get(2);
                    }
                }
            }


            // if not, next find if there are any elevators that are idle below it (with no destinations)
            if(closestID == 0){
                shortestDistance = 22;
                for(Integer key: elevators.keySet()){

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) < sourceFloor)
                    {
                        if((int) beingChecked.get(2) - sourceFloor < shortestDistance){
                            closestID = key;
                            shortestDistance = sourceFloor - (int) beingChecked.get(2);
                        }
                    }
                }
            }

            //if not, next find if there are any elevators moving in the same direction that will pass src;
            if(closestID == 0){
                int closestSoFar = 22;
                for (Integer key: elevators.keySet())
                {
                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 2 && (int) beingChecked.get(2) > sourceFloor && (int) beingChecked.get(4) < sourceFloor)
                    {
                        if(closestSoFar > ((int) elevators.get(key).get(2) - sourceFloor)){
                            closestSoFar = (int) elevators.get(key).get(2) - sourceFloor;
                            closestID = key;
                        }
                    }
                }
            }

            /* if not, select elevator with closest highest value in destination array*/
            if(closestID == 0){
                int closestHighest = 22;

                for (Integer key: elevators.keySet())
                {
                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(4) < sourceFloor){

                        if((int) beingChecked.get(4) < closestHighest){
                            closestHighest = (int) beingChecked.get(4);
                            closestID = key;
                        }
                    }
                }
            }
        }


        System.out.println(closestID);
        sourceFloors.get(closestID).add(sourceFloor);
        srcDestPairs.get(closestID).put(sourceFloor, destFloor);
        //srcErrorPairs.get(closestID).put(sourceFloor, error); place holder for error value in floor request

        if ((int) elevators.get(closestID).get(2) > floorRequestToBeProcessed.getSourceFloor()) { //determine last direction

            if ( (((int) elevators.get(closestID).get(4) < sourceFloor && destFloor < sourceFloor )|| ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1)))
            {
                sendCommand(closestID, sourceFloor);
            }
        }
        else if ((int) elevators.get(closestID).get(2) < floorRequestToBeProcessed.getSourceFloor()) {

            if ( ((int) elevators.get(closestID).get(4) > sourceFloor && destFloor > sourceFloor) || ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1))
            {
                sendCommand(closestID, sourceFloor);
            }
        }

        System.out.println("New Lists: " + closestID + " " + sourceFloors.get(closestID));
        System.out.println("New Lists: " + closestID + " " + destFloors.get(closestID));
    }

    /*
    Setters and getters for testing purposes
     */
    public void setReadFloorRequest() {
        try {
            this.floorRequestToBeProcessed = this.store.getFloorRequest();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProcessFloorRequest() {
        this.processedRequest = this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getFloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }

    public static void main(String[] args)
    {
        try {
            SchedulerStoreInt store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");

            Scheduler scheduler = new Scheduler(store);

            Thread schedulerThread = new Thread(scheduler);
            schedulerThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}