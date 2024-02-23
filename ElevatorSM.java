import java.util.HashMap;
import java.util.Map;

interface ElevatorState{
    void floorRequest(ElevatorSM context);

    void arrivedAtFloor(ElevatorSM context);

    void destinationRequest(ElevatorSM context);

    void arrivedAtDestination(ElevatorSM context);

    void doorsClosed(ElevatorSM context);

    void displayState();

}

class Idle implements ElevatorState{
    @Override
    public void floorRequest(ElevatorSM context) {
        System.out.println("Floor requested.");
        context.setCurrentState("MovingToFloor", false);
    }

    @Override
    public void arrivedAtFloor(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void destinationRequest(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void arrivedAtDestination(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void doorsClosed(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }
}

class MovingToFloor implements ElevatorState{
    @Override
    public void floorRequest(ElevatorSM context) {
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void arrivedAtFloor(ElevatorSM context){
        System.out.println("Elevator arrived at loading floor.");
        context.setCurrentState("Loading", true);
    }

    @Override
    public void destinationRequest(ElevatorSM context){
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void arrivedAtDestination(ElevatorSM context){
        System.out.println("Elevator is still moving.");
    }

    @Override
    public void doorsClosed(ElevatorSM context){
        System.out.println("Elevator is still moving.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is moving.");
    }
}

class MovingToDestination implements ElevatorState{
    @Override
    public void floorRequest(ElevatorSM context) {
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void arrivedAtFloor(ElevatorSM context){
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void destinationRequest(ElevatorSM context){
        System.out.println("Elevator is still moving to destination.");
    }

    @Override
    public void arrivedAtDestination(ElevatorSM context){
        System.out.println("Elevator arrived at destination floor.");
        context.setCurrentState("Unloading", true);
    }

    @Override
    public void doorsClosed(ElevatorSM context){
        System.out.println("Elevator is still moving to destination.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is moving to destination.");
    }
}

class Loading implements ElevatorState{
    @Override
    public void floorRequest(ElevatorSM context) {
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void arrivedAtFloor(ElevatorSM context){
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void destinationRequest(ElevatorSM context){
        System.out.println("Destination floor requested.");
        context.setCurrentState("MovingToDestination", false);
    }

    @Override
    public void arrivedAtDestination(ElevatorSM context){
        System.out.println("Elevator is still loading passengers.");
    }

    @Override
    public void doorsClosed(ElevatorSM context){
        System.out.println("Elevator is still loading passengers.");
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is loading passengers.");
    }
}

class Unloading implements ElevatorState{
    @Override
    public void floorRequest(ElevatorSM context) {
        System.out.println("Elevator is still unload requested.");
    }

    @Override
    public void arrivedAtFloor(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void destinationRequest(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void arrivedAtDestination(ElevatorSM context){
        System.out.println("Elevator is still idling.");
    }

    @Override
    public void doorsClosed(ElevatorSM context){
        System.out.println("Elevator doors now closed.");
        context.setCurrentState("Idle", false);
    }
    @Override
    public void displayState() {
        System.out.println("Elevator is idle.");
    }
}

public class ElevatorSM {
    private Map<String, ElevatorState> states;
    private ElevatorState currentState;

    private Boolean doorsOpen;

    public ElevatorSM() {
        states = new HashMap<>();
        states.put("Idle", new Idle());
        states.put("MovingToFloor", new MovingToFloor());
        states.put("Loading", new Loading());
        states.put("MovingToDestination", new MovingToDestination());
        states.put("Unloading", new Unloading());

        currentState = states.get("Idle");
        doorsOpen = false;
    }

    public void floorRequested() {
        currentState.floorRequest(this);
        currentState.displayState();
    }

    public void arrivedAtFloor(){
        currentState.arrivedAtFloor(this);
        currentState.displayState();
    }

    public void destinationRequest(){
        currentState.destinationRequest(this);
        currentState.displayState();
    }

    public void arrivedAtDestination(){
        currentState.arrivedAtDestination(this);
        currentState.displayState();
    }

    public void doorsClosed(){
        currentState.doorsClosed(this);
        currentState.displayState();
    }

    public void setCurrentState(String nextState, boolean doors){
        this.currentState = states.get(nextState);
    }

    public static void main(String[] args) {
        ElevatorSM elevatorSM = new ElevatorSM();

        elevatorSM.floorRequested();

        elevatorSM.arrivedAtFloor();

        elevatorSM.destinationRequest();

        elevatorSM.arrivedAtDestination();

        elevatorSM.doorsClosed();

    }

}
