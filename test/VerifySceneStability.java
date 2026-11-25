package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.swing.*;
import timeescape.system.ImageAssets;
import timeescape.model.Era;
public final class VerifySceneStability {
 static void field(Object o,String n,Object v)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 public static void main(String[] args)throws Exception{
  SwingUtilities.invokeAndWait(()->{try{
   int checks=0;
   for(int[] size:new int[][]{{1484,775},{1280,720},{1024,768},{1920,1014},{2560,1080}}){
    var t=new FirstTravelOverlay();t.setSize(size[0],size[1]);
    var on=ImageAssets.background(Era.PRESENT);var off=ImageAssets.presentOff();field(t,"on",on);field(t,"off",off);
    var scene=new GameScenePanel(id->{});scene.setSize(size[0],size[1]);
    var method=GameScenePanel.class.getDeclaredMethod("displayRect");method.setAccessible(true);
    Rectangle r=(Rectangle)method.invoke(scene);
    for(double time:new double[]{0,.2,4.8,5.47,5.7,5.92,6.3,7.4,7.599}){
     field(t,"seconds",time);
     BufferedImage actual=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB),expected=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);
     var g=actual.createGraphics();t.paintComponent(g);g.dispose();
     g=expected.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(time<.6716?on:off,r.x,r.y,r.width,r.height,null);g.dispose();
     if(!Arrays.equals(actual.getRGB(0,0,size[0],size[1],null,0,size[0]),expected.getRGB(0,0,size[0],size[1],null,0,size[0])))throw new AssertionError("Geometry changed "+size[0]+"x"+size[1]+" at "+time);
     checks++;
    }
   }
   System.out.println("PASS "+checks+" exact pixel comparisons across 5 viewport ratios; no zoom, ghost or crop jump");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}

