import timeescape.ui.GameScenePanel;
import timeescape.scene.*;
import timeescape.model.Era;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
public class VerifyHotspots {
 public static void main(String[] args) throws Exception {
 SwingUtilities.invokeAndWait(() -> { try {
  var panel=new GameScenePanel(id -> {});
  var hit=GameScenePanel.class.getDeclaredMethod("hitTest",Point.class); hit.setAccessible(true);
  int[][] points={{1145,780},{935,535},{1070,460}};
  Era[] eras={Era.PAST,Era.PRESENT,Era.FUTURE};
  for(int i=0;i<eras.length;i++) {
   panel.setEra(eras[i]);
   for(int[] size:new int[][]{{1920,1080},{1120,700},{800,900}}) {
    panel.setSize(size[0],size[1]);
    double s=Math.min(size[0]/1920.0,size[1]/1080.0);
    int w=(int)Math.round(1920*s),h=(int)Math.round(1080*s);
    Point p=new Point((size[0]-w)/2+(int)Math.round(points[i][0]*w/1920.0),(size[1]-h)/2+(int)Math.round(points[i][1]*h/1080.0));
    if(hit.invoke(panel,p)==null) throw new AssertionError("Scaled hit failed: "+eras[i]);
    if(hit.invoke(panel,new Point(0,0))!=null) throw new AssertionError("Background hit");
   }
   panel.setSize(1920,1080);
   panel.getActionMap().get("revealObjects").actionPerformed(new ActionEvent(panel,0,""));
   BufferedImage img=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_RGB);
   var g=img.createGraphics(); panel.paint(g);g.dispose();
   ImageIO.write(img,"png",new File("outputs/v7-"+eras[i]+"-hotspots.png"));
   panel.getActionMap().get("hideObjects").actionPerformed(new ActionEvent(panel,0,""));
  }
  if(HotspotCatalog.of(Era.PRESENT).get(0).contains(850,450)) throw new AssertionError("Window included");
  if(HotspotCatalog.of(Era.PAST).get(0).contains(1020,650)) throw new AssertionError("Table included");
  if(HotspotCatalog.of(Era.FUTURE).get(0).contains(980,460)) throw new AssertionError("Ring gap included");
  var held=GameScenePanel.class.getDeclaredField("revealHeld");held.setAccessible(true);
  panel.getActionMap().get("revealObjects").actionPerformed(new ActionEvent(panel,0,""));
  panel.getActionMap().get("revealObjects").actionPerformed(new ActionEvent(panel,0,""));
  if(!held.getBoolean(panel)) throw new AssertionError("Repeat press toggled hint off");
  panel.getActionMap().get("hideObjects").actionPerformed(new ActionEvent(panel,0,""));
  if(held.getBoolean(panel)) throw new AssertionError("Release left hint on");
  panel.getActionMap().get("revealObjects").actionPerformed(new ActionEvent(panel,0,""));
  var listener=GameScenePanel.class.getDeclaredField("windowFocusListener");listener.setAccessible(true);
  ((java.beans.PropertyChangeListener)listener.get(panel)).propertyChange(new java.beans.PropertyChangeEvent(panel,"focusedWindow",null,null));
  if(held.getBoolean(panel)) throw new AssertionError("Focus loss left hint on");
  if(panel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).get(javax.swing.KeyStroke.getKeyStroke("F3"))!=null) throw new AssertionError("F3 still bound");
  var inventory=new timeescape.ui.InventoryPanel(id -> {});
  inventory.refresh(new timeescape.core.GameState(),false);
  var layout=new javax.swing.JPanel(new java.awt.BorderLayout());
  layout.add(panel,java.awt.BorderLayout.CENTER);layout.add(inventory,java.awt.BorderLayout.EAST);
  layout.setSize(1410,790);layout.doLayout();inventory.doLayout();
  for(var c:inventory.getComponents()) if(c instanceof java.awt.Container container)container.doLayout();
  inventory.doLayout();
  for(var c:inventory.getComponents()) if(c instanceof java.awt.Container container)container.doLayout();
  panel.setEra(Era.PRESENT);
  panel.getActionMap().get("revealObjects").actionPerformed(new ActionEvent(panel,0,""));
  var preview=new BufferedImage(1410,790,BufferedImage.TYPE_INT_RGB);
  var pg=preview.createGraphics();layout.paint(pg);pg.dispose();
  ImageIO.write(preview,"png",new File("outputs/v7-shift-preview.png"));
  System.out.println("Shift press/repeat/release/focus-loss and F3 removal passed.");
  System.out.println("Hotspot checks passed: 3 eras x 3 sizes; background and ring gap excluded; Shift renders saved.");
 } catch(Exception e){throw new RuntimeException(e);} });
 }
}


