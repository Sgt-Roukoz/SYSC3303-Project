public class Floor extends Thread{
    boolean upLampOn;
    boolean downLampOn;
    boolean elevatorArrived;
    private final int floorNumber;

    public Floor(String name, int floorNumber) {
        super(name);
        this.floorNumber = floorNumber;
        this.upLampOn = false;
        this.downLampOn = false;
        this.elevatorArrived = false;
    }

    public void upButtonPressed() {
        upLampOn = true;
        ElevatorEvent event = new ElevatorEvent(java.time.LocalTime.now().toString(), floorNumber,ELEVATOR_BUTTON.UP,1);
    }

    public void DownButtonPressed() {
        downLampOn = true;
        ElevatorEvent event = new ElevatorEvent(java.time.LocalTime.now().toString(), floorNumber,ELEVATOR_BUTTON.DOWN,1);
    }
}
