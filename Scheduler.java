import java.util.ArrayDeque;

/**
 * Scheduler class
 * Acts as a communication channel for the floor and elevator subsystems
 *
 * @author Marwan Zeid
 * @version 2024-01-30
 */

public class Scheduler implements Runnable{

    private ArrayDeque<ElevatorEvent> storedEvents;
    private boolean noEvent = true;
    private boolean isFull = false;
    private final int maxEvents = 5;

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

        if (storedEvents.size() < maxEvents)
        {
            isFull = false;
        }
        if (storedEvents.isEmpty())
        {
            noEvent = true;
        }
        notifyAll();
        return storedEvents.poll();
    }

    public synchronized void setEvent(ElevatorEvent event)
    {
        while (!noEvent && !isFull)
        {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        storedEvents.add(event);
        if (storedEvents.size() >= maxEvents)
        {
            isFull = true;
        }
        noEvent = false;
        notifyAll();
    }


}
