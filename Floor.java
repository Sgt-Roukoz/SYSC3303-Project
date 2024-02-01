public class Floor extends Thread{
    boolean upLampOn;
    boolean downLampOn;
    boolean elevatorArrived;
    private final int floorNumber;
    private final boolean isTopFloor;
    private final boolean isBottomFloor;

    public Floor(String name, int floorNumber, boolean isTopFloor, boolean isBottomFloor) {
        super(name);
        this.floorNumber = floorNumber;
        this.upLampOn = false;
        this.downLampOn = false;
        this.elevatorArrived = false;
        this.isBottomFloor = isBottomFloor;
        this.isTopFloor = isTopFloor;
    }

    public void upButtonPressed(int destFloor) {
        if(isTopFloor) {
            System.out.println("This is the top floor, there is no up button.");
            return;
        }
        upLampOn = true;
        ElevatorEvent event = new ElevatorEvent(java.time.LocalTime.now().toString(), floorNumber,ELEVATOR_BUTTON.UP,destFloor);
    }

    public void DownButtonPressed(int destFloor) {
        if(isBottomFloor) {
            System.out.println("This is the bottom floor, there is no down button.");
            return;
        }
        downLampOn = true;
        ElevatorEvent event = new ElevatorEvent(java.time.LocalTime.now().toString(), floorNumber,ELEVATOR_BUTTON.DOWN,destFloor);
    }
}
