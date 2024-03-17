/**
 * Testing class for the Main.Scheduler subsystem
 */

package Testing;

import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

class SchedulerTest {
    SchedulerTest() {
    }

    Scheduler scheduler;
    SchedulerStoreInt store;

    /**
     * Called before each test
     */
    @BeforeEach
    void setUp() throws RemoteException {
        try {
            store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");
        } catch (NotBoundException | ConnectException | MalformedURLException e) {
            store = new SchedulerStore();
        }
        scheduler = new Scheduler(store);
    }

    /**
     * Test reading floor requests
     */
    @Test
    void testReadFloorRequest() throws RemoteException {

        ElevatorEvent elevatorEvent = new ElevatorEvent("14:05:15.0", 3, ELEVATOR_BUTTON.UP, 5);
        store.setFloorRequest(elevatorEvent);
        scheduler.setReadFloorRequest();
        Assertions.assertEquals(scheduler.getFloorRequestToBeProcessed(), elevatorEvent);
    }

    /**
     * Testing processing floor requests
     */
    @Test
    void testProcessFloorRequest() {
        scheduler.setProcessFloorRequest();
        Assertions.assertEquals(scheduler.getProcessedRequest(), scheduler.getFloorRequestToBeProcessed());
    }

}
