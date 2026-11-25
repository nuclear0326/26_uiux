package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;import timeescape.core.GameState;
public final class VerifyNotebook91 {
 static int n;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 static void layout(Container c){c.doLayout();for(var x:c.getComponents())if(x instanceof Container p)layout(p);}
 static void save(GameHud h,String name)throws Exception{var image=new BufferedImage(h.getWidth(),h.getHeight(),BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();h.printAll(g);g.dispose();ImageIO.write(image,"png",timeescape.testutil.ArtifactPaths.output("v91-preview/"+name+".png").toFile());}
 static JButton close(Container c){for(var x:c.getComponents()){if(x instanceof JButton b&&"notebook-close".equals(b.getName()))return b;if(x instanceof Container p){var b=close(p);if(b!=null)return b;}}return null;}
 public static void main(String[] a)throws Exception{Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v91-preview"));SwingUtilities.invokeAndWait(()->{try{
 GameFonts.installDefaults();var s=GameState.newGame();s.takePrototype();var scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.restorePowered(true);var m=new MissionPanel();m.refresh(s);int[] clicks={0};var inv=new InventoryPanel(id->clicks[0]++);inv.refresh(s,false);var h=new GameHud(scene,m,inv,()->{},()->{},()->{});
 float last=0;int lastWidth=0;
 for(int[] size:new int[][]{{1020,650},{1440,821},{1920,1080},{2560,1440}}){
  h.setSize(size[0],size[1]);layout(h);layout(h);h.openNotebook("records");h.notebook.finishTurn();layout(h);layout(h);
  double scale=h.notebook.uiScale();ok(h.recordTab.getFont().getSize2D()>last,"font grows");ok(h.notebook.getWidth()>lastWidth,"note grows");last=h.recordTab.getFont().getSize2D();lastWidth=h.notebook.getWidth();
  ok(h.notebook.getX()>=0&&h.notebook.getY()+h.notebook.getHeight()<=h.layers.getHeight(),"within scene");save(h,"records-"+size[0]);
  Font font=h.recordTab.getFont();h.openNotebook("inventory");ok(h.notebook.isTurning(),"page turn active");ok(h.recordTab.getFont().equals(font),"font unchanged by animation");h.notebook.setTurnProgress(.5f);save(h,"turn-"+size[0]);h.notebook.finishTurn();layout(h);layout(h);save(h,"inventory-"+size[0]);
  var high=h.notebook.capture(2,2);ok(high.getWidth()==h.notebook.getWidth()*2&&high.getHeight()==h.notebook.getHeight()*2,"HiDPI capture resolution");
  ((JButton)inv.getComponent(0)).doClick();ok(clicks[0]>0,"item click after scaling");
  var x=close(h.notebook);ok(x!=null&&!x.isContentAreaFilled()&&!x.isBorderPainted(),"plain close icon");x.doClick();ok(!h.notebook.isVisible(),"close works");
 }
 h.openNotebook("records");h.openNotebook("inventory");h.setSize(1280,720);layout(h);ok(!h.notebook.isTurning(),"resize cancels stale snapshots");
 h.openNotebook("records");h.notebook.finishTurn();layout(h);Font before=h.recordTab.getFont();for(int i=0;i<5;i++)layout(h);ok(before.equals(h.recordTab.getFont()),"no cumulative scaling");
 h.removeNotify();System.out.println("PASS "+n+" notebook scale, font, HiDPI, turn and input checks");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
