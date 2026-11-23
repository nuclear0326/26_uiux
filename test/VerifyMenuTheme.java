import timeescape.ui.MainMenuPanel;
import java.awt.*;import java.awt.image.BufferedImage;import java.nio.file.*;import javax.imageio.ImageIO;import javax.swing.*;
public class VerifyMenuTheme {
 static int checks;static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static java.lang.reflect.Field f(String n)throws Exception{var f=MainMenuPanel.class.getDeclaredField(n);f.setAccessible(true);return f;}
 static void call(MainMenuPanel p,String name,Class<?> type,Object value)throws Exception{var m=MainMenuPanel.class.getDeclaredMethod(name,type);m.setAccessible(true);m.invoke(p,value);}
 static void render(MainMenuPanel p,String name)throws Exception{var im=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();p.paint(g);g.dispose();ImageIO.write(im,"png",Path.of("outputs/v39-preview/"+name+".png").toFile());}
 public static void main(String[] args)throws Exception{
 Files.createDirectories(Path.of("outputs/v39-preview/idle"));Files.createDirectories(Path.of("outputs/v39-preview/start"));
 SwingUtilities.invokeAndWait(()->{try{
  int[] count=new int[4];var p=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){count[0]++;}public void continueGame(){count[1]++;}public void showSettings(){count[2]++;}public void exitGame(){count[3]++;}});p.setSize(1440,810);
  check(!((Timer)f("refreshTimer").get(p)).isRunning());
  call(p,"activate",int.class,1);check(count[1]==0);
  p.setContinueAvailable(true);call(p,"activate",int.class,1);check(count[1]==1);p.setContinueAvailable(false);
  call(p,"activate",int.class,2);call(p,"activate",int.class,3);check(count[2]==1&&count[3]==1);
  var hit=MainMenuPanel.class.getDeclaredMethod("buttonAt",Point.class);hit.setAccessible(true);
  check((int)hit.invoke(p,new Point(120,300))==0);check((int)hit.invoke(p,new Point(120,385))==-1);
  p.setSize(1200,900);check((int)hit.invoke(p,new Point(100,360))==0);p.setSize(1440,810);
  render(p,"main-menu");
  // Sample ambient visuals over 5 seconds, including a normally infrequent storm.
  f("elapsed").setDouble(p,10);
  for(int i=0;i<100;i++){call(p,"advance",double.class,.05);}
  check(f("lightningAt").getDouble(p)>10);check(f("thunderDue").getDouble(p)<0);
  call(p,"activate",int.class,0);call(p,"activate",int.class,0);check(count[0]==0);
  for(int i=0;i<162;i++){call(p,"advance",double.class,.05); if(i==98)check(!f("glassPlayed").getBoolean(p)); if(i==100)check(f("glassPlayed").getBoolean(p));}
  call(p,"advance",double.class,.05);check(count[0]==1);call(p,"advance",double.class,.1);check(count[0]==1);
  call(p,"activate",int.class,0);p.setVisible(false);check(f("pending").get(p)==null);check(!((Timer)f("refreshTimer").get(p)).isRunning());
  p.setTransitionAnimation(false);call(p,"activate",int.class,0);check(count[0]==2);
  System.out.println("PASS "+checks+" menu checks; ambient/start snapshots rendered headlessly with no audio playback.");
 }catch(Exception ex){throw new RuntimeException(ex);}});
 }
}
