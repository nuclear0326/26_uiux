package timeescape.ui;
import timeescape.system.MenuImageAssets;import java.awt.image.BufferedImage;
public class VerifyWeather {
 static java.lang.reflect.Field f(String n)throws Exception{var f=MainMenuPanel.class.getDeclaredField(n);f.setAccessible(true);return f;}
 public static void main(String[]args)throws Exception{
  javax.swing.SwingUtilities.invokeAndWait(()->{try{
   var p=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
   var advance=MainMenuPanel.class.getDeclaredMethod("advance",double.class);advance.setAccessible(true);
   f("elapsed").setDouble(p,11.99);advance.invoke(p,.02);
   double lightning=f("lightningAt").getDouble(p),due=f("thunderDue").getDouble(p);
   if(Math.abs(due-lightning-1.2)>1e-9)throw new AssertionError("Thunder delay");
   f("elapsed").setDouble(p,due-.02);advance.invoke(p,.01);
   if(f("thunderDue").getDouble(p)<0)throw new AssertionError("Early thunder");
   advance.invoke(p,.02);if(f("thunderDue").getDouble(p)!=-1)throw new AssertionError("Missing thunder");
   advance.invoke(p,.05);if(f("thunderDue").getDouble(p)!=-1)throw new AssertionError("Repeated thunder");
   p.setVisible(false);
   var scene=new MenuAtmosphere(new MenuImageAssets());var tf=MenuAtmosphere.class.getDeclaredField("textureRain");tf.setAccessible(true);
   var rain=(TextureRain)tf.get(scene);var im=new BufferedImage(1600,900,BufferedImage.TYPE_INT_RGB);
   for(double base:new double[]{0,120,600,3600}){
    boolean wet=false;
    for(int n=0;n<8;n++){var g=im.createGraphics();rain.draw(g,base+n*.2);g.dispose();for(float d:rain.delta)if(Math.abs(d)>.1){wet=true;break;}}
    if(!wet)throw new AssertionError("Rain stopped after "+base);
   }
   if(MenuAtmosphere.flash(-.1)!=0||MenuAtmosphere.flash(.08)<.2||MenuAtmosphere.flash(2)!=0)throw new AssertionError("Lightning envelope");
   System.out.println("PASS lightning/thunder 1.2s boundary and single trigger; rain active through one hour; no audio output/window.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
