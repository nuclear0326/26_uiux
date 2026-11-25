package timeescape.ui;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;
/** Exact approved fracture layers, generated at build time. */
final class ClockGlassEffect {
 private final BufferedImage main=read("clock_crack_main.png"),fine=read("clock_crack_fine.png");
 private final Shape face=new Ellipse2D.Double(MenuAtmosphere.CX-MenuAtmosphere.RX,MenuAtmosphere.CY-MenuAtmosphere.RY,MenuAtmosphere.RX*2,MenuAtmosphere.RY*2);
 private static BufferedImage read(String name){try(var in=ClockGlassEffect.class.getResourceAsStream("/assets/effects/"+name)){
  if(in==null)throw new java.io.IOException("Missing fracture layer "+name);
  var image=javax.imageio.ImageIO.read(in);if(image==null)throw new java.io.IOException("Invalid fracture layer");return image;
 }catch(java.io.IOException e){throw new IllegalStateException(e);}}
 void draw(Graphics2D canvas,double age){
  if(age<0)return;Graphics2D g=(Graphics2D)canvas.create();g.clip(face);
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  int x=(int)(MenuAtmosphere.CX-MenuAtmosphere.RX),y=(int)(MenuAtmosphere.CY-MenuAtmosphere.RY);
  g.drawImage(main,x,y,256,292,null);
  if(age>=.06)g.drawImage(fine,x,y,256,292,null);
  g.dispose();
 }
}
