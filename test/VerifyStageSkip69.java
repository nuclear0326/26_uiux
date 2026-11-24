package timeescape.ui;
import timeescape.system.*;import timeescape.puzzle.RepairModels.*;import javax.swing.*;import java.nio.file.*;import java.awt.*;import java.awt.event.KeyEvent;
public class VerifyStageSkip69 {
 static int n;static void check(boolean b,String m){if(!b)throw new AssertionError(m);n++;}
 static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static void chord(PuzzleSkipChord c,Runnable run){for(int key:new int[]{56,57,48})if(c.accept(KeyEvent.KEY_PRESSED,key,true))run.run();}
 static void release(PuzzleSkipChord c){for(int key:new int[]{56,57,48})c.accept(KeyEvent.KEY_RELEASED,key,true);}
 static void solveWires(WiresPuzzlePanel p)throws Exception{Wires w=(Wires)field(p,"wires");int i=0;for(String line:Files.readAllLines(Path.of("test/fixtures/wires_"+(w.hard?"hard":"easy")+"_solution.txt"))){w.begin(i);String[] tokens=line.split(" ");for(int j=1;j<tokens.length;j++){String[] xy=tokens[j].split(",");check(w.step(i,new Point(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))),"legal wire move");}i++;}var refresh=WiresPuzzlePanel.class.getDeclaredMethod("refreshPuzzle");refresh.setAccessible(true);refresh.invoke(p);p.action.doClick(0);}
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
 int[] solved={0};WiresPuzzlePanel p=new WiresPuzzlePanel(()->solved[0]++,()->{});PuzzleSkipChord c=new PuzzleSkipChord();chord(c,p::developerSkip);check(((Wires)field(p,"wires")).hard&&solved[0]==0,"first chord only advances easy");chord(c,p::developerSkip);check(solved[0]==0,"hold no second stage");release(c);chord(c,p::developerSkip);check(solved[0]==1,"second chord completes");release(c);chord(c,p::developerSkip);check(solved[0]==1,"no double completion");
 WiresPuzzlePanel normal=new WiresPuzzlePanel(()->solved[0]++,()->{});solveWires(normal);check(solved[0]==1&&((Wires)field(normal,"wires")).hard,"normal easy unchanged");solveWires(normal);check(solved[0]==2,"normal hard completes");
 GearsPuzzlePanel gears=new GearsPuzzlePanel(()->solved[0]++,()->{});Gears model=(Gears)field(gears,"gears");for(int y=0;y<4;y++)for(int x=0;x<4;x++)model.put(y,x,model.target[y][x]);var refresh=GearsPuzzlePanel.class.getDeclaredMethod("refreshPuzzle");refresh.setAccessible(true);refresh.invoke(gears);gears.action.doClick(0);check(solved[0]==3,"normal gears unchanged");
 WiresPuzzlePanel cancelled=new WiresPuzzlePanel(()->solved[0]++,()->{});cancelled.cancelSession();cancelled.developerSkip();check(solved[0]==3,"closed panel no completion");
 System.out.println("PASS "+n+" staged chord and normal gear/wire completion checks; no window or audio.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
