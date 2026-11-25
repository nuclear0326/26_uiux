import java.util.*;import java.util.concurrent.atomic.*;import java.nio.file.*;import timeescape.core.*;import timeescape.puzzle.*;import timeescape.system.*;
public class VerifyPuzzleRunner {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static Map<String,PuzzleRunner.Launcher> all(PuzzleRunner.Launcher f){Map<String,PuzzleRunner.Launcher> m=new HashMap<>();for(String k:List.of("present","rush_hour","gears","wires","confirm","action","temporary_power"))m.put(k,f);return m;}
 static PuzzleRunner runner(GameState s){return new PuzzleRunner(all(step->PuzzleResult.SOLVED),()->s,()->true,step->{});}
 static void done(PuzzleRunner r,String id){check(r.run(id)==PuzzleRunner.Outcome.COMPLETED,id+" completed");}
 public static void main(String[] args)throws Exception{
  AtomicReference<GameState> current=new AtomicReference<>(new GameState());AtomicInteger launched=new AtomicInteger(),commits=new AtomicInteger();AtomicReference<PuzzleResult> result=new AtomicReference<>(PuzzleResult.CANCELLED);AtomicBoolean active=new AtomicBoolean(true);
  PuzzleRunner r=new PuzzleRunner(all(step->{launched.incrementAndGet();return result.get();}),current::get,active::get,step->{check(current.get().done(step.id()),"callback after state commit");commits.incrementAndGet();});
  check(r.run("PZ006")==PuzzleRunner.Outcome.IGNORED&&launched.get()==0,"prerequisites block launch");check(r.run("GATE001")==PuzzleRunner.Outcome.IGNORED,"automatic step not launched");var before=current.get().snapshot();check(r.run("PZ004")==PuzzleRunner.Outcome.CANCELLED&&current.get().snapshot().equals(before)&&commits.get()==0,"cancel no progress or save callback");
  result.set(PuzzleResult.SOLVED);done(r,"PZ004");check(current.get().powered()&&commits.get()==1,"environment granted once");check(r.run("PZ004")==PuzzleRunner.Outcome.IGNORED&&commits.get()==1,"duplicate no repeat callback");active.set(false);check(r.run("PZ006")==PuzzleRunner.Outcome.IGNORED,"shutdown prevents launch");active.set(true);
  GameState old=current.get();PuzzleRunner stale=new PuzzleRunner(all(step->{current.set(new GameState());return PuzzleResult.SOLVED;}),current::get,()->true,step->{throw new AssertionError("stale committed");});check(stale.run("PZ006")==PuzzleRunner.Outcome.IGNORED&&!old.done("PZ006")&&!current.get().done("PZ006"),"replaced session cannot receive completion");
  GameState s=new GameState();PuzzleRunner[] nested={null};nested[0]=new PuzzleRunner(all(step->{check(nested[0].run(step.id())==PuzzleRunner.Outcome.IGNORED,"reentrant launch ignored");return PuzzleResult.SOLVED;}),()->s,()->true,step->{});done(nested[0],"PZ004");
  AtomicBoolean fail=new AtomicBoolean(true);PuzzleRunner retry=new PuzzleRunner(all(step->{if(fail.getAndSet(false))throw new IllegalStateException("fake UI failure");return PuzzleResult.SOLVED;}),()->s,()->true,step->{});try{retry.run("PZ006");throw new AssertionError("expected throw");}catch(IllegalStateException expected){checks++;}done(retry,"PZ006");
  try{new PuzzleRunner(Map.of(),()->new GameState(),()->true,step->{}).run("PZ004");throw new AssertionError("unregistered kind");}catch(IllegalArgumentException expected){checks++;}
  for(boolean usbFirst:new boolean[]{true,false}){
   GameState game=new GameState(),reference=new GameState();AtomicInteger saves=new AtomicInteger();PuzzleRunner p=new PuzzleRunner(all(step->PuzzleResult.SOLVED),()->game,()->true,step->saves.incrementAndGet());
   done(p,"PZ004");reference.complete("PZ004");
   for(boolean usb:new boolean[]{usbFirst,!usbFirst}){
    if(usb){for(String id:List.of("PZ006","PZ001")){done(p,id);reference.complete(id);}game.collect("USB");reference.collect("USB");done(p,"PZ007");reference.complete("PZ007");}
    else{done(p,"PZ008");reference.complete("PZ008");for(String item:List.of("CASE","CELL","MODULE")){game.collect(item);reference.collect(item);}for(String id:List.of("PZ010","PZ011")){done(p,id);reference.complete(id);}}
    check(game.snapshot().equals(reference.snapshot()),"horizontal branch progression unchanged");
   }
   check(game.canDepart()&&game.has("USB")&&!game.has("CELL"),"gate rewards/consumption unchanged");game.moveToPast();reference.moveToPast();done(p,"PZ002");reference.complete("PZ002");check(game.has("SPARE"),"gear reward");game.repairWatch();reference.repairWatch();game.moveToFuture();reference.moveToFuture();done(p,"PZ003");reference.complete("PZ003");check(game.has("CORE"),"temporary power reward");check(game.snapshot().equals(reference.snapshot())&&saves.get()==9,"all puzzle kinds match existing progress and one callback each");
   Path dir=Files.createTempDirectory("timeescape-runner-");SaveQueue queue=new SaveQueue(new SaveSlots(dir,ProgressCatalog.standard()));queue.saveAuto(game.snapshot(),true).get(5,java.util.concurrent.TimeUnit.SECONDS);check(queue.load(SaveSlots.Slot.AUTO).get(5,java.util.concurrent.TimeUnit.SECONDS).state().snapshot().equals(game.snapshot()),"new completion state persists in existing format");queue.close();
  }
  // Rush Hour uses the same 600ms cancellation gate, but reports a result instead of mutating progress.
  for(String mode:List.of("solve","reset","close")){
   GameState game=new GameState();game.complete("PZ004");game.complete("PZ006");AtomicInteger saved=new AtomicInteger();PuzzleRunner p=new PuzzleRunner(Map.of("rush_hour",step->{
    List<Runnable> pending=new ArrayList<>();CompletionReservation reservation=new CompletionReservation((delay,cb)->{check(delay==600,"Rush Hour delay retained");pending.add(cb);return ()->{};});AtomicReference<PuzzleResult> outcome=new AtomicReference<>(PuzzleResult.CANCELLED);
    reservation.schedule(600,()->true,()->outcome.set(PuzzleResult.SOLVED));if(mode.equals("reset"))reservation.cancel();if(mode.equals("close"))reservation.close();pending.get(0).run();pending.get(0).run();return outcome.get();
   }),()->game,()->true,step->saved.incrementAndGet());var outcome=p.run("PZ001");check(game.done("PZ001")==mode.equals("solve"),"delayed completion "+mode);check(saved.get()==(mode.equals("solve")?1:0),"single completion callback "+mode);
  }
  FileRushHourModel model=new FileRushHourModel();for(var move:model.solution())check(model.move(move.blockId(),move.to()),"Rush Hour legal solution");check(model.solved(),"Rush Hour solved");
  System.out.println("PASS "+checks+" common puzzle runner/progression/save/lifecycle checks; no game or dialog launched");
 }
}
