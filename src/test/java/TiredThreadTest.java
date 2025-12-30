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

    @Test
    void testExecutionAndBusyState() throws InterruptedException {
        // Goal: Verify the thread actually picks up a task, sets busy=true, runs it, and resets busy=false.

        worker = new TiredThread(1, 1.0);
        worker.start();

        // Latches to synchronize the test with the worker thread
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch allowTaskToFinish = new CountDownLatch(1);
        CountDownLatch taskFinished = new CountDownLatch(1);

        worker.newTask(() -> {
            taskStarted.countDown(); // 1. Signal that we are running
            try {
                // 2. Wait here so the main test thread has time to check isBusy()
                allowTaskToFinish.await();
                // Simulate some "Work" time to update fatigue manually (since run() doesn't do it automatically)

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            taskFinished.countDown(); // 3. Signal completion
        });

        // Wait for the worker to pick up the task
        assertTrue(taskStarted.await(1, TimeUnit.SECONDS), "Task should start execution");

        // ASSERT: Worker should be busy while inside the task
        assertTrue(worker.isBusy(), "Worker should be marked busy while running a task");

        // Let the task finish
        allowTaskToFinish.countDown();
        assertTrue(taskFinished.await(1, TimeUnit.SECONDS), "Task should complete");

        // ASSERT: Worker should not be busy after task finishes
        // We use a small loop/sleep here because there is a tiny gap between task.run() returning and busy.set(false)
        int retries = 0;
        while (worker.isBusy() && retries < 10) {
            Thread.sleep(10);
            retries++;
        }
        assertFalse(worker.isBusy(), "Worker should be free after task finishes");
    }

    @Test
    void testIdleTimeAccumulation() throws InterruptedException {
        // Goal: Verify that the worker tracks how long it sits waiting for a task.

        worker = new TiredThread(2, 1.0);
        worker.start();

        // 1. Wait for ~100ms with no task. The worker is "Idle" during this time.
        long sleepTimeMs = 100;
        Thread.sleep(sleepTimeMs);

        // 2. Submit a quick task to force the worker to wake up and calculate the previous idle interval.
        CountDownLatch latch = new CountDownLatch(1);
        worker.newTask(latch::countDown);

        assertTrue(latch.await(1, TimeUnit.SECONDS));

        // 3. Verify Idle Time
        long idleTimeNanos = worker.getTimeIdle();
        long minExpectedNanos = TimeUnit.MILLISECONDS.toNanos(sleepTimeMs);

        // We check that idle time is at least the sleep time (allowing for some OS scheduling jitter)
        assertTrue(idleTimeNanos >= minExpectedNanos,
                "Idle time (" + idleTimeNanos + ") should be >= waited time (" + minExpectedNanos + ")");
    }
}