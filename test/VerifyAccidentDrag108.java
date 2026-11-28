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
import timeescape.system.*;

public final class VerifyAccidentDrag108 {
 static int checks;
 static void ok(boolean value,String detail){if(!value)throw new AssertionError(detail);checks++;}
 static void event(AccidentLogPanel p,int type,int x,int y){p.board.dispatchEvent(new MouseEvent(p.board,type,0,type==MouseEvent.MOUSE_DRAGGED?MouseEvent.BUTTON1_DOWN_MASK:0,x,y,1,false,MouseEvent.BUTTON1));}
 static void press(AccidentLogPanel p,int row){Rectangle r=p.board.row(row);event(p,MouseEvent.MOUSE_PRESSED,r.x+220,r.y+r.height/2);}
 static void drop(AccidentLogPanel p,int boundary){int y=boundary==6?p.board.getHeight()-10:p.board.row(boundary).y;event(p,MouseEvent.MOUSE_DRAGGED,240,y);event(p,MouseEvent.MOUSE_RELEASED,240,y);}
 static void move(AccidentLogPanel p,int from,int to){press(p,from);drop(p,to>from?to+1:to);}
 static void order(AccidentLogPanel p){for(int i=0;i<6;i++)move(p,p.model.order().indexOf(i),i);}
 static void click(AccidentLogPanel p,int row){press(p,row);Rectangle r=p.board.row(row);event(p,MouseEvent.MOUSE_RELEASED,r.x+220,r.y+20);}
 static void image(AccidentLogPanel p,int w,int h,Path path)throws Exception{
  ImageIO.write(VerifyAccident94.paint(p,w,h),"png",path.toFile());VerifyLogin102.bounds(p);VerifyPuzzleLayout106.textBounds(p);
  int width=p.board.row(0).width;for(var r:AccidentLogPuzzle.RECORDS){
   ok(p.board.getFontMetrics(GameFonts.body(14)).stringWidth(r.clue())<width-100,"clue fits");
   ok(p.board.getFontMetrics(GameFonts.body(14)).stringWidth(r.normal())<(width-205)/2-10,"normal column fits");
   ok(p.board.getFontMetrics(GameFonts.body(14)).stringWidth(r.failed())<(width-205)/2-10,"failed column fits");
  }
 }
 public static void main(String[] args)throws Exception{
  Path out=timeescape.testutil.ArtifactPaths.output("v108-preview");Files.createDirectories(out);
  GameState state=VerifyAccident94.ready();SaveSlots disk=new SaveSlots(Files.createTempDirectory("log108-save"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);
  AtomicInteger saves=new AtomicInteger();AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   for(int[] size:new int[][]{{1000,820},{900,760}}){
    int[] completed={0},closed={0};AccidentLogPanel p=new AccidentLogPanel(()->completed[0]++,()->closed[0]++);
    image(p,size[0],size[1],out.resolve("01-order-"+size[0]+".png"));
    ok(VerifyRepairPanels.button(p,"↑")==null&&VerifyRepairPanels.button(p,"↓")==null,"arrow buttons removed");
    for(int from=0;from<6;from++)for(int to=0;to<6;to++){
     p.reset.doClick(0);var expected=new ArrayList<>(p.model.order());Integer item=expected.remove(from);expected.add(to,item);move(p,from,to);ok(p.model.order().equals(expected),"insert "+from+" -> "+to+" at "+size[0]);
    }
    p.reset.doClick(0);var initial=p.model.order();
    for(Point outside:new Point[]{new Point(-10,160),new Point(p.board.getWidth()+10,160),new Point(240,0),new Point(240,p.board.getHeight()+20)}){
     press(p,2);event(p,MouseEvent.MOUSE_DRAGGED,outside.x,outside.y);event(p,MouseEvent.MOUSE_RELEASED,outside.x,outside.y);ok(p.model.order().equals(initial),"outside drop does not mutate");
    }
    press(p,2);p.getActionMap().get("cancel-drag").actionPerformed(null);drop(p,0);ok(p.model.order().equals(initial)&&closed[0]==0,"Escape cancels only drag");
    press(p,2);p.reset.doClick(0);drop(p,0);ok(p.model.order().equals(initial),"reset cancels pending release");
    press(p,2);for(var listener:p.board.getFocusListeners())listener.focusLost(new FocusEvent(p.board,FocusEvent.FOCUS_LOST));drop(p,0);ok(p.model.order().equals(initial),"lost focus cancels drag");
    p.action.doClick(0);ok(p.model.stage()==1&&completed[0]==0,"incorrect order rejected");
    press(p,3);event(p,MouseEvent.MOUSE_DRAGGED,240,p.board.top+9);image(p,size[0],size[1],out.resolve("02-drag-"+size[0]+".png"));drop(p,0);
    order(p);p.action.doClick(0);ok(p.model.stage()==2&&completed[0]==0,"real drag solution advances without completion");
    image(p,size[0],size[1],out.resolve("03-compare-"+size[0]+".png"));
    click(p,3);p.action.doClick(0);ok(completed[0]==0&&p.model.stage()==2,"wrong anomaly rejected");
    image(p,size[0],size[1],out.resolve("04-wrong-anomaly-"+size[0]+".png"));
    click(p,2);p.action.doClick(0);p.action.doClick(0);p.reset.doClick(0);p.developerSkip();ok(completed[0]==1&&p.model.stage()==3,"final comparison completes exactly once");
    AccidentLogPanel cancelled=new AccidentLogPanel(()->completed[0]++,()->closed[0]++);VerifyAccident94.paint(cancelled,size[0],size[1]);press(cancelled,2);var unchanged=cancelled.model.order();cancelled.cancelSession();drop(cancelled,0);cancelled.action.doClick(0);cancelled.reset.doClick(0);cancelled.developerSkip();ok(cancelled.model.order().equals(unchanged)&&completed[0]==1,"closed panel rejects late input");
    AccidentLogPanel esc=new AccidentLogPanel(()->{},()->closed[0]++);esc.getActionMap().get("cancel-drag").actionPerformed(null);ok(closed[0]==1,"Escape without drag closes");
    AccidentLogPanel skip=new AccidentLogPanel(()->completed[0]++,()->{});VerifyAccident94.paint(skip,size[0],size[1]);press(skip,2);skip.developerSkip();drop(skip,0);ok(skip.model.stage()==2&&completed[0]==1&&skip.source<0,"skip cancels drag and only advances");skip.developerSkip();skip.developerSkip();ok(completed[0]==2,"second skip completes once");
   }
   var before=state.snapshot();
   PuzzleRunner cancelRunner=new PuzzleRunner(Map.of("log_analysis",step->{
    AccidentLogPanel p=new AccidentLogPanel(()->{throw new AssertionError("partial completion");},()->{});VerifyAccident94.paint(p,1000,820);order(p);p.action.doClick(0);p.cancelSession();return PuzzleResult.CANCELLED;
   }),()->state,()->true,step->{throw new AssertionError("cancelled puzzle saved");});
   ok(cancelRunner.run("PZ021")==PuzzleRunner.Outcome.CANCELLED&&state.snapshot().equals(before),"leaving after ordering changes no progress");
   ok(!disk.store(SaveSlots.Slot.AUTO).exists(),"partial analysis creates no save file");
   PuzzleRunner runner=new PuzzleRunner(Map.of("log_analysis",step->{
    int[] done={0};AccidentLogPanel p=new AccidentLogPanel(()->done[0]++,()->{});VerifyAccident94.paint(p,1000,820);p.openEvidence();p.evidence.incident.doClick(0);p.closeEvidence();order(p);p.action.doClick(0);p.openEvidence();p.evidence.normal.doClick(0);p.closeEvidence();
    ok(done[0]==0&&!state.has("FAULT_REPORT")&&!state.done("PZ021")&&!state.available("PZ008")&&saves.get()==0,"drag ordering gives no reward unlock or save");
    VerifyAccident94.paint(p,1000,820);click(p,3);p.action.doClick(0);ok(done[0]==0&&saves.get()==0,"wrong comparison no completion");
    click(p,2);p.action.doClick(0);p.action.doClick(0);ok(done[0]==1,"panel completion once");return PuzzleResult.from(done[0]==1);
   }),()->state,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(state.snapshot(),true));});
   ok(runner.run("PZ021")==PuzzleRunner.Outcome.COMPLETED,"real panel final completion committed by runner");
   ok(state.done("PZ021")&&state.has("FAULT_REPORT")&&state.has("USB")&&state.available("PZ008")&&saves.get()==1,"report USB repair and one autosave");
   runner.run("PZ021");ok(saves.get()==1,"repeat runner no duplicate reward save");
  }catch(Exception e){throw new RuntimeException(e);}});
  pending.get().get(5,TimeUnit.SECONDS);queue.close();
  SaveSlots reopened=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard());GameState loaded=reopened.load(SaveSlots.Slot.AUTO).state();
  ok(loaded.snapshot().equals(state.snapshot())&&loaded.done("PZ021")&&loaded.has("FAULT_REPORT")&&loaded.available("PZ008"),"fresh load retains completion report repair unlock");
  System.out.println("PASS "+checks+" real panel drag, cancellation, two-stage completion, runner rewards, autosave and reload checks; no game windows.");
 }
}
