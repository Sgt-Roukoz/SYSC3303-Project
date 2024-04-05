package Main;

import com.sun.nio.sctp.PeerAddressChangeNotification;

import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduler class
 * Acts as the coordinator for the Elevator subsystem
 *
 * @author Garrison Su
 * @author Marwan Zeid
 * @author Masrur Husain
 * @author Adham Elmahi
 * @version 2024-04-04
 */

public class Scheduler implements Runnable {
    private static final int MAX_PASSENGERS = 5;
    private DatagramSocket sendReceiveSocket;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;
    private SchedulerStoreInt store;
    private Map<Integer, LinkedList<Integer>> sourceFloors;
    private Map<Integer, LinkedList<Integer>> destFloors;
    private Map<Integer, String> lastKnownDirection;
    private Map<Integer, Map<Integer, LinkedList<Integer>>> srcDestPairs;
    private  Map<Integer, Map<Integer, Integer>> srcErrorPairs;

    private Map<Integer, Integer> currentPassengers;


    /**
     * Scheduler class constructor
     *
     */
    public Scheduler(SchedulerStoreInt store) {

        sourceFloors = new HashMap<>();
        destFloors = new HashMap<>();
        lastKnownDirection = new HashMap<>();
        srcDestPairs = new HashMap<>();
        srcErrorPairs = new HashMap<>();
        currentPassengers = new HashMap<>();

        try {
            this.store =  store;
            sendReceiveSocket = new DatagramSocket();
            for(Integer i : store.getElevators().keySet()){
                System.out.println("Elevator Found");
                sourceFloors.put(i, new LinkedList<>());
                currentPassengers.put(i, 0);
                destFloors.put(i, new LinkedList<>());
                srcDestPairs.put(i, new HashMap<>());
                srcErrorPairs.put(i, new HashMap<>());
                lastKnownDirection.put(i, "IDLE");
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
                    System.out.println("Scheduler is processing: " + floorRequestToBeProcessed);
                    findClosest(floorRequestToBeProcessed.getSourceFloor(), floorRequestToBeProcessed.getDestFloor(), floorRequestToBeProcessed.getFaultType());
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
                int destination = (int) entry.getValue().get(4);
                sourceFloors.remove(entry.getKey()); //remove corresponding sourceFloors queue
                srcErrorPairs.get(entry.getKey()).put(destination, 0);
                for (Map.Entry<Integer, LinkedList<Integer>> subEntry: srcDestPairs.get(entry.getKey()).entrySet()) //reassign unserviced requests
                {
                    for (Integer subDest: subEntry.getValue())
                    {
                        if (!destFloors.get(entry.getKey()).contains(subDest)) // if the elevator has not reached the source floor of a request
                        {
                            findClosest(subEntry.getKey(), subDest, srcErrorPairs.get(entry.getKey()).get(subEntry.getKey())); //reassign request
                        }
                    }
                }
                destFloors.remove(entry.getKey()); //remove corresponding destination floors queue
                srcDestPairs.remove(entry.getKey());
                srcErrorPairs.remove(entry.getKey());
                System.out.println("Scheduler removing Elevator " + entry.getKey() + " from scheduling");
                store.removeElevator(entry.getKey()); // remove elevator from store
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
                    System.out.println("Elevator " + key + " at floor " + sourceElevs.get(key).get(2));
                    System.out.println("Destinations contains: " + srcDestPairs.get(key));
                    System.out.println("Adding the following: " + srcDestPairs.get(key).get((Integer) sourceElevs.get(key).get(2)));
                    int passengers = currentPassengers.get(key);
                    if (passengers < MAX_PASSENGERS) {
                        currentPassengers.put(key, passengers + 1);
                        // Send the elevator to the closest floor as it has space and just loaded a passenger
                        sendToClosest(key);
                    } else {
                        // Elevator is full, print a message or handle accordingly
                        System.out.println("Elevator " + key + " at floor " + sourceElevs.get(key).get(2) + " is full.");

                        // Reassign the source floor to another elevator
                        Integer newElevatorID = findNewElevatorForReassignment(key, (Integer)sourceElevs.get(key).get(2));
                        if (newElevatorID != null) {
                            // Reassign the pickup request to the new elevator
                            reassignPickupRequest(newElevatorID, (Integer)sourceElevs.get(key).get(2));
                        } else {
                            // Handle the case where no elevators are available for reassignment
                            System.out.println("No available elevators to reassign the pickup request at floor " + sourceElevs.get(key).get(2));
                        }

                        // Do not forget to remove the source floor from the current elevator's queue since it's full
                        sourceFloors.get(key).remove((Integer)sourceElevs.get(key).get(2));
                    }
                    destFloors.get(key).addAll(srcDestPairs.get(key).get((Integer) sourceElevs.get(key).get(2)));
                    srcDestPairs.get(key).remove((Integer)sourceElevs.get(key).get(2));
                    sourceFloors.get(key).removeAll((Collections.singleton(sourceElevs.get(key).get(2))));
                    srcErrorPairs.get(key).remove(sourceElevs.get(key).get(2)); //remove the error pair
                    Thread.sleep(200);
                    sendToClosest(key);
                } else if (!destFloors.get(key).isEmpty() && contains(destFloors.get(key),(Integer)sourceElevs.get(key).get(2))) //is it at a destination floor?
                {
                    System.out.println("Elevator " + key + " arrived at destination floor " + sourceElevs.get(key).get(2));
                    // Decrease passenger count when elevator arrives at destination floor
                    int passengers = currentPassengers.get(key);
                    currentPassengers.put(key, Math.max(0, passengers - 1));
                    destFloors.get(key).removeFirstOccurrence(sourceElevs.get(key).get(2));
                    Thread.sleep(200);
                    sendToClosest(key);
                }
            }
        }
    }

    /**
     * Finds a new elevator for reassignment of a pickup request when the current elevator is full.
     * It searches through all elevators and selects the one with the fewest passengers and closest proximity
     * to the source floor that is not the current elevator.
     *
     * @param currentElevatorID The ID of the current full elevator.
     * @param sourceFloor       The floor where the pickup request is made.
     * @return The ID of the new elevator selected for reassignment, or null if no suitable elevator is found.
     * @throws RemoteException If there is a communication-related exception that may occur during the execution of a remote method call.
     */
    private Integer findNewElevatorForReassignment(Integer currentElevatorID, Integer sourceFloor) throws RemoteException {
        Map<Integer, ArrayList<Serializable>> elevators = store.getElevators();
        Integer selectedElevatorID = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<Integer, ArrayList<Serializable>> entry : elevators.entrySet()) {
            if (!entry.getKey().equals(currentElevatorID)) {
                int currentFloor = (int) entry.getValue().get(2); // current floor
                int passengerCount = (int) entry.getValue().get(5); // current passenger count
                int distanceToSourceFloor = Math.abs(currentFloor - sourceFloor);

                if (passengerCount < MAX_PASSENGERS && distanceToSourceFloor < minDistance) {
                    minDistance = distanceToSourceFloor;
                    selectedElevatorID = entry.getKey();
                }
            }
        }

        return selectedElevatorID;
    }


    /**
     * Reassigns a pickup request to a different elevator. Adds the source floor to the new elevator's queue of source floors.
     *
     * @param newElevatorID The ID of the new elevator to which the request is being reassigned.
     * @param sourceFloor   The floor where the pickup request is made.
     */
    private void reassignPickupRequest(Integer newElevatorID, Integer sourceFloor) {
        // Add the source floor to the new elevator's list of floors to visit
        sourceFloors.get(newElevatorID).add(sourceFloor);
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
    private String createMessage(int elevID, int floor, int fault, boolean isSource) throws RemoteException {
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
        msg += isSource ? "1" : "0";
        return msg;
    }

    /**
     * Send command to elevator
     * @param elevID target elevator
     * @param floor floor to move to
     */
    private void sendCommand(int elevID, int floor, int fault, boolean isSource)
    {
        byte[] acknowledged;
        int msgLen;
        DatagramPacket receivePacket;

        try {
            byte[] toSend = HelperFunctions.generateMsg(createMessage(elevID, floor, fault, isSource));
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
    private void sendToClosest(Integer elevID) {
        boolean isAtSourceFloor;
        boolean isAtDestinationFloor;
        int passengers;
        int floorToGo;

        // Check if the elevator has destinations or sources to process
        if (!sourceFloors.get(elevID).isEmpty() || !destFloors.get(elevID).isEmpty()) {
            if (!sourceFloors.get(elevID).isEmpty()) {
                // Check if the elevator is at a source floor to pick up passengers
                floorToGo = sourceFloors.get(elevID).getFirst();
                isAtSourceFloor = true;
                passengers = currentPassengers.get(elevID);
                // If the elevator is full, do not add more passengers, instead find the next destination
                if (passengers >= MAX_PASSENGERS) {
                    isAtSourceFloor = false; // Set to false since we can't pick up more passengers
                    if (!destFloors.get(elevID).isEmpty()) {
                        // Elevator full, move to next destination floor
                        floorToGo = destFloors.get(elevID).getFirst();
                        isAtDestinationFloor = true;
                    }
                }
            } else {
                // If there are no source floors, just go to the next destination floor
                floorToGo = destFloors.get(elevID).getFirst();
                isAtSourceFloor = false;
            }

            sendCommand(elevID, floorToGo, srcErrorPairs.get(elevID).getOrDefault(floorToGo, 0), isAtSourceFloor);
        } else {
            // If there are no source floors or destination floors to process, set elevator to idle
            lastKnownDirection.put(elevID, "IDLE");
        }
    }


    /**
     * Iterate over elevators to find which is the closest for a given request.
     *
     * @param sourceFloor The source floor of a request being processed
     * @param destFloor The destination floor of a request being processed
     */
    public void findClosest(int sourceFloor, int destFloor, int fault) {
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
                int closestHighest = 100;
                for (Integer key: elevators.keySet())
                {

                    //ArrayList<Serializable> beingChecked = elevators.get(key);
                    if (!srcDestPairs.get(key).isEmpty())
                    {
                        List<Integer> destinations = srcDestPairs.get(key).values().stream().flatMap(LinkedList::stream).toList();
                        int largestDestination = Collections.max(destinations);
                        if (Math.abs(largestDestination - sourceFloor) <  closestHighest)
                        {
                            closestHighest = Math.abs(largestDestination - sourceFloor);
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
                    //ArrayList<Serializable> beingChecked = elevators.get(key);
                    if (!srcDestPairs.get(key).isEmpty())
                    {
                        List<Integer> destinations = srcDestPairs.get(key).values().stream().flatMap(LinkedList::stream).toList();
                        int largestDestination = Collections.max(destinations);
                        if (Math.abs(largestDestination - sourceFloor) <  closestHighest)
                        {
                            closestHighest = Math.abs(largestDestination - sourceFloor);
                            closestID = key;
                        }
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

        srcErrorPairs.get(closestID).put(sourceFloor, fault); //place holder for error value in floor request

        if ((int) elevators.get(closestID).get(2) > sourceFloor) { //determine last direction

            if ( (((int) elevators.get(closestID).get(4) < sourceFloor && destFloor < sourceFloor )|| ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1)))
            {
                sendCommand(closestID, sourceFloor, fault,true);
            }
        }
        else if ((int) elevators.get(closestID).get(2) < sourceFloor) {

            if ( ((int) elevators.get(closestID).get(4) > sourceFloor && destFloor > sourceFloor) || ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1))
            {
                sendCommand(closestID, sourceFloor, fault, true);
            }
        }

        System.out.println("New Lists: " + closestID + " " + sourceFloors.get(closestID));
        System.out.println("New Lists: " + closestID + " " + srcDestPairs.get(closestID).values());
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