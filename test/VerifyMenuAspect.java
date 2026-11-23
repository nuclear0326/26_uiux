import timeescape.ui.MainMenuPanel;import java.awt.*;import java.awt.image.BufferedImage;import javax.swing.*;import javax.imageio.ImageIO;import java.nio.file.*;
public class VerifyMenuAspect {
 public static void main(String[] args)throws Exception{
  Files.createDirectories(Path.of("outputs/v39-aspect"));
  SwingUtilities.invokeAndWait(()->{try{
   var p=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
   p.setContinueAvailable(true);var hit=MainMenuPanel.class.getDeclaredMethod("buttonAt",Point.class);hit.setAccessible(true);
   int checks=0;
   for(int[] size:new int[][]{{1920,1080},{2560,1440},{1920,1200},{2560,1080},{1200,900},{1020,650}}){
    int w=size[0],h=size[1];p.setSize(w,h);double s=Math.min(w/1600.,h/900.);int y=(h-(int)Math.round(900*s))/2;
    for(int i=0;i<4;i++){
     Point center=new Point((int)(328*s),y+(int)((336+96*i)*s));
     if((int)hit.invoke(p,center)!=i)throw new AssertionError("hit mismatch "+w+" "+i);
     if((662*s+y)>h)throw new AssertionError("buttons cropped");checks++;
    }
    var im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();p.paint(g);g.dispose();
    for(int x:new int[]{0,w-1})if(im.getRGB(x,h/2)==new Color(35,27,21).getRGB())throw new AssertionError("letterbox");
    ImageIO.write(im,"png",Path.of("outputs/v39-aspect/"+w+"x"+h+".png").toFile());
   }
   p.setVisible(false);System.out.println("PASS six aspect/size layouts, "+checks+" button hits, no blank side bars; headless.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
