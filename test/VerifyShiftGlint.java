import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.ui.GameScenePanel;
import timeescape.model.Era;
import timeescape.system.ShiftGlint;

public class VerifyShiftGlint {
 static java.lang.reflect.Field field(String name)throws Exception{var f=GameScenePanel.class.getDeclaredField(name);f.setAccessible(true);return f;}
 public static void main(String[] args)throws Exception{
  Path out=Path.of("outputs/v21-preview/frames");Files.createDirectories(out);
  if(ShiftGlint.frame(30).getWidth()!=32)throw new AssertionError("sheet");
  SwingUtilities.invokeAndWait(()->{try{
   var p=new GameScenePanel(id->{});p.setEra(Era.PRESENT);p.setSize(1440,810);p.setPowered(true);
   ((Timer)field("lightingTimer").get(p)).stop();field("lightLevel").setFloat(p,1);
   p.getActionMap().get("revealObjects").actionPerformed(null);((Timer)field("hoverTimer").get(p)).stop();
   for(int i=0;i<64;i++){
    field("particlePhase").setDouble(p,i*.1125);
    var image=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();p.paint(g);g.dispose();
    ImageIO.write(image,"png",out.resolve(String.format("%03d.png",i)).toFile());
   }
   p.getActionMap().get("hideObjects").actionPerformed(null); for(int step=0;step<5;step++) for(var l:((Timer)field("hoverTimer").get(p)).getActionListeners()) l.actionPerformed(new java.awt.event.ActionEvent(p,0,""));
   if(field("revealHeld").getBoolean(p)||((Timer)field("hoverTimer").get(p)).isRunning())throw new AssertionError("release");
   p.getActionMap().get("revealObjects").actionPerformed(null);p.setEra(Era.PAST);
   if(field("revealHeld").getBoolean(p)||((Timer)field("hoverTimer").get(p)).isRunning())throw new AssertionError("era cleanup");
   System.out.println("PASS: asset loading, Shift release, era cleanup; 64 headless frames.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
