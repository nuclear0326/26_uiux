package timeescape.ui;
import timeescape.core.*;
import timeescape.model.Era;
import timeescape.scene.InteractionHint;
import timeescape.system.*;
import java.nio.file.*;
public final class VerifyFailedPrototype {
 static int n;
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);n++;}
 static void denied(Runnable action){try{action.run();throw new AssertionError("must block");}catch(IllegalStateException expected){n++;}}
 public static void main(String[] args)throws Exception{
  var s=GameState.newGame();check(s.powered()&&s.introPresent(),"lit present entry");denied(s::usePrototype);
  s.takePrototype();check(s.has("DEVICE"),"prototype pickup");s.usePrototype();
  check(s.era()==Era.PRESENT&&!s.powered()&&s.prototypeFailed(),"failed travel remains dark present");
  check(!s.roomUnlocked("past_1")&&!s.firstTravelComplete(),"no premature past unlock");
  check(s.flag("device_broken")&&!s.done("PZ004"),"failure does not complete power puzzle");
  denied(s::usePrototype);denied(s::moveToPast);var failed=s;denied(()->failed.complete("ACT_FIRST_TRAVEL"));
  check(s.available("PZ004")&&!s.available("PZ006"),"power recovery required");
  check(InteractionHint.of(s,"POWER")==InteractionHint.READY,"power is current objective");
  var store=new SaveStore(Files.createTempDirectory("failed-prototype-"),ProgressCatalog.standard());store.save(s.snapshot());
  s=store.load().orElseThrow().state();check(s.era()==Era.PRESENT&&!s.powered()&&s.has("DEVICE"),"restore accident");
  s.complete("PZ004");check(s.powered()&&s.available("ACT_GET_CIPHER")&&!s.available("PZ006")&&!s.available("PZ008"),"repair blocked until USB analysis");
  s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");s.complete("PZ007");s.complete("PZ021");check(!s.canDepart(),"logs alone insufficient");
  s.complete("PZ008");for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);s.complete("PZ010");s.complete("PZ011");
  check(s.canDepart()&&s.roomUnlocked("past_1"),"repair and logs unlock past");new TimeTravelController(s).travel();
  check(s.era()==Era.PAST,"successful travel only after repair");store.save(s.snapshot());check(store.load().orElseThrow().state().snapshot().equals(s.snapshot()),"repaired destination saved");
  var legacy=new GameState();legacy.complete("PZ004");check(legacy.powered()&&!legacy.prototypeFailed(),"legacy path unchanged");
  var old=new GameState.Snapshot(2,"past_1",java.util.Set.of("INTRO_START","TUT001","ACT_FIRST_TRAVEL"),java.util.Set.of("DEVICE"),java.util.Set.of("intro_power_on","device_broken"),java.util.Set.of("present_1","past_1"),java.util.Set.of("DEVICE"));
  check(GameState.restore(ProgressCatalog.standard(),old).era()==Era.PAST,"retired opening saves still load");
  System.out.println("PASS "+n+" failure, power, logs, repair, travel and compatibility checks");
 }
}
