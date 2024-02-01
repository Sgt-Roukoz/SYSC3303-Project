/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Marwan Zeid
 * @version 2024-01-30
 */

public class Scheduler implements Runnable{

    private ElevatorEvent storedEvent;
    private ElevatorEvent responseEvent;
    private boolean noEvent = true;
    private boolean noResponseEvent = true;

    public Scheduler(){}

    @Override
    public void run() {}

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
     * Returns the event that is currently stored in the scheduler
     * @return Returns storedEvent as ElevatorEvent
     */

    public synchronized ElevatorEvent getEvent()
    {
        while (noEvent)
        {
            mutex();
        }

        noEvent = true;
        return storedEvent;
    }

    /**
     * Saves the event in the scheduler.
     *
     * @param event The event to be stored
     */
    public synchronized void setEvent(ElevatorEvent event)
    {
        while (!noEvent)
        {
            mutex();
        }

        storedEvent = event;
        noEvent = false;
        notifyAll();
    }
    /**
     * Saves a response event to the scheduler
     *
     * @param event The event to be stored
     */
    public synchronized void setResponseEvent(ElevatorEvent event)
    {
        responseEvent = event;
        noResponseEvent = false;
        notifyAll();
    }

    /**
     * Returns the response event currently stored by the scheduler
     * @return Returns the current response
     */
    public synchronized ElevatorEvent getResponseEvent()
    {
        while(noResponseEvent)
        {
            mutex();
        }

        noResponseEvent = true;
        return responseEvent;
    }

}
