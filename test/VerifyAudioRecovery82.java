package timeescape.system;
import java.util.concurrent.*;import java.util.concurrent.atomic.*;import java.util.function.*;import javax.sound.sampled.Clip;import javax.swing.SwingUtilities;
public final class VerifyAudioRecovery82 {
 static int checks;static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
 static void drain(GameAudio a)throws Exception{VerifyAsyncAudio.drain(a);}
 static void until(BooleanSupplier ready)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(3);while(!ready.getAsBoolean()){if(System.nanoTime()>end)throw new AssertionError("Timed out");Thread.sleep(5);}}
 static void close(GameAudio a)throws Exception{a.shutdown();check(VerifyAsyncAudio.worker(a).awaitTermination(2,TimeUnit.SECONDS),"worker shuts down");}
 public static void main(String[] args)throws Exception{
  var b=new VerifyAsyncAudio.Backend();var calls=new ConcurrentHashMap<String,AtomicInteger>();
  GameAudio a=new GameAudio(path->{int n=calls.computeIfAbsent(path,k->new AtomicInteger()).incrementAndGet();if((path.equals("sfx/menu_start.wav")||path.equals("ambience/rain.wav"))&&n<3)return null;return b.apply(path);},false,new long[]{15,35,80});
  SwingUtilities.invokeAndWait(a::menu);drain(a);CountDownLatch pulse=new CountDownLatch(1);SwingUtilities.invokeLater(pulse::countDown);check(pulse.await(300,TimeUnit.MILLISECONDS),"EDT responsive while recovery waits");
  until(()->b.by.containsKey("ambience/rain.wav"));drain(a);
  check(b.by.get("ambience/rain.wav").running,"rain recovers after transient failure");check(b.by.get("sfx/menu_start.wav").open,"intro prewarm recovers");
  check(b.counts.get("bgm/menu.wav").get()==1,"healthy BGM never restarted by rain recovery");check(calls.get("ambience/rain.wav").get()==3,"only failed clip retried");
  a.menu();drain(a);check(b.counts.get("bgm/menu.wav").get()==1&&b.counts.get("ambience/rain.wav").get()==1,"same scene has no duplicate loops");close(a);check(!b.wrong.get(),"device operations stay on worker");check(b.all.stream().noneMatch(f->f.open),"recovered clips released");

  var permanentCalls=new AtomicInteger();var b2=new VerifyAsyncAudio.Backend();
  GameAudio permanent=new GameAudio(path->{if(path.equals("ambience/rain.wav")){permanentCalls.incrementAndGet();return null;}return b2.apply(path);},false,new long[]{10,20});
  permanent.menu();until(()->permanentCalls.get()==3);Thread.sleep(80);drain(permanent);check(permanentCalls.get()==3,"permanent failure retry count bounded");check(b2.counts.get("bgm/menu.wav").get()==1,"no BGM duplication on permanent rain failure");close(permanent);

  var staleCalls=new AtomicInteger();var b3=new VerifyAsyncAudio.Backend();
  GameAudio switched=new GameAudio(path->{if(path.equals("ambience/rain.wav")){staleCalls.incrementAndGet();return null;}return b3.apply(path);},false,new long[]{100});
  switched.menu();drain(switched);switched.era(timeescape.model.Era.PAST);drain(switched);Thread.sleep(150);drain(switched);check(staleCalls.get()==1,"old scene recovery cancelled");check(b3.by.get("bgm/past.wav").running,"new scene runs");close(switched);

  var introCalls=new AtomicInteger();var b4=new VerifyAsyncAudio.Backend();
  GameAudio intro=new GameAudio(path->{if(introCalls.incrementAndGet()<3)return null;return b4.apply(path);},false,new long[]{20,40});
  intro.startMenuTransition();drain(intro);check(intro.menuTransitionSeconds()==-2,"UI holds timeline during retries");until(()->b4.by.containsKey("sfx/menu_start.wav"));drain(intro);var clip=b4.by.get("sfx/menu_start.wav");check(clip.starts==1,"recovered intro starts once");clip.us=5_000_000;until(()->intro.menuTransitionSeconds()==5);check(intro.menuTransitionSeconds()==5,"fracture time follows original audio clock");close(intro);

  var cancelCalls=new AtomicInteger();var b5=new VerifyAsyncAudio.Backend();
  GameAudio cancel=new GameAudio(path->{if(cancelCalls.incrementAndGet()==1)return null;return b5.apply(path);},false,new long[]{100});
  cancel.startMenuTransition();drain(cancel);cancel.stopMenuTransition();drain(cancel);Thread.sleep(150);drain(cancel);check(cancelCalls.get()==1&&b5.all.isEmpty(),"cancel prevents delayed intro playback");check(cancel.menuTransitionSeconds()==-1,"cancel releases timeline wait");close(cancel);

  var noDeviceCalls=new AtomicInteger();GameAudio noDevice=new GameAudio(path->{noDeviceCalls.incrementAndGet();return null;},false,new long[]{10,20});noDevice.startMenuTransition();until(()->noDevice.menuTransitionSeconds()==-1);check(noDeviceCalls.get()==3,"no-device intro falls back after bounded attempts");close(noDevice);
  var shots=new AtomicInteger();GameAudio oneShot=new GameAudio(path->{shots.incrementAndGet();return null;},false,new long[]{10,20});oneShot.play("menu_thunder");drain(oneShot);Thread.sleep(80);drain(oneShot);check(shots.get()==1,"no delayed thunder retry");close(oneShot);
  var shutdownCalls=new AtomicInteger();GameAudio shutdown=new GameAudio(path->{shutdownCalls.incrementAndGet();return null;},false,new long[]{150});shutdown.menu();drain(shutdown);int count=shutdownCalls.get();close(shutdown);Thread.sleep(200);check(shutdownCalls.get()==count,"no recovery after shutdown");
  System.out.println("PASS "+checks+" audio transient failure, bounded retry, duplicate prevention, cancellation and timing checks");
 }
}
