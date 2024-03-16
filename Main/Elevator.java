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
     * Handles the event when a destination is requested
    * @param context The context of the elevator state machine
    */
    void destinationRequest(Elevator context);

    /**
     * Handles the event when elevator arrives at destination floor
    * @param context The context of the elevator state machine
    */
    void arrivedAtDestination(Elevator context);

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
        System.out.println("Elevator doors are closed.");
        System.out.println("Elevator is Idle");
        context.sendIdleStatusUpdate();
        context.waitMessageScheduler(); 
    }

    @Override
    public void floorRequest(Elevator context) {
        context.setCurrentState("Moving");
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void destinationRequest(Elevator context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void arrivedAtDestination(Elevator context){
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
        context.waitMessageScheduler(); 
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
    public void destinationRequest(Elevator context) {
        System.out.println("Elevator is moving.");
    }

    @Override
    public void arrivedAtDestination(Elevator context) {
        context.setCurrentState("LoadingUnloading");
    }

    @Override
    public void doorsClosed(Elevator context) {
        System.out.println("Elevator is moving.");
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
        System.out.println("Elevator is loading/unloading passengers.");
        context.notifySchedulerOfArrival();
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public void destinationRequest(Elevator context) {
        System.out.println("Destination floor requested.");
        context.setCurrentState("Moving");
    }

    @Override
    public void arrivedAtDestination(Elevator context) {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public void doorsClosed(Elevator context) {
        try {
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
    private int currentFloor;
    private final int elevatorId;
    private boolean doorsOpen;
    private final Map<String, ElevatorState> states;
    private ElevatorState currentState;    
    
    //Socket related stuff
    int serverPort = 5000;
    private DatagramSocket sendReceiveSocket;
    private boolean acknowledged = false; // Flag to check if the scheduler has acknowledged the elevator's existence
    private final int MAX_ATTEMPTS = 5;
    private String direction;
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
            sendReceiveSocket.setSoTimeout(3000); // acknowdgement timeout
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  sendReceiveSocket.close();
    }
 
    public int getElevatorId(){return elevatorId;}
    public void floorRequested() {currentState.floorRequest(this);currentState.displayState();}
    public void arrivedAtFloor(){currentState.arrivedAtFloor(this);currentState.displayState();}
    public void destinationRequest(){currentState.destinationRequest(this);currentState.displayState();}
    public void arrivedAtDestination(){currentState.arrivedAtDestination(this);currentState.displayState();}
    public void doorsClosed(){currentState.doorsClosed(this);currentState.displayState();}
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
                direction = translatedMessage.substring(2,3);
                currentFloor = Integer.parseInt(translatedMessage.substring(5,6));
            } 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void packetSender(String message){
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            int attempts = 0;
            while (!acknowledged && attempts < MAX_ATTEMPTS) {
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
                    if (translatedMessage.startsWith("ACK")) {
                        // if (message.startsWith("04Moving")) {
                        //     direction = translatedMessage.substring(12,13);
                        //     currentFloor = Integer.parseInt(translatedMessage.substring(15, 15));
                        //     destinationFloor = Integer.parseInt(translatedMessage.substring(17, 17));
                        // } else if (message.startsWith("04Idle")) {
                        //     currentFloor = Integer.parseInt(translatedMessage.substring(10,10));
                        // } else{
                        //     System.out.println("Elevator " + elevatorId + " has been acknowledged by the scheduler.");
                        // }
                        acknowledged = true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Attempt " + attempts + ": No response from scheduler, retrying...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void sendIdleStatusUpdate(){
        packetSender("04Idle," + currentFloor + "0"); // 04 stuff
    }

    protected void sendMovingStatusUpdate() {
        packetSender("04Moving," + direction + "," + currentFloor + "," + destinationFloor + "0");
    }

     /**
      * The run method that is the entry point for the elevator's thread.
      * It continuously polls for events from the Scheduler and processes them.
      */
     @Override
     public void run() {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        
        System.out.println("Sending IEXIST message to the scheduler.");       
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
                        System.out.println("Elevator " + elevatorId + " has been acknowledged by the scheduler.");
                    }
                } catch (SocketTimeoutException e) {System.out.println("Attempt " + attempts + ": No response from scheduler, retrying...");}
            }
            if (!acknowledged) {System.out.println("Failed to receive acknowledgement from scheduler after " + MAX_ATTEMPTS + " attempts.");}
        } catch (IOException e) {e.printStackTrace();}
        
        try{sendReceiveSocket.setSoTimeout(0);} catch (Exception e) {e.printStackTrace();}
        currentState = states.get("Idle");
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
        int floorDifference = Math.abs(floor - currentFloor);
        long travelTimePerFloor = TIME_PER_FLOOR;
        sendReceiveSocket.setSoTimeout((int) travelTimePerFloor);
        for (int i = 0; i < floorDifference; i++) {
            try {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                sendReceiveSocket.receive(receivePacket);     
                String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
                // System.out.println(translatedMessage);
                if (translatedMessage.startsWith("03")) {
                    currentFloor = Integer.parseInt(translatedMessage.substring(5,6));
                }
            } catch (SocketTimeoutException e) {
                if (floor > currentFloor) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
                System.out.println("Elevator " + elevatorId + " is now at floor " + currentFloor);
            }
        }
        this.currentFloor = floor;
        System.out.println("Elevator " + elevatorId + " arrived at floor " + currentFloor);
    }
 
     /**
      * Notifies the Scheduler of the elevator's arrival at a floor.
      * This method constructs an arrival event and sends it to the Scheduler.
      */
    protected void notifySchedulerOfArrival() {
        System.out.println("Elevator " + elevatorId + " notifying scheduler of arrival at floor " + currentFloor);
        //  eventQueue.elevatorArrived();
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
 }
