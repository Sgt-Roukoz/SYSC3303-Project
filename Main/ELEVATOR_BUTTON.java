package Main;

/**
 * Enum representing different types of buttons and actions within the elevator system.
 * This includes directions, door operations, and an indication for an internal button press or status update.
 */
public enum ELEVATOR_BUTTON {
    UP,         // Indicates a request to move up
    DOWN,       // Indicates a request to move down
    INSIDE,     // Indicates a button press inside the elevator
    START_MOVE, // Indicates the elevator is starting to move
    ARRIVAL     // Indicates the elevator has arrived at a floor
}


