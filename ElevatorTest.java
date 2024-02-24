/**
 * Testing class for the Elevator subsystem
 */
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ElevatorTest {

    EventQueue eventQueue;
    Elevator testingElevator;

    ElevatorTest() {
    }

    /**
     * Called before each test
     */
    @BeforeEach
    void setUp()
    {
        eventQueue = new EventQueue();
        testingElevator = new Elevator(eventQueue, 1);
    }

    /**
     * Testing door state after opening doors
     */
    @Test
    void testOpenDoor() {
        testingElevator.setOpenDoors();
        Assertions.assertTrue(testingElevator.getDoorBoolean());
    }

    /**
     * Testing door state after closing doors
     */
    @Test
    void testCloseDoor() {
        testingElevator.setCloseDoors();
        Assertions.assertFalse(testingElevator.getDoorBoolean());
    }

    /**
     * Testing elevator notifying scheduler of arrival
     */
    @Test
    void TestNotifySchedulerOfArrival() {
        testingElevator.setNotifySchedulerOfArrival();
        Assertions.assertEquals(1, eventQueue.processedEvents);
    }

    /**
     * Testing Elevator State Machine transitions
     */
    @Test
    void testStateChanges()
    {
        System.out.println("Test Idle State");
        Assertions.assertEquals("Idle", testingElevator.getCurrentState());

        testingElevator.floorRequested();
        System.out.println("Test MovingToFloor State");
        Assertions.assertEquals("MovingToFloor", testingElevator.getCurrentState());

        testingElevator.arrivedAtFloor();
        System.out.println("Test Loading State");
        Assertions.assertEquals("Loading", testingElevator.getCurrentState());

        testingElevator.destinationRequest();
        System.out.println("Test MovingToDestination State");
        Assertions.assertEquals("MovingToDestination", testingElevator.getCurrentState());

        testingElevator.arrivedAtDestination();
        System.out.println("Test Unloading State");
        Assertions.assertEquals("Unloading", testingElevator.getCurrentState());
    }

}
