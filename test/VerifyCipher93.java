package timeescape.ui;
import timeescape.core.*;import timeescape.system.*;import timeescape.puzzle.present.*;import java.awt.*;import java.awt.image.BufferedImage;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;
public final class VerifyCipher93 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container cc)layout(cc);}
 static BufferedImage paint(JComponent c,int w,int h){c.setSize(w,h);layout(c);layout(c);BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();c.paint(g);g.dispose();return im;}
 public static void main(String[] args)throws Exception{
  GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");ok(!s.available("PZ006"),"PC requires memo");s.complete("ACT_GET_CIPHER");ok(s.has("CIPHER_TABLE")&&s.isItemUnread("CIPHER_TABLE")&&s.available("PZ006"),"memo unlock and unread reward");var snap=s.snapshot();s.complete("ACT_GET_CIPHER");ok(s.snapshot().equals(snap),"no duplicate grant");
  SaveStore disk=new SaveStore(Files.createTempDirectory("cipher93-save"),ProgressCatalog.standard());disk.save(s.snapshot());var loaded=disk.load().orElseThrow().state();ok(loaded.has("CIPHER_TABLE")&&loaded.isItemUnread("CIPHER_TABLE"),"item persists");loaded.markItemRead("CIPHER_TABLE");disk.save(loaded.snapshot());ok(!disk.load().orElseThrow().state().isItemUnread("CIPHER_TABLE"),"read flag persists");
  for(int code=0;code<10000;code++){String digits=String.format(java.util.Locale.ROOT,"%04d",code);var model=new ArchiveCodePuzzle();for(int i=0;i<4;i++)for(int j=0;j<digits.charAt(i)-'0';j++)model.turn(i);ok(model.solved()==(code==7135)&&ArchiveCodePuzzle.accepts(digits)==(code==7135),"unique new answer");}
  Path out=timeescape.testutil.ArtifactPaths.output("v93-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{GameFonts.installDefaults();int[] done={0},closed={0},viewed={0};ArchiveLoginPanel login=new ArchiveLoginPanel(()->done[0]++,()->closed[0]++,()->viewed[0]++);
   login.password.setText("4321");login.submit.doClick();ok(done[0]==0,"old answer rejected");login.password.setText("abcd");ok(!login.password.getText().equals("abcd"),"numeric input only");login.password.setText("12345");ok(login.password.getText().length()<=4,"length limited");login.password.setText("");
   GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.restorePowered(true);scene.setProgressHints(s);MissionPanel missions=new MissionPanel();missions.refresh(s);GameHud[] holder={null};InventoryPanel inv=new InventoryPanel(id->{if(id.equals("CIPHER_TABLE"))holder[0].openCipherMemo();});inv.refresh(s,false);GameHud hud=new GameHud(scene,missions,inv,()->{},()->{},()->{});holder[0]=hud;
   paint(hud,1440,900);hud.openCipherMemo();ImageIO.write(paint(hud,1440,900),"png",out.resolve("01-memo-center.png").toFile());ok(hud.cipherMemo.isVisible(),"center viewer visible");
   for(int[] size:new int[][]{{1020,650},{1920,1080}}){paint(hud,size[0],size[1]);Rectangle b=hud.cipherMemo.paperBounds();ok(b.x>=0&&b.y>=0&&b.x+b.width<=hud.cipherMemo.getWidth()&&b.y+b.height<=hud.cipherMemo.getHeight(),"responsive image bounds");}
   hud.getActionMap().get("closeNotebook").actionPerformed(null);ok(!hud.cipherMemo.isVisible(),"escape closes image");hud.openNotebook("inventory");hud.notebook.finishTurn();ImageIO.write(paint(hud,1440,900),"png",out.resolve("02-inventory.png").toFile());JButton memo=null;for(Component c:inv.getComponents())if("CIPHER_TABLE".equals(c.getName()))memo=(JButton)c;ok(memo!=null,"inventory row");memo.doClick();ok(hud.cipherMemo.isVisible(),"inventory reopens same image");hud.closeCipherMemo();hud.closeNotebook();
   BufferedImage shot=paint(hud,1440,900),panel=paint(new ArchiveLoginPanel(()->{},()->{},()->{}),900,680);Graphics2D g=shot.createGraphics();g.setColor(new Color(0,0,0,120));g.fillRect(0,65,1440,835);g.drawImage(panel,270,110,null);g.dispose();ImageIO.write(shot,"png",out.resolve("03-pc-login.png").toFile());
   login.password.setText("7135");login.password.postActionEvent();login.password.postActionEvent();ok(done[0]==1,"enter completes once");hud.removeNotify();
  }catch(Exception e){throw new RuntimeException(e);}});
  s.complete("PZ006");ok(s.available("PZ001"),"PC unlock opens Rush Hour");System.out.println("PASS "+checks+" cipher, PC input, persistence and offscreen UI checks; no game window.");
 }
}
