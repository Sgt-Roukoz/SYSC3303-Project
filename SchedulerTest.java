import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchedulerTest {
    SchedulerTest() {
    }

    Scheduler scheduler;
    EventQueue t1;

    @BeforeEach
    void setUp()
    {
        t1 = new EventQueue();
        scheduler = new Scheduler(t1);

    }

    @Test
    void testReadFloorRequest() {
        scheduler.setReadFloorRequest();
        Assertions.assertEquals(scheduler.getFloorRequestToBeProcessed(), t1.getFloorRequest());
    }

    @Test
    void testProcessFloorRequest() {
        scheduler.setProcessFloorRequest();
        Assertions.assertEquals(scheduler.getProcessedRequest(), scheduler.getFloorRequestToBeProcessed());
    }

    @Test
    void testSendElevatorRequest() {
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        scheduler.setReadFloorRequest();
        scheduler.setProcessFloorRequest();
        scheduler.setSendElevatorRequest();
        Assertions.assertEquals(t1.getElevatorRequest(), scheduler.getProcessedRequest());
    }

    @Test
    void testStateChange()
    {
        Thread testThread = new Thread(scheduler);
        testThread.start();
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        Assertions.assertEquals(scheduler.getState(), Scheduler.SchedulerState.IDLE);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(scheduler.getState(), Scheduler.SchedulerState.PROCESSING_COMMAND);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(scheduler.getState(), Scheduler.SchedulerState.WAITING);
        testThread.interrupt();
    }
}
