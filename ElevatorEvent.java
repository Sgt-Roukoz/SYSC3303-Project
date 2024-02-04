/**
 * ElevatorEvent class
 * encapsulates information required for an elevator subsystem to know its next course of action
 *
 * @author Marwan Zeid
 * @version 2024-01-30
 */

public class ElevatorEvent { //convert to Record?
    private final String time;
    private final int floor;
    private final ELEVATOR_BUTTON button;
    private final int car_button;

    //later add value for specific elevator

    public ElevatorEvent(String time, int floor, ELEVATOR_BUTTON button, int car_button)
    {
        this.time = time;
        this.floor = floor;
        this.button = button;
        this.car_button = car_button;
    }

    public String toString(){
        return time + " " + floor + " " + button + " " + car_button;
    }

    // Getters for each of the values
    public String getTime()
    {
        return time;
    }

    public int getFloor() {
        return floor;
    }

    public ELEVATOR_BUTTON getButton() {
        return button;
    }

    public int getCar_button() {
        return car_button;
    }
}
