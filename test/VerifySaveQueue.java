import timeescape.system.*;import timeescape.core.*;import java.nio.file.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.*;
public class VerifySaveQueue {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static <T>T await(CompletableFuture<T> f)throws Exception{return f.get(5,TimeUnit.SECONDS);}
 static void fail(CompletableFuture<?> f)throws Exception{try{await(f);throw new AssertionError("expected failure");}catch(ExecutionException e){checks++;}}
 public static void main(String[] args)throws Exception{
  Path dir=Files.createTempDirectory("timeescape-save-queue-");SaveSlots slots=new SaveSlots(dir,ProgressCatalog.standard());SaveQueue q=new SaveQueue(slots);
  CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);var blocker=q.submit(()->{entered.countDown();if(!release.await(3,TimeUnit.SECONDS))throw new Exception("timeout");return null;});check(entered.await(2,TimeUnit.SECONDS),"worker held");
  GameState state=new GameState();var fresh=state.snapshot();state.complete("PZ004");var power=state.snapshot();
  AtomicReference<CompletableFuture<Void>> a=new AtomicReference<>();AtomicReference<CompletableFuture<Boolean>> m=new AtomicReference<>();AtomicReference<CompletableFuture<SaveStore.Loaded>> l=new AtomicReference<>();AtomicBoolean callbackEdt=new AtomicBoolean();
  SwingUtilities.invokeAndWait(()->{a.set(q.saveAuto(power,true));a.get().thenRun(()->callbackEdt.set(SwingUtilities.isEventDispatchThread()));m.set(q.saveManual(SaveSlots.Slot.ONE,power));l.set(q.load(SaveSlots.Slot.ONE));});
  long before=System.nanoTime();SwingUtilities.invokeAndWait(()->{});double pulse=(System.nanoTime()-before)/1e6;check(!a.get().isDone()&&!m.get().isDone()&&!l.get().isDone(),"IO queued without blocking EDT");
  state.complete("ACT_GET_CIPHER");state.complete("PZ006");release.countDown();await(blocker);await(a.get());await(m.get());check(await(l.get()).state().snapshot().equals(power),"manual snapshot immutable/order");check(callbackEdt.get(),"completion on EDT");
  var newer=state.snapshot();var f1=q.saveAuto(power,false);var f2=q.saveAuto(newer,false);var f3=q.load(SaveSlots.Slot.AUTO);await(f1);await(f2);check(await(f3).state().snapshot().equals(newer),"latest auto wins");
  var summaries=await(q.summaries());check(summaries.size()==5&&summaries.get(1).loadable(),"slot query serialized");
  byte[] manual=Files.readAllBytes(slots.store(SaveSlots.Slot.ONE).file());await(q.saveAuto(fresh,true));check(java.util.Arrays.equals(manual,Files.readAllBytes(slots.store(SaveSlots.Slot.ONE).file())),"new game preserves manual");
  await(q.saveAuto(newer,false));await(q.saveAuto(power,false));Files.writeString(slots.store(SaveSlots.Slot.AUTO).file(),"broken");check(await(q.load(SaveSlots.Slot.AUTO)).recoveredFromBackup(),"old backup recovery preserved");
  fail(q.load(SaveSlots.Slot.FOUR));check(await(q.load(SaveSlots.Slot.ONE)).state().snapshot().equals(power),"failed load leaves other files intact");
  await(q.saveManual(SaveSlots.Slot.TWO,newer));var finalSave=q.saveAuto(newer,false);var barrier=q.barrier();await(barrier);check(finalSave.isDone(),"barrier after final save");q.close();check(slots.load(SaveSlots.Slot.AUTO).state().snapshot().equals(newer),"final save persisted before shutdown");fail(q.barrier());
  Path blocked=Files.createTempFile("timeescape-save-blocked-", ".tmp");SaveSlots badSlots=new SaveSlots(blocked,ProgressCatalog.standard());SaveQueue retry=new SaveQueue(badSlots);fail(retry.saveAuto(fresh,true));Files.delete(blocked);Files.createDirectory(blocked);await(retry.saveAuto(power,false));check(badSlots.load(SaveSlots.Slot.AUTO).state().snapshot().equals(power),"retry after disk failure");check(Files.exists(blocked.resolve("progress.save.bak")),"pending reset retained after failed first save");retry.close();
  System.out.printf("PASS %d save queue checks; EDT pulse %.3f ms with blocked IO; temporary test saves only%n",checks,pulse);
 }
}
