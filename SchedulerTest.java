import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {
    @Test
    void testReadFloorRequest(){
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        scheduler.setReadFloorRequest();
        assertEquals(scheduler.getfloorRequestToBeProcessed(), t1.getFloorRequest());
    }
    @Test
    void testProcessFloorRequest(){
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        scheduler.setProcessFloorRequest();
        assertEquals(scheduler.getProcessedRequest(), scheduler.getfloorRequestToBeProcessed());
    }

    @Test
    void testSendElevatorRequest(){
        EventQueue t1 = new EventQueue();
        Scheduler scheduler = new Scheduler(t1);
        t1.setFloorRequest(new ElevatorEvent("14:05:15.0", 2, ELEVATOR_BUTTON.UP, 4));
        scheduler.setReadFloorRequest();
        scheduler.setProcessFloorRequest();
        scheduler.setSendElevatorRequest();
        assertEquals(t1.getElevatorRequest(), scheduler.getProcessedRequest());
    }
}
