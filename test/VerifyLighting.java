import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import timeescape.ui.GameScenePanel;
import timeescape.system.PowerRestoration;
import timeescape.model.Era;
public class VerifyLighting {
 static int checks;
 static void check(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
 static BufferedImage render(GameScenePanel p,int width,int height){p.setSize(width,height);var img=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();p.paint(g);g.dispose();return img;}
 static void gif(java.util.List<BufferedImage> images)throws Exception{
  ImageWriter writer=ImageIO.getImageWritersBySuffix("gif").next();
  try(ImageOutputStream out=ImageIO.createImageOutputStream(timeescape.testutil.ArtifactPaths.output("v19-preview/power-restoration.gif").toFile())){
   writer.setOutput(out);writer.prepareWriteSequence(null);
   for(int i=0;i<images.size();i++){
    var type=ImageTypeSpecifier.createFromRenderedImage(images.get(i));var meta=writer.getDefaultImageMetadata(type,writer.getDefaultWriteParam());String format=meta.getNativeMetadataFormatName();var root=(IIOMetadataNode)meta.getAsTree(format);
    var control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);control.setAttribute("delayTime",i==images.size()-1?"180":"10");control.setAttribute("disposalMethod","none");
    if(i==0){var extensions=new IIOMetadataNode("ApplicationExtensions");var loop=new IIOMetadataNode("ApplicationExtension");loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);}
    meta.setFromTree(format,root);writer.writeToSequence(new IIOImage(images.get(i),null,meta),writer.getDefaultWriteParam());
   }writer.endWriteSequence();
  }finally{writer.dispose();}
 }
 public static void main(String[] args)throws Exception{
  Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v19-preview"));
  check(PowerRestoration.level(0)==0);check(PowerRestoration.level(4.2)==1);check(PowerRestoration.level(10)==1);
  check(PowerRestoration.level(.3)>PowerRestoration.level(.46));check(PowerRestoration.level(.84)>PowerRestoration.level(1.02));
  for(double t=0;t<4.3;t+=.01){float l=PowerRestoration.level(t);check(l>=0&&l<=1);}
  SwingUtilities.invokeAndWait(()->{try{
   var p=new GameScenePanel(id->{});p.setEra(Era.PRESENT);
   var level=GameScenePanel.class.getDeclaredField("lightLevel");level.setAccessible(true);
   var timerField=GameScenePanel.class.getDeclaredField("lightingTimer");timerField.setAccessible(true);Timer timer=(Timer)timerField.get(p);
   var start=GameScenePanel.class.getDeclaredField("lightStarted");start.setAccessible(true);
   ImageIO.write(render(p,1440,810),"png",timeescape.testutil.ArtifactPaths.output("v19-preview/before.png").toFile());
   p.setPowered(true);check(timer.isRunning());long first=start.getLong(p);p.setPowered(true);check(start.getLong(p)==first);timer.stop();
   var frames=new java.util.ArrayList<BufferedImage>();
   for(int i=0;i<=42;i++){level.setFloat(p,PowerRestoration.level(i/10.));frames.add(render(p,960,540));}
   gif(frames);
   level.setFloat(p,PowerRestoration.level(2.6));ImageIO.write(render(p,1440,810),"png",timeescape.testutil.ArtifactPaths.output("v19-preview/during.png").toFile());
   level.setFloat(p,1);ImageIO.write(render(p,1440,810),"png",timeescape.testutil.ArtifactPaths.output("v19-preview/after.png").toFile());
   p.setPowered(false);check(level.getFloat(p)==0&&!timer.isRunning());
   p.setPowered(true);p.setEra(Era.PAST);check(!timer.isRunning()&&level.getFloat(p)==1);
   p.setEra(Era.PRESENT);check(!timer.isRunning());p.setPowered(false);
   System.out.println("PASS "+checks+" lighting checks, PNGs and animated GIF exported without game window.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
