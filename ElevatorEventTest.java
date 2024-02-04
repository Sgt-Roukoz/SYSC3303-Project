import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElevatorEventTest {
    ElevatorEventTest() {
    }

    @Test
    void elevatorEventInitializationTest() {
        ElevatorEvent elevatorEvent = new ElevatorEvent("14:05:15.0", 3, ELEVATOR_BUTTON.UP, 5);
        Assertions.assertEquals("14:05:15.0", elevatorEvent.getTime());
        Assertions.assertEquals(3, elevatorEvent.getFloor());
        Assertions.assertEquals(ELEVATOR_BUTTON.UP, elevatorEvent.getButton());
        Assertions.assertEquals(5, elevatorEvent.getCar_button());
    }
}
