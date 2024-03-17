package Main;

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
    //int selectedElevator;
    String newEvent;

    private Map<Integer, LinkedList<Integer>> sourceFloors;
    private Map<Integer, LinkedList<Integer>> destFloors;
    private Map<Integer, String> lastKnownDirection;

    /**
     * Scheduler class constructor
     *
     */
    public Scheduler() {

        sourceFloors = new HashMap<>();
        destFloors = new HashMap<>();
        lastKnownDirection = new HashMap<>();

        try {
            this.store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");
            sendReceiveSocket = new DatagramSocket(100);
            System.out.println(store.getElevators());
            for(Integer i : store.getElevators().keySet()){
                System.out.println("Elevator Found");
                sourceFloors.put(i, new LinkedList<>());
                destFloors.put(i, new LinkedList<>());
                lastKnownDirection.put(i, "IDLE");
            }
        } catch (SocketException | RemoteException se) {
            se.printStackTrace();
            System.exit(1);
        } catch (MalformedURLException | NotBoundException e) {
            throw new RuntimeException(e);
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
//                if (arrayNotEmpty(sourceFloors)) checkArrivedAtDestination();
//                if (arrayNotEmpty(destFloors)) checkArrivedAtSource();
                checkArrivedAtAnyFloor();
                readFloorRequest();
                if (floorRequestToBeProcessed != null) {
                    System.out.println("Scheduler is processing: " + floorRequestToBeProcessed);
                    findClosest(floorRequestToBeProcessed);
//                    newEvent = processFloorRequest();
//                    sendSourceFloorToElevator(newEvent);
                }

                Thread.sleep(100);

            } catch (InterruptedException | RemoteException e) {
                e.printStackTrace();
                //Thread.currentThread().interrupt();
                System.out.println("Scheduler was interrupted.");
            }
        }
    }

    private boolean arrayNotEmpty(Map<Integer, LinkedList<Integer>> map)
    {
        for (LinkedList<Integer> vals : map.values())
        {
            if (!vals.isEmpty()) return true;
        }
        return false;
    }

    /**
     * Read an ElevatorEvent from the floor request queue in store
     */
    private void readFloorRequest() throws RemoteException {
        floorRequestToBeProcessed = store.getFloorRequest();
    }

//    /**
//     * Process the ElevatorEvent request and convert into an event usable by Elevator
//     */
//    private String processFloorRequest() throws RemoteException {
//        String translated = "03";
//        if(((int) store.getElevators().get(selectedElevator).get(2)) > floorRequestToBeProcessed.getSourceFloor()){ // Finding where the request came from in relation to the elevator's current position
//            translated += "DN,";
//        }else{
//            translated += "UP,";
//        }
//        int floor = floorRequestToBeProcessed.getSourceFloor();
//        if (floor < 9) translated += "0" + floor;
//        else translated += floor;
//
//        translated += "0";
//
//        System.out.println("Scheduler: Processing floor event: " + floorRequestToBeProcessed);
//        processedRequest = floorRequestToBeProcessed;
//        return translated;
//    }


//    /**
//     * Process and regurgitate the destination floor message to be sent to the elevator
//     * */
//    private String destFloor(int destFloor) throws RemoteException {
//        String desti = "03";
//        if(destFloor > (int) store.getElevators().get(selectedElevator).get(2)){
//            desti += "UP,";
//        }else{
//            desti += "DN,";
//        }
//        if (destFloor < 9) desti += "0" + destFloor;
//        else desti += destFloor;
//
//        desti += "0";
//        return desti;
//    }


//    /**
//     * Send the source floor in the processed ElevatorEvent to the elevator in elevatorTracker
//     */
//    private void sendSourceFloorToElevator(String happening) {
//        System.out.println("Sending elevator event: " + processedRequest + "to elevator: " + selectedElevator);
//
//        try {
//            byte[] toSend = HelperFunctions.generateMsg(happening);
//            DatagramPacket sendPacket;
//            InetAddress ipAddress = (InetAddress) store.getElevators().get(selectedElevator).getFirst();
//            sendPacket = new DatagramPacket(toSend, toSend.length,
//                    ipAddress, (int) store.getElevators().get(selectedElevator).get(1));
//            sendReceiveSocket.send(sendPacket);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        byte[] acknowledged = new byte[100];
//        DatagramPacket receivePacket = new DatagramPacket(acknowledged, acknowledged.length);
//
//        try {
//            // Block until Elevator acknowledges
//            sendReceiveSocket.receive(receivePacket);
//        } catch(IOException e) {
//            e.printStackTrace();
//            System.exit(1);
//        }
//
//        int msgLen = receivePacket.getLength();
//        System.out.println("Scheduler: Source ACK received: ");
//        HelperFunctions.printDataInfo(acknowledged, msgLen);
//
//        //sendCommand();
//
//    }

    /**
     * Send the destination floor to the elevator once it reaches the source floor.
     * */
    private void sendDestFloorToElevator(int floor) {


    }

    /**
     * Check if any elevators have arrived at a destination floor, and send them to their next destination floor if they have one.
     * */
    private void checkArrivedAtDestination() throws RemoteException {
        Map<Integer, ArrayList<Serializable>> updatedElevs = store.getElevators();
        //System.out.println(destFloors);
        for(int count = 1; count <= updatedElevs.size(); count++){
            for (Map.Entry<Integer, LinkedList<Integer>> entry : destFloors.entrySet()) {
                if((int) updatedElevs.get(count).get(3) == 0){ //Is the elevator idle?
                    if(!entry.getValue().isEmpty() && (int) updatedElevs.get(count).get(2) == entry.getValue().getFirst()){ //Does it match the destination floor? The destination floors should be in the order they came in, hence the 0
                        entry.getValue().removeFirst(); //Remove the destination floor
                        if(entry.getValue().getFirst() != null){ //Is there another destination after this one?
                            sendDestFloorToElevator(destFloors.get(count).getFirst());
                        }
                    }else{
                        if(!destFloors.isEmpty()){
                            System.out.println("Elevator " + entry.getKey().toString() + " is Idle on floor " + updatedElevs.get(count).get(2).toString() + ". It should not be there!");
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if any elevators have arrived at a source floor, and send them to their first destination floor.
     * */
    private void checkArrivedAtSource() throws RemoteException {
        Map<Integer, ArrayList<Serializable>> sourceElevs = store.getElevators();
        //System.out.println(sourceFloors);
        for(int count = 1; count < sourceElevs.size(); count++){
            for (Map.Entry<Integer, LinkedList<Integer>> entry : sourceFloors.entrySet()) {
                if((int) sourceElevs.get(count).get(3) == 0){ //Is the elevator idle?
                    if(!entry.getValue().isEmpty() && (int) sourceElevs.get(count).get(2) == entry.getValue().getFirst()){ //Does it match the source floor? The destination floors should be in the order they came in, hence the 0
                        entry.getValue().removeFirst(); //Remove the source floor
                        sendDestFloorToElevator(destFloors.get(count).getFirst());
                    }else{
                        if(!sourceFloors.isEmpty()){
                            System.out.println("Elevator " + entry.getKey() + " is Idle on floor " + sourceElevs.get(count).get(2).toString() + ". It should not be there!");
                        }

                    }
                }
            }
        }
    }

    private void checkArrivedAtAnyFloor() throws RemoteException, InterruptedException {
        Map<Integer, ArrayList<Serializable>> sourceElevs = store.getElevators();
        for (Integer key : sourceElevs.keySet())
        {
            if ((int)sourceElevs.get(key).get(3) == 0) //is elevator idle?
            {
                if (!sourceFloors.get(key).isEmpty() && (int)sourceElevs.get(key).get(2) == sourceFloors.get(key).getFirst()) //is it at a source floor?
                {
                    System.out.println("Checking");
                    sourceFloors.get(key).removeFirst();
                    Thread.sleep(200);
                    sendToClosest(key);
                } else if (!destFloors.get(key).isEmpty() && (int)sourceElevs.get(key).get(2) == destFloors.get(key).getFirst()) //is it at a destination floor?
                {
                    destFloors.get(key).removeFirst();
                    if (destFloors.get(key).isEmpty()) lastKnownDirection.put(key, "IDLE"); //was this the last destination?
                    else
                    {
                        Thread.sleep(200);
                        sendToClosest(key);
                    }
                }
            }
        }
    }


    private String createMessage(int elevID, int floor) throws RemoteException {
        String msg = "03";
        String direction = lastKnownDirection.get(elevID);
        if (direction.equals("IDLE"))
        {
            if(floor > (int) store.getElevators().get(elevID).get(2)){
                msg += "UP,";
            }else{
                msg += "DN,";
            }
        }
        else{
            if (direction.equals("UP")) msg += "UP,";
            else msg += "DN,";
        }
        if (floor <= 9) msg += "0" + floor;
        else msg += floor;

        msg += "0";
        return msg;
    }

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

    private void sendToClosest(Integer elevID)
    {
        if (sourceFloors.get(elevID).isEmpty() && !destFloors.get(elevID).isEmpty())
        {
            sendCommand(elevID, destFloors.get(elevID).getFirst());
        }
        else if (!sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            sendCommand(elevID, sourceFloors.get(elevID).getFirst());
        }
        else if (sourceFloors.get(elevID).isEmpty() && destFloors.get(elevID).isEmpty())
        {
            lastKnownDirection.put(elevID, "IDLE");
        }
        else
        {
            if (lastKnownDirection.get(elevID).equals("UP"))
            {
                int floorToGo = Math.min(sourceFloors.get(elevID).getFirst(), destFloors.get(elevID).getFirst());
                sendCommand(elevID,floorToGo);
            }
            else if (lastKnownDirection.get(elevID).equals("DN"))
            {
                int floorToGo = Math.max(sourceFloors.get(elevID).getFirst(), destFloors.get(elevID).getFirst());
                sendCommand(elevID,floorToGo);
            }
        }
    }

    /**
     * Iterate over elevators to find which is the closest for a given request.
     *
     * @param floor The floor event containing the source floor. The elevator which is closest is the one that will be
     *              chosen to service the request.
     *
     */
    public synchronized void findClosest(ElevatorEvent floor) {
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
        if (floor.getSourceFloor() < floor.getDestFloor())
        {
            //check if there are idle elevators beneath source:
            for (Integer key: elevators.keySet())
            {
                ArrayList<Serializable> beingChecked = elevators.get(key);
                if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) <= floor.getSourceFloor())
                {
                    int tempCheck = (int) elevators.get(key).get(2) - floor.getSourceFloor();
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
                    if ((int) beingChecked.get(3) == 0 && (int) beingChecked.get(2) > floor.getSourceFloor())
                    {
                        int tempCheck = (int) elevators.get(key).get(2) - floor.getSourceFloor();
                        if (tempCheck < check) {
                            check = tempCheck;
                            closestID = key;
                        }
                    }
                }
            }

            //check if there are any moving elevators moving upwards that will pass by the source floor
            if(closestID == 0){
                for (Integer key: elevators.keySet())
                {
                    int closestSoFar = 22;

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(2) < floor.getSourceFloor() && (int) beingChecked.get(4) > floor.getSourceFloor())
                    {
                        if(closestSoFar > (floor.getSourceFloor() - (int) elevators.get(key).get(2))){
                            closestSoFar = floor.getSourceFloor() - (int) elevators.get(key).get(2);
                            closestID = key;
                        }
                    }
                }
            }

            //check if there are elevators with closest highest value in destination array
            if(closestID == 0){
                for (Integer key: elevators.keySet())
                {

                    int closestHighest = 0;

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 1 && (int) beingChecked.get(4) < floor.getSourceFloor())
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
            for (Integer key: elevators.keySet())
            {
                int shortestDistance = 22;
                ArrayList<Serializable> beingChecked = elevators.get(key);
                if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) > floor.getSourceFloor())
                {
                    if(floor.getSourceFloor() - (int) beingChecked.get(2) < shortestDistance){
                        closestID = key;
                        shortestDistance = floor.getSourceFloor() - (int) beingChecked.get(2);
                    }
                }
            }

            // if not, next find if there are any elevators that are idle below it (with no destinations)

            if(closestID == 0){
                for(Integer key: elevators.keySet()){
                    int shortestDistance = 22;
                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 0 && destFloors.get(key).isEmpty() && (int) beingChecked.get(2) < floor.getSourceFloor())
                    {
                        if((int) beingChecked.get(2) - floor.getSourceFloor() < shortestDistance){
                            closestID = key;
                            shortestDistance = floor.getSourceFloor() - (int) beingChecked.get(2);
                        }
                    }
                }
            }

            //if not, next find if there are any elevators moving in the same direction that will pass src;
            if(closestID == 0){
                for (Integer key: elevators.keySet())
                {
                    int closestSoFar = 22;

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 2 && (int) beingChecked.get(2) > floor.getSourceFloor() && (int) beingChecked.get(4) < floor.getSourceFloor())
                    {
                        if(closestSoFar > ((int) elevators.get(key).get(2) - floor.getSourceFloor())){
                            closestSoFar = (int) elevators.get(key).get(2) - floor.getSourceFloor();
                            closestID = key;
                        }
                    }
                }
            }
            /* if not, select elevator with closest lowest value in destination array*/
            if(closestID == 0){
                for (Integer key: elevators.keySet())
                {

                    int closestHighest = 22;

                    ArrayList<Serializable> beingChecked = elevators.get(key);
                    if ((int) beingChecked.get(3) == 2 && (int) beingChecked.get(4) > floor.getSourceFloor())
                    {
                        if((int) beingChecked.get(4) < closestHighest){
                            closestHighest = (int) beingChecked.get(4);
                            closestID = key;
                        }
                    }
                }
            }
        }

//        for (Map.Entry<Integer, ArrayList<Serializable>> entry : elevators.entrySet()) {
//            int check = (int) entry.getValue().get(2);
//            if(Math.abs(check - floor.getSourceFloor()) < Math.abs(previousFloor - floor.getSourceFloor())){
//                previousFloor = check;
//                closestID = entry.getKey();
//            }
//        }



        sourceFloors.get(closestID).add(floorRequestToBeProcessed.getSourceFloor());
        destFloors.get(closestID).add(floorRequestToBeProcessed.getDestFloor());
        System.out.println("New Lists: " + closestID + " " + sourceFloors.get(closestID));
        System.out.println("New Lists: " + closestID + " " + destFloors.get(closestID));

        LinkedList<Integer> srcBeSorted = (LinkedList<Integer>) sourceFloors.get(closestID).clone();
        LinkedList<Integer> destBeSorted = (LinkedList<Integer>) destFloors.get(closestID).clone();


        if ((int) elevators.get(closestID).get(2) > floorRequestToBeProcessed.getSourceFloor()) { //determine last direction
            lastKnownDirection.put(closestID, "UP");
            Collections.sort(srcBeSorted);
            sourceFloors.put(closestID, srcBeSorted);
            Collections.sort(destBeSorted);
            destFloors.put(closestID, destBeSorted);

            if ( (int) elevators.get(closestID).get(4) > sourceFloors.get(closestID).getFirst() || ((int)elevators.get(closestID).get(3) == 0 && (int) elevators.get(closestID).get(2)==1))
            {
                sendCommand(closestID, sourceFloors.get(closestID).getFirst());
            }
        }
        else if ((int) elevators.get(closestID).get(2) < floorRequestToBeProcessed.getSourceFloor()) {
            lastKnownDirection.put(closestID, "DN");
            Collections.sort(srcBeSorted, Collections.reverseOrder());
            sourceFloors.put(closestID, srcBeSorted);
            Collections.sort(destBeSorted,Collections.reverseOrder());
            destFloors.put(closestID, destBeSorted);

            if ( (int) elevators.get(closestID).get(4) < sourceFloors.get(closestID).getFirst())
            {
                sendCommand(closestID, sourceFloors.get(closestID).getFirst());
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

    /*public void setSendElevatorRequest() {
        this.eventQueue.setElevatorRequest(this.processedRequest);
    }*/

    public ElevatorEvent getFloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }

    public static void main(String[] args)
    {
        try {

            Scheduler scheduler = new Scheduler();

            Thread schedulerThread = new Thread(scheduler);
            schedulerThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

