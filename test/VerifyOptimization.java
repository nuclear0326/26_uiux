import timeescape.system.ScaledBackground;
import timeescape.system.ImageAssets;
import timeescape.model.Era;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
public class VerifyOptimization {
 public static void main(String[] args) throws Exception {
  var cache=new ScaledBackground();
  var source=ImageAssets.background(Era.PRESENT);
  var a=cache.get(source,1120,630);
  if(cache.get(source,1120,630)!=a)throw new AssertionError("Cache not reused");
  var expected=new BufferedImage(1120,630,BufferedImage.TYPE_INT_RGB);
  var g=expected.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(source,0,0,1120,630,null);g.dispose();
  for(int y=0;y<630;y++)for(int x=0;x<1120;x++)if(a.getRGB(x,y)!=expected.getRGB(x,y))throw new AssertionError("Pixels changed");
  if(cache.get(source,800,450)==a)throw new AssertionError("Resize invalidation");
  var b=cache.get(source,800,450);
  if(cache.get(ImageAssets.background(Era.PAST),800,450)==b)throw new AssertionError("Era invalidation");
  cache.clear();if(cache.get(source,0,0)!=null)throw new AssertionError("Zero size");
  var target=new BufferedImage(1120,630,BufferedImage.TYPE_INT_RGB);
  var paint=target.createGraphics();paint.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
  for(int i=0;i<30;i++){paint.drawImage(source,0,0,1120,630,null);paint.drawImage(cache.get(source,1120,630),0,0,null);}
  long start=System.nanoTime();
  for(int i=0;i<150;i++)paint.drawImage(source,0,0,1120,630,null);
  long original=System.nanoTime()-start;start=System.nanoTime();
  for(int i=0;i<150;i++)paint.drawImage(cache.get(source,1120,630),0,0,null);
  long cached=System.nanoTime()-start;paint.dispose();
  System.out.printf("150 background draws only: original %.1f ms; cached %.1f ms%n",original/1e6,cached/1e6);
  javax.swing.SwingUtilities.invokeAndWait(() -> {
   var menu=new timeescape.ui.MainMenuPanel(new timeescape.ui.MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
   try{var field=menu.getClass().getDeclaredField("refreshTimer");field.setAccessible(true);var timer=(javax.swing.Timer)field.get(menu);
   if(timer.isRunning())throw new AssertionError("Detached menu timer running");
   timer.start();menu.removeNotify();if(timer.isRunning())throw new AssertionError("Removed menu timer running");
   }catch(ReflectiveOperationException e){throw new RuntimeException(e);}
  });
  System.out.println("Cache identity, resize/era invalidation, exact pixels and timer cleanup passed.");
 }
}
