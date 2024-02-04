//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.time.LocalTime;

public class Elevator implements Runnable {
    private int currentFloor;
    private boolean doorsOpen;
    private final Scheduler scheduler;

    public Elevator(Scheduler scheduler, int elevatorId) {
        this.scheduler = scheduler;
        this.currentFloor = 0;
        this.doorsOpen = false;
    }

    public void run() {
        while(true) {
            ElevatorEvent event = this.scheduler.getEvent();
            this.processEvent(event);
        }
    }

    private void moveToFloor(int floor) {
        this.currentFloor = floor;
        this.openDoors();
        this.closeDoors();
    }

    private void openDoors() {
        this.doorsOpen = true;
    }

    private void closeDoors() {
        this.doorsOpen = false;
    }

    public void handleInsideButtonPress(int floor) {
        ElevatorEvent buttonPressEvent = new ElevatorEvent(LocalTime.now().toString(), floor, ELEVATOR_BUTTON.INSIDE, floor);
        this.sendEventToScheduler(buttonPressEvent);
    }

    private void sendEventToScheduler(ElevatorEvent event) {
        this.scheduler.setEvent(event);
    }

    private void processEvent(ElevatorEvent event) {
        this.sendEventToScheduler(new ElevatorEvent(LocalTime.now().toString(), this.currentFloor, ELEVATOR_BUTTON.START_MOVE, event.getFloor()));
        this.moveToFloor(event.getFloor());
        this.sendEventToScheduler(new ElevatorEvent(LocalTime.now().toString(), this.currentFloor, ELEVATOR_BUTTON.ARRIVAL, event.getFloor()));
    }
}
