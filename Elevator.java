
/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes events from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @version 2024-02-02
 */

public class Elevator implements Runnable {

    private static final long TIME_PER_FLOOR = 800; // Average time per floor in milliseconds
    private static final long DOOR_OPERATION_TIME = 1100; // Average door operation time in milliseconds

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
        while (eventQueue.processedEvents < eventQueue.maxEvents) {
            try {
                ElevatorEvent event = eventQueue.getElevatorRequest();
                if (event != null) {
                    processEvent(event);
                }
            } catch (Exception e) {
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
        System.out.println("Elevator: Processing " + event);
        try {
            switch (event.getButton()) {
                case UP:
                case DOWN:
                    // Simulate moving to the specified floor.
                    moveToFloor(event.getFloor());
                    // Simulate time for doors to open and close.
                    openDoors();
                    Thread.sleep(DOOR_OPERATION_TIME);
                    closeDoors();
                    notifySchedulerOfArrival();
                    break;
                case INSIDE:
                    // Simulate moving to the floor requested by an internal button press.
                    moveToFloor(event.getCar_button());
                    notifySchedulerOfArrival();
                    break;
                default:
                    break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        Thread.sleep(travelTime);
        this.currentFloor = floor;
        openDoors();
        Thread.sleep(DOOR_OPERATION_TIME); // Simulate doors being open for loading/unloading
        closeDoors();
    }

    /**
     * Notifies the Scheduler of the elevator's arrival at a floor.
     * This method constructs an arrival event and sends it to the Scheduler.
     */
    private void notifySchedulerOfArrival() {
        System.out.println("Arrived to floor ?");
        eventQueue.elevatorArrived();
    }

    /**
     * Simulates opening the elevator doors.
     */
    private void openDoors() throws InterruptedException {
        doorsOpen = true;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors opening
    }

    /**
     * Simulates closing the elevator doors.
     */
    private void closeDoors() throws InterruptedException {
        doorsOpen = false;
        Thread.sleep(DOOR_OPERATION_TIME / 2); // Simulate doors closing
    }

}

