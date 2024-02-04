import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevatorTest {

    @Test
    void testOpenDoor()throws InterruptedException{
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setOpenDoors();
        assertTrue(testingElevator.getDoorBoolean());
    }

    @Test
    void testCloseDoor()throws InterruptedException{
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setCloseDoors();
        assertFalse(testingElevator.getDoorBoolean());
    }

    @Test
    void TestNotifySchedulerOfArrival(){
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setNotifySchedulerOfArrival();
        assertEquals(1, eventQueue.processedEvents);
    }
}
