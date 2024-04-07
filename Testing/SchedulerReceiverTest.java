/**
 * Testing class for the Main.Scheduler subsystem
 */

package Testing;

import Main.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.*;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

class SchedulerReceiverTest {
    static SchedulerReceiver receiver;
    static SchedulerStoreInt store;

    SchedulerReceiverTest(){
    }

    @BeforeAll
    static void registrySetup() throws RemoteException, MalformedURLException, NotBoundException {
        try {
            store = (SchedulerStoreInt) Naming.lookup("rmi://localhost/store");
        } catch (NotBoundException | ConnectException e) {
            store = new SchedulerStore();
        }

        receiver = new SchedulerReceiver(store);
    }

    /**
     * Called before each test
     */
    @BeforeEach
    void setUp() throws RemoteException, MalformedURLException {
        // Bind the remote object's stub in the registry
        Naming.rebind("store", store);
    }

    /**
     * Tests storing floor event message
     * @throws IOException
     */
    @Test
    void testSendingFloorMessage() throws IOException {
        DatagramPacket receivePacket = getReceivePacket("0105,UP,09,0,00");

        //testing acknowledgement
        String receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());
        Assertions.assertEquals("ACK0105,UP,09,0,00", receiveString);

        //testing floor event exists
        ElevatorEvent testEvent = new ElevatorEvent("00:00", 5,ELEVATOR_BUTTON.UP,9, 0, false);
        Assertions.assertEquals(testEvent.toString(), store.getFloorRequest().toString());
    }

    /**
     * Tests storing elevator
     * @throws IOException
     */
    @Test
    void testSendingElevatorExistMessage() throws IOException {
        DatagramPacket receivePacket = getReceivePacket("0230");

        //testing acknowledgement
        String receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());
        Assertions.assertEquals("ACK0230", receiveString);

        //testing elevator got added
        Assertions.assertTrue(store.getElevators().containsKey(3));
    }

    /**
     * Tests storing elevator
     * @throws IOException
     */
    @Test
    void testSendingElevatorStatusMessage() throws IOException {
        DatagramPacket receivePacket = getReceivePacket("0230");

        //testing acknowledgement
        String receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());
        Assertions.assertEquals("ACK0230", receiveString);

        //testing elevator got added
        Assertions.assertTrue(store.getElevators().containsKey(3));
    }

    private DatagramPacket getReceivePacket(String message) throws IOException {
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        byte[] testMessage = message.getBytes();
        byte[] receive = new byte[100];
        DatagramPacket testPacket = new DatagramPacket(testMessage, testMessage.length, InetAddress.getLocalHost(), 5000);
        DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
        DatagramSocket socket = new DatagramSocket();
        socket.send(testPacket);
        socket.receive(receivePacket);
        return receivePacket;
    }
}