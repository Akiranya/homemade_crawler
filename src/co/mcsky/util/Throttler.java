package co.mcsky.util;

public class Throttler {

    private final long interval;
    private long lastExecute;

    public Throttler(long interval) {
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
