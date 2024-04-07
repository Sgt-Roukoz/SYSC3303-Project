package Main;

import java.io.IOException;

/**
 * Concrete class implementation of the ElevatorState interface representing the Moving state.
 */
class Moving implements ElevatorState {
    @Override
    public void entry(Elevator context) {
        context.sendMovingStatusUpdate();

        try {
            context.moveToFloor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is moving.");
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        context.setCurrentState("LoadingUnloading");
    }

    @Override
    public void doorsClosed(Elevator context) {
        System.out.println("Door is already closed, elevator is moving");
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is moving.");
    }

    @Override
    public String toString() {
        return "Moving";
    }
}