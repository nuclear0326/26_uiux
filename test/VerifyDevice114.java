package timeescape.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.model.Era;

public final class VerifyDevice114 {
 static int checks;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static GameState copy(GameState s){return GameState.restore(s.catalog(),s.snapshot());}
 static GameState ready(){GameState s=GameState.newGame();s.takePrototype();s.usePrototype();for(String id:new String[]{"PZ004","ACT_GET_CIPHER","PZ006","PZ001"})s.complete(id);s.collect("USB");s.complete("PZ007");s.complete("PZ021");s.complete("PZ008");for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);s.complete("PZ010");s.complete("PZ011");return s;}
 static java.util.List<GameState> states(){
  var out=new ArrayList<GameState>();GameState s=GameState.newGame();out.add(copy(s));s.takePrototype();out.add(copy(s));s.usePrototype();out.add(copy(s));s.complete("PZ004");out.add(copy(s));
  for(String id:new String[]{"ACT_GET_CIPHER","PZ006","PZ001"}){s.complete(id);out.add(copy(s));}s.collect("USB");s.complete("PZ007");out.add(copy(s));s.complete("PZ021");out.add(copy(s));s.complete("PZ008");out.add(copy(s));for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);s.complete("PZ010");out.add(copy(s));s.complete("PZ011");out.add(copy(s));
  s.moveToPast();out.add(copy(s));s.collectSparePart();out.add(copy(s));s.repairWatch();out.add(copy(s));s.moveToFuture();out.add(copy(s));s.collectCorePart();out.add(copy(s));s.installCorePart();out.add(copy(s));s.escape();out.add(copy(s));out.add(new GameState());
  var legacy=new GameState.Snapshot(2,"past_1",Set.of("INTRO_START","TUT001","ACT_FIRST_TRAVEL"),Set.of("DEVICE"),Set.of("intro_power_on","device_broken"),Set.of("present_1","past_1"),Set.of("DEVICE"));out.add(GameState.restore(ProgressCatalog.standard(),legacy));return out;
 }
 static void parity(){for(GameState input:states())for(boolean selected:new boolean[]{false,true}){
  GameState a=copy(input),b=copy(input);var prior=new LegacyDevice113(a,selected);int[] calls={0,0};var next=new DeviceActions(b,selected,()->calls[0]++,()->calls[1]++,null);
  ok(prior.isActionEnabled()==next.isActionEnabled(),"action gates equal");ok(prior.actionName().equals(next.actionName()),"action names equal");ok(prior.describe().equals(next.describe()),"descriptions equal");
  boolean enabled=prior.isActionEnabled();if(enabled)prior.execute();next.activate();ok(a.snapshot().equals(b.snapshot()),"same state transition");ok(calls[0]==(enabled?1:0)&&calls[1]==(enabled?1:0),"one close and changed callback only on valid action");var snapshot=b.snapshot();next.activate();ok(b.snapshot().equals(snapshot)&&calls[1]==(enabled?1:0),"double activation suppressed");
  GameState cancel=copy(input);DeviceActions c=new DeviceActions(cancel,selected,()->{throw new AssertionError();},()->{throw new AssertionError();},null);c.cancel();c.activate();ok(cancel.snapshot().equals(input.snapshot()),"cancel then late activation has no mutation");
 }}
 static void press(VerifyInteractions97.Host h,ItemUseHandler items)throws Exception{
  items.use("DEVICE");int saved=h.saves;int[] closed={0};DeviceActions a=new DeviceActions(h.deviceState,h.deviceSelection,()->closed[0]++,h.changed,h.intro);
  ok(a.isActionEnabled(),"real handler action allowed");SwingUtilities.invokeAndWait(a::activate);SwingUtilities.invokeAndWait(a::activate);
  ok(closed[0]==1&&h.saves==saved+1,"real handler one change and autosave");h.roundTrip();
 }
 static void integration()throws Exception{
  try(var h=new VerifyInteractions97.Host()){
   var items=new ItemUseHandler(h);h.current.takePrototype();items.use("DEVICE");int n=h.saves;int[] closed={0};DeviceActions intro=new DeviceActions(h.current,false,()->closed[0]++,h.changed,h.intro);intro.activate();intro.activate();
   ok(closed[0]==1&&!h.current.prototypeFailed()&&h.saves==n,"intro waits for unchanged failure sequence");h.arrival.run();ok(h.current.prototypeFailed()&&h.current.era()==Era.PRESENT&&h.saves==n+1,"intro failure then autosave once");h.roundTrip();
   items.use("DEVICE");n=h.saves;var locked=new DeviceActions(h.current,false,()->{throw new AssertionError();},h.changed,null);locked.activate();ok(h.saves==n,"locked action no save");
   h.current=ready();press(h,items);ok(h.current.era()==Era.PAST,"past destination restored from disk");h.current.collectSparePart();items.use("SPARE");press(h,items);ok(!h.current.hasSparePart()&&h.current.isWatchRepaired()&&h.current.canEnter("future_1"),"part consumption and unlock persisted");
   press(h,items);ok(h.current.era()==Era.FUTURE,"future travel persisted");h.current.collectCorePart();press(h,items);ok(h.current.isCoreInstalled()&&!h.current.hasCorePart(),"core consumption persisted");press(h,items);ok(h.current.isEscaped()&&h.endings==1,"ending and save order retained");
  }
 }
 static void layout(Container c){c.doLayout();for(Component a:c.getComponents())if(a instanceof Container cc)layout(cc);}
 static BufferedImage room(GameState s,int w,int h,boolean notebook){
  GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.restorePowered(s.powered());scene.setProgressHints(s);InventoryPanel inv=new InventoryPanel(id->{});inv.refresh(s,false);
  GameHud hud=new GameHud(scene,new MissionPanel(),inv,()->{},()->{},()->{});hud.setLocation(s.era()==Era.PAST?"과거":s.era()==Era.FUTURE?"미래":"현재","연구실");hud.setSize(w,h);layout(hud);layout(hud);
  if(notebook){hud.openNotebook("inventory");layout(hud);}
  var image=DevicePanel.capture(hud);if(notebook){hud.closeNotebook();var clean=DevicePanel.capture(hud);for(int y=65;y<h;y++)for(int x=0;x<w;x++)if(clean.getRGB(x,y)!=image.getRGB(x,y))throw new AssertionError("snapshot excludes notebook");checks++;}return image;
 }
 static void visuals(Path out)throws Exception{
  GameFonts.installDefaults();int stateIndex=0;
  BufferedImage blank=new BufferedImage(900,600,BufferedImage.TYPE_INT_RGB);
  for(GameState s:states()){
   var a=new DeviceActions(s,true,()->{},()->{},null);for(int[] size:new int[][]{{1440,875},{900,600},{600,500}}){var p=new DevicePanel(blank,65,a,()->{});p.setSize(size[0],size[1]);layout(p);ok(p.textFits(),"text inside panel state "+stateIndex+" width "+size[0]);ok(p.panel.contains(p.action.getBounds())&&p.panel.contains(p.close.getBounds()),"actions inside panel");}
   stateIndex++;
  }
  var all=states();int[] indexes={1,2,11,13,14,16,17};
  for(int i=0;i<indexes.length;i++){
   GameState s=all.get(indexes[i]);var snap=s.snapshot();int[] counts={0};DeviceActions a=new DeviceActions(s,true,()->counts[0]++,()->counts[0]++,null);
   var p=new DevicePanel(room(s,1440,875,true),65,a,()->{a.cancel();counts[0]++;});p.setSize(1440,875);layout(p);BufferedImage im=new BufferedImage(1440,875,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();p.printAll(g);g.dispose();ImageIO.write(im,"png",out.resolve(String.format("%02d-device.png",i+1)).toFile());
   ok(s.snapshot().equals(snap)&&counts[0]==0,"render has no action or save");p.getActionMap().get("close").actionPerformed(new ActionEvent(p,0,""));p.action.doClick(0);ok(counts[0]==1&&s.snapshot().equals(snap),"Esc blocks stale action");
   for(boolean x:new boolean[]{false,true}){DeviceActions cancelled=new DeviceActions(s,true,()->{throw new AssertionError();},()->{throw new AssertionError();},null);int[] closes={0};var panel=new DevicePanel(blank,65,cancelled,()->{cancelled.cancel();closes[0]++;});(x?panel.dismiss:panel.close).doClick(0);panel.action.doClick(0);ok(closes[0]==1&&s.snapshot().equals(snap),"X and close never mutate or save");}
  }
 }
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"headless only");parity();integration();Path out=timeescape.testutil.ArtifactPaths.output("v114-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{visuals(out);}catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" prior-rule parity, real item callbacks, single autosave/load, tutorial, parts, travel, ending, cancellation and responsive layout checks; no game windows.");
 }
}
