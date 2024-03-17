package Main;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    int selectedElevator;
    String newEvent;

    private Map<Integer, ArrayList<Integer>> sourceFloors;
    private Map<Integer, ArrayList<Integer>> destFloors;

    /**
     * Scheduler class constructor
     *
     */
    public Scheduler(SchedulerStoreInt store) throws RemoteException {
        this.store = store;
        sourceFloors = new HashMap<>();
        destFloors = new HashMap<>();
        for(int i = 0; i < store.getElevators().size(); i++){
            System.out.println("Elevator Found");
            sourceFloors.put(i, new ArrayList<>());
            destFloors.put(i, new ArrayList<>());
        }
        try {
            sendReceiveSocket = new DatagramSocket(100);
        } catch (SocketException se) {
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
                if (arrayNotEmpty(sourceFloors)) checkArrivedAtDest();
                if (arrayNotEmpty(destFloors)) checkArrivedAtSource();
                readFloorRequest();
                if (floorRequestToBeProcessed != null) {
                    System.out.println("Scheduler is processing: " + floorRequestToBeProcessed);
                    selectedElevator = store.findClosest(floorRequestToBeProcessed);
                    newEvent = processFloorRequest();
                    sendSourceFloorToElevator(newEvent);
                }

                Thread.sleep(15);

            } catch (InterruptedException | RemoteException e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler was interrupted.");
            }
        }
    }

    private boolean arrayNotEmpty(Map<Integer, ArrayList<Integer>> map)
    {
        for (ArrayList<Integer> vals : map.values())
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

    /**
     * Process the ElevatorEvent request and convert into an event usable by Elevator
     */
    private String processFloorRequest() throws RemoteException {
        String translated = "03";
        if(((int) store.getElevators().get(selectedElevator).get(2)) > floorRequestToBeProcessed.getSourceFloor()){ // Finding where the request came from in relation to the elevator's current position
            translated = translated.concat("DN,");
        }else{
            translated = translated.concat("UP,");
        }
        translated = translated.concat(String.valueOf(floorRequestToBeProcessed.getSourceFloor()));

        System.out.println("Scheduler: Processing floor event: " + floorRequestToBeProcessed);
        processedRequest = floorRequestToBeProcessed;
        return translated;
    }


    /**
     * Process and regurgitate the destination floor message to be sent to the elevator
     * */
    private String destFloor(int destFloor) throws RemoteException {
        String desti = "03";
        if(destFloor > (int) store.getElevators().get(selectedElevator).get(2)){
            desti = desti.concat("UP,");
        }else{
            desti = desti.concat("DN,");
        }
        desti = desti.concat(String.valueOf(destFloor));
        return desti;
    }


    /**
     * Send the source floor in the processed ElevatorEvent to the elevator in elevatorTracker
     */
    private void sendSourceFloorToElevator(String happening) {
        System.out.println("Sending elevator event: " + processedRequest);
        //eventQueue.setElevatorRequest(processedRequest);

        try {
            byte[] toSend = HelperFunctions.generateMsg(happening);
            DatagramPacket sendPacket;
            int ipAddress = (int) store.getElevators().get(selectedElevator).getFirst();
            byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
            InetAddress address = InetAddress.getByAddress(bytes);
            sendPacket = new DatagramPacket(toSend, toSend.length,
                    address, (int) store.getElevators().get(selectedElevator).get(1));
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        byte[] acknowledged = new byte[100];
        DatagramPacket receivePacket = new DatagramPacket(acknowledged, acknowledged.length);

        try {
            // Block until Elevator acknowledges
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int msgLen = receivePacket.getLength();
        System.out.println("Scheduler: Source ACK received: ");
        HelperFunctions.printDataInfo(acknowledged, msgLen);

        sourceFloors.get(selectedElevator).add(processedRequest.getSourceFloor());
        destFloors.get(selectedElevator).add(processedRequest.getDestFloor());
    }

    /**
     * Send the destination floor to the elevator once it reaches the source floor.
     * */

    private void sendDestFloorToElevator(int floor) {
        byte[] acknowledged;
        int msgLen;
        DatagramPacket receivePacket;

        try {
            byte[] toSend = HelperFunctions.generateMsg(destFloor(floor));
            DatagramPacket destPacket;
            int ipAddress = (int) store.getElevators().get(selectedElevator).getFirst();
            byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
            InetAddress address = InetAddress.getByAddress(bytes);
            destPacket = new DatagramPacket(toSend, toSend.length,
                    address, (int) store.getElevators().get(selectedElevator).get(1));
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
     * Check if any elevators have arrived at a destination floor, and send them to their next destination floor if they have one.
     * */
    private void checkArrivedAtDest() throws RemoteException {
        Map<Integer, ArrayList<Serializable>> updatedElevs = store.getElevators();
        System.out.println(destFloors);
        for(int count = 1; count <= updatedElevs.size(); count++){
            for (Map.Entry<Integer, ArrayList<Integer>> entry : destFloors.entrySet()) {
                if((int) updatedElevs.get(count).get(3) == 0){ //Is the elevator idle?
                    if((int) updatedElevs.get(count).get(2) == entry.getValue().getFirst()){ //Does it match the destination floor? The destination floors should be in the order they came in, hence the 0
                        entry.getValue().removeFirst(); //Remove the destination floor
                        if(entry.getValue().getFirst() != null){ //Is there another destination after this one?
                            sendDestFloorToElevator(destFloors.get(count).getFirst());
                        }
                    }else{
                        System.out.println("Elevator " + entry.getKey().toString() + " is Idle on floor " + updatedElevs.get(count).get(2).toString() + ". It should not be there!");
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
        for(int count = 0; count < sourceElevs.size(); count++){
            for (Map.Entry<Integer, ArrayList<Integer>> entry : sourceFloors.entrySet()) {
                if((int) sourceElevs.get(count).get(3) == 0){ //Is the elevator idle?
                    if((int) sourceElevs.get(count).get(2) == entry.getValue().getFirst()){ //Does it match the source floor? The destination floors should be in the order they came in, hence the 0
                        entry.getValue().removeFirst(); //Remove the source floor
                        sendDestFloorToElevator(destFloors.get(count).getFirst());
                    }else{
                        System.out.println("Elevator " + entry.getKey().toString() + " is Idle on floor " + sourceElevs.get(count).get(2).toString() + ". It should not be there!");
                    }
                }
            }
        }
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
            SchedulerStoreInt store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");
            Scheduler scheduler = new Scheduler(store);

            Thread schedulerThread = new Thread(scheduler);
            schedulerThread.start();
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            throw new RuntimeException(e);
        }

    }
}

