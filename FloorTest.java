import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class FloorTest {
    @Test
    void processInput(){
        EventQueue eventQueue = new EventQueue();
        ElevatorEvent elevatorTest = new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4);
        Floor testingFloor = new Floor("Floor 1", eventQueue);

        testingFloor.processInput("14:05:15.0 2 UP 4");
        assertEquals(eventQueue.getFloorRequest().toString(), elevatorTest.toString());


    }

}