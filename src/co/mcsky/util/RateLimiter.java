package co.mcsky.util;

/**
 * This class enables a thread which will call {@link #limit()} run at most ONCE
 * per {@link #interval} milliseconds.
 */
public class RateLimiter {

    private final long interval;
    private long lastExecute;

    public RateLimiter(long interval) {
        this.interval = interval;
    }

    public void limit() {
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
