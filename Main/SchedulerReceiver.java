package Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SchedulerReceiver implements Runnable {
    private SchedulerStore store;
    private final int port = 5000;

    public SchedulerReceiver(SchedulerStore store) {
        this.store = store;
    }

    @Override
    public void run() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            byte[] receiveData = new byte[1024];

            System.out.println("Scheduler Receiver running on port " + port);

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String translatedMessage = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());

                // Extract the message for acknowledgment
                String messageForAck = translatedMessage.trim(); // Trim to remove any trailing zeros


                // Handle message based on type
                if (translatedMessage.startsWith("01")) { //FloorEvent message
                    // Assuming the message is something like "0102UP0300" for FloorEvent
                    int sourceFloor = Integer.parseInt(translatedMessage.substring(2, 4));
                    String directionStr = translatedMessage.substring(4, 6); // "UP" or "DN"
                    int destinationFloor = Integer.parseInt(translatedMessage.substring(6, 8));


                    ELEVATOR_BUTTON direction = directionStr.equals("UP") ? ELEVATOR_BUTTON.UP : ELEVATOR_BUTTON.DOWN;

                    // Create the event; assuming current time is passed as a placeholder for the time parameter
                    String currentTime = "00:00"; // Placeholder, adjust as needed
                    ElevatorEvent event = new ElevatorEvent(currentTime, sourceFloor, direction, destinationFloor);

                    // Store the event
                    store.setFloorRequest(event);
                } if (translatedMessage.startsWith("02")) { // IEXIST message
                    int elevatorID = Integer.parseInt(translatedMessage.substring(2, 6));

                    // Store the elevator's information in SchedulerStore
                    store.addElevator(elevatorID, receivePacket.getAddress(), receivePacket.getPort());
                } else if (translatedMessage.startsWith("04")) { // Elevator Status Update message
                    String[] parts = translatedMessage.substring(2).split(",");
                    int elevatorID = Integer.parseInt(parts[0]);
                    String statustype = parts[1];
                    if("Idle".equals(statustype)){
                        int CurrentFloor = Integer.parseInt(parts[2]);
                        store.updateElevator(elevatorID, 2, CurrentFloor );
                        store.updateElevator(elevatorID, 3, 0);
                    } else if ("Moving".equals(statustype)) {

                        String direction = parts[2];
                        int CurrentFloor = Integer.parseInt(parts[3]);
                        int DestinationFloor = Integer.parseInt(parts[4]);
                        store.updateElevator(elevatorID, 2, CurrentFloor);
                        store.updateElevator(elevatorID, 3 , "UP" .equals(direction) ? 1 : 2);
                        store.updateElevator(elevatorID, 4, DestinationFloor);
                    }


                }
                // Send acknowledgment
                String ackMessage = "ACK" + messageForAck;
                byte[] ackData = ackMessage.getBytes();
                InetAddress returnAddress = receivePacket.getAddress();
                int returnPort = receivePacket.getPort();
                DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, returnAddress, returnPort);
                serverSocket.send(ackPacket);


                // Clear the buffer after handling the message
                receiveData = new byte[1024];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
