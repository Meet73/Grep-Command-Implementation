package org.example;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Custom policy for handling rejected tasks in a ThreadPoolExecutor by retrying with backoff.
 */
public class GrowPolicy2 implements RejectedExecutionHandler {

    private final int maxRetries; // Maximum number of retries
    private final long backoffTime; // Backoff time in milliseconds

    /**
     * Constructs a new GrowPolicy2 instance with the specified maximum retries and backoff time.
     *
     * @param maxRetries  Maximum number of retries allowed before giving up.
     * @param backoffTime Backoff time in milliseconds between retry attempts.
     */
    public GrowPolicy2(int maxRetries, long backoffTime) {
        this.maxRetries = maxRetries;
        this.backoffTime = backoffTime;
    }

    /**
     * Handles the rejected task by retrying with backoff for a limited number of retries.
     *
     * @param r        The task that was rejected.
     * @param executor The ThreadPoolExecutor that rejected the task.
     * @throws RejectedExecutionException If the task cannot be retried due to interruption.
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) throws RejectedExecutionException {
        int retries = 0;
        while (true) {
            if (retries < maxRetries) {
                try {
                    // Sleep for the specified backoff time
                    Thread.sleep(backoffTime);
                } catch (InterruptedException e) {
                    // Restore interrupted status and throw RejectedExecutionException
                    Thread.currentThread().interrupt();
                    throw new RejectedExecutionException("Interrupted while retrying", e);
                }

                // Try offering the task to the executor's queue again
                if (executor.getQueue().offer(r)) {
                    break; // Retry successful, exit the loop
                }
                retries++; // Increment retry count
            } else {
                System.err.println("Max retries reached, task rejected"); // Print rejection message
                break; // Exit the loop after max retries
            }
        }
    }
}
