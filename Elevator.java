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

interface ElevatorState{
    void floorRequest(Elevator context);

    void arrivedAtFloor(Elevator context);

    void destinationRequest(Elevator context);

    void arrivedAtDestination(Elevator context);

    void doorsClosed(Elevator context);

    void displayState();

}

class Idle implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Floor requested.");
        context.setCurrentState("MovingToFloor", false);
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
}
class MovingToFloor implements ElevatorState{
    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void arrivedAtFloor(Elevator context){
        System.out.println("Elevator arrived at loading floor.");
        context.setCurrentState("Loading", true);
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
}

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
        System.out.println("Elevator arrived at destination floor.");
        context.setCurrentState("Unloading", true);
    }

    @Override
    public void doorsClosed(Elevator context){
        System.out.println("Elevator is still moving to destination.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is moving to destination.");
    }
}

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
        System.out.println("Destination floor requested.");
        context.setCurrentState("MovingToDestination", false);
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
}

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
        System.out.println("Elevator doors now closed.");
        context.setCurrentState("Idle", false);
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }
}

public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 8000; // Average time per floor in milliseconds
    private static final long DOOR_OPERATION_TIME = 11000; // Average door operation time in milliseconds

    private int currentFloor;
    private final int elevatorId;
    private final EventQueue eventQueue;

    private boolean doorsOpen;

    private Map<String, ElevatorState> states;
    private ElevatorState currentState;

    //private Boolean doorsOpen;

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
    }

    public void floorRequested() {
        currentState.floorRequest(this);
        currentState.displayState();
    }

    public void arrivedAtFloor(){
        currentState.arrivedAtFloor(this);
        currentState.displayState();
    }

    public void destinationRequest(){
        currentState.destinationRequest(this);
        currentState.displayState();
    }

    public void arrivedAtDestination(){
        currentState.arrivedAtDestination(this);
        currentState.displayState();
    }

    public void doorsClosed(){
        currentState.doorsClosed(this);
        currentState.displayState();
    }

    public void setCurrentState(String nextState, boolean doors){
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
                }
                switch(currentState.toString()){
                    case "Idle":
                        processEvent(event);
                        floorRequested();
                        currentState.displayState();
                        break;
                    case "MovingToFloor":
                        processEvent(event);
                        arrivedAtFloor();
                        currentState.displayState();
                        break;
                    case "Loading":
                        processEvent(event);
                        destinationRequest();
                        currentState.displayState();
                        break;
                    case "MovingToDestination":
                        processEvent(event);
                        arrivedAtDestination();
                        currentState.displayState();
                        break;
                    case "Unloading":
                        processEvent(event);
                        doorsClosed();
                        currentState.displayState();
                        break;
                    default:
                        System.out.println("Elevator is not in any recognised state");
                        currentState.displayState();
                }

            } catch (Exception e) {
                System.out.println("Elevator " + elevatorId + " interrupted.");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Processes an event received from the Scheduler. Depending on the event type,
     * the elevator will move to the requested floor and open/close its doors.
     *
     * @param event The ElevatorEvent to process.
     */
    private void processEvent(ElevatorEvent event) {
        try {
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
    }

    /**
     * Simulates the movement of the elevator to a specified floor.
     * The movement is simulated by pausing the thread for a calculated duration.
     *
     * @param floor The target floor to which the elevator should move.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private void moveToFloor(int floor) throws InterruptedException {
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
   private void notifySchedulerOfArrival() {
        System.out.println("Elevator " + elevatorId + " notifying scheduler of arrival at floor " + currentFloor);
        eventQueue.elevatorArrived();
    }

    /**
     * Simulates opening the elevator doors.
     */
    private void openDoors() throws InterruptedException {
        System.out.println("Elevator " + elevatorId + " doors opening.");
        doorsOpen = true;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors opening
    }

    /**
     * Simulates closing the elevator doors.
     */
    private void closeDoors() throws InterruptedException {
        System.out.println("Elevator " + elevatorId + " doors closing.");
        doorsOpen = false;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors closing
    }

    public void setOpenDoors() {doorsOpen = true;}
    public void setCloseDoors() {doorsOpen = false;}
    public boolean getDoorBoolean() {return doorsOpen;}
    public void setNotifySchedulerOfArrival() {eventQueue.elevatorArrived();}
/*
    public static void main(String[] args) {
        Elevator elevator = new Elevator();

        elevator.floorRequested();

        elevator.arrivedAtFloor();

        elevator.destinationRequest();

        elevator.arrivedAtDestination();

        elevator.doorsClosed();

    }*/
}

