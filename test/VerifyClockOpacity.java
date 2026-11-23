package timeescape.ui;
import timeescape.system.MenuImageAssets;import java.awt.image.BufferedImage;import java.nio.file.*;import javax.imageio.ImageIO;
public class VerifyClockOpacity {
 public static void main(String[] args)throws Exception{
  var scene=new MenuAtmosphere(new MenuImageAssets());
  BufferedImage[] frames=new BufferedImage[2];Files.createDirectories(Path.of("outputs/v39-clock-check"));
  for(int i=0;i<2;i++){
   var im=new BufferedImage(1600,900,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();scene.draw(g,0,-100,i==0?-1:4.2,0);g.dispose();frames[i]=im;
   ImageIO.write(im,"png",Path.of("outputs/v39-clock-check/"+(i==0?"lit":"unlit")+".png").toFile());
  }
  for(int yy:new int[]{330,350,370,390}){
   int x=(int)Math.round(800+(1263-800)*1.018),y=(int)Math.round(450+(yy-450)*1.018);
   int lit=frames[0].getRGB(x,y),unlit=frames[1].getRGB(x,y);
   if(lit!=unlit || (unlit&255)>30 || (unlit>>16&255)>30)throw new AssertionError("Translucent hand at "+yy);
  }
  System.out.println("PASS opaque hand interior at four positions, identical lit/unlit; no game window/audio.");
 }
}
