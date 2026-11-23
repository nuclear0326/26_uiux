import timeescape.puzzle.RepairModels.Wires;
import timeescape.ui.WireBoardRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
public final class VerifyMaze {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  Path root=Path.of(args[0]),out=Path.of(args[1]);Files.createDirectories(out);
  for(boolean hard:new boolean[]{false,true}){
   String name=hard?"hard":"easy";Wires w=new Wires(hard);
   render(w,out.resolve("maze_"+name+".png"));
   // Old straight-lane strategy connects all pairs but must NOT win anymore.
   for(int i=0;i<w.rows.length;i++){w.begin(i);for(int x=4;x<=17;x++)check(w.step(i,new Point(x,w.rows[i])),"straight fixture");}
   check(!w.solved(),"Incomplete coverage accepted");
   w=new Wires(hard);
   var lines=Files.readAllLines(root.resolve("test/fixtures/wires_"+name+"_solution.txt"));
   java.util.List<java.util.List<Point>> solutions=new ArrayList<>();
   for(String line:lines){var list=new ArrayList<Point>();for(String token:line.split(" ")){String[] xy=token.split(",");list.add(new Point(Integer.parseInt(xy[0]),Integer.parseInt(xy[1])));}solutions.add(list);}
   for(int i=0;i<lines.size();i++){
    var path=solutions.get(i);check(path.get(0).equals(w.start(i)),"start");w.begin(i);
    check(!w.step(i,new Point(-1,-1)),"outside accepted");
    check(!w.step(i,w.end(i)),"jump accepted");
    for(int j=1;j<path.size();j++)check(w.step(i,path.get(j)),"solution "+name+":"+i+":"+j);
   }
   check(w.solved(),"No full solution");check(w.coveredCells()==w.totalCells(),"coverage");
   render(w,out.resolve("maze_"+name+"_solved.png"));
   // Test walls and other wires adjacent to a legal partial path.
   int wallChecks=0,overlapChecks=0;
   for(int i=0;i<solutions.size();i++){
    var path=solutions.get(i);w.begin(i);
    for(int j=0;j<path.size();j++){
     if(j>0)check(w.step(i,path.get(j)),"replay");
     Point a=path.get(j);
     for(Point delta:new Point[]{new Point(1,0),new Point(-1,0),new Point(0,1),new Point(0,-1)}){
      Point p=new Point(a.x+delta.x,a.y+delta.y);
      if(w.blocked(p.x,p.y)){check(!w.step(i,p),"wall");wallChecks++;}
      else for(int k=0;k<solutions.size();k++)if(k!=i&&w.paths.get(k).contains(p)){check(!w.step(i,p),"overlap");overlapChecks++;}
     }
    }
   }
   check(wallChecks>0&&overlapChecks>0,"test coverage");
   w.undo(0);check(!w.solved(),"undo did not reset win");
   check(w.step(0,w.end(0))&&w.solved(),"reconnect");
   w.begin(0);check(!w.solved()&&w.paths.get(0).size()==1,"restart line");
   check(w.extend(0,new Point(6,w.rows[0]))&&w.paths.get(0).size()==4,"drag interpolation");
   check(w.extend(0,w.start(0))&&w.paths.get(0).size()==1,"drag undo");
   check(!new Wires(false).solved(),"fresh stage");
   System.out.println(name+": solution + full coverage, walls="+wallChecks+", overlaps="+overlapChecks+", undo/restart/interpolation passed; cells="+w.totalCells());
  }
 }
 static void render(Wires w,Path file)throws Exception{
  BufferedImage image=new BufferedImage(900,560,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(new Color(17,24,32));g.fillRect(0,0,900,560);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);WireBoardRenderer.paint(g,w);g.dispose();ImageIO.write(image,"png",file.toFile());
 }
}
