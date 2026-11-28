package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;

public final class VerifyEnding117 {
 static int checks;
 static void ok(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
 static void close(EndingPanel p,int mode){switch(mode){case 0->p.confirm.doClick(0);case 1->p.dismiss.doClick(0);case 2->p.getActionMap().get("close").actionPerformed(null);case 3->p.confirm.getActionMap().get("activate").actionPerformed(null);default->p.cancelSession();}}
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"no game windows");
  BufferedImage blank=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);
  for(int mode=0;mode<5;mode++){
   final int selected=mode;
   try(var h=new VerifyInteractions97.Host()){
    h.current=VerifyDevice114.ready();h.current.moveToPast();h.current.collectSparePart();h.current.repairWatch();h.current.moveToFuture();h.current.collectCorePart();h.current.installCorePart();
    int[] closed={0};
    InteractionHost proxy=(InteractionHost)Proxy.newProxyInstance(InteractionHost.class.getClassLoader(),new Class<?>[]{InteractionHost.class},(object,method,params)->{
     if(method.getName().equals("showEnding")){
      h.showEnding();ok(h.current.isEscaped()&&h.saves==1&&h.refreshes==1,"completion and one save request before notice");
      var snapshot=h.current.snapshot();int eventCount=h.events.size();
      EndingPanel panel=new EndingPanel(blank,65,()->closed[0]++);close(panel,selected);panel.confirm.doClick(0);panel.dismiss.doClick(0);panel.cancelSession();
      ok(closed[0]==(selected==4?0:1),"confirm X Esc Enter and dispose close at most once");
      ok(snapshot.equals(h.current.snapshot())&&h.events.size()==eventCount&&h.saves==1,"notice close cannot mutate progress or resave");return null;
     }
     return method.invoke(h,params);
    });
    SwingUtilities.invokeAndWait(()->{
     ItemUseHandler items=new ItemUseHandler(proxy);items.use("DEVICE");
     DeviceActions a=new DeviceActions(h.deviceState,h.deviceSelection,()->{},h.changed,h.intro);a.activate();a.activate();
    });
    ok(h.endings==1&&h.saves==1,"no repeated ending or save");h.roundTrip();ok(h.current.isEscaped(),"ending persisted after reload");
   }
  }
  Path out=timeescape.testutil.ArtifactPaths.output("v117-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();GameState s=VerifyDevice114.ready();s.moveToPast();s.collectSparePart();s.repairWatch();s.moveToFuture();s.collectCorePart();s.installCorePart();s.escape();
   var snapshot=s.snapshot();
   for(int[] size:new int[][]{{1440,875},{900,650},{600,500}}){
    EndingPanel p=new EndingPanel(VerifyDevice114.room(s,size[0],size[1],false),65,()->{throw new AssertionError("render must not close");});p.setSize(size[0],size[1]);p.doLayout();
    ok(p.textFits()&&p.panel.contains(p.confirm.getBounds())&&p.panel.contains(p.dismiss.getBounds()),"text and controls fit");
    ok(p.getComponentCount()==2,"only confirm and X, no game exit or menu action");
    BufferedImage im=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();p.printAll(g);g.dispose();ImageIO.write(im,"png",out.resolve("ending-"+size[0]+".png").toFile());
   }
   ok(snapshot.equals(s.snapshot()),"preview does not mutate progress");
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" ending acknowledgment, one completion/autosave before notice, reload and responsive layout checks; no game launched.");
 }
}
