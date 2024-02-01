
/**
 * Elevator class simulates the behavior of an elevator car within the elevator subsystem.
 * It processes events from the scheduler to perform actions such as moving to specific floors,
 * opening and closing doors, and signaling its current state
 *
 * @author Adham Elmahi
 * @version 2024-02-01
 */
public class Elevator implements Runnable {
    private int currentFloor;
    private boolean doorsOpen;
    private final Scheduler scheduler;


    /**
     * Constructs an Elevator object with a reference to the Scheduler and an elevator ID.
     * Initializes the elevator at floor 0 (ground floor) with doors closed.
     *
     * @param scheduler  The scheduler object used for receiving and sending events.
     * @param elevatorId The unique identifier for this elevator.
     */
    public Elevator(Scheduler scheduler, int elevatorId) {
        this.scheduler = scheduler;
        this.currentFloor = 0; // Assuming ground floor as start
        this.doorsOpen = false;
    }

    /**
     * The main run method for the elevator's thread. Continuously polls the scheduler for events
     * and processes them accordingly.
     */
    @Override
    public void run() {
        while (true) {
            ElevatorEvent event = scheduler.getEvent();
            processEvent(event);
        }
    }


    /**
     * Simulates moving the elevator to a specified floor. Updates the current floor and manages
     * the opening and closing of doors.
     *
     * @param floor The destination floor.
     */
    private void moveToFloor(int floor) {
        this.currentFloor = floor;
        // Simulate opening and closing doors upon arrival
        openDoors();
        closeDoors();
    }

    /**
     * Simulates opening the elevator doors by setting the doorsOpen attribute to true.
     */
    private void openDoors() {
        this.doorsOpen = true;
    }

    /**
     * Simulates closing the elevator doors by setting the doorsOpen attribute to false.
     */
    private void closeDoors() {
        this.doorsOpen = false;
    }

    /**
     * Simulates handling a button press inside the elevator. This method creates an event
     * for the button press and sends it to the scheduler for processing.
     *
     * @param floor The floor number that was selected inside the elevator.
     */
    public void handleInsideButtonPress(int floor) {
        // Assume ELEVATOR_BUTTON enum has an appropriate value for indicating an inside button press
        ElevatorEvent buttonPressEvent = new ElevatorEvent(java.time.LocalTime.now().toString(), floor, ELEVATOR_BUTTON.INSIDE, floor);
        sendEventToScheduler(buttonPressEvent);
    }

    /**
     * Sends an event to the scheduler. This method encapsulates the logic for communicating
     * with the scheduler, allowing the elevator to report its actions or requests.
     *
     * @param event The event to send to the scheduler.
     */
    private void sendEventToScheduler(ElevatorEvent event) {
        scheduler.setEvent(event);
    }

    /**
     * Processes an ElevatorEvent by moving to the required floor and performing related actions.
     * Extended to include notifying the scheduler upon arrival.
     *
     * @param event The ElevatorEvent to process.
     */
    private void processEvent(ElevatorEvent event) {
        // Notify scheduler when starting to move and upon arrival
        sendEventToScheduler(new ElevatorEvent(java.time.LocalTime.now().toString(), currentFloor, ELEVATOR_BUTTON.START_MOVE, event.getFloor()));
        moveToFloor(event.getFloor());
        // Notify scheduler of arrival
        sendEventToScheduler(new ElevatorEvent(java.time.LocalTime.now().toString(), currentFloor, ELEVATOR_BUTTON.ARRIVAL, event.getFloor()));
    }


}
