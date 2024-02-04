//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.util.ArrayDeque;

public class EventQueue {
    private final ArrayDeque<ElevatorEvent> floorRequest = new ArrayDeque();
    private final ArrayDeque<ElevatorEvent> elevatorRequest = new ArrayDeque();
    public int processedEvents = 0;
    public int maxEvents = 6;
    private final int maxQueue = 3;

    public EventQueue() {
    }

    private void mutex() {
        try {
            this.wait();
        } catch (InterruptedException var2) {
        }

    }

    public synchronized ElevatorEvent getFloorRequest() {
        if (this.floorRequest.isEmpty()) {
            return null;
        } else {
            ElevatorEvent returningEvent = (ElevatorEvent)this.floorRequest.remove();
            this.notifyAll();
            return returningEvent;
        }
    }

    public synchronized void setFloorRequest(ElevatorEvent event) {
        while(this.floorRequest.size() >= 3) {
            this.mutex();
        }

        this.floorRequest.add(event);
        this.notifyAll();
    }

    public synchronized void setElevatorRequest(ElevatorEvent event) {
        while(this.elevatorRequest.size() >= 3) {
            this.mutex();
        }

        this.elevatorRequest.add(event);
        this.notifyAll();
    }

    public synchronized ElevatorEvent getElevatorRequest() {
        while(this.elevatorRequest.isEmpty()) {
            this.mutex();
        }

        return (ElevatorEvent)this.elevatorRequest.poll();
    }

    public synchronized void elevatorArrived() {
        ++this.processedEvents;
        this.notifyAll();
    }
}
