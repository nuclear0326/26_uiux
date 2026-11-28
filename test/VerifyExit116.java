package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.system.*;

public final class VerifyExit116 {
 static int checks;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static <T>T edt(Callable<T> c)throws Exception{return VerifyController99.edt(c);}
 static MenuConfirmPanel prompt(BufferedImage b,Runnable close){return new MenuConfirmPanel(b,0,close,"게임 종료","게임을 종료하시겠습니까?","종료");}
 static MainMenuPanel menu(){return new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){throw new AssertionError();}public void continueGame(){throw new AssertionError();}public void showSettings(){throw new AssertionError();}public void exitGame(){throw new AssertionError();}});}
 static void waitUntil(java.util.function.BooleanSupplier done)throws Exception{for(int i=0;i<100&&!edt(done::getAsBoolean);i++)Thread.sleep(10);ok(edt(done::getAsBoolean),"asynchronous shutdown stage completed");}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"headless only");Path out=timeescape.testutil.ArtifactPaths.output("v116-preview");Files.createDirectories(out);
  BufferedImage bg=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);
  edt(()->{GameFonts.installDefaults();
   for(int mode=0;mode<4;mode++){int[] calls={0};MenuConfirmPanel p=prompt(bg,()->calls[0]++);switch(mode){case 0->p.cancel.doClick(0);case 1->p.dismiss.doClick(0);case 2->p.getActionMap().get("cancel").actionPerformed(null);default->p.cancelSession();}p.confirm.doClick(0);ok(!p.isConfirmed()&&calls[0]==(mode==3?0:1),"cancel/X/Esc/dispose reject late exit");}
   int[] calls={0};MenuConfirmPanel p=prompt(bg,()->calls[0]++);p.confirm.doClick(0);p.confirm.doClick(0);p.cancel.doClick(0);p.cancelSession();ok(p.isConfirmed()&&calls[0]==1,"exit result retained and closes once");
   for(int[] size:new int[][]{{1440,810},{1020,650},{600,500}}){MainMenuPanel main=menu();main.setSize(size[0],size[1]);BufferedImage back=ExitConfirmDialog.captureVisible(main);MenuConfirmPanel panel=prompt(back,()->{});panel.setSize(size[0],size[1]);panel.doLayout();ok(panel.textFits()&&panel.panel.contains(panel.confirm.getBounds())&&panel.panel.contains(panel.cancel.getBounds()),"exit layout fits");BufferedImage im=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();panel.printAll(g);g.dispose();ImageIO.write(im,"png",out.resolve("exit-"+size[0]+".png").toFile());}
   // Hidden game views must not replace the visible main-menu background.
   JPanel cards=new JPanel(new CardLayout());JPanel visible=new JPanel();visible.setBackground(Color.BLUE);GameScenePanel hidden=new GameScenePanel(id->{});cards.add(visible,"menu");cards.add(hidden,"game");cards.setSize(600,500);cards.doLayout();((CardLayout)cards.getLayout()).show(cards,"menu");ok(ExitConfirmDialog.captureVisible(cards).getRGB(300,250)==Color.BLUE.getRGB(),"capture excludes hidden game card");return null;});
  Path dir=Files.createTempDirectory("exit116-");GameSession session=edt(()->new GameSession(dir));var view=new VerifyController99.View();AtomicBoolean accept=new AtomicBoolean();AtomicInteger shutdown=new AtomicInteger(),settingsCalls=new AtomicInteger();AtomicReference<CompletableFuture<Void>> settings=new AtomicReference<>(new CompletableFuture<>());
  GameView proxy=(GameView)Proxy.newProxyInstance(GameView.class.getClassLoader(),new Class<?>[]{GameView.class},(object,method,params)->{if(method.getName().equals("confirmExit")){MenuConfirmPanel panel=prompt(bg,()->{});(accept.get()?panel.confirm:panel.cancel).doClick(0);return panel.isConfirmed();}return method.invoke(view,params);});
  GameController c=edt(()->new GameController(proxy,session,Map.of(),()->{settingsCalls.incrementAndGet();return settings.get();},shutdown::incrementAndGet));
  try{
   edt(()->{session.startNew();session.state().takePrototype();c.exitGame();return null;});
   ok(!c.isClosing()&&view.enabled&&!view.disposed&&settingsCalls.get()==0&&!Files.exists(session.autoSaveFile()),"cancel leaves window and session active without exit save");
   accept.set(true);edt(()->{c.exitGame();c.requestClose();return null;});waitUntil(()->settingsCalls.get()==1);
   ok(!view.enabled&&!view.disposed&&shutdown.get()==0,"window waits until settings save completes");ok(new SaveSlots(dir,ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state().snapshot().equals(session.state().snapshot()),"progress persisted before final settings and disposal");
   edt(()->{settings.get().completeExceptionally(new IllegalStateException("settings save failure"));return null;});waitUntil(()->view.saveErrors==1);ok(view.enabled&&!view.disposed&&!c.isClosing(),"save failure retains game and permits retry");
   settings.set(new CompletableFuture<>());edt(()->{c.exitGame();c.requestClose();return null;});waitUntil(()->settingsCalls.get()==2);ok(!view.disposed,"retry also waits for settings");edt(()->{settings.get().complete(null);return null;});waitUntil(()->view.disposed);
   edt(()->{c.requestClose();return null;});ok(shutdown.get()==1&&settingsCalls.get()==2,"successful shutdown once, duplicate request ignored");
  }finally{edt(()->{session.close();return null;});}
  // Failure to write progress must not proceed to settings, audio shutdown or dispose.
  Path blocked=Files.createTempDirectory("exit116-block-").resolve("save");GameSession failure=edt(()->new GameSession(blocked));Files.writeString(blocked,"blocked");var failView=new VerifyController99.View();AtomicInteger later=new AtomicInteger();GameController fc=edt(()->new GameController(failView,failure,Map.of(),()->{later.incrementAndGet();return CompletableFuture.completedFuture(null);},later::incrementAndGet));
  try{edt(()->{failure.startNew();fc.exitGame();return null;});waitUntil(()->failView.saveErrors==1);ok(later.get()==0&&failView.enabled&&!failView.disposed,"progress save failure prevents exit");}finally{edt(()->{failure.close();return null;});}
  System.out.println("PASS "+checks+" exit/cancel, compact layout, visible backdrop, real controller save ordering, failure/retry and single shutdown checks; no game windows.");
 }
}
