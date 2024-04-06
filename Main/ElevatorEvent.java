package Main;

import java.io.Serializable;

/**
 * ElevatorEvent class
 * encapsulates information required for an elevator subsystem to know its next course of action
 *
 * @author Marwan Zeid
 * @version 2024-01-30
 */

public class ElevatorEvent implements Serializable { //convert to Record?
    private final String time;
    private final int sourceFloor;
    private final ELEVATOR_BUTTON button;
    private final int destFloor;
    private final int faultType;

    //later add value for specific elevator

    public ElevatorEvent(String time, int floor, ELEVATOR_BUTTON button, int car_button, int fault)
    {
        this.time = time;
        this.sourceFloor = floor;
        this.button = button;
        this.destFloor = car_button;
        this.faultType = fault;
    }

    public String toString(){
        return time + " " + sourceFloor + " " + button + " " + destFloor;
    }

    // Getters for each of the values
    public String getTime()
    {
        return time;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public ELEVATOR_BUTTON getButton() {
        return button;
    }

    public int getDestFloor() {
        return destFloor;
    }

    public int getFaultType() {
        return faultType;
    }
}