import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import timeescape.ui.GameScenePanel;
import timeescape.model.Era;
public class VerifyShiftContrast {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static java.lang.reflect.Field f(String n)throws Exception{var f=GameScenePanel.class.getDeclaredField(n);f.setAccessible(true);return f;}
 static void tick(Timer t){for(var l:t.getActionListeners())l.actionPerformed(new java.awt.event.ActionEvent(t,0,""));}
 public static void main(String[] args)throws Exception{
  SwingUtilities.invokeAndWait(()->{try{
   var p=new GameScenePanel(id->{});p.setEra(Era.PRESENT);
   Timer timer=(Timer)f("hoverTimer").get(p);
   p.getActionMap().get("revealObjects").actionPerformed(null);
   for(int i=0;i<5;i++)tick(timer);
   check(f("revealContrast").getFloat(p)==1);check(timer.isRunning());
   var image=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1920,1080);
   var paint=GameScenePanel.class.getDeclaredMethod("paintContrast",Graphics2D.class);paint.setAccessible(true);paint.invoke(p,g);g.dispose();
   check((image.getRGB(1000,100)&255)==127);check((image.getRGB(805,365)&255)==255);check((image.getRGB(940,540)&255)==255);
   Object mask=f("contrastMask").get(p);g=image.createGraphics();paint.invoke(p,g);g.dispose();check(mask==f("contrastMask").get(p));
   p.getActionMap().get("hideObjects").actionPerformed(null);check(f("revealContrast").getFloat(p)==1);
   tick(timer);check(f("revealContrast").getFloat(p)>0&&f("revealContrast").getFloat(p)<1);
   for(int i=0;i<4;i++)tick(timer);check(f("revealContrast").getFloat(p)==0);check(!timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);tick(timer);p.getActionMap().get("hideObjects").actionPerformed(null);tick(timer);
   check(f("revealContrast").getFloat(p)==0);check(!timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);tick(timer);
   ((java.beans.PropertyChangeListener)f("windowFocusListener").get(p)).propertyChange(new java.beans.PropertyChangeEvent(p,"focusedWindow",null,null));
   check(f("revealContrast").getFloat(p)==0&&!timer.isRunning());
   p.getActionMap().get("revealObjects").actionPerformed(null);tick(timer);p.setEra(Era.PAST);check(f("revealContrast").getFloat(p)==0&&!timer.isRunning());
   System.out.println("PASS "+checks+" Shift contrast/fade/lifecycle checks; no game window.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
