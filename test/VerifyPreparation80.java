package timeescape.ui;
import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.*;import java.awt.image.BufferedImage;import java.util.Arrays;
public final class VerifyPreparation80 {
 static int checks;static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
 public static void main(String[]args)throws Exception{
  AtomicInteger count=new AtomicInteger();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
  var prep=new GamePreparation(()->{check(!SwingUtilities.isEventDispatchThread(),"loader is off EDT");count.incrementAndGet();entered.countDown();try{release.await();}catch(InterruptedException e){throw new RuntimeException(e);}return null;});
  check(count.get()==0,"constructor does not load resources");
  var a=prep.prepare();check(a==prep.prepare(),"single preparation future");check(entered.await(5,TimeUnit.SECONDS),"worker starts");
  AtomicBoolean responsive=new AtomicBoolean();SwingUtilities.invokeAndWait(()->responsive.set(true));check(responsive.get()&&!a.isDone(),"EDT responds while resources wait");
  CompletableFuture<Boolean> callback=prep.readyOnEdt().thenApply(v->SwingUtilities.isEventDispatchThread());release.countDown();check(callback.get(5,TimeUnit.SECONDS),"success completion on EDT");check(count.get()==1,"loader once");
  var broken=new GamePreparation(()->{throw new IllegalStateException("simulated missing asset");});
  check(broken.readyOnEdt().handle((v,e)->e!=null&&SwingUtilities.isEventDispatchThread()).get(5,TimeUnit.SECONDS),"failure completion on EDT");
  var menu=new TextureRain();var lab=new TextureRain(false);check(menu.detail==lab.detail&&menu.events==lab.events,"immutable data shared");
  check(menu.delta!=lab.delta,"animation buffers independent");check(lab.source==null&&lab.weight==null&&lab.layer==null,"no redundant menu-only laboratory buffers");
  var real=new GamePreparation();LabWindowWeather weather=real.prepare().get(15,TimeUnit.SECONDS);
  SwingUtilities.invokeAndWait(()->{
   var scene=new GameScenePanel(id->{},weather);check(scene!=null,"prepared resources construct Swing scene on EDT");
   var first=new BufferedImage(1920,1080,2);var second=new BufferedImage(1920,1080,2);var third=new BufferedImage(1920,1080,2);
   var g=first.createGraphics();weather.draw(g,12.1,.1,1);g.dispose();g=second.createGraphics();weather.draw(g,12.1,.1,1);g.dispose();
   check(Arrays.equals(first.getRGB(0,0,1920,1080,null,0,1920),second.getRGB(0,0,1920,1080,null,0,1920)),"cached frame identical");
   g=third.createGraphics();weather.draw(g,12.1,.1,0);g.dispose();check(!Arrays.equals(first.getRGB(0,0,1920,1080,null,0,1920),third.getRGB(0,0,1920,1080,null,0,1920)),"power changes invalidate cache");
  });
  System.out.println("PASS "+checks+" resource preparation, EDT, shared data and cache checks; no game window/audio.");
 }
}
