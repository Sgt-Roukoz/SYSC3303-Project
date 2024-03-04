/**
 * Testing class for the Main.Floor subsystem
 */
package Testing;

import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
class FloorTest {

    /**
     * Testing floor subsystem processing of string input
     */
    @Test
    void processInput(){
        EventQueue eventQueue = new EventQueue();
        ElevatorEvent elevatorTest = new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4);
        Floor testingFloor = new Floor("Main.Floor 1", eventQueue);

        testingFloor.processInput("14:05:15.0 2 UP 4");
        Assertions.assertEquals(eventQueue.getFloorRequest().toString(), elevatorTest.toString());


    }

}