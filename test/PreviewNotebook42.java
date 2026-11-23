package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;import java.awt.event.ActionEvent;import java.nio.file.*;import javax.imageio.ImageIO;import javax.swing.*;import timeescape.core.GameState;
public class PreviewNotebook42 {
 static int frame;
 static final Path OUT=Path.of("outputs/v42-notebook");
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container n)layout(n);}
 static void shot(GameHud h,String name)throws Exception{for(int i=0;i<3;i++)layout(h);BufferedImage im=new BufferedImage(h.getWidth(),h.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);h.printAll(g);g.dispose();ImageIO.write(im,"png",OUT.resolve(name+".png").toFile());}
 static void frames(GameHud h,int n)throws Exception{for(int i=0;i<n;i++)shot(h,"frames/"+String.format("%04d",frame++));}
 public static void main(String[]args)throws Exception{Files.createDirectories(OUT.resolve("frames"));SwingUtilities.invokeAndWait(()->{try{
  GameFonts.installDefaults();GameState s=new GameState();s.complete("PZ004");for(String id:new String[]{"USB","CASE","CELL","MODULE"})s.collect(id);
  GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.setPowered(true);var finish=GameScenePanel.class.getDeclaredMethod("finishLighting");finish.setAccessible(true);finish.invoke(scene);
  MissionPanel missions=new MissionPanel();missions.refresh(s);InventoryPanel inv=new InventoryPanel(id->{});inv.refresh(s,false);
  GameHud h=new GameHud(scene,missions,inv,()->{},()->{},()->{});h.setLocationTitle("현재 · 연구실");h.setSize(1440,810);shot(h,"00-default");h.openNotebook("records");shot(h,"01-records");
  frames(h,25);h.openNotebook("inventory");h.notebook.turnTimer.stop();if(!h.notebook.isTurning())throw new AssertionError("turn not started");for(int i=0;i<=14;i++){h.notebook.setTurnProgress(i/14f);frames(h,1);}shot(h,"02-inventory");frames(h,35);
  h.openNotebook("records");h.notebook.turnTimer.stop();for(int i=0;i<=14;i++){h.notebook.setTurnProgress(i/14f);frames(h,1);}frames(h,20);
  if(h.notebook.isTurning()||h.notebook.turnTimer.isRunning())throw new AssertionError("turn cleanup");
  h.openNotebook("inventory");h.openNotebook("records");h.closeNotebook();if(h.notebook.isTurning()||h.notebook.turnTimer.isRunning())throw new AssertionError("close cleanup");
  h.openNotebook("inventory");h.notebook.finishTurn();h.setSize(1020,650);shot(h,"03-minimum");
  s.complete("PZ006");s.complete("PZ001");s.complete("PZ007");s.complete("PZ008");s.complete("PZ010");s.complete("PZ011");s.moveToPast();s.collectSparePart();inv.refresh(s,false);h.setSize(1440,810);h.setLocationTitle("과거 · 연구실");scene.setEra(s.era());shot(h,"04-device-spare");
  s.repairWatch();s.moveToFuture();s.collectCorePart();inv.refresh(s,false);h.setLocationTitle("미래 · 연구실");scene.setEra(s.era());shot(h,"05-core");
  h.closeNotebook();System.out.println("PASS: actual HUD snapshots, 110 deterministic page-turn frames, forward/reverse/rapid-switch/close lifecycle; no JFrame or game main.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
