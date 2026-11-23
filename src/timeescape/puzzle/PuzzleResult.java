package timeescape.puzzle;

/** A puzzle reports an outcome; it never writes global progress or saves. */
public enum PuzzleResult {
    SOLVED, CANCELLED;

    public static PuzzleResult from(boolean solved) { return solved ? SOLVED : CANCELLED; }
}
