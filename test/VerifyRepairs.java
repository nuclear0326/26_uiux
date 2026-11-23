import timeescape.puzzle.RepairModels.*;
import java.awt.Point;
public final class VerifyRepairs {
 public static void main(String[] args){
  Gears g=new Gears();int total=java.util.Arrays.stream(g.stock).sum();
  if(total!=16)throw new AssertionError("stock");
  if(!g.put(0,0,2)||g.faceDone(0)||g.put(0,0,1))throw new AssertionError("wrong size/occupied");
  g.remove(0,0);if(java.util.Arrays.stream(g.stock).sum()!=total)throw new AssertionError("return");
  for(int f=0;f<4;f++)for(int s=0;s<g.target[f].length;s++)if(!g.put(f,s,g.target[f][s]))throw new AssertionError("placement");
  if(!g.solved()||java.util.Arrays.stream(g.stock).sum()!=0)throw new AssertionError("solve");
  System.out.println("Gear stock / return / 16 sockets passed.");
 }
}
