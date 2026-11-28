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
import timeescape.puzzle.present.ModuleAssemblyPuzzle;
import timeescape.system.*;

public final class VerifyModule111 {
 static int checks;
 static void ok(boolean value,String why){if(!value)throw new AssertionError(why);checks++;}
 static int[] values(ModuleAssemblyPanel p){return new int[]{p.model.value(0),p.model.value(1),p.model.value(2)};}
 static void mouse(JButton b,int type,int button,Point at){b.dispatchEvent(new MouseEvent(b,type,0,button==1&&type==MouseEvent.MOUSE_PRESSED?MouseEvent.BUTTON1_DOWN_MASK:0,at.x,at.y,1,false,button));}
 static void click(ModuleAssemblyPanel p,int index){JButton b=p.board.parts[index];Point at=new Point(b.getWidth()/2,b.getHeight()/2);mouse(b,MouseEvent.MOUSE_PRESSED,1,at);mouse(b,MouseEvent.MOUSE_RELEASED,1,at);}
 static void align(ModuleAssemblyPanel p){for(int i=0;i<3;i++)while(p.model.value(i)!=i)click(p,i);}
 static void image(ModuleAssemblyPanel p,int w,int h,Path path)throws Exception{var im=VerifyAccident94.paint(p,w,h);VerifyLogin102.bounds(p);VerifyPuzzleLayout106.textBounds(p);ok(p.status.getFontMetrics(p.status.getFont()).stringWidth(p.status.getText())<=p.status.getWidth(),"status text fits");ImageIO.write(im,"png",path.toFile());}
 static GameState ready(){GameState s=VerifyAccident94.ready();s.complete("PZ021");s.complete("PZ008");for(String item:new String[]{"CASE","CELL","MODULE"})s.collect(item);return s;}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"no desktop windows");Path out=timeescape.testutil.ArtifactPaths.output("v111-preview");Files.createDirectories(out);
  SaveSlots disk=new SaveSlots(Files.createTempDirectory("module111-save"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);AtomicInteger saves=new AtomicInteger();AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();GameState state=ready();
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   // Every rotation combination is compared against the unchanged production model.
   for(int code=0;code<64;code++){
    int[] done={0};ModuleAssemblyPanel p=new ModuleAssemblyPanel(()->done[0]++,()->{});ModuleAssemblyPuzzle reference=new ModuleAssemblyPuzzle();int digits=code;
    for(int i=0;i<3;i++){int turns=digits%4;digits/=4;for(int n=0;n<turns;n++){p.board.parts[i].doClick(0);reference.turn(i);}for(int k=0;k<3;k++)ok(p.model.value(k)==reference.value(k),"one component per click");}
    int count=0;for(int i=0;i<3;i++){if(reference.value(i)==i)count++;ok(p.board.parts[i].getAccessibleContext().getAccessibleName().contains(reference.label(i)),"accessible direction matches state");}
    ok(p.status.getText().equals("접점 정렬 "+count+" / 3"),"alignment count matches model");ok(done[0]==0,"rotating alone never completes");p.verify();p.verify();ok(done[0]==(reference.solved()?1:0),"same completion condition, one callback");
   }
   for(int[] size:new int[][]{{1000,820},{900,760},{900,680}}){
    int[] done={0},closed={0};ModuleAssemblyPanel p=new ModuleAssemblyPanel(()->done[0]++,()->closed[0]++);
    String suffix=size[0]+"x"+size[1];image(p,size[0],size[1],out.resolve("01-initial-"+suffix+".png"));
    int[] initial=values(p);
    for(int i=0;i<3;i++){JButton b=p.board.parts[i];Point center=new Point(b.getX()+b.getWidth()/2,b.getY()+b.getHeight()/2);ok(p.board.findComponentAt(center)==b,"scaled hit area selects correct part");for(int j=0;j<4;j++)click(p,i);ok(Arrays.equals(initial,values(p)),"four mouse clicks wrap without changing neighbours");}
    JButton first=p.board.parts[0];Point middle=new Point(first.getWidth()/2,first.getHeight()/2);mouse(first,MouseEvent.MOUSE_PRESSED,3,middle);mouse(first,MouseEvent.MOUSE_RELEASED,3,middle);ok(Arrays.equals(initial,values(p)),"right mouse ignored");
    mouse(first,MouseEvent.MOUSE_PRESSED,1,middle);mouse(first,MouseEvent.MOUSE_EXITED,1,new Point(-1,-1));mouse(first,MouseEvent.MOUSE_RELEASED,1,new Point(-1,-1));ok(Arrays.equals(initial,values(p)),"press then release outside is cancelled");
    p.verify();ok(done[0]==0&&p.status.getText().contains("확인"),"wrong assembly feedback without completion");
    click(p,0);click(p,0);click(p,0);image(p,size[0],size[1],out.resolve("02-one-aligned-"+suffix+".png"));
    ok(p.model.value(0)==0&&p.status.getText().equals("접점 정렬 1 / 3"),"aligned part state");p.reset.doClick(0);ok(Arrays.equals(initial,values(p)),"reset restores exact original values");
    align(p);ok(done[0]==0&&p.model.solved(),"full alignment waits for confirmation");image(p,size[0],size[1],out.resolve("03-aligned-"+suffix+".png"));
    p.action.doClick(0);p.action.doClick(0);p.developerSkip();p.reset.doClick(0);click(p,1);p.turn(2);ok(done[0]==1&&p.model.solved(),"confirmed once and late input ignored");image(p,size[0],size[1],out.resolve("04-complete-"+suffix+".png"));
    ModuleAssemblyPanel cancelled=new ModuleAssemblyPanel(()->done[0]++,()->closed[0]++);VerifyAccident94.paint(cancelled,size[0],size[1]);var untouched=values(cancelled);cancelled.cancelSession();click(cancelled,0);cancelled.verify();cancelled.developerSkip();cancelled.reset.doClick(0);ok(done[0]==1&&Arrays.equals(untouched,values(cancelled)),"cancelled panel rejects callbacks and input");
    ModuleAssemblyPanel esc=new ModuleAssemblyPanel(()->done[0]++,()->closed[0]++);esc.getActionMap().get("close-module").actionPerformed(null);esc.getActionMap().get("close-module").actionPerformed(null);esc.developerSkip();ok(closed[0]==1&&done[0]==1,"Esc closes once without completing");
    ModuleAssemblyPanel skip=new ModuleAssemblyPanel(()->done[0]++,()->{});skip.developerSkip();skip.developerSkip();ok(done[0]==2&&skip.model.solved(),"developer skip completes once");
    PresentPuzzlePanel wrapper=new PresentPuzzlePanel("PZ010",()->{},()->{});ok(wrapper.getComponent(0) instanceof ModuleAssemblyPanel,"existing content entry delegates to approved panel");
   }
   var before=state.snapshot();PuzzleRunner cancelled=new PuzzleRunner(Map.of("present",step->{ModuleAssemblyPanel p=new ModuleAssemblyPanel(()->{throw new AssertionError("partial complete");},()->{});VerifyAccident94.paint(p,1000,820);click(p,0);p.cancelSession();return PuzzleResult.CANCELLED;}),()->state,()->true,step->{throw new AssertionError("cancel saved");});
   ok(cancelled.run("PZ010")==PuzzleRunner.Outcome.CANCELLED&&state.snapshot().equals(before)&&!disk.store(SaveSlots.Slot.AUTO).exists(),"cancel preserves all materials and progress, no save");
   GameState expected=GameState.restore(ProgressCatalog.standard(),before);expected.complete("PZ010");
   PuzzleRunner runner=new PuzzleRunner(Map.of("present",step->{int[] done={0};ModuleAssemblyPanel p=new ModuleAssemblyPanel(()->done[0]++,()->{});VerifyAccident94.paint(p,1000,820);p.verify();align(p);ok(done[0]==0&&state.snapshot().equals(before)&&saves.get()==0,"correct placement only has no reward or save");p.action.doClick(0);p.verify();ok(done[0]==1,"confirm callback exactly once");return PuzzleResult.from(done[0]==1);}),()->state,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(state.snapshot(),true));});
   ok(runner.run("PZ010")==PuzzleRunner.Outcome.COMPLETED&&state.snapshot().equals(expected.snapshot()),"exact existing completion effects");
   ok(state.done("PZ010")&&state.has("DEVICE")&&!state.has("CASE")&&!state.has("CELL")&&!state.has("MODULE")&&state.available("PZ011")&&state.has("USB")&&state.has("FAULT_REPORT"),"original consumption, device reward, next step and retained items");
   runner.run("PZ010");ok(saves.get()==1,"single autosave");
  }catch(Exception e){throw new RuntimeException(e);}});
  pending.get().get(5,TimeUnit.SECONDS);queue.close();
  GameState loaded=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();ok(loaded.snapshot().equals(state.snapshot())&&loaded.available("PZ011")&&loaded.has("DEVICE")&&!loaded.has("MODULE"),"reload preserves completion, consumed items and next step");
  PuzzleRunner already=new PuzzleRunner(Map.of("present",step->{throw new AssertionError("completed puzzle reopened");}),()->loaded,()->true,step->{throw new AssertionError("duplicate save");});ok(already.run("PZ010")==PuzzleRunner.Outcome.IGNORED,"completed saved progress not reset");
  System.out.println("PASS "+checks+" unchanged model, click/keyboard-button actions, reset, layout, one-shot lifecycle, original reward/consumption, autosave and reload checks; no game windows.");
 }
}
