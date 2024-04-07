/**
 * Testing class for the Main.Floor subsystem
 */
package Testing;

import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

class FloorTest {

    static SchedulerReceiver receiver;
    static SchedulerStore store;

    @BeforeAll
    static void registrySetup() throws RemoteException, MalformedURLException, NotBoundException {
        store = new SchedulerStore();
        LocateRegistry.createRegistry(1099);
        // Bind the remote object's stub in the registry
        Naming.rebind("store", store);
        receiver = new SchedulerReceiver(store);
    }

    /**
     * Testing floor subsystem processing of string input
     */
    @Test
    void processInput(){
        //make scheduler receiver for testing
        ElevatorEvent elevatorTest = new ElevatorEvent("00:00", 2, ELEVATOR_BUTTON.UP, 4, 0, false);
        Floor testingFloor = new Floor();
        Thread receiveThred = new Thread(receiver);
        receiveThred.start();

        testingFloor.processInput("14:05:15.0 2 UP 4 0", false);

        Assertions.assertEquals(store.getFloorRequest().toString(), elevatorTest.toString());


    }

}