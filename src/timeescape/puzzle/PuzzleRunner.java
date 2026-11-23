package timeescape.puzzle;

import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import timeescape.core.GameState;
import timeescape.core.ProgressCatalog.Step;

/** UI-independent dispatcher. Called on the UI thread, including modal dialog returns. */
public final class PuzzleRunner {
    @FunctionalInterface public interface Launcher { PuzzleResult play(Step step); }
    public enum Outcome { COMPLETED, CANCELLED, IGNORED }
    private final Map<String, Launcher> launchers;
    private final Supplier<GameState> currentState;
    private final BooleanSupplier active;
    private final Consumer<Step> onCompleted;
    private boolean running;

    public PuzzleRunner(Map<String, Launcher> launchers, Supplier<GameState> currentState,
                        BooleanSupplier active, Consumer<Step> onCompleted) {
        this.launchers = Map.copyOf(launchers);
        this.currentState = Objects.requireNonNull(currentState);
        this.active = Objects.requireNonNull(active);
        this.onCompleted = Objects.requireNonNull(onCompleted);
    }

    public Outcome run(String stepId) {
        if(running || !active.getAsBoolean()) return Outcome.IGNORED;
        GameState session = currentState.get();
        Step step = session.catalog().steps().get(stepId);
        if(step == null) throw new IllegalArgumentException("Unknown puzzle step: " + stepId);
        if(step.automatic() || !session.available(stepId)) return Outcome.IGNORED;
        Launcher launcher = launchers.get(step.kind());
        if(launcher == null) throw new IllegalArgumentException("Unregistered puzzle kind: " + step.kind());
        running = true;
        try {
            PuzzleResult result = Objects.requireNonNull(launcher.play(step), "Puzzle outcome");
            if(result != PuzzleResult.SOLVED) return Outcome.CANCELLED;
            // A modal dialog pumps events. Never apply an old session to newly loaded progress.
            if(!active.getAsBoolean() || currentState.get() != session || !session.available(stepId))
                return Outcome.IGNORED;
            session.complete(stepId);
            onCompleted.accept(step);
            return Outcome.COMPLETED;
        } finally { running = false; }
    }
}
