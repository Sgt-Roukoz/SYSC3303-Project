//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public class Scheduler implements Runnable {
    private final EventQueue eventQueue;
    private ElevatorEvent floorRequestToBeProcessed;
    private ElevatorEvent processedRequest;

    public Scheduler(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void run() {
        while(this.eventQueue.processedEvents < this.eventQueue.maxEvents) {
            this.readFloorRequest();
            if (this.floorRequestToBeProcessed != null) {
                this.processFloorRequest();
                this.sendElevatorRequest();
            }
        }

    }

    private void readFloorRequest() {
        this.floorRequestToBeProcessed = this.eventQueue.getFloorRequest();
    }

    private void processFloorRequest() {
        System.out.println("Scheduler: Processing floor event: " + String.valueOf(this.floorRequestToBeProcessed));
        this.processedRequest = this.floorRequestToBeProcessed;
    }

    private void sendElevatorRequest() {
        System.out.println("Sending elevator event: " + String.valueOf(this.processedRequest));
        this.eventQueue.setElevatorRequest(this.processedRequest);
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

    public ElevatorEvent getfloorRequestToBeProcessed() {
        return this.floorRequestToBeProcessed;
    }

    public ElevatorEvent getProcessedRequest() {
        return this.processedRequest;
    }
}
