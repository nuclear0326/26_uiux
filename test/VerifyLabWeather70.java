package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.swing.*;
import timeescape.model.Era;
import timeescape.system.ImageAssets;
public final class VerifyLabWeather70 {
 private static int checks;
 private static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
 private static BufferedImage render(GameScenePanel p,int w,int h)throws Exception{
  p.setSize(w,h);BufferedImage im=new BufferedImage(w,h,2);Graphics2D g=im.createGraphics();p.printAll(g);g.dispose();return im;
 }
 public static void main(String[] args)throws Exception{
  File out=new File(args[0]);out.mkdirs();
  LabWindowWeather weather=new LabWindowWeather();
  check(!weather.contains(980,300),"major frame excluded");
  check(!weather.contains(835,300),"thin frame excluded");
  check(!weather.contains(815,450),"monitor excluded");
  check(!weather.contains(1440,450),"plant excluded");
  check(weather.contains(1150,280),"glass included");
  BufferedImage layer=new BufferedImage(1920,1080,2);Graphics2D g=layer.createGraphics();weather.draw(g,12.1,.1);g.dispose();
  int visible=0,leaked=0;
  for(int y=0;y<1080;y++)for(int x=0;x<1920;x++)if((layer.getRGB(x,y)>>>24)>0){visible++;if(!weather.contains((x+.5)*1672/1920.,(y+.5)*941/1080.)){
    // Raster clipping can touch the boundary pixel, but never outside a 2-pixel margin.
    boolean near=false;for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)near|=weather.contains((x+dx)*1672/1920.,(y+dy)*941/1080.);
    if(!near)leaked++;
  }}
  check(visible>10000,"lightning visible");check(leaked==0,"no room/frame leakage");
  SwingUtilities.invokeAndWait(()->{try{
   GameScenePanel p=new GameScenePanel(id->{});p.setEra(Era.PRESENT);p.restorePowered(true);
   Field seconds=GameScenePanel.class.getDeclaredField("weatherSeconds");seconds.setAccessible(true);
   Field flash=GameScenePanel.class.getDeclaredField("lightningAt");flash.setAccessible(true);flash.setDouble(p,12);
   Field timer=GameScenePanel.class.getDeclaredField("weatherTimer");timer.setAccessible(true);
   check(!((Timer)timer.get(p)).isRunning(),"hidden panel timer stopped");
   for(int i=0;i<96;i++){
    seconds.setDouble(p,10.5+i/24.);ImageIO.write(render(p,960,540),"png",new File(out,String.format("frame_%03d.png",i)));
   }
   seconds.setDouble(p,12.1);ImageIO.write(render(p,1440,900),"png",new File(out,"present_16x10.png"));
   ImageIO.write(render(p,1680,720),"png",new File(out,"present_ultrawide.png"));
   p.restorePowered(false);ImageIO.write(render(p,1280,720),"png",new File(out,"present_power_off.png"));
   for(Era era:new Era[]{Era.PAST,Era.FUTURE}){
    p.setEra(era);seconds.setDouble(p,1);BufferedImage a=render(p,640,360);seconds.setDouble(p,12.1);BufferedImage b=render(p,640,360);
    boolean equal=true;for(int y=0;y<360;y++)for(int x=0;x<640;x++)equal&=a.getRGB(x,y)==b.getRGB(x,y);
    check(equal,"other era unchanged: "+era);check(!((Timer)timer.get(p)).isRunning(),"other era timer stopped");
   }
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" checks; headless JPanel only; no game windows or audio.");
 }
}
