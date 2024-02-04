import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SchedulerTest {
    SchedulerTest() {
    }

    @Test
    void testReadFloorRequest() {
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        scheduler.setReadFloorRequest();
        Assertions.assertEquals(scheduler.getFloorRequestToBeProcessed(), t1.getFloorRequest());
    }

    @Test
    void testProcessFloorRequest() {
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        scheduler.setProcessFloorRequest();
        Assertions.assertEquals(scheduler.getProcessedRequest(), scheduler.getFloorRequestToBeProcessed());
    }

    @Test
    void testSendElevatorRequest() {
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        scheduler.setReadFloorRequest();
        scheduler.setProcessFloorRequest();
        scheduler.setSendElevatorRequest();
        Assertions.assertEquals(t1.getElevatorRequest(), scheduler.getProcessedRequest());
    }
}
