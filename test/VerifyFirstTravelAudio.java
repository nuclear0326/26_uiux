package timeescape.system;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
public final class VerifyFirstTravelAudio {
 static int checks;
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
 public static void main(String[] args)throws Exception{
  var backend=new VerifyAsyncAudio.Backend();var audio=new GameAudio(backend,false);
  SwingUtilities.invokeAndWait(audio::startFirstTravel);VerifyAsyncAudio.drain(audio);
  var clip=backend.by.get("sfx/first_travel.wav");check(clip!=null&&clip.starts==1,"mixed track starts once");
  clip.us=5_921_600;Thread.sleep(60);check(Math.abs(audio.firstTravelSeconds()-5.9216)<.000001,"visual clock follows track");
  audio.play("menu_thunder");VerifyAsyncAudio.drain(audio);check(!backend.by.containsKey("sfx/menu_thunder.wav"),"unrelated thunder suppressed");
  clip.failPosition=true;Thread.sleep(60);check(audio.firstTravelSeconds()==-1,"device loss falls back");
  audio.stopFirstTravel();VerifyAsyncAudio.drain(audio);check(!clip.open&&!clip.running,"stop releases device");
  audio.startFirstTravel();VerifyAsyncAudio.drain(audio);check(backend.by.get("sfx/first_travel.wav").starts==1,"replay opens fresh track");
  audio.shutdown();VerifyAsyncAudio.worker(audio).awaitTermination(3,TimeUnit.SECONDS);check(backend.all.stream().noneMatch(f->f.open),"shutdown releases all clips");check(!backend.wrong.get(),"no audio device work on EDT");
  CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);var delayed=new VerifyAsyncAudio.Backend();
  var wait=new GameAudio(path->{entered.countDown();try{release.await(3,TimeUnit.SECONDS);}catch(Exception e){throw new RuntimeException(e);}return delayed.apply(path);},false);
  wait.startFirstTravel();check(entered.await(2,TimeUnit.SECONDS),"decode begins asynchronously");check(wait.firstTravelSeconds()==-2,"pending clock");
  wait.stopFirstTravel();release.countDown();VerifyAsyncAudio.drain(wait);check(delayed.all.stream().noneMatch(f->f.open||f.starts>0),"cancel during preparation prevents late sound");wait.shutdown();
  System.out.println("PASS "+checks+" first travel audio checks with fake device; no real playback");
 }
}
