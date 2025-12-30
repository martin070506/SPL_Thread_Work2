package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

    private TiredExecutor executor;
    private final int THREAD_COUNT = 3;

    @BeforeEach
    void setUp() {
        // Initialize with a small number of threads for testing
        executor = new TiredExecutor(THREAD_COUNT);
    }

    @AfterEach
    void tearDown() {
        // Ensure threads are cleaned up after each test
        executor.shutdown();
    }

    // ==========================================
    // PASSING TESTS (Expected to Succeed)
    // ==========================================

    @Test
    void testSubmitRunsAllTasks() {
        // Goal: Verify that simple submission actually runs the code.
        AtomicInteger counter = new AtomicInteger(0);
        int tasksToRun = 10;

        // Create a list of simple tasks
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < tasksToRun; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);

        // Assert: All tasks should have finished incrementing the counter
        assertEquals(tasksToRun, counter.get(), "All tasks should be executed.");
    }

    @Test
    void testSubmitAllBlocksUntilCompletion() throws InterruptedException {
        // Goal: Verify submitAll waits for tasks to finish before returning.
        long sleepTime = 500;
        long startTime = System.currentTimeMillis();

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executor.submitAll(tasks);
        long endTime = System.currentTimeMillis();

        // Assert: The method should have taken at least as long as the sleep
        assertTrue((endTime - startTime) >= sleepTime, "submitAll should block until tasks are finished.");
    }

    @Test
    void testWorkerReportGeneration() {
        // Goal: Verify the report string is formatted correctly and not empty.
        String report = executor.getWorkerReport();
        System.out.println(report); // Optional: print to see output

        assertNotNull(report);
        assertTrue(report.contains("Worker 0"), "Report should contain stats for Worker 0");
        assertTrue(report.contains("Fatigue"), "Report should contain fatigue stats");
    }

    // ==========================================
    // FAILING TESTS (Assertions designed to Fail)
    // ==========================================



    /**
     * THIS TEST WILL FAIL (Potential Bug Exposure).
     * * Why: We are passing null. Standard Java convention suggests immediate validation.
     * However, your submit() method does not check for null.
     * It wraps the null task: wrapped = () -> { task.run(); ... }
     * This wrapper is passed to the worker thread.
     * * result: The Main thread (test) will NOT catch the exception immediately.
     * The exception happens asynchronously inside the Worker Thread, likely killing it.
     * The assertThrows will fail because submit() returns successfully without throwing.
     */
    @Test
    void testSubmitNullThrowsExceptionImmediately() {
        assertThrows(NullPointerException.class, () -> {
            executor.submit(null);
        }, "Submitting null should throw exception on the main thread immediately.");
    }



    // Helper to reflectively call private getFairness if needed,
    // or just call shutdown() which prints it, but here we assume we can't easily get the double
    // without parsing output or if the method was public.
    // *Assuming for this test context that we modify the class to make getFairness package-private or public*
    // If we cannot change code, we rely on the report.
    private double getFairnessFromExecutor() {
        // Since getFairness is private, normally we can't test it directly.
        // For the sake of the 'Fail' test example, assume we rely on calculation or reflection.
        // Here is a dummy implementation to simulate the failure:
        return 1.25; // Simulating a calculated non-zero fairness
    }
}