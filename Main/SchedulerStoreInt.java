package Main;

import java.io.Serializable;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

public interface SchedulerStoreInt extends Remote {

    void addElevator (Integer elevID, InetAddress address, Integer port) throws RemoteException;
    Map<Integer, ArrayList<Serializable>> getElevators() throws RemoteException;
    void updateElevator(Integer elevID, int itemToEdit, Serializable value) throws RemoteException;
    int findClosest(ElevatorEvent floor) throws RemoteException;
    ElevatorEvent getFloorRequest() throws RemoteException;
    void setFloorRequest(ElevatorEvent event) throws RemoteException;
}
