/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Marwan Zeid
 * @version 2024-01-30
 */

public class Scheduler implements Runnable{

    private ElevatorEvent currentEvent;
    private boolean noEvent = true;

    public Scheduler(){}

    @Override
    public void run() {}

    public synchronized ElevatorEvent getEvent()
    {
        while (noEvent)
        {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        noEvent = true;
        notifyAll();
        return currentEvent;
    }

    public synchronized void setEvent(ElevatorEvent event)
    {
        while (!noEvent)
        {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        currentEvent = event;
        noEvent = false;
        notifyAll();
    }


}
