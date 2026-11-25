package timeescape.ui;
import java.awt.*;import java.awt.event.*;import java.awt.image.*;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;import timeescape.core.*;import timeescape.model.Era;
public class VerifyItemNotices66 {
 static int checks;static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);checks++;}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container cc)layout(cc);}
 static BufferedImage render(JPanel p,int w,int h){p.setSize(w,h);layout(p);BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();p.printAll(g);g.dispose();return out;}
 static void stop(ItemAcquisitionToast n)throws Exception{var f=ItemAcquisitionToast.class.getDeclaredField("timer");f.setAccessible(true);((Timer)f.get(n)).stop();}
 static JButton row(InventoryPanel p,String id){for(Component c:p.getComponents())if(c instanceof JButton b&&id.equals(b.getName()))return b;throw new AssertionError(id);}
 static void hover(JButton b){b.dispatchEvent(new MouseEvent(b,MouseEvent.MOUSE_ENTERED,0,0,20,20,0,false));}
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());GameFonts.installDefaults();Path out=timeescape.testutil.ArtifactPaths.output("v66-preview");Files.createDirectories(out.resolve("frames"));
 GameState s=new GameState();GameScenePanel scene=new GameScenePanel(x->{});scene.setEra(Era.PRESENT);scene.restorePowered(true);InventoryPanel inv=new InventoryPanel(x->{});MissionPanel missions=new MissionPanel();GameHud hud=new GameHud(scene,missions,inv,()->{},()->{},()->{});
 inv.refresh(s,false);s.complete("PZ004");s.collect("USB");inv.refresh(s,false);stop(hud.itemNotice);check(inv.isUnread("USB"),"new pickup unread");check(hud.itemNotice.pendingCount()==1,"one notice");inv.refresh(s,false);check(hud.itemNotice.pendingCount()==1,"refresh no duplicate");
 hud.itemNotice.advance(.4);ImageIO.write(render(hud,1440,900),"png",out.resolve("item-acquired.png").toFile());
 for(int i=0;i<=36;i++){hud.itemNotice.advance(i*.05);ImageIO.write(render(hud,1440,900),"png",out.resolve(String.format("frames/%02d.png",i)).toFile());}check(hud.itemNotice.pendingCount()==0&&!hud.itemNotice.isVisible(),"expires within 2s");
 hud.openNotebook("inventory");ImageIO.write(render(hud,1440,900),"png",out.resolve("inventory-unread.png").toFile());check(inv.isUnread("USB"),"opening notebook not acknowledgement");inv.setInteractionAllowed(()->false);hover(row(inv,"USB"));check(inv.isUnread("USB"),"blocked hover preserves dot");inv.setInteractionAllowed(()->true);hover(row(inv,"USB"));check(!inv.isUnread("USB"),"hover clears dot");inv.refresh(s,false);check(!inv.isUnread("USB"),"read persists refresh");
 s.collect("CASE");s.collect("CELL");inv.refresh(s,false);stop(hud.itemNotice);check(hud.itemNotice.pendingCount()==2,"rapid pickups queued");hud.itemNotice.advance(1.8);stop(hud.itemNotice);check(hud.itemNotice.pendingCount()==1,"second queued notice");
 GameState restored=GameState.restore(s.catalog(),s.snapshot());inv.refresh(restored,false);check(hud.itemNotice.pendingCount()==0&&!inv.isUnread("USB")&&inv.isUnread("CASE"),"load restores unread without replaying acquisitions");
 inv.refresh(s,false);s.complete("PZ008");s.collect("MODULE");inv.refresh(s,false);s.complete("PZ010");inv.refresh(s,false);stop(hud.itemNotice);check(inv.isUnread("DEVICE"),"puzzle reward detected");check(!inv.isUnread("MODULE"),"consumed unread cleared");hud.itemNotice.clear();
 GearsPuzzlePanel gears=new GearsPuzzlePanel(()->{},()->{});ImageIO.write(render(gears,1000,820),"png",out.resolve("gears-buttons.png").toFile());
 JButton b=new JButton("전체 부품 회수");PuzzleChrome.styleButton(b);b.setSize(160,44);BufferedImage button=new BufferedImage(160,44,BufferedImage.TYPE_INT_RGB);Graphics2D g=button.createGraphics();b.paint(g);g.dispose();check(new Color(button.getRGB(12,12)).getRed()<80,"native LAF no white fill");b.setEnabled(false);g=button.createGraphics();b.paint(g);g.dispose();check(new Color(button.getRGB(12,12)).getRed()<80,"disabled no white fill");check(!hud.itemNotice.contains(10,10),"toast does not block input");
 System.out.println("PASS "+checks+" acquisition, queue, expiry, hover, reload, reward and native-theme button checks. No game window or audio.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
