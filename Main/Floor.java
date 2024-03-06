package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;
import java.util.Scanner;

/**
 * Floor class
 * Simulates all floors of the building, including lamps and buttons
 *
 * @author Eric Wang
 * @version 2024-03-05
 */
public class Floor extends Thread{
    private final EventQueue eventQueue;
    private final int MAX_FLOORS = 22;
    private final Random rand;
    private final DatagramSocket socket;
    private final DatagramPacket packet;

    public Floor(String name, EventQueue eventQueue) {
        super(name);
        this.eventQueue = eventQueue;
        this.rand = new Random();
        this.packet = new DatagramPacket(new byte[100], 100);
        try {
            this.socket = new DatagramSocket();
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
                Thread.sleep(rand.nextInt(500,2000));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes given string input, sending the appropriate event to the scheduler
     * @param input the given string input to be processed
     */
    public void processInput(String input) {
        String[] split = input.split(" ");
        if(split[2].equalsIgnoreCase("UP")) System.out.println("Floor " + split[3] + " up lamp on");
        else System.out.println("Floor " + split[3] + " down lamp on");
        byte[] msg = new byte[100];
        //TODO: send UDP
        /*ElevatorEvent event = new ElevatorEvent(split[0], Integer.valueOf(split[1]), ELEVATOR_BUTTON.valueOf(split[2].toUpperCase()), Integer.valueOf(split[3]));
        System.out.println("Floor sending event: " + event );
        eventQueue.setFloorRequest(event);*/
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
}
