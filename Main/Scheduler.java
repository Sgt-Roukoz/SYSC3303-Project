package Main;

import com.sun.nio.sctp.PeerAddressChangeNotification;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.Array;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduler class
 * Acts as the coordinator for the Elevator subsystem
 *
 * @author Garrison Su
 * @author Marwan Zeid
 * @author Masrur Husain
 * @author Adham
 * @version 2024-04-07
 */

public class Scheduler implements Runnable {
    private DatagramSocket sendReceiveSocket;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;
    private SchedulerStoreInt store;
    private Map<Integer, LinkedList<Integer>> sourceFloors;
    private Map<Integer, LinkedList<Integer>> destFloors;
    private Map<Integer, String> lastKnownDirection;
    private Map<Integer, Map<Integer, LinkedList<Integer>>> srcDestPairs;
    private  Map<Integer, Map<Integer, Integer>> destErrorPairs;
    private Map<Integer, Integer> elevatorPassengers;
    private Map<Integer, Integer> maxPassengers;
    private Map<Integer, Integer> totalRequestsDone;
    private int requestsDone = 0;
    private int moveRequestsSent = 0;
    boolean receivedFirst = false;
    boolean lastRequestReceived = false;

    /**
     * Scheduler class constructor
     *
     */
    public Scheduler(SchedulerStoreInt store) {

        sourceFloors = new HashMap<>();
        destFloors = new HashMap<>();
        lastKnownDirection = new HashMap<>();
        srcDestPairs = new HashMap<>();
        destErrorPairs = new HashMap<>();
        elevatorPassengers = new HashMap<>();
        maxPassengers = new HashMap<>();
        totalRequestsDone = new HashMap<>();

        try {
            this.store =  store;
            sendReceiveSocket = new DatagramSocket();
            for(Integer i : store.getElevators().keySet()){
                sourceFloors.put(i, new LinkedList<>());
                destFloors.put(i, new LinkedList<>());
                srcDestPairs.put(i, new HashMap<>());
                destErrorPairs.put(i, new HashMap<>());
                elevatorPassengers.put(i, 0);
                lastKnownDirection.put(i, "IDLE");
                totalRequestsDone.put(i, 0);
                maxPassengers.put(i, 0);
            }
        } catch (SocketException | RemoteException se) {
            se.printStackTrace();
            System.exit(1);
        }
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
                    if (!receivedFirst) {
                        store.setFirstRequest(String.valueOf(new Timestamp((new Date()).getTime())));
                        receivedFirst = true;
                    }
                    lastRequestReceived = floorRequestToBeProcessed.isLastReq();
                    store.addLog("Scheduler-1: Scheduler is processing: " + floorRequestToBeProcessed);
                    findClosest(floorRequestToBeProcessed.getSourceFloor(), floorRequestToBeProcessed.getDestFloor(), floorRequestToBeProcessed.getFaultType(), -1);
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
            if ((int)entry.getValue().get(3) == 3)
            {
                store.removeElevator(entry.getKey()); // remove elevator from store
                int destination = (int) entry.getValue().get(4);
                sourceFloors.remove(entry.getKey()); //remove corresponding sourceFloors queue
                destErrorPairs.get(entry.getKey()).put(destination, 0);
                for (Map.Entry<Integer, LinkedList<Integer>> subEntry: srcDestPairs.get(entry.getKey()).entrySet()) //reassign unserviced requests
                {
                    for (Integer subDest: subEntry.getValue()) {
                        store.addLog("Scheduler-1: Reassigning: " + subEntry.getKey() + " " + subDest + " " + destErrorPairs.get(entry.getKey()).get(subDest));
                        findClosest(subEntry.getKey(), subDest, destErrorPairs.get(entry.getKey()).get(subDest), -1); //reassign request
                    }
                }
                destFloors.remove(entry.getKey()); //remove corresponding destination floors queue
                srcDestPairs.remove(entry.getKey());
                destErrorPairs.remove(entry.getKey());
                store.addLog("Scheduler-1: removing Elevator " + entry.getKey() + " from scheduling");
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
                int currFloor = (Integer)sourceElevs.get(key).get(2);
                if (!sourceFloors.get(key).isEmpty() && contains(sourceFloors.get(key),currFloor)) //is it at a source floor?
                {
                    store.addLog("Scheduler-1: Elevator " + key + " at floor " + sourceElevs.get(key).get(2));
                    //is it also a destfloor?
                    if (Collections.frequency(destFloors.get(key), currFloor) > 0)
                    {
                        doDestinations(sourceElevs, key, currFloor);
                    }

                    if (elevatorPassengers.get(key) < 5)
                    {
                        int estimatedPassengers = elevatorPassengers.get(key);
                        while (estimatedPassengers < 5 && !srcDestPairs.get(key).get(currFloor).isEmpty()) // add destinations based on number of passengers boarding
                        {
                            destFloors.get(key).add(srcDestPairs.get(key).get(currFloor).remove());
                            estimatedPassengers++;
                        }

                        // check number of people boarding:
                        store.addLog("Elevator-" + key + ": " + (estimatedPassengers - elevatorPassengers.get(key) + " passengers boarded"));
                        elevatorPassengers.put(key, estimatedPassengers);
                        if (maxPassengers.get(key) < estimatedPassengers) maxPassengers.put(key, estimatedPassengers);
                        store.updateElevator(key, 5, estimatedPassengers);
                        if (!srcDestPairs.get(key).get(currFloor).isEmpty()) //if requests remain, reassign them
                        {
                            while (!srcDestPairs.get(key).get(currFloor).isEmpty())
                            {
                                int dest = srcDestPairs.get(key).get(currFloor).remove();
                                findClosest(currFloor, dest, 0, key);
                            }
                        }

                    }
                    else //if elevator is full, "press the button again" aka reassign requests
                    {
                        store.addLog("Scheduler-1: Elevator " + key + " is full! Reassigning");
                        if (!srcDestPairs.get(key).get(currFloor).isEmpty())
                        {
                            while (!srcDestPairs.get(key).get(currFloor).isEmpty())
                            {
                                int dest = srcDestPairs.get(key).get(currFloor).remove();
                                findClosest(currFloor, dest, 0, key);
                            }
                        }
                    }
                    srcDestPairs.get(key).remove((Integer)sourceElevs.get(key).get(2));

                    store.addLog("Elevator-" + key + ": now has " + elevatorPassengers.get(key) + " passengers");
                    store.addLog("Scheduler-1: Destination now contains: " + destFloors.get(key));

                    //destFloors.get(key).addAll(srcDestPairs.get(key).get((Integer) sourceElevs.get(key).get(2)));
                    sourceFloors.get(key).removeAll((Collections.singleton(sourceElevs.get(key).get(2))));

                    Thread.sleep(10);
                    sendToClosest(key);
                } else if (!destFloors.get(key).isEmpty() && contains(destFloors.get(key),(Integer)sourceElevs.get(key).get(2))) //is it at a destination floor?
                {

                    doDestinations(sourceElevs, key, currFloor);
                    if (checkAllElevatorsIdle() && lastRequestReceived)
                    {
                        store.setLastRequest(String.valueOf(new Timestamp((new Date()).getTime())));
                    }
                    store.addLog("Scheduler-1: Elevator " + key + " arrived at destination floor " + sourceElevs.get(key).get(2));

                    if (checkAllElevatorsIdle() && lastRequestReceived)
                    {
                        store.addLog("Scheduler-1: Final Elevator Values:");
                        for (Integer pkey: maxPassengers.keySet())
                        {
                            store.addLog("Scheduler-1: Elevator " + pkey + " max passengers: " + maxPassengers.get(pkey));
                            store.addLog("Scheduler-1: Elevator " + pkey + " total requests done: " + totalRequestsDone.get(pkey));
                        }
                        Timestamp firstTime = Timestamp.valueOf(store.getFirstRequest());
                        Timestamp lastTime = Timestamp.valueOf(store.getLastRequest());
                        long totalTime = (lastTime.getTime() - firstTime.getTime())/(long)60000;
                        store.addLog("Scheduler-1: Total time passed: " + totalTime);
                        store.addLog("Scheduler-1: Throughput: " + ((long)requestsDone)/totalTime + " per minute");
                    }
                    Thread.sleep(10);
                    sendToClosest(key);
                }
            }
        }
    }

    /**
     * Check if all elevators are idle
     * @return Returns true if all elevators are idle, false otherwise
     * @throws RemoteException
     */
    private boolean checkAllElevatorsIdle() throws RemoteException {
        for (Integer key : store.getElevators().keySet())
        {
            if ((int)store.getElevators().get(key).get(3) != 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if an elevator is at a destination
     * @param sourceElevs elevators in question
     * @param key the elevator id
     * @param currFloor current floor
     * @throws RemoteException
     */
    private void doDestinations(Map<Integer, ArrayList<Serializable>> sourceElevs, Integer key, int currFloor) throws RemoteException {
        int passengers = Collections.frequency(destFloors.get(key), currFloor);
        elevatorPassengers.put(key, elevatorPassengers.get(key) - passengers);
        store.updateElevator(key, 5, elevatorPassengers.get(key));
        totalRequestsDone.put(key, totalRequestsDone.get(key) + passengers);
        requestsDone += passengers;
        store.setPassengersServiced(requestsDone);
        store.addLog("Elevator-" + key + ": dropped " + passengers + " passengers");
        store.addLog("Elevator-" + key + ": now has " + elevatorPassengers.get(key) + " passengers");
        destErrorPairs.get(key).remove(sourceElevs.get(key).get(2)); //remove the error pair
        destFloors.get(key).removeAll((Collections.singleton(sourceElevs.get(key).get(2))));
        store.addLog("Scheduler-1: Finished " + requestsDone + " requests!");
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
    private String createMessage(int elevID, int floor, int fault) throws RemoteException {
        String msg = "03";
        if(floor >= (int) store.getElevators().get(elevID).get(2)){
            msg += "UP,";
            lastKnownDirection.put(elevID,"UP");
        }else{
            msg += "DN,";
            lastKnownDirection.put(elevID,"DN");
        }

        if (floor <= 9) msg += "0";
        msg+= floor;
        msg += "," + fault;
        msg += "0";
        return msg;
    }

    /**
     * Send command to elevator
     * @param elevID target elevator
     * @param floor floor to move to
     */
    private void sendCommand(int elevID, int floor, int fault)
    {
        byte[] acknowledged;
        int msgLen;
        DatagramPacket receivePacket;

        try {
            byte[] toSend = HelperFunctions.generateMsg(createMessage(elevID, floor, fault));
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
        else
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
    private void sendToClosest(Integer elevID) throws RemoteException {
        int currentFloor = (int) store.getElevators().get(elevID).get(2);
        if (sourceFloors.get(elevID).isEmpty() && !destFloors.get(elevID).isEmpty())
        {
            int floorToGo = findNextClosest(currentFloor, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

            sendCommand(elevID, floorToGo, destErrorPairs.get(elevID).get(floorToGo));
            moveRequestsSent++;
        }
        else if (!sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            int floorToGo = findNextClosest(currentFloor, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

            sendCommand(elevID, floorToGo, 0);
            moveRequestsSent++;
        }
        else if (sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            lastKnownDirection.put(elevID, "IDLE");
        }
        else
        {
            if (lastKnownDirection.get(elevID).equals("UP"))
            {
                int floorToGo = findNextClosest(currentFloor, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));
                sendCommand(elevID, floorToGo, destErrorPairs.get(elevID).getOrDefault(floorToGo, 0));
            }
            else if (lastKnownDirection.get(elevID).equals("DN"))
            {
                int floorToGo = findNextClosest(currentFloor, sourceFloors.get(elevID), destFloors.get(elevID), lastKnownDirection.get(elevID));

                sendCommand(elevID, floorToGo, destErrorPairs.get(elevID).getOrDefault(floorToGo, 0));
            }
            moveRequestsSent++;
        }
        store.addLog("Scheduler-1: Total move requests sent: " + moveRequestsSent);
        store.setMovesDone(moveRequestsSent);
    }

    /**
     * Iterate over elevators to find which is the closest for a given request.
     *
     * @param sourceFloor The source floor of a request being processed
     * @param destFloor The destination floor of a request being processed
     */
    public void findClosest(int sourceFloor, int destFloor, int fault, int elevatorNotBeingConsidered) throws RemoteException {
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
                if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) <= sourceFloor && key != elevatorNotBeingConsidered)
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
                    if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) > sourceFloor && key != elevatorNotBeingConsidered)
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
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(2) < sourceFloor && (int) beingChecked.get(4) > sourceFloor && key != elevatorNotBeingConsidered)
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
                int closestHighest = 100;
                for (Integer key: elevators.keySet())
                {
                    int largestDestination;
                    List<Integer> destinations = srcDestPairs.get(key).values().stream().flatMap(LinkedList::stream).toList();
                    if (destinations.isEmpty()) destinations = destFloors.get(key);
                    if (destinations.isEmpty())  largestDestination = 0;
                    else largestDestination = Collections.max(destinations);
                    if (Math.abs(largestDestination - sourceFloor) <  closestHighest && key != elevatorNotBeingConsidered)
                    {
                        closestHighest = Math.abs(largestDestination - sourceFloor);
                        closestID = key;
                    }
                }
            }

        }else{

            //first find if there are elevators above src that are idle (with no destinations)
            int shortestDistance = 22;
            for (Integer key: elevators.keySet())
            {
                ArrayList<Serializable> beingChecked = elevators.get(key);
                if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) > sourceFloor && key != elevatorNotBeingConsidered)
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
                    if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) < sourceFloor && key != elevatorNotBeingConsidered)
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
                    if ((int) beingChecked.get(3) == 2 && (int) beingChecked.get(2) > sourceFloor && (int) beingChecked.get(4) < sourceFloor && key != elevatorNotBeingConsidered)
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
                    //ArrayList<Serializable> beingChecked = elevators.get(key);
                    int largestDestination;
                    List<Integer> destinations = srcDestPairs.get(key).values().stream().flatMap(LinkedList::stream).toList();
                    if (destinations.isEmpty()) destinations = destFloors.get(key);
                    if (destinations.isEmpty()) largestDestination = 0;
                    else largestDestination = Collections.max(destinations);
                    if (Math.abs(largestDestination - sourceFloor) <  closestHighest && key != elevatorNotBeingConsidered)
                    {
                        closestHighest = Math.abs(largestDestination - sourceFloor);
                        closestID = key;
                    }
                }
            }
        }

        sourceFloors.get(closestID).add(sourceFloor);
        if (srcDestPairs.get(closestID).containsKey(sourceFloor)) srcDestPairs.get(closestID).get(sourceFloor).add(destFloor);
        else
        {
            srcDestPairs.get(closestID).put(sourceFloor, new LinkedList<>());
            srcDestPairs.get(closestID).get(sourceFloor).add(destFloor);
        }

        destErrorPairs.get(closestID).put(destFloor, fault); //place holder for error value in floor request

        if ((int) elevators.get(closestID).get(3) == 0)
        {
            sendCommand(closestID, sourceFloor, fault);
        }
        else if ((int) elevators.get(closestID).get(2) > sourceFloor) { //determine last direction

            if ( (((int) elevators.get(closestID).get(4) < sourceFloor && destFloor < sourceFloor )|| ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1)))
            {
                sendCommand(closestID, sourceFloor, fault);
            }
        }
        else if ((int) elevators.get(closestID).get(2) < sourceFloor) {

            if ( ((int) elevators.get(closestID).get(4) > sourceFloor && destFloor > sourceFloor) || ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1))
            {
                sendCommand(closestID, sourceFloor, fault);
            }
        }

        store.addLog("Scheduler-1: Elevator " + closestID + " source floors: " + sourceFloors.get(closestID));
        store.addLog("Scheduler-1: Elevator " + closestID + " destination floors:  " + srcDestPairs.get(closestID).values());
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