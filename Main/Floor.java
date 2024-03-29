package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Floor class
 * Simulates all floors of the building, including lamps and buttons
 *
 * @author Eric Wang
 * @author Marwan Zeid
 * @version 2024-03-15
 */
public class Floor implements Runnable{
    private final int MAX_FLOORS = 22;
    private final Random rand;
    private final DatagramSocket sendReceiveSocket;
    private DatagramPacket sendPacket;

    public Floor() {
        this.rand = new Random();
        this.sendPacket = new DatagramPacket(new byte[100], 100);
        try {
            this.sendReceiveSocket = new DatagramSocket();
            sendReceiveSocket.setSoTimeout(2000);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads from an input file in specified format and processes each line as a command
     * @param file the input file
     */
    public void processFile(File file) {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                this.processInput(line);
                Thread.sleep(rand.nextInt(3000,6000));
            }
            scanner.close();
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes given string input, sending the appropriate event to the scheduler
     * @param input the given string input to be processed
     */
    public void processInput(String input) {
        String[] split = input.split(" ");
        String msg = "01";
        msg += split[1];
        if(split[2].equalsIgnoreCase("UP")) {
            System.out.println("Floor " + split[3] + " up lamp on");
            msg += ",UP,";
        }
        else {
            System.out.println("Floor " + split[3] + " down lamp on");
            msg += ",DN,";
        }
        msg += split[3] + ",";

        if(split[4] == "0"){
            msg += "0";
        } else if (split[4] == "1") {
            msg += "1";
        } else{
            msg += "2";
        }


        byte[] byteMsg = HelperFunctions.generateMsg(msg);

        DatagramPacket receivePacket;
        try {
            sendPacket = new DatagramPacket(byteMsg, byteMsg.length, InetAddress.getLocalHost(), 5000);
            HelperFunctions.printDataInfo(sendPacket.getData(), sendPacket.getLength());

            // Initialize receivePacket before using it
            byte[] receiveData = new byte[100];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // Perform sending and receiving with timeout handling
        int attempt = 0;
        boolean receivedResponse = false;

        while (attempt < 3 && !receivedResponse) { // Retry up to 3 times
            System.out.println(Thread.currentThread().getName() + ": Attempt " + (attempt + 1));
            try {
                sendReceiveSocket.send(sendPacket);
                // Attempt to receive the acknowledgment
                sendReceiveSocket.receive(receivePacket);
                // Handle the acknowledgment
                String received = HelperFunctions.translateMsg(receivePacket.getData(), receivePacket.getLength());
                if (received.equals("ACK"+msg)) receivedResponse = true;
            } catch (SocketTimeoutException ste) {
                // Handle timeout exception
                System.out.println(Thread.currentThread().getName() + ": Timeout. Resending packet.");
                attempt++;
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        if (!receivedResponse) {
            System.out.println(Thread.currentThread().getName() + ": No response after multiple attempts. Exiting.");
            System.exit(1);
        }
    }


    /**
     * Runs this thread
     */
    @Override
    public void run() {
        System.out.println("Started Floor");
        processFile(new File("test.txt"));
        System.out.println("Floor done");
    }

    public static void main(String[] args)
    {
        Floor floor = new Floor();
        Thread floorThread = new Thread(floor);
        floorThread.start();
    }
}
