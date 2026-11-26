package timeescape.ui;
import timeescape.core.*;import timeescape.puzzle.*;import timeescape.system.*;
import java.awt.*;import java.awt.image.BufferedImage;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.*;import javax.imageio.ImageIO;
public final class VerifyAccident94 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container cc)layout(cc);}
 static BufferedImage paint(JComponent c,int w,int h){c.setSize(w,h);layout(c);layout(c);BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();c.paint(g);g.dispose();return im;}
 static void order(AccidentLogPuzzle m){for(int i=0;i<6;i++)m.move(m.order().indexOf(i),i);}
 static GameState ready(){GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.complete("PZ001");s.collect("USB");s.complete("PZ007");return s;}
 public static void main(String[] args)throws Exception{
  GameState s=ready();ok(s.available("PZ021")&&!s.available("PZ008"),"USB analysis gates repair");ok(s.nextObjectStep("PRESENT_PC").orElseThrow().id().equals("PZ021"),"PC and USB share analysis ID");
  try{s.complete("PZ008");throw new AssertionError("repair bypass");}catch(IllegalStateException expected){checks++;}
  AccidentLogPuzzle rules=new AccidentLogPuzzle();ok(!rules.verify()&&rules.stage()==1,"wrong order rejected");order(rules);ok(rules.verify()&&rules.stage()==2,"normal order advances only");for(int i:new int[]{0,1,3,4,5}){rules.choose(i);ok(!rules.verify(),"consequence not cause");}rules.choose(2);ok(rules.verify()&&rules.stage()==3,"first anomaly accepted");
  SaveSlots disk=new SaveSlots(Files.createTempDirectory("log94-save"),ProgressCatalog.standard());SaveQueue queue=new SaveQueue(disk);AtomicInteger saves=new AtomicInteger();AtomicReference<CompletableFuture<Void>> pending=new AtomicReference<>();
  var before=s.snapshot();PuzzleRunner cancelled=new PuzzleRunner(Map.of("log_analysis",step->{AccidentLogPuzzle m=new AccidentLogPuzzle();m.skipStage();return PuzzleResult.CANCELLED;}),()->s,()->true,step->{throw new AssertionError("partial completion saved");});cancelled.run("PZ021");ok(s.snapshot().equals(before),"leaving after stage one changes no progress");ok(!disk.store(SaveSlots.Slot.AUTO).exists(),"stage one does not create autosave");
  PuzzleRunner runner=new PuzzleRunner(Map.of("log_analysis",step->{AccidentLogPuzzle m=new AccidentLogPuzzle();order(m);m.verify();ok(!s.done("PZ021")&&!s.has("FAULT_REPORT")&&!s.available("PZ008")&&saves.get()==0,"stage one no reward or save");m.choose(2);m.verify();return m.stage()==3?PuzzleResult.SOLVED:PuzzleResult.CANCELLED;}),()->s,()->true,step->{saves.incrementAndGet();pending.set(queue.saveAuto(s.snapshot(),true));});
  ok(runner.run("PZ021")==PuzzleRunner.Outcome.COMPLETED,"runner commits final");pending.get().get(5,TimeUnit.SECONDS);ok(s.has("USB")&&s.has("FAULT_REPORT")&&s.isItemUnread("FAULT_REPORT")&&s.available("PZ008"),"reward and repair unlock without USB consumption");runner.run("PZ021");ok(saves.get()==1,"autosave once");ok(disk.load(SaveSlots.Slot.AUTO).state().snapshot().equals(s.snapshot()),"autosave roundtrip");
  SaveSlots reopened=new SaveSlots(disk.store(SaveSlots.Slot.AUTO).file().getParent(),ProgressCatalog.standard());
  GameState loaded=reopened.load(SaveSlots.Slot.AUTO).state();
  ok(loaded!=s&&loaded.done("PZ021")&&loaded.has("FAULT_REPORT")&&loaded.has("USB"),"fresh load retains analysis and report");
  ok(loaded.available("PZ008")&&!loaded.missionLocked("M_DEVICE"),"fresh load retains repair unlock");
  loaded.complete("PZ008");ok(loaded.done("PZ008"),"repair can actually start after loading");
  ok(!s.done("PZ008"),"loaded state independent of original");

  s.markItemRead("FAULT_REPORT");queue.saveManual(SaveSlots.Slot.ONE,s.snapshot()).get(5,TimeUnit.SECONDS);ok(!queue.load(SaveSlots.Slot.ONE).get(5,TimeUnit.SECONDS).state().isItemUnread("FAULT_REPORT"),"report read flag persists");GameState manual=queue.load(SaveSlots.Slot.ONE).get(5,TimeUnit.SECONDS).state();ok(manual.done("PZ021")&&manual.has("FAULT_REPORT")&&manual.available("PZ008")&&!manual.missionLocked("M_DEVICE"),"manual load retains analysis report and repair unlock");queue.close();
  // Already-repaired older saves retain access and do not replay new rewards.
  s.complete("PZ008");var snap=s.snapshot();Set<String> oldDone=new HashSet<>(snap.completed());oldDone.remove("PZ021");Set<String> oldItems=new HashSet<>(snap.items());oldItems.remove("FAULT_REPORT");Set<String> oldUnread=new HashSet<>(snap.unreadItems());oldUnread.remove("FAULT_REPORT");GameState legacy=GameState.restore(ProgressCatalog.standard(),new GameState.Snapshot(2,snap.roomId(),oldDone,oldItems,snap.flags(),snap.unlockedRooms(),oldUnread));ok(legacy.done("PZ008")&&!legacy.missionLocked("M_DEVICE")&&!legacy.has("FAULT_REPORT"),"older mid-repair save preserved");
  Path out=timeescape.testutil.ArtifactPaths.output("v94-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{GameFonts.installDefaults();int[] solved={0};AccidentLogPanel panel=new AccidentLogPanel(()->solved[0]++,()->{});
   GameState pre=ready();GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(pre.era());scene.restorePowered(true);scene.setProgressHints(pre);MissionPanel missions=new MissionPanel();missions.refresh(pre);InventoryPanel inv=new InventoryPanel(id->{});inv.refresh(pre,false);GameHud hud=new GameHud(scene,missions,inv,()->{},()->{},()->{});
   for(int stage=1;stage<=2;stage++){BufferedImage frame=paint(hud,1440,960),content=paint(panel,1000,820);Graphics2D g=frame.createGraphics();g.setColor(new Color(0,0,0,145));g.fillRect(0,65,1440,895);g.drawImage(content,220,100,null);g.dispose();ImageIO.write(frame,"png",out.resolve(stage==1?"01-order.png":"02-compare.png").toFile());
    if(stage==1){order(panel.model);panel.action.doClick(0);ok(solved[0]==0&&panel.model.stage()==2,"real panel stage one callback absent");}
   }
   panel.model.choose(3);panel.action.doClick(0);ok(solved[0]==0,"wrong cause cannot solve panel");panel.model.choose(2);panel.action.doClick(0);panel.action.doClick(0);ok(solved[0]==1,"normal panel one-shot");
   AccidentLogPanel skip=new AccidentLogPanel(()->solved[0]++,()->{});skip.developerSkip();ok(solved[0]==1&&skip.model.stage()==2,"cheat first press only next stage");skip.developerSkip();skip.developerSkip();ok(solved[0]==2,"cheat second press completes once");AccidentLogPanel closed=new AccidentLogPanel(()->solved[0]++,()->{});closed.cancelSession();closed.developerSkip();ok(solved[0]==2,"closed panel cannot complete");
   inv.refresh(s,false);hud.openNotebook("inventory");hud.notebook.finishTurn();ImageIO.write(paint(hud,1440,960),"png",out.resolve("03-report.png").toFile());hud.removeNotify();
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" accident analysis, staged completion, reward, persistence and offscreen UI checks; no game launched.");
 }
}
