package timeescape.puzzle.present;
import java.util.Arrays;
/** Shared bounded cyclic controls and exact target comparison. Arrays never escape. */
abstract class CyclicValuesPuzzle implements PresentPuzzle {
 private final String id; private final int modulo; private final int[] values;
 CyclicValuesPuzzle(String id,int modulo,int[] initial){this.id=id;this.modulo=modulo;values=initial.clone();}
 public final String id(){return id;} public final int size(){return values.length;}
 public final int value(int index){return values[index];}
 public final void turn(int index){if(index<0||index>=values.length)throw new IllegalArgumentException();advance(index);afterTurn(index);}
 protected final void advance(int index){values[index]=(values[index]+1)%modulo;}
 protected void afterTurn(int index){}
 protected final boolean matches(int[] target){return Arrays.equals(values,target);}
 protected final String direction(int index){return new String[]{"↑","→","↓","←"}[value(index)];}
}
