//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

public class ElevatorEvent {
    private final String time;
    private final int floor;
    private final ELEVATOR_BUTTON button;
    private final int car_button;

    public ElevatorEvent(String time, int floor, ELEVATOR_BUTTON button, int car_button) {
        this.time = time;
        this.floor = floor;
        this.button = button;
        this.car_button = car_button;
    }

    public String toString() {
        String var10000 = this.time;
        return var10000 + " " + this.floor + " " + String.valueOf(this.button) + " " + this.car_button;
    }

    public String getTime() {
        return this.time;
    }

    public int getFloor() {
        return this.floor;
    }

    public ELEVATOR_BUTTON getButton() {
        return this.button;
    }

    public int getCar_button() {
        return this.car_button;
    }
}
