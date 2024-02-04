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
        while (eventQueue.processedEvents < eventQueue.maxEvents) {

            readFloorRequest();
            if (floorRequestToBeProcessed != null) {
                processFloorRequest();
                sendElevatorRequest();
            }
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
        //do stuff, functionality ot be added in later iteration

        System.out.println("Scheduler: Processing floor event: " + floorRequestToBeProcessed);
        processedRequest = floorRequestToBeProcessed;
    }

    /**
     * Send the processed ElevatorEvent to the elevator queue in eventQueue
     */
    private void sendElevatorRequest()
    {
            System.out.println("Sending elevator event: " + processedRequest);
            eventQueue.setElevatorRequest(processedRequest);
    }


}
