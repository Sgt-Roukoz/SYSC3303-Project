/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes UDP from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @author Masrur Husain
 * @author Marwan Zeid
 * @author Garrison Su
 * @version 2024-03-29
 */

package Main;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import static java.lang.Math.abs;


public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 3000; // Average time per floor in milliseconds (Halved)
    protected static final long DOOR_OPERATION_TIME = 5000; // Average door operation time in milliseconds (Halved)
    private static final int ACK_LOOP_WAIT_TIME = 3000; // Time to wait for a response from the scheduler
    private int currentFloor; // current floor of elevator
    private final int elevatorId; // ID of elevator
    private boolean doorsOpen; // boolean for if doors are open
    private final Map<String, ElevatorState> states; // map of states for state machine
    private ElevatorState currentState; // current state of elevator
    private static final int serverPort = 5000; // scheduler port
    private DatagramSocket sendReceiveSocket; // socket for sending and receiving UDP
    private boolean acknowledged = false;
    private final int MAX_ATTEMPTS = 5; // Maximum number of attempts to send a message to the scheduler
    private String direction; // UP or DN
    private int destinationFloor; //current destination floor
    protected boolean transientFault = false;
    protected boolean hardFault = false;

    /**
     * Constructs an Elevator object with a specified Scheduler and elevator ID.
     * The elevator is initialized on the ground floor with doors closed.
     *
     * @param elevatorId The ID of the elevator, unique within the elevator system.
     */
    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 1; // Assuming ground floor as start.
        this.doorsOpen = false;
        states = new HashMap<>();
        states.put("Idle", new Idle());
        states.put("LoadingUnloading", new LoadingUnloading());
        states.put("Moving", new Moving());

        try{
            sendReceiveSocket = new DatagramSocket();
            sendReceiveSocket.setSoTimeout(ACK_LOOP_WAIT_TIME); // acknowdgement timeout loop time
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Activated!");
        //ElevatorInspector.getInstance().moveElevatorGUI(elevatorId,1,0);

    }

    //getters and event calls for state machine
    public void floorRequested() {currentState.floorRequest(this);currentState.displayState();}
    public void arrivedAtFloor(){currentState.arrivedAtFloor(this);currentState.displayState();}
    public void doorsClosed(){currentState.doorsClosed(this);currentState.displayState();}
    public int getCurrentFloor(){return currentFloor;}
    public int getDestinationFloor(){return destinationFloor;}
    public int getElevatorId(){return elevatorId;}

    /**
     * Method to call the floorRequest event handling method (for transitioning from Idle to MovingToFloor)
     * @param nextState The string representation of what the state to come after the current one is
     */
    public void setCurrentState(String nextState){this.currentState = states.get(nextState);currentState.entry(this);}

    /**
     * Send message to scheduler indicating that this elevator exists
     */
    private void sendIExistMessage() {
        try {
            String message = "02" + elevatorId + "0";
            byte[] sendData = HelperFunctions.generateMsg(message);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), serverPort); // 5000
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wait for message from a scheduler, and send acknowledgement
     */
    protected void waitMessageScheduler(){
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            sendReceiveSocket.setSoTimeout(0);
            sendReceiveSocket.receive(receivePacket);
            String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
            System.out.println("Elevator " + elevatorId + " Received: " + translatedMessage + " (waitScheduler)" );

            if (translatedMessage.startsWith("03")) {
                direction = translatedMessage.substring(2,4);
                destinationFloor = Integer.parseInt(translatedMessage.substring(5,7));
                checkFaultType(translatedMessage);
            }
            byte[] ackMessage = ("ACK" + translatedMessage).getBytes();
            DatagramPacket sendPack = new DatagramPacket(ackMessage, ackMessage.length, receivePacket.getAddress(), receivePacket.getPort());
            sendReceiveSocket.send(sendPack);

        } catch (IOException e) {
            e.printStackTrace();
        }

        floorRequested();

    }

    /**
     * Send message to scheduler and wait for acknowledgement
     * @param message Message to be sent
     */
    protected void packetSentGetAck(String message){
        boolean ackBool = false;
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            int attempts = 0;
            while (!ackBool && attempts < MAX_ATTEMPTS) {
                try {
                    //System.out.println("Elevator " + elevatorId + " Sending: " + message + " (packetAck)" );
                    byte[] sendData = HelperFunctions.generateMsg(message);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), serverPort); // 5000
                    sendReceiveSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                attempts++;
                try {
                    sendReceiveSocket.receive(receivePacket);
                    String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
                    //System.out.println("Elevator " + elevatorId + " Received: " + translatedMessage + " (packetSentAck)" );

                    if (translatedMessage.equals("ACK" + message)) {
                        ackBool = true;
                        //System.out.println("Elevator " + elevatorId + " has been acknowledged by the scheduler.");
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Attempt " + attempts + ": No response from scheduler, retrying...");
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to receive acknowledgement from scheduler after " + MAX_ATTEMPTS + " attempts.");
            e.printStackTrace();
        }
    }

    /**
     * Sends status update to scheduler indicating elevator is idle
     */
    protected void sendIdleStatusUpdate(){
        String message = "04" + elevatorId + ",Idle," + currentFloor + "0";
        //System.out.println("Elevator "+ getElevatorId() + " sending idle status update...");
        packetSentGetAck(message); // 04 stuff
    }

    /**
     * Sends status update to scheduler indicating elevator is moving
     */
    protected void sendMovingStatusUpdate() {
        String message = "04" + elevatorId + ",Moving," + direction + "," + currentFloor + "," + destinationFloor + "0";
        //System.out.println("Sending Moving status update...");
        packetSentGetAck(message);
    }

    /**
     * The run method that is the entry point for the elevator's thread.
     * It continuously polls for events from the Scheduler and processes them.
     */
    @Override
    public void run() {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        System.out.println("Elevator " + getElevatorId() + " Sending an IEXIST message to the scheduler.");
        try {
            int attempts = 0;
            while (!acknowledged && attempts < MAX_ATTEMPTS) {
                sendIExistMessage();
                attempts++;
                try {
                    sendReceiveSocket.receive(receivePacket);
                    String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
                    if (translatedMessage.startsWith("ACK")) {
                        acknowledged = true;
                        //System.out.println("Elevator " + elevatorId + " has been acknowledged");
                    }
                } catch (SocketTimeoutException e) {System.out.println("Attempt " + attempts + ": No response from scheduler, retrying...");}
            }
            if (!acknowledged) {
                System.out.println("Failed to receive acknowledgement from scheduler after " + MAX_ATTEMPTS + " attempts.");
                System.exit(1);
            }
        } catch (IOException e) {e.printStackTrace();}

        try{sendReceiveSocket.setSoTimeout(0);} catch (Exception e) {e.printStackTrace();}
        setCurrentState("Idle");
    }

    /**
     * Simulates the movement of the elevator to a specified floor.
     * The movement is simulated by pausing the thread for a calculated duration.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     * @throws IOException if an I/O error occurs while sending or receiving a packet.
     */
    protected void moveToFloor() throws InterruptedException, IOException {
        sendReceiveSocket.setSoTimeout((int) TIME_PER_FLOOR);
        System.out.println("Elevator " + elevatorId + ": Moving to floor " + destinationFloor + " (current floor: " + currentFloor + ")");
        //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Elevator " + elevatorId + ": Moving to floor " + destinationFloor + " (current floor: " + currentFloor + ")");

        while (currentFloor != destinationFloor){
            try {

                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);



                sendReceiveSocket.receive(receivePacket);
                String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
                if (translatedMessage.startsWith("03")) {
                    destinationFloor = Integer.parseInt(translatedMessage.substring(5,7));
                    byte[] ack = HelperFunctions.generateMsg("ACK"+ translatedMessage);
                    DatagramPacket tempack = new DatagramPacket(ack, ack.length, receivePacket.getAddress(), receivePacket.getPort());
                    sendReceiveSocket.send(tempack);
                    if (abs(currentFloor - destinationFloor) == 1 && hardFault) //simulating getting stuck between floors
                    {
                        System.out.println("ERROR-2: Elevator" + getElevatorId() + " hasn't reached its destination, ceasing function");
                        //ElevatorInspector.getInstance().updateElevatorLog(getElevatorId(), "ERROR-2: Elevator" + getElevatorId() + " hasn't reached its destination, ceasing function");
                        String message = "04" + elevatorId + ",Out," + currentFloor + "0";
                        packetSentGetAck(message);
                        //ElevatorInspector.getInstance().moveElevatorGUI(elevatorId, currentFloor, 2 ); //Change color of elevator
                        break;
                    }
                    checkFaultType(translatedMessage);
                    System.out.println("Elevator " + elevatorId + ": Moving to floor " + destinationFloor + " (current floor: " + currentFloor + ")");
                    //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Elevator " + elevatorId + ": Moving to floor " + destinationFloor + " (current floor: " + currentFloor + ")");
                }

            } catch (SocketTimeoutException e) {
                if (destinationFloor > currentFloor) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
                packetSentGetAck("04" + elevatorId + ",Moving," + direction + "," + currentFloor + "," + destinationFloor + "0");
                System.out.println("Elevator " + elevatorId + " is now at floor " + currentFloor);
                //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Elevator " + elevatorId + " is now at floor " + currentFloor);
                //ElevatorInspector.getInstance().moveElevatorGUI(elevatorId, currentFloor, 0);
            }
        }

        if (!hardFault)
        {
            sendReceiveSocket.setSoTimeout(0);
            arrivedAtFloor();
            System.out.println("Elevator " + elevatorId + " arrived at floor " + destinationFloor);
            //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Arrived at floor " + destinationFloor);

        }
    }

    /**
     * Check if any important messages were missed
     */
    protected void checkForIncomingMessages()
    {
        try
        {
            sendReceiveSocket.setSoTimeout(200);
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            sendReceiveSocket.receive(receivePacket);
            String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
            if (translatedMessage.startsWith("03")) {
                destinationFloor = Integer.parseInt(translatedMessage.substring(5,7));
                checkFaultType(translatedMessage);
                byte[] ack = HelperFunctions.generateMsg("ACK"+ translatedMessage);
                DatagramPacket tempack = new DatagramPacket(ack, ack.length, receivePacket.getAddress(), receivePacket.getPort());
                sendReceiveSocket.send(tempack);
                System.out.println("Moving to floor " + destinationFloor);
                floorRequested();
            }
        } catch (IOException e) {
            System.out.println("No messages");
        }
    }

    /**
     * Checks the fault type of the message received, and sets up the trigger for that fault
     * @param msg the message received
     */
    private void checkFaultType(String msg)
    {
        int faultType = Integer.parseInt(String.valueOf(msg.charAt(8)));
        switch (faultType)
        {
            case 1:
                hardFault = false;
                transientFault = true;
                break;
            case 2:
                hardFault = true;
                transientFault = false;
                break;
            default:
                hardFault = false;
                transientFault = false;
                break;
        }
    }

    /**
     * Simulates opening the elevator doors.
     */
    protected void openDoors() throws InterruptedException {
//        System.out.println("Elevator " + elevatorId + " doors opening.");
        //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Elevator " + elevatorId + " doors opening.");
        doorsOpen = true;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors opening
    }

    /**
     * Simulates closing the elevator doors.
     */
    protected void closeDoors() throws InterruptedException {
//        System.out.println("Elevator " + elevatorId + " doors closing.");
        //ElevatorInspector.getInstance().updateElevatorLog(elevatorId, "Elevator " + elevatorId + " doors closing.");
        doorsOpen = false;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors closing
    }

    //Setters and getters for Testing purposes
    public void setOpenDoors() {doorsOpen = true;}
    public void setCloseDoors() {doorsOpen = false;}
    public boolean getDoorBoolean() {return doorsOpen;}
    public String getCurrentState(){return currentState.toString();}

    public static void main(String[] args){
        System.out.println("Elevator Main Subsystem starting...");

        Elevator elevator1 = new Elevator(1);
        Elevator elevator2 = new Elevator(2);
        Elevator elevator3 = new Elevator(3);
        Elevator elevator4 = new Elevator(4);

        Thread elevatorThread1 = new Thread(elevator1);
        Thread elevatorThread2 = new Thread(elevator2);
        Thread elevatorThread3 = new Thread(elevator3);
        Thread elevatorThread4 = new Thread(elevator4);

        try
        {
            elevatorThread1.start();
            Thread.sleep(25);
            elevatorThread2.start();
            Thread.sleep(25);
            elevatorThread3.start();
            Thread.sleep(25);
            elevatorThread4.start();
            Thread.sleep(25);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}