package timeescape.puzzle;

/** Small deterministic clock-reading model used by the cross-era clock puzzle. */
public final class ClockFocusPuzzle {
    public static final int TARGET_HOUR = 10;
    public static final int TARGET_MINUTE = 10;
    private int hour = 12;
    private int minute = 0;

    public int hour() { return hour; }
    public int minute() { return minute; }
    public void setHour(int value) {
        if (value < 1 || value > 12) throw new IllegalArgumentException("hour must be 1..12");
        hour = value;
    }
    public void setMinute(int value) {
        if (value < 0 || value >= 60 || value % 5 != 0) throw new IllegalArgumentException("minute must be a five-minute tick");
        minute = value;
    }
    public boolean solved() { return hour == TARGET_HOUR && minute == TARGET_MINUTE; }
    public String formatted() { return String.format(java.util.Locale.ROOT, "%02d:%02d", hour, minute); }
}
