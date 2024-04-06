package Main;

//Loading/Unloading state
class LoadingUnloading implements ElevatorState {
    @Override
    public void entry(Elevator context) {
        context.doorsClosed();
    }

    @Override
    public void floorRequest(Elevator context) {
        System.out.println("Elevator is loading/unloading.");
    }

    @Override
    public void arrivedAtFloor(Elevator context) {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public void doorsClosed(Elevator context) {
        try {
            context.openDoors();
            if (context.transientFault)
            {
                Thread.sleep(Elevator.DOOR_OPERATION_TIME/2);
                System.out.println("ERROR-1: Elevator " + context.getElevatorId() + " doors failed to close, trying again!");
                context.transientFault = false;
            }
            context.closeDoors();
            context.setCurrentState("Idle");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Elevator " + context.getElevatorId() + " was interrupted.");
        }
    }

    @Override
    public void displayState() {
        System.out.println("Elevator is loading/unloading passengers.");
    }

    @Override
    public String toString() {
        return "LoadingUnloading";
    }
}