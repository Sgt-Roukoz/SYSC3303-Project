/**
 * Testing class for the Elevator subsystem
 */

package Testing;
import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

class ElevatorTest {

    Elevator testingElevator;

    ElevatorTest() {
    }

    /**
     * Called before each test
     */
    @BeforeEach
    void setUp()
    {
        testingElevator = new Elevator(1);
    }

    /**
     * Testing door state after opening doors
     */
    @Test
    void testOpenDoor() {
        testingElevator.setOpenDoors();
        Assertions.assertTrue(testingElevator.getDoorBoolean());
    }

    /**
     * Testing door state after closing doors
     */
    @Test
    void testCloseDoor() {
        testingElevator.setCloseDoors();
        Assertions.assertFalse(testingElevator.getDoorBoolean());
    }

    /**
     * Testing Elevator State Machine transitions
     */
    @Test
    void testStateChanges() throws IOException, InterruptedException {
        SchedulerStoreInt store;
        try {
            store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");
        } catch (NotBoundException | ConnectException e) {
            store = new SchedulerStore();
        }

        Thread receiver = new Thread(new SchedulerReceiver(store));
        receiver.start();
        Thread elevThread = new Thread(testingElevator);
        elevThread.start();

        Thread.sleep(25);
        Assertions.assertEquals("Idle", testingElevator.getCurrentState());

        byte[] testMessage = "03UP,020".getBytes();
        InetAddress address = (InetAddress) store.getElevators().get(1).getFirst();
        int port = (int) store.getElevators().get(1).get(1);
        DatagramPacket testPacket = new DatagramPacket(testMessage, testMessage.length, address, port);
        DatagramSocket socket = new DatagramSocket();
        socket.send(testPacket);

        Thread.sleep(25);
        System.out.println("Test Moving State");
        Assertions.assertEquals("Moving", testingElevator.getCurrentState());

        Thread.sleep(3050); // wait for elevator to move to floor
        System.out.println("Test Loading State");
        Assertions.assertEquals("LoadingUnloading", testingElevator.getCurrentState());
        elevThread.interrupt();

    }

}
