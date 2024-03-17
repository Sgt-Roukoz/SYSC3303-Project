package Main;

public interface ElevatorState {
    /**
     * The entry action, called when state is entered
     *
     * @param context The context of the elevator state machine
     */
    void entry(Elevator context);

    /**
     * Handles the event when a floor request arrives
     *
     * @param context The context of the elevator state machine
     */
    void floorRequest(Elevator context);

    /**
     * Handles the event when elevator arrives at source floor
     *
     * @param context The context of the elevator state machine
     */
    void arrivedAtFloor(Elevator context);

    /**
     * Handles the event when elevator doors are requested to be closed
     *
     * @param context The context of the elevator state machine
     */
    void doorsClosed(Elevator context);

    /**
     * Displays current state information
     */
    void displayState();

    String toString();
}
