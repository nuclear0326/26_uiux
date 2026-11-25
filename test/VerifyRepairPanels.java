package timeescape.ui;
import java.awt.*;import java.awt.event.*;import java.awt.image.*;import java.nio.file.*;import java.util.*;import javax.swing.*;import javax.imageio.ImageIO;import timeescape.puzzle.RepairModels.*;
public class VerifyRepairPanels {
 static int checks,frames;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static Object field(Object x,String name){try{var f=x.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(x);}catch(Exception e){throw new RuntimeException(e);}}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container n)layout(n);}
 static BufferedImage render(JPanel p,int w,int h){p.setSize(w,h);layout(p);BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();p.printAll(g);g.dispose();return im;}
 static JButton button(Container p,String text){for(Component c:p.getComponents()){if(c instanceof JButton b&&text.equals(b.getText()))return b;if(c instanceof Container n){JButton b=button(n,text);if(b!=null)return b;}}return null;}
 static void event(Object panel,int type,int x,int y){JPanel board=(JPanel)field(panel,"board");double scale=Math.min(board.getWidth()/900.,board.getHeight()/560.);int ox=(int)((board.getWidth()-900*scale)/2),oy=(int)((board.getHeight()-560*scale)/2);board.dispatchEvent(new MouseEvent(board,type,0,0,ox+(int)Math.round(x*scale),oy+(int)Math.round(y*scale),1,false,type==MouseEvent.MOUSE_DRAGGED?MouseEvent.NOBUTTON:MouseEvent.BUTTON1));}
 static void click(Object p,int x,int y){event(p,MouseEvent.MOUSE_PRESSED,x,y);event(p,MouseEvent.MOUSE_RELEASED,x,y);}
 static void drag(Object p,int x,int y,int endX,int endY){event(p,MouseEvent.MOUSE_PRESSED,x,y);event(p,MouseEvent.MOUSE_DRAGGED,endX,endY);event(p,MouseEvent.MOUSE_RELEASED,endX,endY);}
 static void fillWires(Object p,boolean hard)throws Exception{
  for(String line:Files.readAllLines(Path.of("test/fixtures/wires_"+(hard?"hard":"easy")+"_solution.txt"))){boolean first=true;int x=0,y=0;for(String token:line.split(" ")){String[] xy=token.split(",");x=190+Integer.parseInt(xy[0])*24;y=30+Integer.parseInt(xy[1])*24;event(p,first?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_DRAGGED,x,y);first=false;}event(p,MouseEvent.MOUSE_RELEASED,x,y);}
 }
 public static void main(String[] args)throws Exception{Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v60-validation"));SwingUtilities.invokeAndWait(()->{try{
  GameFonts.installDefaults();int[] solved={0},closed={0};GearsPuzzlePanel gear=new GearsPuzzlePanel(()->solved[0]++,()->closed[0]++);
  render(gear,980,780);
  click(gear,700,410);click(gear,155,85);check(((Gears)field(gear,"gears")).placed[0][0]==2&&!gear.action.isEnabled(),"wrong size placement not solved");click(gear,155,85);
  drag(gear,700,160,155,85);check(((Gears)field(gear,"gears")).placed[0][0]==0,"drag placement");button(gear,"전체 부품 회수").doClick(0);
  int[][] target=((Gears)field(gear,"gears")).target;for(int row=0;row<4;row++)for(int col=0;col<4;col++){click(gear,700,160+target[row][col]*135);click(gear,155+col*130,85+row*130);}check(gear.action.isEnabled(),"all 16 enable success");ImageIO.write(render(gear,980,780),"png",timeescape.testutil.ArtifactPaths.output("v60-validation/gears.png").toFile());gear.action.doClick(0);gear.action.doClick(0);gear.developerSkip();check(solved[0]==1,"gear result once");
  int[] wireSolved={0};WiresPuzzlePanel wires=new WiresPuzzlePanel(()->wireSolved[0]++,()->closed[0]++);
  render(wires,980,780);Wires model=(Wires)field(wires,"wires");int y=30+model.start(0).y*24;click(wires,190+model.start(0).x*24,y);event(wires,MouseEvent.MOUSE_DRAGGED,190+6*24,y);
  button(wires,"한 칸 취소").doClick(0);button(wires,"선 지우기").doClick(0);
  button(wires,"현재 면 배선 초기화").doClick(0);fillWires(wires,false);check(wires.action.isEnabled(),"easy ready");wires.action.doClick(0);check(wireSolved[0]==0&&((Wires)field(wires,"wires")).hard,"easy does not finish puzzle");
  fillWires(wires,true);check(wires.action.isEnabled(),"hard ready");ImageIO.write(render(wires,980,780),"png",timeescape.testutil.ArtifactPaths.output("v60-validation/wires.png").toFile());wires.action.doClick(0);wires.action.doClick(0);check(wireSolved[0]==1,"hard completion once");
  int[] skips={0},exits={0};WiresPuzzlePanel skip=new WiresPuzzlePanel(()->skips[0]++,()->exits[0]++);skip.developerSkip();check(((Wires)field(skip,"wires")).hard&&skips[0]==0,"skip easy advances only");button(skip,"현재 면 배선 초기화").doClick(0);check(((Wires)field(skip,"wires")).hard,"reset keeps hard stage");skip.developerSkip();skip.developerSkip();check(skips[0]==1,"skip hard completes once");
  WiresPuzzlePanel cancel=new WiresPuzzlePanel(()->skips[0]++,()->exits[0]++);button(cancel,"조사 종료").doClick(0);cancel.developerSkip();check(exits[0]==1&&skips[0]==1,"exit cancels");WiresPuzzlePanel reopen=new WiresPuzzlePanel(()->{},()->{});check(!((Wires)field(reopen,"wires")).hard,"reopen starts easy");reopen.cancelSession();reopen.developerSkip();check(!((Wires)field(reopen,"wires")).hard,"window close prevents late skip");
  check(Arrays.stream(GearsPuzzlePanel.class.getDeclaredFields()).noneMatch(f->f.getType()==Wires.class)&&Arrays.stream(WiresPuzzlePanel.class.getDeclaredFields()).noneMatch(f->f.getType()==Gears.class),"independent models");
  System.out.println("PASS "+checks+" current panel input/lifecycle checks without legacy pixel comparisons; no dialogs");
 }catch(Exception e){throw new RuntimeException(e);}});}
}