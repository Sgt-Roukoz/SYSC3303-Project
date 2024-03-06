package Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Floor class
 * Simulates a single floor of the building, including lamps and buttons
 *
 * @author Eric Wang
 * @version 2024-01-31
 */
public class Floor extends Thread{
    boolean upLampOn;
    boolean downLampOn;
    boolean elevatorArrived;
    private final EventQueue eventQueue;


    public Floor(String name, EventQueue eventQueue) {
        super(name);
        this.eventQueue = eventQueue;
        this.upLampOn = false;
        this.downLampOn = false;
        this.elevatorArrived = false;

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
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes given string input, sending the appropriate event to the scheduler
     * @param input the given string input to be processed
     */
    public void processInput(String input) {
        String[] split = input.split(" ");
        /*if(isTopFloor && split[2].equalsIgnoreCase("UP")) {
            System.out.println("This is the top floor, there is no up button.");
            return;
        }
        else if (isBottomFloor && split[2].equalsIgnoreCase("DOWN")) {
            System.out.println("This is the bottom floor, there is no down button.");
            return;
        }*/
        if(split[2].equalsIgnoreCase("UP")) upLampOn = true;
        else downLampOn = true;
        ElevatorEvent event = new ElevatorEvent(split[0], Integer.valueOf(split[1]), ELEVATOR_BUTTON.valueOf(split[2].toUpperCase()), Integer.valueOf(split[3]));
        System.out.println("Floor adding event: " + event );
        eventQueue.setFloorRequest(event);
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
