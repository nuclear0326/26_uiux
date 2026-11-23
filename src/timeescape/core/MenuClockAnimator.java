package timeescape.core;

import java.time.LocalTime;

/** Clock follows OS local time except during a short fast-forward animation. */
public final class MenuClockAnimator {
    public static final long DURATION_NANOSECONDS = 1900_000_000L;
    public static final double FAST_FORWARD_SECONDS = 8 * 3600.0;

    private boolean running;
    private long startedAt;
    private double startTimeSeconds;
    private Runnable whenFinished;

    public void start(Runnable callback) {
        if (running) return;
        LocalTime now = LocalTime.now();
        startTimeSeconds = toSeconds(now);
        startedAt = System.nanoTime();
        whenFinished = callback;
        running = true;
    }

    public boolean isRunning() {
        return running;
    }

    public double displayedSeconds() {
        if (!running) return toSeconds(LocalTime.now());
        double fraction = progress();
        // Smooth acceleration and deceleration without creating frame images.
        double easing = fraction < 0.5
            ? 4.0 * fraction * fraction * fraction
            : 1.0 - Math.pow(-2.0 * fraction + 2.0, 3) / 2.0;
        return startTimeSeconds + FAST_FORWARD_SECONDS * easing;
    }

    /** Call on the Swing timer: execute completion callback exactly once. */
    public void tick() {
        if (running && progress() >= 1.0) {
            running = false;
            Runnable callback = whenFinished;
            whenFinished = null;
            if (callback != null) callback.run();
        }
    }

    private double progress() {
        return Math.min(1.0, Math.max(0.0,
            (System.nanoTime() - startedAt) / (double) DURATION_NANOSECONDS));
    }

    private static double toSeconds(LocalTime time) {
        return time.toSecondOfDay() + time.getNano() / 1_000_000_000.0;
    }
}
