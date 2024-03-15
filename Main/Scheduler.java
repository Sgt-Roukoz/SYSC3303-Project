package Main;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Garrison Su
 * @author Marwan Zeid
 * @version 2024-02-24
 */

public class Scheduler implements Runnable {
    public enum SchedulerState {
        IDLE,
        PROCESSING_COMMAND,
        WAITING
    }
    private DatagramSocket sendReceiveSocket;
    private SchedulerState state;
    private final EventQueue eventQueue;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;
    private SchedulerStore store;
    int selectedElevator;

    private HashMap<Integer, ArrayList> destinations;

    /**
     * Scheduler class constructor
     *
     * @param eventQueue EventQueue object where various events will be stored
     */
    public Scheduler(EventQueue eventQueue, SchedulerStore store) {
        this.eventQueue = eventQueue;
        this.state = SchedulerState.IDLE; // starting state
        this.store = store;
        for(int i = 0; i < store.getElevators().size(); i++){
            destinations.put(i, new ArrayList());
        }
        try {
            sendReceiveSocket = new DatagramSocket(100);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Main Scheduler subsystem run loop
     */
    @Override
    public void run() {
        while (eventQueue.processedEvents < eventQueue.maxEvents && !Thread.interrupted()) {
            try {
                switch (state) {
                    case IDLE:
                        readFloorRequest();
                        if (floorRequestToBeProcessed != null) {
                            System.out.println("Scheduler State: " + floorRequestToBeProcessed + ": Idle");
                            state = SchedulerState.PROCESSING_COMMAND;
                        }
                        break;
                    case PROCESSING_COMMAND:
                        System.out.println("Scheduler State: PROCESSING_COMMAND");
                        Thread.sleep(15);
                        selectedElevator = store.findClosest(floorRequestToBeProcessed);
                        String newEvent;
                        newEvent = processFloorRequest();
                        sendElevatorRequest(newEvent);
                        state = SchedulerState.WAITING;
                        break;
                    case WAITING:
                        System.out.println("Scheduler State: WAITING");
                        Thread.sleep(15);
                        state = SchedulerState.IDLE;
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler was interrupted.");
            }
        }
    }

    /**
     * Read an ElevatorEvent from the floor request queue in eventQueue
     */
    private void readFloorRequest() {
        floorRequestToBeProcessed = eventQueue.getFloorRequest();
    }

    /**
     * Process the ElevatorEvent request and convert into an event usable by Elevator
     */
    private String processFloorRequest() {
        String translated = "03";
        if(((int) store.getElevators().get(selectedElevator).get(2)) >
                floorRequestToBeProcessed.getSourceFloor()){ //Finding where the request came from in relation to the
            // elevator's current position
            translated.concat("DOWN,");
        }else{
            translated.concat("UP,");
        }
        translated.concat(String.valueOf(floorRequestToBeProcessed.getSourceFloor()));

        System.out.println("Scheduler: Processing floor event: " + floorRequestToBeProcessed);
        processedRequest = floorRequestToBeProcessed;
        return translated;
    }

    /**
     * Send the destination to the elevator after sending it the source floor
     * */
    private String destFloor(){
        String desti = "03";
        if(floorRequestToBeProcessed.getDestFloor() > (int) store.getElevators().get(selectedElevator).get(2)){
            desti.concat("UP,");
        }else{
            desti.concat("DOWN,");
        }
        desti.concat(String.valueOf(floorRequestToBeProcessed.getDestFloor()));
        return desti;
    }


    /**
     * Send the processed ElevatorEvent to the elevator queue in eventQueue
     */
    private void sendElevatorRequest(String happening) {
        System.out.println("Sending elevator event: " + processedRequest);
        eventQueue.setElevatorRequest(processedRequest);

        try {
            byte[] toSend = HelperFunctions.generateMsg(happening);
            DatagramPacket sendPacket;
            int ipAddress = (int) store.getElevators().get(selectedElevator).get(0);
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
        System.out.println("Scheduler: Source ACK received:");
        HelperFunctions.printDataInfo(acknowledged, msgLen);

        destinations.get(selectedElevator).add(processedRequest.getDestFloor());

        /*if(store.getElevators().get(selectedElevator).getStatus() != IDLE){ Meaning it's yet to arrive at the source floor
            wait();
        }
        *
        * */

        try {
            byte[] toSend = HelperFunctions.generateMsg(destFloor());
            DatagramPacket destPacket;
            int ipAddress = (int) store.getElevators().get(selectedElevator).get(0);
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

        /*if(store.getElevators().get(selectedElevator).getStatus() != IDLE){ Meaning it's yet to arrive at the dest floor
            wait();
        }else{
            for(int i = 0; i < destinations.get(selectedElevator).size(); i++){ //Get the first dest floor that matches, as
                                                                             //the floors are added in order
                if(destinations.get(selectedElevator).get(i) == processedRequest.getDestFloor()){
                    destinations.get(selectedElevator).remove(i);
                    break;
                }

            }
        }
        *
        * */

        // eventQueue.processedEvents++;
    }

    /*
    Setters and getters for testing purposes
     */
    public void setReadFloorRequest() {
        this.floorRequestToBeProcessed = this.eventQueue.getFloorRequest();
    }

    public void setProcessFloorRequest() {
        this.processedRequest = this.floorRequestToBeProcessed;
    }

    public void setSendElevatorRequest() {
        this.eventQueue.setElevatorRequest(this.processedRequest);
    }

    public ElevatorEvent getFloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }

    public SchedulerState getState()
    {
        return state;
    }
}

