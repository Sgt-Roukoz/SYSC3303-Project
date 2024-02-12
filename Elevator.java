
/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes events from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @version 2024-02-02
 */

public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 8000; // Average time per floor in milliseconds
    private static final long DOOR_OPERATION_TIME = 11000; // Average door operation time in milliseconds

    private int currentFloor;
    private final int elevatorId;
    private final EventQueue eventQueue;

    private boolean doorsOpen;

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
                    processEvent(event);
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
}

