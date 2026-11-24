package timeescape.puzzle;
/** Team starter example: integer ticks avoid floating-point answer errors. */
public final class FrequencyPuzzle {
 private int value;
 public int value(){return value;}
 public void turn(int delta){value=Math.max(0,Math.min(10,value+delta));}
 public boolean solved(){return value==7;}
 public void reset(){value=0;}
}
