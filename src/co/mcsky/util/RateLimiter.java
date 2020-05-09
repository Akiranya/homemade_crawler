package co.mcsky.util;

/**
 * This class enables a thread to
 */
public class RateLimiter {

    private final long interval;
    private long lastExecute;

    public RateLimiter(long interval) {
        this.interval = interval;
    }

    public synchronized void limit() {
        if (check()) {
            lastExecute = System.currentTimeMillis();
        } else {
            try {
                Thread.sleep(left());
                lastExecute = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long diff() {
        return System.currentTimeMillis() - lastExecute;
    }

    private boolean check() {
        return diff() > interval;
    }

    private long left() {
        return interval - diff();
    }

}
