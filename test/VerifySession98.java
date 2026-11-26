import timeescape.core.*;import timeescape.system.*;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;import javax.swing.SwingUtilities;
public final class VerifySession98 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static <T>T edt(Callable<T> task)throws Exception{FutureTask<T> f=new FutureTask<>(task);SwingUtilities.invokeAndWait(f);return f.get();}
 static <T>T waitFor(CompletableFuture<T> f)throws Exception{return f.get(8,TimeUnit.SECONDS);}
 static void failure(CompletableFuture<?> f)throws Exception{try{waitFor(f);throw new AssertionError("expected IO failure");}catch(ExecutionException expected){checks++;}}
 static void analyzed(GameState s){s.takePrototype();s.usePrototype();s.complete("PZ004");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.complete("PZ001");s.collect("USB");s.complete("PZ007");s.complete("PZ021");}
 public static void main(String[] args)throws Exception{
  Path dir=Files.createTempDirectory("session98-");GameSession session=edt(()->new GameSession(dir));
  ok(!session.started(),"menu-only session not started");waitFor(edt(session::saveAuto));ok(!Files.exists(session.autoSaveFile()),"closing before play does not create save");ok(!waitFor(edt(()->session.saveManual(SaveSlots.Slot.ONE))),"manual save before start ignored");
  waitFor(edt(session::refreshAvailability));ok(!session.anySave()&&!session.autoExists(),"empty slot availability");
  edt(()->{session.startNew();analyzed(session.state());return null;});GameState original=session.state();GameState.Snapshot complete=original.snapshot();
  CompletableFuture<Void> saved=edt(session::saveAuto);ok(waitFor(saved.thenApply(v->SwingUtilities.isEventDispatchThread())),"save completion delivered on EDT");ok(session.autoExists()&&session.anySave(),"save updates availability");
  SaveSlots disk=new SaveSlots(dir,ProgressCatalog.standard());ok(disk.load(SaveSlots.Slot.AUTO).state().snapshot().equals(complete),"existing format exact round trip");
  for(var slot:SaveSlots.Slot.values())if(slot!=SaveSlots.Slot.AUTO)ok(waitFor(edt(()->session.saveManual(slot))),"manual slot "+slot);
  Map<String,byte[]> manual=new HashMap<>();for(int i=1;i<=4;i++)manual.put("slot"+i+".save",Files.readAllBytes(dir.resolve("slot"+i+".save")));
  edt(()->{session.state().markItemRead("FAULT_REPORT");return null;});waitFor(edt(session::saveAuto));GameState beforeLoad=session.state();
  GameSession.PendingLoad pending=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.ONE)));ok(session.state()==beforeLoad&&!session.state().isItemUnread("FAULT_REPORT"),"prepare load does not replace active state before UI ready");
  ok(edt(()->session.applyLoaded(pending)),"ready view commits load");ok(session.state()!=beforeLoad&&!session.isCurrent(beforeLoad)&&session.isCurrent(session.state()),"session identity changed for stale callbacks");
  ok(session.state().done("PZ021")&&session.state().has("FAULT_REPORT")&&session.state().available("PZ008")&&session.state().isItemUnread("FAULT_REPORT"),"report repair gate and unread preserved on load");ok(!edt(()->session.applyLoaded(pending)),"load ticket is single use");waitFor(edt(session::saveAuto));
  edt(()->{session.startNew();return null;});waitFor(edt(session::saveAuto));ok(!disk.load(SaveSlots.Slot.AUTO).state().done("PZ021"),"new game replaces only auto progress");for(var e:manual.entrySet())ok(Arrays.equals(e.getValue(),Files.readAllBytes(dir.resolve(e.getKey()))),"manual file remains byte identical");
  GameSession.PendingLoad stale=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.ONE)));edt(()->{session.startNew();return null;});ok(!edt(()->session.applyLoaded(stale))&&!session.state().done("PZ021"),"new game rejects old pending load");
  GameSession.PendingLoad first=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.ONE))),second=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.TWO)));ok(!edt(()->session.applyLoaded(first))&&edt(()->session.applyLoaded(second)),"latest requested load wins");
  GameState retained=session.state();Files.writeString(dir.resolve("slot4.save"),"invalid save");failure(edt(()->session.prepareLoad(SaveSlots.Slot.FOUR)));ok(session.state()==retained,"failed load leaves current state intact");
  failure(edt(()->session.saveManual(SaveSlots.Slot.AUTO)));ok(session.state()==retained,"failed manual request leaves current state intact");
  // Existing backup recovery remains visible to the UI without replaying rewards.
  waitFor(edt(()->session.saveManual(SaveSlots.Slot.THREE)));Files.writeString(dir.resolve("slot3.save"),"corrupt");GameSession.PendingLoad recovered=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.THREE)));ok(recovered.recoveredFromBackup(),"backup recovery forwarded");ok(edt(()->session.applyLoaded(recovered))&&session.state().has("FAULT_REPORT"),"backup progress applied");
  GameSession.PendingLoad closing=waitFor(edt(()->session.prepareLoad(SaveSlots.Slot.ONE)));waitFor(edt(session::saveAuto));edt(()->{session.close();return null;});ok(!edt(()->session.applyLoaded(closing))&&!session.isCurrent(session.state()),"closed session rejects load and callbacks");
  GameSession restarted=edt(()->new GameSession(dir));GameSession.PendingLoad restart=waitFor(edt(()->restarted.prepareLoad(SaveSlots.Slot.AUTO)));ok(edt(()->restarted.applyLoaded(restart))&&restarted.state().available("PZ008"),"fresh session restores repair unlock");edt(()->{restarted.close();return null;});
  Path blocked=Files.createTempFile("session98-blocked-",".tmp");GameSession broken=edt(()->new GameSession(blocked));edt(()->{broken.startNew();return null;});GameState safe=broken.state();failure(edt(broken::saveAuto));ok(broken.state()==safe&&broken.started(),"failed save retains active game for retry");Files.delete(blocked);Files.createDirectory(blocked);waitFor(edt(broken::saveAuto));ok(new SaveSlots(blocked,ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state().snapshot().equals(safe.snapshot()),"retry writes pending new-game save");edt(()->{broken.close();return null;});
  System.out.println("PASS "+checks+" session lifecycle, delayed commit, autosave/manual, backup, stale load, failure/retry and restart checks; no game window.");
 }
}
