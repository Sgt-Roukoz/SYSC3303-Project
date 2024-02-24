
/**
 * Main Class, sets up all subsystems and starts them in the proper order.
 */

public class Main {
    public static void main(String[] args) {
        EventQueue eventQueue = new EventQueue();
        Scheduler scheduler = new Scheduler(eventQueue);
        Elevator elevator = new Elevator(eventQueue, 1);
        Floor floor = new Floor("Floor 1", eventQueue);

        Thread floorThread = new Thread(floor);
        Thread schedulerThread = new Thread(scheduler);
        Thread elevatorThread = new Thread(elevator);

        elevatorThread.start();
        floorThread.start();
        schedulerThread.start();
    }
}
