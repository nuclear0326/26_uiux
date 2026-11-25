import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import timeescape.ui.*;
import timeescape.core.GameState;
import timeescape.model.Era;
import timeescape.scene.*;
public class VerifyLabDesign {
 static int checks;
 static void check(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container x)layout(x);}
 static java.util.List<JButton> buttons(Container c){var out=new ArrayList<JButton>();for(Component x:c.getComponents()){if(x instanceof JButton b)out.add(b);else if(x instanceof Container n)out.addAll(buttons(n));}return out;}
 static void render(JComponent c,int w,int h,String name)throws Exception{c.setSize(w,h);layout(c);layout(c);var img=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();c.printAll(g);g.dispose();ImageIO.write(img,"png",timeescape.testutil.ArtifactPaths.output("archive/qa-v16/"+name+".png").toFile());}
 public static void main(String[] args)throws Exception{Files.createDirectories(timeescape.testutil.ArtifactPaths.output("archive/qa-v16"));SwingUtilities.invokeAndWait(()->{try{
  String[] ids={"PZ004","PZ006","PZ008","PZ010","PZ011"};int[][] moves={{0,0,1,0,1},{4,3,2,1},{3,2,0,3},{3,3,2},{2,0,1}};
  for(int k=0;k<ids.length;k++){
   int[] won={0},closed={0};var p=new PresentPuzzlePanel(ids[k],()->won[0]++,()->closed[0]++);var b=buttons(p);int n=moves[k].length;
   check(b.size()==n+3);b.get(n+2).doClick(0);check(won[0]==0);b.get(n+1).doClick(0);check(closed[0]==1&&won[0]==0);
   render(p,900,640,ids[k]);
   for(int i=0;i<n;i++)for(int j=0;j<moves[k][i];j++)b.get(i).doClick(0);
   b.get(n+2).doClick(0);check(won[0]==1);b.get(n).doClick(0);b.get(n+2).doClick(0);check(won[0]==1);
   render(p,804,580,ids[k]+"-small");
  }
  java.util.List<String> clicks=new ArrayList<>();var scene=new GameScenePanel(clicks::add);scene.setEra(Era.PRESENT);
  for(int[] size:new int[][]{{1920,1080},{1120,700},{680,540}}){scene.setSize(size[0],size[1]);double s=Math.min(size[0]/1920.,size[1]/1080.);int w=(int)Math.round(1920*s),h=(int)Math.round(1080*s);
   for(Hotspot hot:HotspotCatalog.of(Era.PRESENT)){Rectangle r=hot.bounds();int x=(size[0]-w)/2+(int)Math.round(r.getCenterX()*w/1920.),y=(size[1]-h)/2+(int)Math.round(r.getCenterY()*h/1080.);scene.dispatchEvent(new MouseEvent(scene,MouseEvent.MOUSE_CLICKED,0,0,x,y,1,false,MouseEvent.BUTTON1));check(clicks.get(clicks.size()-1).equals(hot.id()));}
  }
  var state=new GameState();var missions=new MissionPanel();missions.refresh(state);render(missions,320,450,"mission-dark");
  state.complete("PZ004");state.complete("PZ006");state.collect("USB");missions.refresh(state);render(missions,320,450,"mission-progress");
  var inventory=new InventoryPanel(id->{});inventory.refresh(state,false);JPanel side=new JPanel(new BorderLayout());side.add(missions,BorderLayout.NORTH);side.add(inventory);JScrollPane scroll=new JScrollPane(side);scroll.setPreferredSize(new Dimension(340,700));scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  JPanel full=new JPanel(new BorderLayout());full.add(scene);full.add(scroll,BorderLayout.EAST);scene.setPowered(true);render(full,1500,790,"scene");scene.setPowered(false);render(full,1500,790,"scene-dark");
  scene.getActionMap().get("revealObjects").actionPerformed(null);render(full,1500,790,"scene-shift");scene.getActionMap().get("hideObjects").actionPerformed(null);
  System.out.println("PASS "+checks+" themed controls and scene hotspot checks. Offscreen images saved.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
