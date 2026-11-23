import timeescape.puzzle.RepairModels.*;
import timeescape.ui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
public class VerifyResume {
 public static void main(String[] args)throws Exception{
  for(boolean hard:new boolean[]{false,true}){
   Wires w=new Wires(hard);int a=w.selectAt(w.start(0));w.extend(a,new Point(6,w.rows[0]));
   int before=w.paths.get(0).size();int b=w.selectAt(w.start(1));w.extend(b,new Point(5,w.rows[1]));
   int second=w.paths.get(1).size();
   if(w.selectAt(new Point(4,w.rows[0]))!=0||w.paths.get(0).size()!=before)throw new AssertionError("body selection destroys path");
   if(w.selectAt(w.start(0))!=0||w.paths.get(0).size()!=before)throw new AssertionError("terminal selection destroys path");
   Point tip=w.paths.get(0).get(before-1);
   if(w.selectAt(tip)!=0||!w.extend(0,new Point(7,w.rows[0])))throw new AssertionError("resume failed");
   if(w.paths.get(1).size()!=second)throw new AssertionError("other path modified");
   w.undo(0);if(w.paths.get(0).size()!=before)throw new AssertionError("undo");
   w.begin(0);if(w.paths.get(0).size()!=1||w.paths.get(1).size()!=second)throw new AssertionError("explicit reset");
  }
  Gears gears=new Gears();gears.put(0,0,0);gears.put(0,1,1);gears.put(1,2,2);
  BufferedImage img=new BufferedImage(900,560,BufferedImage.TYPE_INT_RGB);Graphics2D g=img.createGraphics();g.setColor(new Color(17,24,32));g.fillRect(0,0,900,560);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);GearBoardRenderer.paint(g,gears,1,-1,null);g.dispose();ImageIO.write(img,"png",Path.of(args[0]).toFile());
  System.out.println("Both stages: switch color, reselect body/start/tip, resume, preserve other line, undo and explicit reset passed.");
 }
}
