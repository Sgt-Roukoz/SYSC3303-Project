/**
 * Testing class for the Main.Scheduler subsystem
 */

package Testing;

import Main.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerTest {
    SchedulerTest() {
    }

    Scheduler scheduler;
    EventQueue t1;

    /**
     * Called before each test
     */
    /*@BeforeEach
    void setUp()
    {
        t1 = new EventQueue();
        SchedulerStore store = new SchedulerStore();
        scheduler = new Scheduler(t1, store);

    }*/

    /**
     * Test reading floor requests
     */
    /*@Test
    void testReadFloorRequest() {
        scheduler.setReadFloorRequest();
        Assertions.assertEquals(scheduler.getFloorRequestToBeProcessed(), t1.getFloorRequest());
    }*/

    /**
     * Testing processing floor requests
     */
    @Test
    void testProcessFloorRequest() {
        scheduler.setProcessFloorRequest();
        Assertions.assertEquals(scheduler.getProcessedRequest(), scheduler.getFloorRequestToBeProcessed());
    }

    /**
     * Testing sending an elevator request
     */
    /*@Test
    void testSendElevatorRequest() {
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        scheduler.setReadFloorRequest();
        scheduler.setProcessFloorRequest();
        scheduler.setSendElevatorRequest();
        Assertions.assertEquals(t1.getElevatorRequest(), scheduler.getProcessedRequest());
    }*/

    /**
     * Testing scheduler state machine transitions
     */
    /*@Test
    void testStateChange()
    {
        Thread testThread = new Thread(scheduler);
        testThread.start();
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        Assertions.assertEquals(Scheduler.SchedulerState.IDLE, scheduler.getState());
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(Scheduler.SchedulerState.PROCESSING_COMMAND, scheduler.getState());
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(Scheduler.SchedulerState.WAITING, scheduler.getState());
        testThread.interrupt();
    }*/
}
