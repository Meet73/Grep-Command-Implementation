package org.example;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom policy for handling rejected tasks in a ThreadPoolExecutor by dynamically growing the pool size.
 * This implementation increases the maximum pool size by one and resubmits the rejected task.
 * Changes made on 18/03/24 5:49 PM.
 */
public class GrowPolicy implements RejectedExecutionHandler {

    private final Lock lock = new ReentrantLock();

    /**
     * Called when a task submission is rejected by the ThreadPoolExecutor.
     *
     * @param r       The task that was rejected.
     * @param executor The ThreadPoolExecutor that rejected the task.
     * @throws RejectedExecutionException If this task should be discarded.
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) throws RejectedExecutionException {
        lock.lock();
        try {
            // Increase the maximum pool size by one
            executor.setMaximumPoolSize(executor.getMaximumPoolSize() + 1);
        } finally {
            lock.unlock();
        }

        // Resubmit the rejected task
        executor.submit(r);
    }
}
