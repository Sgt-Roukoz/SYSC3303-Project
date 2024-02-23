/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Garrison Su
 * @version 2024-02-22
 */

public class Scheduler implements Runnable {
    private enum SchedulerState {
        IDLE,
        PROCESSING_COMMAND,
        WAITING
    }
    private SchedulerState state;
    private final EventQueue eventQueue;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;

    /**
     * Scheduler class constructor
     *
     * @param eventQueue EventQueue object where various events will be stored
     */
    public Scheduler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
        this.state = SchedulerState.IDLE; // starting state
    }

//    @Override
//    public void run() {
//        while (eventQueue.processedEvents < eventQueue.maxEvents) {
//
//            readFloorRequest();
//            if (floorRequestToBeProcessed != null) {
//                processFloorRequest();
//                sendElevatorRequest();
//            }
//        }
//    }

    @Override
    public void run() {
        while (eventQueue.processedEvents < eventQueue.maxEvents) {
            switch (state) {
                case IDLE:
                    readFloorRequest();
                    if (floorRequestToBeProcessed != null) {
                        System.out.println("Scheduler State: " + floorRequestToBeProcessed + ": Idle");
                        state = SchedulerState.PROCESSING_COMMAND;
                    }
                    break;
                case PROCESSING_COMMAND:
                    System.out.println("Scheduler State: PROCESSING_COMMAND");
                    processFloorRequest();
                    sendElevatorRequest();
                    state = SchedulerState.WAITING;
                    break;
                case WAITING:
                    System.out.println("Scheduler State: WAITING");
                    state = SchedulerState.IDLE;
                    break;
            }
        }
    }

    /**
     * Read an ElevatorEvent from the floor request queue in eventQueue
     */
    private void readFloorRequest() {
        floorRequestToBeProcessed = eventQueue.getFloorRequest();
    }

    /**
     * Process the ElevatorEvent request and convert into an event usable by Elevator
     */
    private void processFloorRequest() {
        //do stuff, functionality ot be added in later iteration

        System.out.println("Scheduler: Processing floor event: " + floorRequestToBeProcessed);
        processedRequest = floorRequestToBeProcessed;
    }

    /**
     * Send the processed ElevatorEvent to the elevator queue in eventQueue
     */
    private void sendElevatorRequest() {
        System.out.println("Sending elevator event: " + processedRequest);
        eventQueue.setElevatorRequest(processedRequest);
        // eventQueue.processedEvents++;
    }

    public void setReadFloorRequest() {
        this.floorRequestToBeProcessed = this.eventQueue.getFloorRequest();
    }

    public void setProcessFloorRequest() {
        this.processedRequest = this.floorRequestToBeProcessed;
    }

    public void setSendElevatorRequest() {
        this.eventQueue.setElevatorRequest(this.processedRequest);
    }

    public EventQueue getEventQueue() {
        return this.eventQueue;
    }

    public ElevatorEvent getFloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }



    // public static void main(String[] args) {
    //     EventQueue eventQueue = new EventQueue();

    //     // Simulate adding floor requests to the event queue
    //     ElevatorEvent floorRequest = new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4);
    //     eventQueue.setFloorRequest(floorRequest);
    //     ElevatorEvent floorRequest2 = new ElevatorEvent("17:05:20.0", 3, ELEVATOR_BUTTON.DOWN, 5);
    //     eventQueue.setFloorRequest(floorRequest2);
    //     ElevatorEvent floorRequest3 = new ElevatorEvent("18:05:15.0", 1, ELEVATOR_BUTTON.UP, 6);
    //     eventQueue.setFloorRequest(floorRequest3);

    //     Scheduler scheduler = new Scheduler(eventQueue);
    //     scheduler.run();
    //     System.out.println("All events finished!");
    // }
}

