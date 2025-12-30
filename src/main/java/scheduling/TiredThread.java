package scheduling;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TiredThread extends Thread implements Comparable<TiredThread> {

    private static final Runnable POISON_PILL = () -> {}; // Special task to signal shutdown

    private final int id; // Worker index assigned by the executor
    private final double fatigueFactor; // Multiplier for fatigue calculation

    private final AtomicBoolean alive = new AtomicBoolean(true); // Indicates if the worker should keep running

    // Single-slot handoff queue; executor will put tasks here
    private final BlockingQueue<Runnable> handoff = new ArrayBlockingQueue<>(1);

    private final AtomicBoolean busy = new AtomicBoolean(false); // Indicates if the worker is currently executing a task

    private final AtomicLong timeUsed = new AtomicLong(0); // Total time spent executing tasks
    private final AtomicLong timeIdle = new AtomicLong(0); // Total time spent idle
    private final AtomicLong idleStartTime = new AtomicLong(0); // Timestamp when the worker became idle

    public TiredThread(int id, double fatigueFactor) {
        this.id = id;
        this.fatigueFactor = fatigueFactor;
        this.idleStartTime.set(System.nanoTime());
        setName(String.format("FF=%.2f", fatigueFactor));
    }

    public int getWorkerId() {
        return id;
    }

    public double getFatigue() {
        return fatigueFactor * timeUsed.get();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public long getTimeUsed() {
        return timeUsed.get();
    }

    public long getTimeIdle() {
        return timeIdle.get();
    }

    /**
     * Assign a task to this worker.
     * This method is non-blocking: if the worker is not ready to accept a task,
     * it throws IllegalStateException.
     */
    public void newTask(Runnable task) {
        ///

        if (!handoff.offer(task))
            throw new IllegalStateException();
    }

    /**
     * Request this worker to stop after finishing current task.
     * Inserts a poison pill so the worker wakes up and exits.
     */
    public void shutdown() {
        ///
        newTask(POISON_PILL);
    }

    @Override
    public void run() {

        while (alive.get()) {
            Runnable task = null;
            try {
                task = handoff.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            timeIdle.addAndGet(System.nanoTime() - idleStartTime.get());

            if (task == POISON_PILL)
                alive.set(false);
                // interrupt();
            // INFO i commented because we made alive false but it still works without interrupting, because alive is false so the thread will just exit the run

            busy.set(true);
            long startWorkTime = System.nanoTime();

            try {
                task.run();
            } finally {
                timeUsed.addAndGet(System.nanoTime() - startWorkTime);
                busy.set(false);
                idleStartTime.set(System.nanoTime());
            }
        }
    }

    @Override
    public int compareTo(TiredThread o) {
        ///

        return Double.compare(this.getFatigue(), o.getFatigue());
    }
}