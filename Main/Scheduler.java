package Main;

/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Garrison Su
 * @author Marwan Zeid
 * @version 2024-02-24
 */

public class Scheduler implements Runnable {
    public enum SchedulerState {
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

    /**
     * Main Scheduler subsystem run loop
     */
    @Override
    public void run() {
        while (eventQueue.processedEvents < eventQueue.maxEvents && !Thread.interrupted()) {
            try {
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
                        Thread.sleep(15);
                        processFloorRequest();
                        sendElevatorRequest();
                        state = SchedulerState.WAITING;
                        break;
                    case WAITING:
                        System.out.println("Scheduler State: WAITING");
                        Thread.sleep(15);
                        state = SchedulerState.IDLE;
                        break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Scheduler was interrupted.");
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
        //do stuff, functionality to be added in later iteration

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

    /*
    Setters and getters for testing purposes
     */
    public void setReadFloorRequest() {
        this.floorRequestToBeProcessed = this.eventQueue.getFloorRequest();
    }

    public void setProcessFloorRequest() {
        this.processedRequest = this.floorRequestToBeProcessed;
    }

    public void setSendElevatorRequest() {
        this.eventQueue.setElevatorRequest(this.processedRequest);
    }

    public ElevatorEvent getFloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }

    public SchedulerState getState()
    {
        return state;
    }
}

