package co.mcsky.util;

/**
 * This class enables a thread to run at most once per {@link #interval}
 * milliseconds specified.
 */
public class RateLimiter {

    private final long interval;
    private long lastExecute;

    /**
     * Creates a rate limiter with specified interval.
     *
     * @param interval in milliseconds.
     */
    public RateLimiter(long interval) {
        this.interval = interval;
    }

    /**
     * Invocation on this method restricts current thread to run at most once
     * per {@code interval} milliseconds specified in this constructor. If the
     * thread invokes this method under the specified interval (say, the thread
     * is calling this method too frequent), then the thread will sleep for a
     * particular length of time so that it effectively runs at most once per
     * {@code interval}.
     */
    public void await() {
        if (check()) {
            lastExecute = System.currentTimeMillis();
        } else {
            try {
                Thread.sleep(left());
                lastExecute = System.currentTimeMillis();
            } catch (InterruptedException e) {
                System.err.println("Waiting terminated early");
            }
        }
    }

    /**
     * Resets the internal timer of this throttler. This will make the next
     * invocation on {@link #await()} immediately return without sleeping.
     */
    public void reset() {
        lastExecute = 0;
    }

    private boolean check() {
        return diff() > interval;
    }

    private long left() {
        return interval - diff();
    }

    private long diff() {
        return System.currentTimeMillis() - lastExecute;
    }

}
