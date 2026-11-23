package timeescape.ui;
import java.awt.*;import java.awt.image.*;import java.nio.file.*;import java.util.*;import javax.swing.*;import javax.imageio.ImageIO;
public class VerifyPresentPanelParity {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static void layout(Container p){p.doLayout();for(Component c:p.getComponents())if(c instanceof Container n)layout(n);}
 static BufferedImage render(JPanel p,int w,int h){p.setSize(w,h);layout(p);BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();p.printAll(g);g.dispose();return im;}
 static java.util.List<JButton> buttons(Container p){var all=new ArrayList<JButton>();for(Component c:p.getComponents()){if(c instanceof JButton b)all.add(b);else if(c instanceof Container n)all.addAll(buttons(n));}return all;}
 static void parity(JPanel a,JPanel b,int w,int h,String id){var x=render(a,w,h);var y=render(b,w,h);check(Arrays.equals(x.getRGB(0,0,w,h,null,0,w),y.getRGB(0,0,w,h,null,0,w)),"pixels "+id);}
 public static void main(String[] args)throws Exception{Files.createDirectories(Path.of("outputs/v61-validation"));SwingUtilities.invokeAndWait(()->{try{
 String[] ids={"PZ004","PZ006","PZ008","PZ010","PZ011"};int[][] solutions={{1,1,0,0,1},{4,3,2,1},{3,2,0,3},{3,3,2},{2,0,1}};GameFonts.installDefaults();
 for(int k=0;k<ids.length;k++){int[] oldEvents={0,0},events={0,0};var old=new LegacyPresentPuzzlePanel(ids[k],()->oldEvents[0]++,()->oldEvents[1]++);var fresh=new PresentPuzzlePanel(ids[k],()->events[0]++,()->events[1]++);
  var a=buttons(old);var b=buttons(fresh);int n=solutions[k].length;
  parity(old,fresh,900,640,ids[k]+" initial");parity(old,fresh,804,580,ids[k]+" small");
  a.get(n+2).doClick(0);b.get(n+2).doClick(0);check(events[0]==0,"wrong answer no completion");parity(old,fresh,900,640,ids[k]+" error");
  for(int i=0;i<n;i++)for(int j=0;j<solutions[k][i];j++){a.get(i).doClick(0);b.get(i).doClick(0);}parity(old,fresh,900,640,ids[k]+" solved");a.get(n+2).doClick(0);b.get(n+2).doClick(0);check(events[0]==oldEvents[0]&&events[0]==1,"success once");
  ImageIO.write(render(fresh,900,640),"png",Path.of("outputs/v61-validation/"+ids[k]+".png").toFile());a.get(n).doClick(0);b.get(n).doClick(0);parity(old,fresh,900,640,ids[k]+" reset");a.get(n+1).doClick(0);b.get(n+1).doClick(0);check(events[1]==oldEvents[1]&&events[1]==1,"close callback");
 }
 System.out.println("PASS "+checks+" panel checks including 25 pixel-identical comparisons; no window");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
