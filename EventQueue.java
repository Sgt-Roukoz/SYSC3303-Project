import java.util.ArrayDeque;
/**
 * EventQueue class
 * This class is used as a way to synchronize the Elevator, Floor, and Scheduler threads
 *
 * @author Marwan Zeid
 * @version 2024-02-02
 */

public class EventQueue {
    private final ArrayDeque<ElevatorEvent> floorRequest;
    private final ArrayDeque<ElevatorEvent> elevatorRequest;
    public int processedEvents = 0;
    public int maxEvents = 6; // Max events for testing purposes
    private final int maxQueue = 3;

    public EventQueue(){
        floorRequest = new ArrayDeque<>();
        elevatorRequest = new ArrayDeque<>();
    }

    /**
     * Method holds the wait() function for synchronization purposes
     */
    private void mutex()
    {
        try {
            wait();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Returns a floor request event that is currently stored in the scheduler
     * @return Returns storedEvent as ElevatorEvent
     */
    public synchronized ElevatorEvent getFloorRequest()
    {
        if (floorRequest.isEmpty()) return null;

        ElevatorEvent returningEvent = floorRequest.remove();
        notifyAll();
        return returningEvent;
    }

    /**
     * Saves a floor request event in the scheduler.
     *
     * @param event The event to be stored
     */
    public synchronized void setFloorRequest(ElevatorEvent event)
    {
        while (!(floorRequest.size() < maxQueue))
        {
            mutex();
        }

        floorRequest.add(event);
        notifyAll();
    }

    /**
     * Saves an elevator request event to the scheduler
     *
     * @param event The event to be stored
     */
    public synchronized void setElevatorRequest(ElevatorEvent event)
    {
        while (!(elevatorRequest.size() < maxQueue))
        {
            mutex();
        }

        elevatorRequest.add(event);
        notifyAll();
    }

    /**
     * Returns an elevator request event currently stored by the scheduler
     * @return Returns the current response
     */
    public synchronized ElevatorEvent getElevatorRequest()
    {
        while(elevatorRequest.isEmpty())
        {
            mutex();
        }

        return elevatorRequest.poll();
    }

    public synchronized void elevatorArrived()
    {
        processedEvents++;
        notifyAll();
    }
}
