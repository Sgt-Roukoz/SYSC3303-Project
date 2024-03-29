/**
 * SchedulerStore is meant to be a thread-safe object for scheduler to share information between its receive thread and logic thread.
 * @author Marwan Zeid
 * @version 2024-03-06
 */

package Main;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SchedulerStore extends UnicastRemoteObject implements SchedulerStoreInt {
    private ArrayDeque<ElevatorEvent> floorRequests;
    private Map<Integer, ArrayList<Serializable>> elevators;
    //Map with elevator ID as keys, and arraylist containing: 0 as address, 1 as port, 2 as current floor, 3 as moving direction and 4 as destination
    //current floor default is 1
    // moving direction is 0 if Idle, 1 if UP and 2 if DOWN
    // destination is 0 if none (idle) and some floor number if not.

    /**
     * Constructor for SchedulerStore
     */
    public SchedulerStore() throws RemoteException
    {
        super();
        floorRequests = new ArrayDeque<>();
        elevators = new HashMap<>();
    }

    /**
     * Add elevator to the store
     * @param elevID ID of elevator to be added
     * @param address Address of elevator thread
     * @param port port of elevator thread
     */
    public synchronized void addElevator (Integer elevID, InetAddress address, Integer port)
    {
        System.out.println("Adding elevator");
        elevators.put(elevID, new ArrayList<>(Arrays.asList(address, port, 1, 0, 0)));
        System.out.println(elevators);
    }

    /**
     * Get status of all elevators
     * @return Returns a map of all elevators
     */
    public synchronized Map<Integer, ArrayList<Serializable>> getElevators()
    {
        return elevators;
    }

    /**
     * Update state of an elevator
     * @param elevID ID of elevator to be modified
     * @param itemToEdit index of item to be modified (0 through 4)
     * @param value value to be replaced with
     */
    public synchronized void updateElevator(Integer elevID, int itemToEdit, Serializable value)
    {
        elevators.get(elevID).set(itemToEdit, value);
    }

    /**
     * Returns a floor request event that is currently stored in the scheduler
     * @return Returns storedEvent as ElevatorEvent
     */
    public synchronized ElevatorEvent getFloorRequest()
    {
        if (floorRequests.isEmpty()) return null;
        return floorRequests.remove();
    }

    /**
     * Saves a floor request event in the scheduler.
     *
     * @param event The event to be stored
     */
    public synchronized void setFloorRequest(ElevatorEvent event)
    {
        floorRequests.addLast(event);
    }

    /**
     * Removes an elevator from the elevator list
     * @param id ID of elevator being removed
     */
    public synchronized void removeElevator(int id)
    {
        elevators.remove(id);
    }
}
