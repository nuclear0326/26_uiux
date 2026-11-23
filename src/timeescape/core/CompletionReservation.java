package timeescape.core;

import java.util.function.BooleanSupplier;

/** One pending completion per puzzle session. Called only on the Swing event thread. */
public final class CompletionReservation {
    @FunctionalInterface public interface Scheduler {
        Runnable schedule(int delayMillis, Runnable callback);
    }
    private final Scheduler scheduler;
    private Runnable cancelTimer;
    private long generation;
    private boolean closed;

    public CompletionReservation(Scheduler scheduler) { this.scheduler = scheduler; }

    public void schedule(int delayMillis, BooleanSupplier valid, Runnable commit) {
        if (closed || cancelTimer != null) return;
        long ticket = ++generation;
        cancelTimer = scheduler.schedule(delayMillis, () -> {
            if (closed || ticket != generation) return;
            cancelTimer = null;
            ++generation; // A queued or repeated timer event cannot commit twice.
            if (valid.getAsBoolean()) commit.run();
        });
    }

    public void cancel() {
        ++generation;
        if (cancelTimer != null) cancelTimer.run();
        cancelTimer = null;
    }

    public void close() { closed = true; cancel(); }
}
