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
import timeescape.puzzle.present.TimeCoordinatesPuzzle;
import timeescape.system.*;

public final class VerifyCoordinates112 {
 static int checks;
 static void ok(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 static int[] values(TimeCoordinatesPanel p){return new int[]{p.model.value(0),p.model.value(1),p.model.value(2)};}
 static void mouse(JButton b,int type,int button,Point pt){b.dispatchEvent(new MouseEvent(b,type,0,button==1&&type==MouseEvent.MOUSE_PRESSED?MouseEvent.BUTTON1_DOWN_MASK:0,pt.x,pt.y,1,false,button));}
 static void click(TimeCoordinatesPanel p,int i){JButton b=p.board.rings[i];Point pt=new Point(b.getWidth()/2,b.getHeight()/2);mouse(b,MouseEvent.MOUSE_PRESSED,1,pt);mouse(b,MouseEvent.MOUSE_RELEASED,1,pt);}
 static void alignFresh(TimeCoordinatesPanel p){p.reset.doClick(0);click(p,0);click(p,0);click(p,2);}
 static void image(TimeCoordinatesPanel p,int w,int h,Path path)throws Exception{var im=VerifyAccident94.paint(p,w,h);VerifyLogin102.bounds(p);VerifyPuzzleLayout106.textBounds(p);for(JLabel label:new JLabel[]{p.status,p.link})ok(label.getFontMetrics(label.getFont()).stringWidth(label.getText())<=label.getWidth(),"status and linkage fit");ImageIO.write(im,"png",path.toFile());}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"offscreen only");Path out=timeescape.testutil.ArtifactPaths.output("v112-preview");Files.createDirectories(out);
  GameState state=VerifyModule111.ready();state.complete("PZ010");SaveSlots disk=new SaveSlots(Files.createTempDirectory("coordinates112-save"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);AtomicInteger saves=new AtomicInteger();AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   for(int code=0;code<64;code++)for(int next=0;next<3;next++){
    int[] done={0};TimeCoordinatesPanel p=new TimeCoordinatesPanel(()->done[0]++,()->{});TimeCoordinatesPuzzle ref=new TimeCoordinatesPuzzle();int digits=code;
    for(int i=0;i<3;i++){int n=digits%4;digits/=4;for(int k=0;k<n;k++){p.board.rings[i].doClick(0);ref.turn(i);}}
    int[] before=values(p);p.board.rings[next].doClick(0);ref.turn(next);
    for(int i=0;i<3;i++){ok(p.model.value(i)==ref.value(i),"unchanged reference rules");ok(p.model.value(i)==(before[i]+(i==next||i==(next+1)%3?1:0))%4,"only selected and cyclic next rotate");}
    ok(done[0]==0,"no completion on rotation");p.verify();p.verify();ok(done[0]==(ref.solved()?1:0),"same confirmation result exactly once");
    if(!ref.solved()){p.developerSkip();ok(done[0]==1&&p.model.solved(),"bounded skip solves every reachable state");p.developerSkip();ok(done[0]==1,"skip cannot complete twice");}
   }
   for(int[] size:new int[][]{{1000,820},{900,760},{900,680}}){
    String suffix=size[0]+"x"+size[1];int[] done={0},closed={0};TimeCoordinatesPanel p=new TimeCoordinatesPanel(()->done[0]++,()->closed[0]++);
    image(p,size[0],size[1],out.resolve("01-initial-"+suffix+".png"));int[] initial=values(p);
    for(int i=0;i<3;i++){
     JButton ring=p.board.rings[i];Point center=new Point(ring.getX()+ring.getWidth()/2,ring.getY()+ring.getHeight()/2);ok(p.board.findComponentAt(center)==ring,"scaled click region");
     mouse(ring,MouseEvent.MOUSE_ENTERED,0,new Point(3,3));ok(p.selected==i&&Arrays.equals(initial,values(p)),"hover selects without rotating");ok(p.link.getText().contains((char)('A'+i)+" + "+(char)('A'+(i+1)%3)),"link label reflects cyclic pair");
     for(var f:ring.getFocusListeners())f.focusGained(new FocusEvent(ring,FocusEvent.FOCUS_GAINED));ok(p.selected==i&&Arrays.equals(initial,values(p)),"keyboard focus only selects");
     for(int n=0;n<4;n++)click(p,i);ok(Arrays.equals(initial,values(p)),"four clicks wrap both linked rings");
    }
    JButton first=p.board.rings[0];Point middle=new Point(first.getWidth()/2,first.getHeight()/2);mouse(first,MouseEvent.MOUSE_PRESSED,3,middle);mouse(first,MouseEvent.MOUSE_RELEASED,3,middle);ok(Arrays.equals(initial,values(p)),"right click ignored");
    mouse(first,MouseEvent.MOUSE_PRESSED,1,middle);mouse(first,MouseEvent.MOUSE_EXITED,1,new Point(-1,-1));mouse(first,MouseEvent.MOUSE_RELEASED,1,new Point(-1,-1));ok(Arrays.equals(initial,values(p)),"release outside cancels");
    // Invoke the actual Space press/release actions inherited from Swing's button bindings.
    Object press=first.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("pressed SPACE"));Object release=first.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("released SPACE"));
    ok(press!=null&&release!=null,"Space bindings retained");first.getActionMap().get(press).actionPerformed(new ActionEvent(first,0,""));first.getActionMap().get(release).actionPerformed(new ActionEvent(first,0,""));ok(Arrays.equals(values(p),new int[]{2,3,3}),"Space uses same linked rotation");
    image(p,size[0],size[1],out.resolve("02-after-a-"+suffix+".png"));
    p.reset.doClick(0);click(p,2);ok(Arrays.equals(values(p),new int[]{2,2,0})&&p.selected==2&&p.link.getText().contains("C + A"),"C wraps to A");image(p,size[0],size[1],out.resolve("03-after-c-"+suffix+".png"));
    p.verify();ok(done[0]==0&&p.status.getText().contains("확인"),"partial alignment not complete");
    p.reset.doClick(0);ok(Arrays.equals(initial,values(p))&&p.selected==0,"reset restores original start and selection");alignFresh(p);ok(p.model.solved()&&done[0]==0,"aligned state waits for confirmation");image(p,size[0],size[1],out.resolve("04-aligned-"+suffix+".png"));
    p.action.doClick(0);p.verify();p.reset.doClick(0);p.developerSkip();click(p,1);p.turn(2);ok(done[0]==1&&p.model.solved(),"completion once, stale input blocked");image(p,size[0],size[1],out.resolve("05-complete-"+suffix+".png"));
    TimeCoordinatesPanel cancelled=new TimeCoordinatesPanel(()->done[0]++,()->closed[0]++);VerifyAccident94.paint(cancelled,size[0],size[1]);cancelled.cancelSession();click(cancelled,0);cancelled.select(2);cancelled.verify();cancelled.developerSkip();cancelled.reset.doClick(0);ok(done[0]==1&&Arrays.equals(initial,values(cancelled)),"cancelled lifecycle blocks input and skip");
    TimeCoordinatesPanel esc=new TimeCoordinatesPanel(()->done[0]++,()->closed[0]++);esc.getActionMap().get("close-coordinates").actionPerformed(null);esc.getActionMap().get("close-coordinates").actionPerformed(null);esc.developerSkip();ok(done[0]==1&&closed[0]==1,"Esc closes once without completion");
    PresentPuzzlePanel wrapper=new PresentPuzzlePanel("PZ011",()->{},()->{});ok(wrapper.getComponent(0) instanceof TimeCoordinatesPanel,"existing entry delegates to new UI");
   }
   var before=state.snapshot();ok(!state.canDepart()&&!state.roomUnlocked("past_1"),"past locked before coordinates");
   PuzzleRunner cancelRunner=new PuzzleRunner(Map.of("present",step->{TimeCoordinatesPanel p=new TimeCoordinatesPanel(()->{throw new AssertionError("partial complete");},()->{});VerifyAccident94.paint(p,1000,820);click(p,2);p.cancelSession();return PuzzleResult.CANCELLED;}),()->state,()->true,step->{throw new AssertionError("cancel saved");});
   ok(cancelRunner.run("PZ011")==PuzzleRunner.Outcome.CANCELLED&&state.snapshot().equals(before)&&!disk.store(SaveSlots.Slot.AUTO).exists(),"partial exit leaves inventory gate and save unchanged");
   GameState expected=GameState.restore(ProgressCatalog.standard(),before);expected.complete("PZ011");
   PuzzleRunner runner=new PuzzleRunner(Map.of("present",step->{int[] done={0};TimeCoordinatesPanel p=new TimeCoordinatesPanel(()->done[0]++,()->{});VerifyAccident94.paint(p,1000,820);p.verify();alignFresh(p);ok(done[0]==0&&state.snapshot().equals(before)&&saves.get()==0,"alignment alone gives no gate or save");p.action.doClick(0);p.verify();ok(done[0]==1,"confirmed once");return PuzzleResult.from(done[0]==1);}),()->state,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(state.snapshot(),true));});
   ok(runner.run("PZ011")==PuzzleRunner.Outcome.COMPLETED&&state.snapshot().equals(expected.snapshot()),"exact prior completion effects");ok(state.done("PZ011")&&state.canDepart()&&state.roomUnlocked("past_1")&&state.snapshot().items().equals(before.items())&&state.snapshot().roomId().equals(before.roomId()),"past unlock, items kept and no automatic travel");runner.run("PZ011");ok(saves.get()==1,"one autosave");
  }catch(Exception e){throw new RuntimeException(e);}});
  pending.get().get(5,TimeUnit.SECONDS);queue.close();GameState loaded=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();ok(loaded.snapshot().equals(state.snapshot())&&loaded.canDepart()&&loaded.roomUnlocked("past_1"),"reload retains completion and travel access");
  PuzzleRunner already=new PuzzleRunner(Map.of("present",step->{throw new AssertionError("reopened");}),()->loaded,()->true,step->{throw new AssertionError("saved twice");});ok(already.run("PZ011")==PuzzleRunner.Outcome.IGNORED,"completed save not reset");loaded.moveToPast();ok(loaded.snapshot().roomId().equals("past_1"),"original next travel remains available");
  System.out.println("PASS "+checks+" linked model/input/selection/keyboard/layout/lifecycle, original past gate, autosave and reload checks; no game windows.");
 }
}
