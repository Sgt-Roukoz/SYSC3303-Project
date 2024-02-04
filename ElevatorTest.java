import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ElevatorTest {
    ElevatorTest() {
    }

    @Test
    void testOpenDoor() throws InterruptedException {
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setOpenDoors();
        Assertions.assertTrue(testingElevator.getDoorBoolean());
    }

    @Test
    void testCloseDoor() throws InterruptedException {
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setCloseDoors();
        Assertions.assertFalse(testingElevator.getDoorBoolean());
    }

    @Test
    void TestNotifySchedulerOfArrival() {
        EventQueue eventQueue = new EventQueue();
        Elevator testingElevator = new Elevator(eventQueue, 1);
        testingElevator.setNotifySchedulerOfArrival();
        Assertions.assertEquals(1, eventQueue.processedEvents);
    }

}
