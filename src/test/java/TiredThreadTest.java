package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class TiredThreadTest {

    private TiredThread worker;

    @AfterEach
    void tearDown() {
        if (worker != null && worker.isAlive()) {
            worker.shutdown();
        }
    }

    // ==========================================
    // PASSING TESTS (Expected to Succeed)
    // ==========================================

    @Test
    void testInitialization() {
        // Goal: Verify initial state of stats and identity
        worker = new TiredThread(1, 1.5);

        assertEquals(1, worker.getWorkerId());
        assertEquals(0, worker.getTimeUsed(), "Initial time used should be 0");
        assertEquals(0, worker.getTimeIdle(), "Initial time idle should be 0");
        // Fatigue = 0 * 1.5 = 0
        assertEquals(0.0, worker.getFatigue(), 0.001);
        assertFalse(worker.isBusy(), "Worker should not be busy initially");
    }





    @Test
    void testShutdownLifecycle() throws InterruptedException {
        // Goal: Verify shutdown sends the poison pill and stops the thread
        worker = new TiredThread(1, 1.0);
        worker.start();

        assertTrue(worker.isAlive());

        worker.shutdown();

        // join() waits for the thread to die
        worker.join(2000);

        assertFalse(worker.isAlive(), "Thread should be dead after shutdown");
    }




    // ==========================================
    // FAILING TESTS (Assertions designed to Fail)
    // ==========================================

    /**
     * THIS TEST WILL FAIL (Throws Exception).
     * * Why: The internal queue (ArrayBlockingQueue) has a capacity of 1.
     * We are not starting the thread, so it never pulls from the queue.
     * The first 'newTask' fills the slot.
     * The second 'newTask' attempts to 'offer' to a full queue, which fails.
     * Your code throws 'IllegalStateException' when offer returns false.
     */
    @Test
    void testQueueCapacityOverflow() {
        worker = new TiredThread(1, 1.0);
        // We purposefully do NOT call worker.start(), so the queue doesn't drain.

        worker.newTask(() -> System.out.println("Task 1"));

        // This line will throw IllegalStateException, causing the test to fail/error
       assertThrows(Exception.class,()->worker.newTask(() -> System.out.println("Task 2")));
    }


}