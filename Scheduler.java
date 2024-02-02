/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Marwan Zeid
 * @version 2024-02-02
 */

public class Scheduler implements Runnable{

    private final EventQueue eventQueue;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;
    private int processedEvents = 0;

    /**
     * Scheduler class constructor
     * @param eventQueue EventQueue object where various events will be stored
     */
    public Scheduler(EventQueue eventQueue){
        this.eventQueue = eventQueue;
    }

    /**
     * Main thread loop
     */
    @Override
    public void run() {
        while (processedEvents < 6) {
            readFloorRequest();
            processFloorRequest();
            processedEvents++;
            sendElevatorRequest();
        }
    }

    /**
     * Read an ElevatorEvent from the floor request queue in eventQueue
     */
    private void readFloorRequest()
    {
        floorRequestToBeProcessed = eventQueue.getFloorRequest();
    }

    /**
     * Process the ElevatorEvent request and convert into an event usable by Elevator
     */
    private void processFloorRequest()
    {
        System.out.println("Processing " + floorRequestToBeProcessed);
        //do stuff, functionality ot be added in later iteration
        processedRequest = floorRequestToBeProcessed;
    }

    /**
     * Send the processed ElevatorEvent to the elevator queue in eventQueue
     */
    private void sendElevatorRequest()
    {
        eventQueue.setElevatorRequest(processedRequest);
    }


}
