package Main;

/**
 * Concrete class implementation of the ElevatorState interface representing the Idle state
 */
class Idle implements ElevatorState {
    @Override
    public void entry(Elevator context) {
        context.sendIdleStatusUpdate();
        System.out.println("Elevator " + context.getElevatorId() + " is Idle");
        context.waitMessageScheduler();
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Floor Requested...Elevator is moving.");
        if (context.getCurrentFloor() == context.getDestinationFloor()) {
            context.setCurrentState("LoadingUnloading");
        } else {
            context.setCurrentState("Moving");
        }
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void doorsClosed(Elevator context) {
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }

    @Override
    public String toString() {
        return "Idle";
    }
}
