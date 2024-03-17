package Main;

import java.io.IOException;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class SchedulerReceiver implements Runnable {
    private SchedulerStoreInt store;
    private final int port = 5000;

    public SchedulerReceiver(SchedulerStoreInt store) {
        this.store = store;
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)){
            byte[] receiveData = new byte[1024];

            System.out.println("Scheduler Receiver running on port " + port);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());

                // Extract the message for acknowledgment
                String messageForAck = translatedMessage.trim(); // Trim to remove any trailing zeros


                // Handle message based on type
                if (translatedMessage.startsWith("01")) { // FloorEvent message
                    // Extract message details, assuming a format like "01srcFloor,Direction,destFloor".
                    String[] messageParts = translatedMessage.substring(2, translatedMessage.length() - 1).split(",");
                    if(messageParts.length >= 3) {
                        int sourceFloor = Integer.parseInt(messageParts[0].trim());
                        String directionStr = messageParts[1].trim(); // "UP" or "DN"
                        int destinationFloor = Integer.parseInt(messageParts[2].trim());

                        ELEVATOR_BUTTON direction = "UP".equals(directionStr) ? ELEVATOR_BUTTON.UP : ELEVATOR_BUTTON.DOWN;

                        // Create the event; assuming current time is passed as a placeholder for the time parameter
                        String currentTime = "00:00"; // Placeholder, adjust as needed
                        ElevatorEvent event = new ElevatorEvent(currentTime, sourceFloor, direction, destinationFloor);

                        // Store the event
                        store.setFloorRequest(event);
                    }
                } if (translatedMessage.startsWith("02")) { // IEXIST message
                    int elevatorID = Integer.parseInt(translatedMessage.substring(2, translatedMessage.length() - 1));


                    // Store the elevator's information in SchedulerStore
                    store.addElevator(elevatorID, receivePacket.getAddress(), receivePacket.getPort());
                } else if (translatedMessage.startsWith("04")) { // Elevator Status Update message
                    // Correctly parsing the message by excluding the trailing 0 byte
                    String[] parts = translatedMessage.substring(2, translatedMessage.length() - 1).split(",");
                    if(parts.length >= 5) { // elevatorID, status, direction (for moving), current floor, destination floor
                        int elevatorID = Integer.parseInt(parts[0]);
                        String statusType = parts[1];

                        if("Idle".equals(statusType)){
                            int currentFloor = Integer.parseInt(parts[2]);
                            store.updateElevator(elevatorID, 2, currentFloor);
                            store.updateElevator(elevatorID, 3, 0); // Assuming '0' signifies idle state in your system
                        } else if ("Moving".equals(statusType)) {
                            String direction = parts[2];
                            int currentFloor = Integer.parseInt(parts[3]);
                            int destinationFloor = Integer.parseInt(parts[4]);
                            store.updateElevator(elevatorID, 2, currentFloor);
                            store.updateElevator(elevatorID, 3, "UP".equals(direction) ? 1 : 2); // Assuming '1' for up and '2' for down
                            store.updateElevator(elevatorID, 4, destinationFloor);
                        }
                    }
                }

                // Send acknowledgment.
                String ackMessage = "ACK" + messageForAck;
                byte[] ackData = ackMessage.getBytes();
                InetAddress returnAddress = receivePacket.getAddress();
                int returnPort = receivePacket.getPort();
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, returnAddress, returnPort);
                serverSocket.send(ackPacket);


                // Clear the buffer after handling the message
                receiveData = new byte[1024];
            }
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args)
    {
        try {
            SchedulerStoreInt store = new SchedulerStore();
            // Create and export the RMI registry
            LocateRegistry.createRegistry(1099);

            // Bind the remote object's stub in the registry
            Naming.rebind("store", store);

            SchedulerReceiver receiver = new SchedulerReceiver(store);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
        } catch (RemoteException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
