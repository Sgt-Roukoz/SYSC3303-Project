import java.util.HashMap;
import java.util.Map;

/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes events from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @version 2024-02-02
 */

/**
 * Interface representing an abstraction of all Elevator states
 * */
interface ElevatorState{

    void floorRequest(Elevator context);

    void arrivedAtFloor(Elevator context);

    void destinationRequest(Elevator context);

    void arrivedAtDestination(Elevator context);

    void doorsClosed(Elevator context);

    void displayState();

}

/**
 * Concrete class implementation of the ElevatorState interface representing the Idle state
 * */
class Idle implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        try {
            System.out.println(context.getCurrentEvent().getSourceFloor() + "th floor requested.");
            context.setCurrentState("MovingToFloor");
            context.moveToFloor(context.getCurrentEvent().getSourceFloor());
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
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
 * Concrete class implementation of the ElevatorState interface representing the MovingToFloor state
 * */
class MovingToFloor implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        try{
            System.out.println("Elevator arrived at loading floor.");
            context.setCurrentState("Loading");
            context.openDoors();
            Thread.sleep(Elevator.DOOR_OPERATION_TIME / 2); // Simulate doors opening
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
    }

    @Override
    public void destinationRequest(Elevator context){
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void arrivedAtDestination(Elevator context){
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void doorsClosed(Elevator context){
        System.out.println("Elevator is still moving.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is moving.");
    }

    @Override
    public String toString(){
        return "MovingToFloor";
    }
}

/**
 * Concrete class implementation of the ElevatorState interface representing the MovingToDestination state
 * */
class MovingToDestination implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void destinationRequest(Elevator context){
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void arrivedAtDestination(Elevator context){
        try{
            context.openDoors();
            Thread.sleep(Elevator.DOOR_OPERATION_TIME); // Simulate doors staying open for people to exit/enter
            context.notifySchedulerOfArrival();
            System.out.println("Elevator arrived at destination floor.");
            context.setCurrentState("Unloading");
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }

    }

    @Override
    public void doorsClosed(Elevator context){
        System.out.println("Elevator is still moving to destination.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is moving to destination.");
    }

    @Override
    public String toString(){
        return "MovingToDestination";
    }
}

/**
 * Concrete class implementation of the ElevatorState interface representing the Loading state
 * */
class Loading implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void destinationRequest(Elevator context){
        try {
            context.closeDoors();
            Thread.sleep(Elevator.DOOR_OPERATION_TIME / 2); // Simulate doors closing
            System.out.println("Destination floor requested.");
            context.moveToFloor(context.getCurrentEvent().getDestFloor());
            context.setCurrentState("MovingToDestination");
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
    }

    @Override
    public void arrivedAtDestination(Elevator context){
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void doorsClosed(Elevator context){
        System.out.println("Elevator is still loading passengers.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is loading passengers.");
    }

    @Override
    public String toString(){
        return "Loading";
    }
}

/**
 * Concrete class implementation of the ElevatorState interface representing the Unloading state
 * */
class Unloading implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is still unload requested.");
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
        try{
            context.closeDoors();
            System.out.println("Elevator doors now closed.");
            context.setCurrentState("Idle");
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }

    @Override
    public String toString(){
        return "Unloading";
    }
}

public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 8000; // Average time per floor in milliseconds
    protected static final long DOOR_OPERATION_TIME = 11000; // Average door operation time in milliseconds

    private int currentFloor;
    private final int elevatorId;
    private final EventQueue eventQueue;
    private ElevatorEvent currentEvent;
    private boolean doorsOpen;

    private final Map<String, ElevatorState> states;
    private ElevatorState currentState;

    /**
     * Constructs an Elevator object with a specified Scheduler and elevator ID.
     * The elevator is initialized on the ground floor with doors closed.
     *
     * @param eventQueue  The eventQueue used for receiving events and sending responses.
     * @param elevatorId The ID of the elevator, unique within the elevator system.
     */
    public Elevator(EventQueue eventQueue, int elevatorId) {
        this.eventQueue = eventQueue;
        this.elevatorId = elevatorId;
        this.currentFloor = 0; // Assuming ground floor as start.
        this.doorsOpen = false;

        states = new HashMap<>();
        states.put("Idle", new Idle());
        states.put("MovingToFloor", new MovingToFloor());
        states.put("Loading", new Loading());
        states.put("MovingToDestination", new MovingToDestination());
        states.put("Unloading", new Unloading());

        currentState = states.get("Idle");
        currentEvent = null;

    }

    public int getElevatorId(){
        return elevatorId;
    }

    /**
     * Method to call the floorRequest event handling method (for transitioning from Idle to MovingToFloor)
     * */
    public void floorRequested() {
        currentState.floorRequest(this);
        currentState.displayState();
    }

    /**
     * Method to call the arrivedAtFloor event handling method (for transitioning from MovingToFloor to Loading)
     * */
    public void arrivedAtFloor(){
        currentState.arrivedAtFloor(this);
        currentState.displayState();
    }

    /**
     * Method to call the destinationRequest event handling method (for transitioning from Loading to MovingToDestination)
     * */
    public void destinationRequest(){
        currentState.destinationRequest(this);
        currentState.displayState();
    }

    /**
     * Method to call the arrivedAtDestination event handling method (for transitioning from MovingToDestination to Unloading)
     * */
    public void arrivedAtDestination(){
        currentState.arrivedAtDestination(this);
        currentState.displayState();
    }

    /**
     * Method to call the doorsClosed event handling method (for transitioning from Unloading to Idle)
     * */
    public void doorsClosed(){
        currentState.doorsClosed(this);
        currentState.displayState();
    }

    /**
     * Method to call the floorRequest event handling method (for transitioning from Idle to MovingToFloor)
     *
     * @param nextState The string representation of what the state to come after the current one is
     */
    public void setCurrentState(String nextState){
        this.currentState = states.get(nextState);
    }


    /**
     * The run method that is the entry point for the elevator's thread.
     * It continuously polls for events from the Scheduler and processes them.
     */
    @Override
    public void run() {
        System.out.println("Elevator " + elevatorId + " is starting on floor " + currentFloor);
        while (eventQueue.processedEvents < eventQueue.maxEvents) {
            try {
                ElevatorEvent event = eventQueue.getElevatorRequest();
                if (event != null) {
                    System.out.println("Elevator " + elevatorId + " received event: " + event);
                    currentEvent = event;
                    //processEvent(event);
                }
                switch(currentState.toString()){
                    case "Idle":
                        floorRequested();
                        currentState.displayState();
                        break;
                    case "MovingToFloor":
                        arrivedAtFloor();
                        currentState.displayState();
                        break;
                    case "Loading":
                        destinationRequest();
                        currentState.displayState();
                        break;
                    case "MovingToDestination":
                        arrivedAtDestination();
                        currentState.displayState();
                        break;
                    case "Unloading":
                        doorsClosed();
                        currentState.displayState();
                        break;
                }

            } catch (Exception e) {
                System.out.println("Elevator " + elevatorId + " interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public ElevatorEvent getCurrentEvent(){
        return this.currentEvent;
    }


    /**
     * Processes an event received from the Scheduler. Depending on the event type,
     * the elevator will move to the requested floor and open/close its doors.
     *
     * @param event The ElevatorEvent to process.
     */
    /*
    private void processEvent(ElevatorEvent event) {
        try {
            this.currentEvent = event;
            System.out.println("Elevator " + elevatorId + ": Processing " + event);
            if (currentFloor != event.getSourceFloor()) {
                moveToFloor(event.getSourceFloor());
                openDoors();
                Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors opening
                closeDoors();
                Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors closing
            }
            if (currentFloor != event.getDestFloor()) {
                moveToFloor(event.getDestFloor());
                openDoors();
                Thread.sleep(DOOR_OPERATION_TIME); // Simulate doors staying open for people to exit/enter
                closeDoors();
            }
            notifySchedulerOfArrival();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + elevatorId + " was interrupted.");
        }
    }*/

    /**
     * Simulates the movement of the elevator to a specified floor.
     * The movement is simulated by pausing the thread for a calculated duration.
     *
     * @param floor The target floor to which the elevator should move.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    protected void moveToFloor(int floor) throws InterruptedException {
        int floorDifference = Math.abs(floor - currentFloor);
        long travelTime = floorDifference * TIME_PER_FLOOR;
        System.out.println("Elevator " + elevatorId + " moving from floor " + currentFloor + " to floor " + floor);
        Thread.sleep(travelTime);
        this.currentFloor = floor;
        System.out.println("Elevator " + elevatorId + " arrived at floor " + currentFloor);
    }

    /**
     * Notifies the Scheduler of the elevator's arrival at a floor.
     * This method constructs an arrival event and sends it to the Scheduler.
     */
   protected void notifySchedulerOfArrival() {
        System.out.println("Elevator " + elevatorId + " notifying scheduler of arrival at floor " + currentFloor);
        eventQueue.elevatorArrived();
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

    public void setOpenDoors() {doorsOpen = true;}
    public void setCloseDoors() {doorsOpen = false;}
    public boolean getDoorBoolean() {return doorsOpen;}
    public void setNotifySchedulerOfArrival() {eventQueue.elevatorArrived();}

    public static void main(String[] args) {
        EventQueue eventQueue = new EventQueue();
        Scheduler scheduler = new Scheduler(eventQueue);
        Elevator elevator = new Elevator(eventQueue, 1);

        elevator.floorRequested();

        elevator.arrivedAtFloor();

        elevator.destinationRequest();

        elevator.arrivedAtDestination();

        elevator.doorsClosed();

    }
}

