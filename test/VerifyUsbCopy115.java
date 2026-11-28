package timeescape.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.puzzle.*;
import timeescape.system.*;

public final class VerifyUsbCopy115 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static final BufferedImage BG=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);
 static GameState ready(boolean usb){GameState s=GameState.newGame();s.takePrototype();s.usePrototype();for(String id:new String[]{"PZ004","ACT_GET_CIPHER","PZ006","PZ001"})s.complete(id);if(usb)s.collect("USB");return s;}
 static UsbCopyPanel panel(Runnable closed){return new UsbCopyPanel(BG,65,closed);}
 static void cancel(UsbCopyPanel p,int kind){switch(kind){case 0->p.cancel.doClick(0);case 1->p.dismiss.doClick(0);case 2->p.getActionMap().get("cancel").actionPerformed(new ActionEvent(p,0,""));default->p.cancelSession();}}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"headless only");Path out=timeescape.testutil.ArtifactPaths.output("v115-preview");Files.createDirectories(out);
  GameState state=ready(true);var original=state.snapshot();SaveSlots disk=new SaveSlots(Files.createTempDirectory("usb115-"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();AtomicInteger saves=new AtomicInteger();
  try{
   SwingUtilities.invokeAndWait(()->{try{
    GameFonts.installDefaults();
    for(int kind=0;kind<4;kind++){
     final int mode=kind;int[] closes={0};PuzzleRunner runner=new PuzzleRunner(Map.of("confirm",step->{
      UsbCopyPanel p=panel(()->closes[0]++);ok(p.result()==PuzzleResult.CANCELLED,"default closed state cancels");cancel(p,mode);p.copy.doClick(0);p.cancel.doClick(0);ok(closes[0]==(mode==3?0:1),"cancel closes once and rejects late copy");return p.result();
     }),()->state,()->true,step->{throw new AssertionError("cancel must not complete");});
     ok(runner.run("PZ007")==PuzzleRunner.Outcome.CANCELLED&&state.snapshot().equals(original),"cancel/X/Esc/dispose preserve all progress");
    }
    ok(!disk.store(SaveSlots.Slot.AUTO).exists(),"cancel does not create completion autosave");
    for(boolean chooseCopy:new boolean[]{false,true}){
     int[] closes={0};UsbCopyPanel p=panel(()->closes[0]++);JButton focus=chooseCopy?p.copy:p.cancel;focus.getActionMap().get("activate").actionPerformed(new ActionEvent(focus,0,""));
     ok(p.result()==PuzzleResult.from(chooseCopy)&&closes[0]==1,"Enter activates the selected text button");p.cancelSession();ok(p.result()==PuzzleResult.from(chooseCopy),"dispose preserves chosen result");
    }
    GameState missing=ready(false);PuzzleRunner locked=new PuzzleRunner(Map.of("confirm",step->{throw new AssertionError("missing USB launched");}),()->missing,()->true,step->{throw new AssertionError();});ok(locked.run("PZ007")==PuzzleRunner.Outcome.IGNORED,"missing USB cannot launch");
    GameState early=GameState.newGame();PuzzleRunner premature=new PuzzleRunner(Map.of("confirm",step->{throw new AssertionError();}),()->early,()->true,step->{throw new AssertionError();});ok(premature.run("PZ007")==PuzzleRunner.Outcome.IGNORED,"earlier progress cannot launch");
    for(int[] size:new int[][]{{1440,875},{1020,650},{600,500}}){
     UsbCopyPanel p=panel(()->{});p.setSize(size[0],size[1]);p.doLayout();ok(p.textFits(),"text fits "+size[0]);ok(p.panel.contains(p.copy.getBounds())&&p.panel.contains(p.cancel.getBounds())&&p.panel.contains(p.dismiss.getBounds()),"all controls fit");ok(p.cancel.getX()<p.copy.getX(),"cancel left / copy right");
    }
    for(int[] size:new int[][]{{1440,875},{1020,650}}){
     BufferedImage background=VerifyDevice114.room(state,size[0],size[1],false);UsbCopyPanel p=new UsbCopyPanel(background,65,()->{});p.setSize(size[0],size[1]);p.doLayout();BufferedImage im=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();p.printAll(g);g.dispose();ImageIO.write(im,"png",out.resolve("usb-copy-"+size[0]+".png").toFile());ok(state.snapshot().equals(original),"render has no completion or save");
    }
    // A solved dialog that belongs to a replaced/closed game cannot complete the old session.
    for(boolean replace:new boolean[]{false,true}){
     AtomicReference<GameState> current=new AtomicReference<>(state);AtomicBoolean active=new AtomicBoolean(true);
     PuzzleRunner stale=new PuzzleRunner(Map.of("confirm",step->{UsbCopyPanel p=panel(()->{});p.copy.doClick(0);if(replace)current.set(GameState.newGame());else active.set(false);return p.result();}),current::get,active::get,step->{throw new AssertionError("stale completion");});
     ok(stale.run("PZ007")==PuzzleRunner.Outcome.IGNORED&&state.snapshot().equals(original),"stale confirmation ignored");
    }
    int[] closes={0};PuzzleRunner runner=new PuzzleRunner(Map.of("confirm",step->{
     UsbCopyPanel p=panel(()->closes[0]++);p.copy.doClick(0);p.copy.doClick(0);p.cancel.doClick(0);p.cancelSession();ok(closes[0]==1&&p.result()==PuzzleResult.SOLVED,"copy closes once; late events cannot replace success");ok(!state.done("PZ007"),"panel does not write global progress");return p.result();
    }),()->state,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(state.snapshot(),false));});
    ok(runner.run("PZ007")==PuzzleRunner.Outcome.COMPLETED,"runner commits copy result");
    GameState expected=GameState.restore(state.catalog(),original);expected.complete("PZ007");ok(expected.snapshot().equals(state.snapshot()),"identical to former YES completion");
    ok(state.usbComplete()&&state.has("USB")&&state.available("PZ021")&&!state.has("FAULT_REPORT")&&!state.available("PZ008")&&!state.canCollect("CASE"),"copy retains USB and opens analysis only");
    ok(saves.get()==1&&runner.run("PZ007")==PuzzleRunner.Outcome.IGNORED&&saves.get()==1,"one completion autosave; repeated copy ignored");
   }catch(Exception e){throw new RuntimeException(e);}});
   pending.get().get(5,TimeUnit.SECONDS);GameState loaded=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();
   ok(loaded.snapshot().equals(state.snapshot())&&loaded.usbComplete()&&loaded.available("PZ021")&&!loaded.available("PZ008"),"disk reload keeps copy and analysis gate without early repair unlock");
  }finally{queue.close();}
  System.out.println("PASS "+checks+" copy/cancel/X/Esc/dispose, keyboard, duplicate/stale result, layout, exact completion, autosave and reload checks; no game windows.");
 }
}
