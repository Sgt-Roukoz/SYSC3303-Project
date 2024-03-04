package Testing;

import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventQueueTest {
    EventQueueTest() {
    }

    @Test
    void testSetElevatorRequest() {
        EventQueue testingQueue = new EventQueue();
        ElevatorEvent elevatorRequestEvent = new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4);
        testingQueue.setElevatorRequest(elevatorRequestEvent);
        Assertions.assertEquals(elevatorRequestEvent, testingQueue.getElevatorRequest());
    }

    @Test
    void testSetFloorRequest() {
        EventQueue eventQueue = new EventQueue();
        ElevatorEvent floorRequestEvent = new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4);
        eventQueue.setFloorRequest(floorRequestEvent);
        Assertions.assertEquals(floorRequestEvent, eventQueue.getFloorRequest());
    }

    @Test
    void testElevatorArrived() {
        EventQueue eventQueue = new EventQueue();
        Assertions.assertEquals(0, eventQueue.processedEvents);
        eventQueue.elevatorArrived();
        Assertions.assertEquals(1, eventQueue.processedEvents);
        eventQueue.elevatorArrived();
        eventQueue.elevatorArrived();
        eventQueue.elevatorArrived();
        eventQueue.elevatorArrived();
        Assertions.assertEquals(5, eventQueue.processedEvents);
    }
}
