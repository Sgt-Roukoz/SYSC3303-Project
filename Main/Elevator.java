/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes UDP from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @author Masrur Husain
 * @author Marwan Zeid
 * @author Garrison Su
 * @version 2024-03-14
 */
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
/**
 * Interface representing an abstraction of all Elevator states
* */
interface ElevatorState{

    /**
     * The entry action, called when state is entered
    * @param context The context of the elevator state machine
    */
    void entry(Elevator context);

    /**
     * Handles the event when a floor request arrives
    * @param context The context of the elevator state machine
    */
    void floorRequest(Elevator context);

    /**
     * Handles the event when elevator arrives at source floor
    * @param context The context of the elevator state machine
    */
    void arrivedAtFloor(Elevator context);


    /**
     * Handles the event when elevator doors are requested to be closed
    * @param context The context of the elevator state machine
    */
    void doorsClosed(Elevator context);

    /**
     * Displays current state information
    */
    void displayState();

    String toString();
 
 }
 
 /**
  * Concrete class implementation of the ElevatorState interface representing the Idle state
  * */
class Idle implements ElevatorState{
    @Override
    public void entry(Elevator context) {
        context.sendIdleStatusUpdate();
        System.out.println("Elevator " + context.getElevatorId() + " is Idle");
        context.waitMessageScheduler(); 
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Floor Requested...Elevator is moving.");
        if (context.getCurrentFloor() == context.getDestinationFloor()) {
            context.setCurrentState("LoadingUnloading");
        } else {
            context.setCurrentState("Moving");
        }
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void doorsClosed(Elevator context){
        System.out.println("Elevator is still idling.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }

    @Override
    public String toString(){
        return "Idle";
    }
}

/**
 * Concrete class implementation of the ElevatorState interface representing the Moving state.
 */
class Moving implements ElevatorState {
    @Override
    public void entry(Elevator context) {
        System.out.println("Elevator is moving.");
        context.sendMovingStatusUpdate();

        try {
            context.moveToFloor(context.getDestinationFloor());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } 
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is moving.");      
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        context.setCurrentState("LoadingUnloading");
    }

    @Override
    public void doorsClosed(Elevator context) {
        System.out.println("Door is already closed, elevator is moving");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is moving.");
    }

    @Override
    public String toString() {
        return "Moving";
    }
}



//Loading/Unloading state
class LoadingUnloading implements ElevatorState {
    @Override
    public void entry(Elevator context) {
        doorsClosed(context);
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is loading/unloading.");
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public void doorsClosed(Elevator context) {
        try {
            context.openDoors();
            context.closeDoors();
            context.setCurrentState("Idle");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public String toString() {
        return "LoadingUnloading";
    }
}
 
public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 3000; // Average time per floor in milliseconds (Halved)
    protected static final long DOOR_OPERATION_TIME = 5000; // Average door operation time in milliseconds (Halved)
    private static final int ACK_LOOP_WAIT_TIME = 3000; // Time to wait for a response from the scheduler
    private int currentFloor;
    private final int elevatorId;
    private boolean doorsOpen;
    private final Map<String, ElevatorState> states;
    private ElevatorState currentState;    
    int serverPort = 5000;
    private DatagramSocket sendReceiveSocket;
    private boolean acknowledged = false; 
    private final int MAX_ATTEMPTS = 5; // Maximum number of attempts to send a message to the scheduler
    private String direction; // UP or DN
    private int destinationFloor; 
    /**
     * Constructs an Elevator object with a specified Scheduler and elevator ID.
    * The elevator is initialized on the ground floor with doors closed.
    *
    * @param elevatorId The ID of the elevator, unique within the elevator system.
    */
    public Elevator(int elevatorId) {
        this.elevatorId = elevatorId;
        this.currentFloor = 0; // Assuming ground floor as start.
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
    }
 
    public int getElevatorId(){return elevatorId;}
    public void floorRequested() {currentState.floorRequest(this);currentState.displayState();}
    public void arrivedAtFloor(){currentState.arrivedAtFloor(this);currentState.displayState();}
    public void doorsClosed(){currentState.doorsClosed(this);currentState.displayState();}
    public int getCurrentFloor(){return currentFloor;}
    public int getDestinationFloor(){return destinationFloor;}
    /**
    * Method to call the floorRequest event handling method (for transitioning from Idle to MovingToFloor)
    * @param nextState The string representation of what the state to come after the current one is
    */
    public void setCurrentState(String nextState){this.currentState = states.get(nextState);currentState.entry(this);}
    
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
    protected void waitMessageScheduler(){
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            sendReceiveSocket.receive(receivePacket);
            String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
            if (translatedMessage.startsWith("03")) {
                direction = translatedMessage.substring(2,4);
                currentFloor = Integer.parseInt(translatedMessage.substring(5,7));
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }

        floorRequested();
        
    }
    protected void packetSentGetAck(String message){
        boolean ackBool = false;
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            int attempts = 0;
            while (!ackBool && attempts < MAX_ATTEMPTS) {
                try {
                    
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
                    if (translatedMessage.equals("ACK" + message)) {
                        ackBool = true;
                        System.out.println("Elevator " + elevatorId + " has been acknowledged by the scheduler.");
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

    protected void sendIdleStatusUpdate(){
        String message = "04" + elevatorId + ",Idle," + currentFloor + "0";
        System.out.println("Elevator "+ getElevatorId() + " sending idle status update...");
        packetSentGetAck(message); // 04 stuff
    }

    protected void sendMovingStatusUpdate() {
        String message = "04" + elevatorId + ",Moving," + direction + "," + currentFloor + "," + destinationFloor + "0";
        System.out.println("Sending Moving status update...");
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
        
        System.out.println("Elevator " + String.valueOf(getElevatorId())+ " Sending an IEXIST message to the scheduler.");       
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
                        System.out.println("Elevator " + elevatorId + " has been acknowledged");
                    }
                } catch (SocketTimeoutException e) {System.out.println("Attempt " + attempts + ": No response from scheduler, retrying...");}
            }
            if (!acknowledged) {System.out.println("Failed to receive acknowledgement from scheduler after " + MAX_ATTEMPTS + " attempts.");}
        } catch (IOException e) {e.printStackTrace();}
        
        try{sendReceiveSocket.setSoTimeout(0);} catch (Exception e) {e.printStackTrace();}
        setCurrentState("Idle");
    }
 
     /**
      * Simulates the movement of the elevator to a specified floor.
      * The movement is simulated by pausing the thread for a calculated duration.
      * @param floor The target floor to which the elevator should move.
      * @throws InterruptedException if the thread is interrupted while sleeping.
      * @throws IOException if an I/O error occurs while sending or receiving a packet.
      */
    protected void moveToFloor(int floor) throws InterruptedException, IOException {
        // int floorDifference = Math.abs(floor - currentFloor);
        long travelTimePerFloor = TIME_PER_FLOOR;
        sendReceiveSocket.setSoTimeout((int) travelTimePerFloor);
        
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
                }
            } catch (SocketTimeoutException e) {
                if (floor > currentFloor) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
                packetSentGetAck("04" + elevatorId + ",Moving," + direction + "," + currentFloor + "," + destinationFloor + "0");
                System.out.println("Elevator " + elevatorId + " is now at floor " + currentFloor);
            }
        }
        currentFloor = floor;

        arrivedAtFloor();
        System.out.println("Elevator " + elevatorId + " arrived at floor " + destinationFloor);
    }
 
    /**
    * Simulates opening the elevator doors.
    */
    protected void openDoors() throws InterruptedException {
        System.out.println("Elevator " + elevatorId + " doors opening.");
        doorsOpen = true;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors opening
    }
 
    /**
    * Simulates closing the elevator doors.
    */
    protected void closeDoors() throws InterruptedException {
        System.out.println("Elevator " + elevatorId + " doors closing.");
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

        SchedulerStore store = new SchedulerStore();
        SchedulerReceiver schedulerReceiver = new SchedulerReceiver(store); 
        // Scheduler scheduler = new Scheduler(store);  
        Elevator elevator1 = new Elevator(1);
        Elevator elevator2 = new Elevator(2);
        Elevator elevator3 = new Elevator(3);
        Elevator elevator4 = new Elevator(4);

        // Thread schedulerThread = new Thread(scheduler);
        Thread schedulerReceiverThread = new Thread(schedulerReceiver); 
        Thread elevatorThread1 = new Thread(elevator1);
        Thread elevatorThread2 = new Thread(elevator2);
        Thread elevatorThread3 = new Thread(elevator3);
        Thread elevatorThread4 = new Thread(elevator4);
        
        elevatorThread1.start();
        elevatorThread2.start();
        elevatorThread3.start();
        elevatorThread4.start();
        schedulerReceiverThread.start(); 
        //schedulerThread.start();
    }
}


