import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.ui.GameScenePanel;
import timeescape.model.Era;
public class VerifyWallHints {
 static int count;
 static void check(boolean b){count++;if(!b)throw new AssertionError("check "+count);}
 static void render(GameScenePanel panel,String name)throws Exception{
  panel.setSize(1440,810);var image=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();panel.paint(g);g.dispose();ImageIO.write(image,"png",timeescape.testutil.ArtifactPaths.output("v18-preview/"+name+".png").toFile());
 }
 public static void main(String[] args)throws Exception{
  Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v18-preview"));
  SwingUtilities.invokeAndWait(()->{try{
   var clicks=new java.util.ArrayList<String>();var p=new GameScenePanel(clicks::add);p.setEra(Era.PRESENT);
   var timerField=GameScenePanel.class.getDeclaredField("hoverTimer");timerField.setAccessible(true);Timer timer=(Timer)timerField.get(p);
   var held=GameScenePanel.class.getDeclaredField("revealHeld");held.setAccessible(true);
   p.setSize(1920,1080);p.dispatchEvent(new MouseEvent(p,MouseEvent.MOUSE_CLICKED,0,0,807,366,1,false,MouseEvent.BUTTON1));check(clicks.equals(java.util.List.of("POWER")));
   p.dispatchEvent(new MouseEvent(p,MouseEvent.MOUSE_CLICKED,0,0,448,570,1,false,MouseEvent.BUTTON1));check(clicks.size()==1);
   p.setPowered(true);render(p,"distribution-box");p.setPowered(false);render(p,"distribution-box-dark");
   p.getActionMap().get("revealObjects").actionPerformed(null);check(held.getBoolean(p)&&timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);check(held.getBoolean(p));
   p.setPowered(true);render(p,"particles");
   var phase=GameScenePanel.class.getDeclaredField("particlePhase");phase.setAccessible(true);double before=phase.getDouble(p);
   for(int i=0;i<20;i++)for(ActionListener l:timer.getActionListeners())l.actionPerformed(new ActionEvent(timer,0,""));check(phase.getDouble(p)>before);render(p,"particles-moving");
   p.getActionMap().get("hideObjects").actionPerformed(null); for(int step=0;step<5;step++) for(var l:timer.getActionListeners()) l.actionPerformed(new ActionEvent(timer,0,""));check(!held.getBoolean(p)&&!timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);
   var focus=GameScenePanel.class.getDeclaredField("windowFocusListener");focus.setAccessible(true);((java.beans.PropertyChangeListener)focus.get(p)).propertyChange(new java.beans.PropertyChangeEvent(p,"focusedWindow",null,null));check(!held.getBoolean(p)&&!timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);p.setEra(Era.PAST);check(!held.getBoolean(p)&&!timer.isRunning());
   System.out.println("PASS "+count+" distribution-box and particle lifecycle checks.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
