package timeescape.ui;
import java.awt.image.BufferedImage;
import timeescape.system.MenuImageAssets;
public class VerifyMenuRainParity72 {
 public static void main(String[]args)throws Exception{
  var old=new LegacyMenuAtmosphere(new MenuImageAssets());var field=LegacyMenuAtmosphere.class.getDeclaredField("textureRain");field.setAccessible(true);
  var baseline=(LegacyTextureRain)field.get(old);var current=new TextureRain();
  for(double t:new double[]{0,10.5,12.1,24,120,3600}){
   var a=new BufferedImage(1600,400,2);var b=new BufferedImage(1600,400,2);var ga=a.createGraphics();var gb=b.createGraphics();baseline.draw(ga,t);current.draw(gb,t);ga.dispose();gb.dispose();
   if(!java.util.Arrays.equals(a.getRGB(0,0,1600,400,null,0,1600),b.getRGB(0,0,1600,400,null,0,1600)))throw new AssertionError("Menu changed at "+t);
  }
  var weather=new LabWindowWeather();int[][] bounds={{781,967},{996,1213},{1248,1464}};boolean[] active=new boolean[3];
  for(int step=0;step<24;step++){
   var im=new BufferedImage(1920,1080,2);var g=im.createGraphics();weather.draw(g,10.5+step*.25,-100,0);g.dispose();
   for(int bay=0;bay<3;bay++)for(int y=145;y<495;y++)for(int x=bounds[bay][0];x<bounds[bay][1];x++)if((im.getRGB((int)(x*1920./1672),(int)(y*1080./941))>>>24)>0)active[bay]=true;
  }
  for(boolean value:active)if(!value)throw new AssertionError("Inactive window");
  System.out.println("PASS: menu pixel-exact at 6 times including one hour; rain active in all 3 lab windows.");
 }
}