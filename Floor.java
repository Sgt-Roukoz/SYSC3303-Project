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

    public void processInput(String input) {
        String[] split = input.split(" ");
        if(isTopFloor && split[2].toUpperCase() == "UP") {
            System.out.println("This is the top floor, there is no up button.");
            return;
        }
        else if (isBottomFloor && split[2].toUpperCase() == "DOWN") {
            System.out.println("This is the bottom floor, there is no down button.");
            return;
        }
        if(split[2].toUpperCase() == "UP") upLampOn = true;
        else downLampOn = true;
        ElevatorEvent event = new ElevatorEvent(split[0], Integer.valueOf(split[1]), ELEVATOR_BUTTON.valueOf(split[2].toUpperCase()), Integer.valueOf(split[3]));
    }
}
