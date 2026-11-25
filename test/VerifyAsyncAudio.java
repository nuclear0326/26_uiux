package timeescape.system;
import javax.sound.sampled.*;import javax.swing.*;import java.lang.reflect.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import java.util.function.*;
public class VerifyAsyncAudio {
 static int checks; static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static ScheduledThreadPoolExecutor worker(GameAudio a)throws Exception{var f=GameAudio.class.getDeclaredField("audio");f.setAccessible(true);return (ScheduledThreadPoolExecutor)f.get(a);}
 static void drain(GameAudio a)throws Exception{worker(a).submit(()->{}).get(3,TimeUnit.SECONDS);}
 static void close(GameAudio a)throws Exception{a.shutdown();check(worker(a).awaitTermination(3,TimeUnit.SECONDS),"worker terminates");}
 static class Fake implements InvocationHandler {
  volatile boolean open=true,running;volatile int starts;volatile long us;volatile boolean failPosition;final Clip clip;final AtomicBoolean wrong;
  Fake(AtomicBoolean wrong){this.wrong=wrong;clip=(Clip)Proxy.newProxyInstance(Clip.class.getClassLoader(),new Class[]{Clip.class},this);}
  public Object invoke(Object p,Method m,Object[] a){if(!Thread.currentThread().getName().equals("time-escape-audio"))wrong.set(true);
   switch(m.getName()){
    case "isOpen":return open;case "isRunning":case "isActive":return running;
    case "start":starts++;running=true;break;case "loop":running=true;break;case "stop":running=false;break;case "close":open=false;running=false;break;
    case "setFramePosition":us=0;break;case "getMicrosecondPosition":if(failPosition)throw new IllegalStateException("device lost");return us;
    case "getFramePosition":return (int)us;case "getFrameLength":return 9000000;case "getControl":throw new IllegalArgumentException("no gain on fake");
   }
   if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;if(m.getReturnType()==long.class)return 0L;if(m.getReturnType()==float.class)return 0f;return null;
  }
 }
 static class Backend implements Function<String,Clip>{
  final AtomicBoolean wrong=new AtomicBoolean();final List<Fake> all=new CopyOnWriteArrayList<>();final Map<String,Fake> by=new ConcurrentHashMap<>();final Map<String,AtomicInteger> counts=new ConcurrentHashMap<>();
  public Clip apply(String path){if(SwingUtilities.isEventDispatchThread())wrong.set(true);Fake f=new Fake(wrong);all.add(f);by.put(path,f);counts.computeIfAbsent(path,k->new AtomicInteger()).incrementAndGet();return f.clip;}
 }
 public static void main(String[] args)throws Exception{
  Backend b=new Backend();CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);AtomicBoolean first=new AtomicBoolean(true);
  GameAudio a=new GameAudio(path->{if(first.getAndSet(false)){entered.countDown();try{release.await(3,TimeUnit.SECONDS);}catch(InterruptedException e){throw new RuntimeException(e);}}return b.apply(path);},false);
  SwingUtilities.invokeAndWait(a::menu);check(entered.await(2,TimeUnit.SECONDS),"opening in worker");
  CountDownLatch pulse=new CountDownLatch(1);long start=System.nanoTime();SwingUtilities.invokeLater(pulse::countDown);check(pulse.await(500,TimeUnit.MILLISECONDS),"EDT responsive during blocked open");double pulseMs=(System.nanoTime()-start)/1e6;
  SwingUtilities.invokeAndWait(a::startMenuTransition);check(a.menuTransitionSeconds()==-2,"intro waits while preparing");
  a.stopMenuTransition();check(a.menuTransitionSeconds()==-1,"cancel immediate");release.countDown();drain(a);
  check(b.by.get("sfx/menu_start.wav").starts==0,"cancel prevents late start");check(b.by.containsKey("sfx/menu_thunder.wav"),"thunder prewarmed");
  a.startMenuTransition();drain(a);Fake intro=b.by.get("sfx/menu_start.wav");check(intro.starts==1,"intro starts once");check(b.counts.get("sfx/menu_start.wav").get()==1,"cached start clip");
  intro.us=2_450_000;Thread.sleep(45);check(a.menuTransitionSeconds()==2.45,"snapshot follows soundtrack clock");
  intro.failPosition=true;Thread.sleep(45);check(a.menuTransitionSeconds()==-1,"device loss enables UI fallback");intro.failPosition=false;
  a.stopMenuTransition();drain(a);check(!intro.running,"stop resets playback");a.startMenuTransition();drain(a);check(intro.starts==2&&intro.us==0,"replay from zero");
  a.play("menu_thunder");a.play("menu_thunder");drain(a);check(b.counts.get("sfx/menu_thunder.wav").get()==1,"thunder no trigger-time IO");check(b.by.get("sfx/menu_thunder.wav").starts==2,"thunder replay");
  a.era(timeescape.model.Era.PAST);a.era(timeescape.model.Era.FUTURE);a.puzzle(true);a.puzzle(false);drain(a);check(b.by.containsKey("bgm/future.wav"),"scene commands serialized");check(!b.by.get("ambience/terminal.wav").open,"puzzle closed");
  a.play("ui_click");a.play("ui_click");drain(a);check(b.counts.get("sfx/ui_click.wav").get()==1,"effect throttle maintained");close(a);
  check(b.all.stream().noneMatch(f->f.open),"all clips closed");check(!b.wrong.get(),"all device operations confined to audio worker");int n=b.all.size();a.menu();a.play("ui_click");check(b.all.size()==n,"no work after shutdown");
  GameAudio broken=new GameAudio(path->{throw new IllegalStateException("simulated failure");},false,new long[0]);broken.startMenuTransition();drain(broken);check(broken.menuTransitionSeconds()==-1,"open failure does not hang intro");close(broken);
  Backend b2=new Backend();CountDownLatch e2=new CountDownLatch(1),r2=new CountDownLatch(1);GameAudio closing=new GameAudio(path->{e2.countDown();try{r2.await(3,TimeUnit.SECONDS);}catch(Exception e){throw new RuntimeException(e);}return b2.apply(path);},false);
  closing.startMenuTransition();check(e2.await(2,TimeUnit.SECONDS),"opening before shutdown");SwingUtilities.invokeAndWait(closing::shutdown);r2.countDown();check(worker(closing).awaitTermination(3,TimeUnit.SECONDS),"shutdown during open finishes");check(b2.all.stream().noneMatch(f->f.open||f.starts>0),"late prepared clip closed without playing");
  System.out.printf("PASS %d silent async audio checks; EDT pulse %.3f ms during blocked initialization%n",checks,pulseMs);
 }
}
