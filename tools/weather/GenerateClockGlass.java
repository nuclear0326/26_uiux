package timeescape.ui;
import java.nio.file.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
public final class GenerateClockGlass {
 public static void main(String[] args)throws Exception{
  Path dir=Path.of(args[0]).resolve("effects");Files.createDirectories(dir);
  var source=new LegacyClockGlassEffect();
  for(String name:new String[]{"main","fine"}){
   var field=LegacyClockGlassEffect.class.getDeclaredField(name);field.setAccessible(true);
   var im=(BufferedImage)field.get(source);Path target=dir.resolve("clock_crack_"+name+".png");
   if(!ImageIO.write(im,"png",target.toFile()))throw new IllegalStateException("No PNG writer");
   var read=ImageIO.read(target.toFile());
   if(!java.util.Arrays.equals(im.getRGB(0,0,im.getWidth(),im.getHeight(),null,0,im.getWidth()),read.getRGB(0,0,im.getWidth(),im.getHeight(),null,0,im.getWidth())))throw new AssertionError("Fracture pixel mismatch");
  }
  System.out.println("Generated and verified exact clock fracture layers.");
 }
}
