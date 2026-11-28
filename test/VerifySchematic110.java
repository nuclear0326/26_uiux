package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.puzzle.*;
import timeescape.puzzle.present.SchematicPuzzle;
import timeescape.system.*;

public final class VerifySchematic110 {
 static int checks;
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 static void event(SchematicPuzzlePanel p,int type,Point pos){p.board.dispatchEvent(new MouseEvent(p.board,type,0,type==MouseEvent.MOUSE_RELEASED?0:MouseEvent.BUTTON1_DOWN_MASK,pos.x,pos.y,1,false,type==MouseEvent.MOUSE_DRAGGED?MouseEvent.NOBUTTON:MouseEvent.BUTTON1));}
 static void press(SchematicPuzzlePanel p,int from){event(p,MouseEvent.MOUSE_PRESSED,p.board.center(from));}
 static void release(SchematicPuzzlePanel p,int to){event(p,MouseEvent.MOUSE_DRAGGED,p.board.center(to));event(p,MouseEvent.MOUSE_RELEASED,p.board.center(to));}
 static void move(SchematicPuzzlePanel p,int from,int to){press(p,from);release(p,to);}
 static void assemble(SchematicPuzzlePanel p){for(int piece=0;piece<9;piece++)for(int cell=0;cell<18;cell++)if(p.model.pieceAt(cell)==piece){move(p,cell,piece);break;}}
 static void conservation(SchematicPuzzle m){int[] counts=new int[9];for(int v:m.snapshot())if(v>=0)counts[v]++;for(int n:counts)ok(n==1,"exactly one copy of each piece");}
 static void image(SchematicPuzzlePanel p,int w,int h,Path path)throws Exception{var im=VerifyAccident94.paint(p,w,h);VerifyLogin102.bounds(p);VerifyPuzzleLayout106.textBounds(p);ok(p.status.getFontMetrics(p.status.getFont()).stringWidth(p.status.getText())<=p.status.getWidth(),"status fits");ImageIO.write(im,"png",path.toFile());}
 static GameState ready(){GameState s=VerifyAccident94.ready();s.complete("PZ021");return s;}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"headless only");
  for(int from=0;from<18;from++)for(int to=0;to<18;to++){
   SchematicPuzzle m=new SchematicPuzzle();if(m.pieceAt(from)<0)m.move(9,from);int[] expected=m.snapshot();int t=expected[to];expected[to]=expected[from];expected[from]=t;
   m.move(from,to);ok(Arrays.equals(m.snapshot(),expected),"model swap "+from+"/"+to);conservation(m);
  }
  SchematicPuzzle invalid=new SchematicPuzzle();int[] initial=invalid.snapshot();for(int i:new int[]{-1,18,999}){ok(!invalid.move(i,0)&&!invalid.move(9,i),"invalid drop rejected");}ok(Arrays.equals(initial,invalid.snapshot()),"invalid input unchanged");initial[9]=-1;ok(invalid.pieceAt(9)>=0,"snapshot defensive");
  Path out=timeescape.testutil.ArtifactPaths.output("v110-preview");Files.createDirectories(out);
  SaveSlots disk=new SaveSlots(Files.createTempDirectory("schematic110-save"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();AtomicInteger saves=new AtomicInteger();GameState state=ready();
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   for(int[] size:new int[][]{{1000,820},{900,760}}){
    int[] completed={0},closed={0};SchematicPuzzlePanel p=new SchematicPuzzlePanel(()->completed[0]++,()->closed[0]++);
    image(p,size[0],size[1],out.resolve("01-initial-"+size[0]+".png"));
    ok(p.model.placed()==0&&!p.model.solved(),"nine fragments start in tray");
    // All board/tray transfers, including occupied targets, using actual mouse handlers.
    for(int from=0;from<18;from++)for(int to=0;to<18;to++){
     p.reset.doClick(0);if(p.model.pieceAt(from)<0)p.model.move(9,from);int[] expected=p.model.snapshot();int t=expected[to];expected[to]=expected[from];expected[from]=t;move(p,from,to);ok(Arrays.equals(expected,p.model.snapshot()),"UI swap "+from+"/"+to);conservation(p.model);
    }
    p.reset.doClick(0);var before=p.model.snapshot();
    for(Point outside:new Point[]{new Point(-1,200),new Point(p.board.getWidth()+1,200),new Point(100,0),new Point(100,p.board.getHeight()+1),new Point(p.board.getWidth()/2,p.board.getHeight()/2)}){
     press(p,9);event(p,MouseEvent.MOUSE_DRAGGED,outside);event(p,MouseEvent.MOUSE_RELEASED,outside);ok(Arrays.equals(before,p.model.snapshot()),"outside/gutter return to source");
    }
    press(p,9);p.getActionMap().get("cancel-drag").actionPerformed(null);release(p,0);ok(Arrays.equals(before,p.model.snapshot())&&closed[0]==0,"Esc cancels drag only");
    press(p,9);for(var f:p.board.getFocusListeners())f.focusLost(new FocusEvent(p.board,FocusEvent.FOCUS_LOST));release(p,0);ok(Arrays.equals(before,p.model.snapshot()),"focus loss cancels");
    press(p,9);p.reset.doClick(0);release(p,0);ok(Arrays.equals(before,p.model.snapshot()),"reset cancels pending drop");
    p.action.doClick(0);ok(completed[0]==0,"incomplete rejected");
    // Wrong full board cannot unlock anything.
    for(int i=0;i<9;i++)move(p,9+i,i);p.action.doClick(0);ok(p.model.placed()==9&&!p.model.solved()&&completed[0]==0,"wrong placement rejected");
    image(p,size[0],size[1],out.resolve("02-incorrect-"+size[0]+".png"));
    move(p,0,9);ok(p.model.placed()==8,"placed piece returns to tray");
    p.reset.doClick(0);move(p,14,0);move(p,16,2);move(p,15,4);move(p,17,6);press(p,12);Point dest=p.board.center(1);dest.translate(22,20);event(p,MouseEvent.MOUSE_DRAGGED,dest);
    image(p,size[0],size[1],out.resolve("03-drag-"+size[0]+".png"));release(p,1);
    assemble(p);ok(p.model.solved()&&completed[0]==0,"arranging alone does not complete");
    p.action.doClick(0);image(p,size[0],size[1],out.resolve("04-complete-"+size[0]+".png"));var finished=p.model.snapshot();p.verify();p.developerSkip();p.reset.doClick(0);move(p,0,1);ok(completed[0]==1&&Arrays.equals(finished,p.model.snapshot()),"finish once, stale input blocked");
    SchematicPuzzlePanel cancelled=new SchematicPuzzlePanel(()->completed[0]++,()->closed[0]++);VerifyAccident94.paint(cancelled,size[0],size[1]);press(cancelled,9);cancelled.cancelSession();release(cancelled,0);cancelled.verify();cancelled.developerSkip();cancelled.reset.doClick(0);ok(cancelled.model.placed()==0&&completed[0]==1,"cancelled session cannot finish");
    SchematicPuzzlePanel esc=new SchematicPuzzlePanel(()->{},()->closed[0]++);esc.getActionMap().get("cancel-drag").actionPerformed(null);ok(closed[0]==1,"Esc closes without drag");
    SchematicPuzzlePanel skip=new SchematicPuzzlePanel(()->completed[0]++,()->{});skip.developerSkip();skip.developerSkip();ok(completed[0]==2&&skip.model.solved(),"developer skip completes once");
    PresentPuzzlePanel wrapper=new PresentPuzzlePanel("PZ008",()->{},()->{});ok(wrapper.getComponent(0) instanceof SchematicPuzzlePanel,"legacy content entry selects new panel");
   }
   var before=state.snapshot();PuzzleRunner cancelledRunner=new PuzzleRunner(Map.of("present",step->{SchematicPuzzlePanel p=new SchematicPuzzlePanel(()->{throw new AssertionError("partial solved");},()->{});VerifyAccident94.paint(p,1000,820);move(p,9,0);p.cancelSession();return PuzzleResult.CANCELLED;}),()->state,()->true,step->{throw new AssertionError("cancel saved");});
   ok(cancelledRunner.run("PZ008")==PuzzleRunner.Outcome.CANCELLED&&state.snapshot().equals(before)&&!disk.store(SaveSlots.Slot.AUTO).exists(),"partial exit no progression/save");
   // Existing data allows collecting materials after analysis; PZ008 must keep that state.
   for(String item:new String[]{"CASE","CELL","MODULE"})state.collect(item);
   GameState expected=GameState.restore(ProgressCatalog.standard(),state.snapshot());expected.complete("PZ008");
   PuzzleRunner runner=new PuzzleRunner(Map.of("present",step->{int[] done={0};SchematicPuzzlePanel p=new SchematicPuzzlePanel(()->done[0]++,()->{});VerifyAccident94.paint(p,1000,820);assemble(p);ok(!state.done("PZ008")&&done[0]==0&&saves.get()==0,"no automatic commit before confirmation");p.verify();p.verify();ok(done[0]==1,"panel confirms once");return PuzzleResult.from(done[0]==1);}),()->state,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(state.snapshot(),true));});
   ok(runner.run("PZ008")==PuzzleRunner.Outcome.COMPLETED&&state.snapshot().equals(expected.snapshot()),"existing completion effects exactly preserved");
   ok(state.done("PZ009")&&state.available("PZ010")&&state.has("DEVICE")&&state.has("USB")&&state.has("FAULT_REPORT")&&state.has("CASE"),"module unlocked; existing items unchanged; prototype retained without new rewards");
   runner.run("PZ008");ok(saves.get()==1,"one autosave, replay ignored");
  }catch(Exception e){throw new RuntimeException(e);}});
  pending.get().get(5,TimeUnit.SECONDS);queue.close();
  GameState loaded=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();
  ok(loaded.snapshot().equals(state.snapshot())&&loaded.done("PZ008")&&loaded.available("PZ010"),"reload retains completion and next step");
  loaded.complete("PZ010");ok(loaded.has("DEVICE")&&!loaded.has("CASE")&&!loaded.has("CELL")&&!loaded.has("MODULE"),"original next-stage item reward and consumption intact");
  System.out.println("PASS "+checks+" schematic model/input/layout/lifecycle, original completion, autosave and reload checks. No game windows.");
 }
}
