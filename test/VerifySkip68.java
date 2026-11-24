import timeescape.system.*;import timeescape.ui.*;import java.awt.event.KeyEvent;import javax.swing.*;
public class VerifySkip68 {
 static int n;static void check(boolean b){if(!b)throw new AssertionError();n++;}
 static boolean press(PuzzleSkipChord c,int key,boolean shift){return c.accept(KeyEvent.KEY_PRESSED,key,shift);}
 public static void main(String[] args)throws Exception{
  int[][] permutations={{56,57,48},{56,48,57},{57,56,48},{57,48,56},{48,56,57},{48,57,56}};
  for(int[] order:permutations){PuzzleSkipChord c=new PuzzleSkipChord();check(!press(c,order[0],true));check(!press(c,order[1],true));check(press(c,order[2],true));for(int i=0;i<10;i++)check(!press(c,order[2],true));c.reset();check(!press(c,order[2],true));}
  PuzzleSkipChord c=new PuzzleSkipChord();for(int k:new int[]{56,57,48})check(!press(c,k,false));check(!press(c,56,true));c.accept(KeyEvent.KEY_RELEASED,56,true);check(!press(c,57,true));check(!press(c,48,true));c.reset();check(!press(c,104,true));check(!press(c,105,true));check(press(c,96,true));c.accept(KeyEvent.KEY_RELEASED,KeyEvent.VK_SHIFT,false);check(!press(c,96,true));
  SwingUtilities.invokeAndWait(()->{int[] complete={0};WiresPuzzlePanel wires=new WiresPuzzlePanel(()->complete[0]++,()->{});wires.developerSkip();check(complete[0]==0);wires.developerSkip();wires.developerSkip();check(complete[0]==1);GearsPuzzlePanel gears=new GearsPuzzlePanel(()->complete[0]++,()->{});gears.cancelSession();gears.developerSkip();check(complete[0]==1);});
  System.out.println("PASS "+n+" chord order, Shift, release, repeat, numpad, whole puzzle and cancellation checks; no game.");
 }
}
