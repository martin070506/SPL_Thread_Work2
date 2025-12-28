package scheduling;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        ///
        Random rand = new Random();
        double fatigueFactor;
        workers = new TiredThread[numThreads];
        for (int i = 0; i < workers.length; i++) {
            fatigueFactor = 0.5 + rand.nextDouble();
            workers[i] = new TiredThread(i, fatigueFactor);
            idleMinHeap.add(workers[i]);
            workers[i].start();
        }
    }

    public synchronized void submit(Runnable task) {
        ///
        while (idleMinHeap.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        TiredThread worker = idleMinHeap.poll();

        if (worker != null) {
            Runnable wrapped = () -> {
                try {
                    task.run();
                } finally {
                    synchronized(this) {
                        inFlight.decrementAndGet();
                        idleMinHeap.offer(worker);
                        notifyAll();
                    }
                }
            };

            worker.newTask(wrapped);
            inFlight.incrementAndGet();
        }
    }

    public synchronized void submitAll(Iterable<Runnable> tasks) {
        /// submit tasks one by one and wait until all finish

        for (Runnable task : tasks)
            submit(task);

        while (inFlight.get() != 0)
            try {
                wait();
            } catch (InterruptedException ignored) {}
    }

    public void shutdown() {
        ///

        for (TiredThread worker : workers)
            worker.shutdown();

        for (TiredThread worker : workers)
            try {
                worker.join();
            } catch (InterruptedException ignored) {}

    }

    public synchronized String getWorkerReport() {
        /// return readable statistics for each worker

        StringBuilder sb = new StringBuilder();

        for (TiredThread t : workers) {
            sb.append(String.format("Worker %d: Used=%d, Idle=%d, Fatigue=%.2f\n",
                    t.getWorkerId(), t.getTimeUsed(), t.getTimeIdle(), t.getFatigue()));
        }

        return sb.toString();
    }
}
