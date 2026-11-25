package timeescape.ui;

import timeescape.core.*;
import timeescape.model.Era;
import timeescape.scene.InteractionHint;
import timeescape.system.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;

/** Headless state/UI-component checks; no JFrame, audio or game launch. */
public final class VerifyIntroSequence {
    static int checks;
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    static void blocked(Runnable action){try{action.run();throw new AssertionError("action should be blocked");}catch(IllegalStateException expected){checks++;}}
    static GameState roundTrip(GameState state,Path dir)throws Exception{
        SaveStore store=new SaveStore(dir,ProgressCatalog.standard());store.save(state.snapshot());
        GameState loaded=store.load().orElseThrow().state();
        check(loaded.snapshot().equals(state.snapshot()),"save round trip");return loaded;
    }
    public static void main(String[] args)throws Exception{
        Path temp=Files.createTempDirectory("timeescape-intro-");
        GameState s=GameState.newGame();
        check(s.era()==Era.PRESENT&&s.powered()&&!s.done("PZ004"),"powered entry without completing power puzzle");
        check(!s.has("DEVICE")&&!s.roomUnlocked("past_1")&&!s.canDepart(),"pickup required before travel");
        check(InteractionHint.of(s,"CASE")==InteractionHint.READY&&InteractionHint.of(s,"POWER")==InteractionHint.NONE,"box only current target");
        blocked(s::usePrototype);blocked(()->s.enterRoom("past_1"));blocked(()->s.collect("CASE"));blocked(()->s.complete("PZ004"));
        check(roundTrip(s,temp.resolve("start")).introPresent(),"start reload preserves tutorial");
        s.takePrototype();var picked=s.snapshot();s.takePrototype();
        check(s.snapshot().equals(picked),"duplicate box click grants once");
        check(s.has("DEVICE")&&s.isItemUnread("DEVICE")&&s.canDepart()&&!s.done("PZ010"),"prototype available without assembly");
        check(!s.roomUnlocked("past_1"),"pickup alone does not move or unlock");
        check(InteractionHint.of(s,"CASE")==InteractionHint.NONE,"empty box has no hint");
        GameState resumed=roundTrip(s,temp.resolve("picked"));
        AtomicInteger clicks=new AtomicInteger(),notices=new AtomicInteger();
        SwingUtilities.invokeAndWait(()->{
            InventoryPanel p=new InventoryPanel(id->{if(id.equals("DEVICE"))clicks.incrementAndGet();});
            GameState ui=GameState.newGame();p.refresh(ui,false);
            p.setAcquisitionListener((id,name)->notices.incrementAndGet(),()->{});
            ui.takePrototype();p.refresh(ui,false);p.refresh(ui,false);
            check(notices.get()==1,"single pickup notice");
            JButton device=null;for(var c:p.getComponents())if(c instanceof JButton b&&"DEVICE".equals(b.getName()))device=b;
            check(device!=null,"prototype inventory row before PZ010");device.doClick();check(clicks.get()==1,"row routes device action");
            p.acknowledge("DEVICE");check(!ui.isItemUnread("DEVICE"),"hover read state");
        });
        new TimeTravelController(resumed).travel();
        check(resumed.era()==Era.PRESENT&&!resumed.powered()&&resumed.flag("device_broken")&&resumed.prototypeFailed(),"use commits failure in dark present");
        check(resumed.has("DEVICE")&&!resumed.done("PZ007")&&!resumed.done("PZ011"),"travel independent of old missions");
        check(!resumed.roomUnlocked("future_1")&&!resumed.available("ACT_REPAIR"),"no old future shortcut in new scenario");
        blocked(resumed::usePrototype);blocked(resumed::moveToFuture);
        check(roundTrip(resumed,temp.resolve("failure")).prototypeFailed(),"failure and broken state survive reload");
        SaveSlots slots=new SaveSlots(temp.resolve("queue"),ProgressCatalog.standard());SaveQueue q=new SaveQueue(slots);
        q.saveAuto(resumed.snapshot(),true).get(5,TimeUnit.SECONDS);
        check(q.load(SaveSlots.Slot.AUTO).get(5,TimeUnit.SECONDS).state().snapshot().equals(resumed.snapshot()),"autosave includes destination and damage");q.close();
        // Old saves carry no INTRO_START marker and retain their existing progression.
        GameState old=new GameState();old.complete("PZ004");old.complete("PZ006");old.completeDiagnosis();old.collect("USB");old.complete("PZ007");
        old.complete("PZ008");old.collect("CASE");old.collect("CELL");old.collect("MODULE");old.complete("PZ010");old.complete("PZ011");
        old=roundTrip(old,temp.resolve("legacy"));check(!old.introScenario()&&old.canDepart(),"legacy route preserved");
        old.moveToPast();old.collectSparePart();old.repairWatch();old.moveToFuture();check(old.era()==Era.FUTURE,"legacy repair and travel preserved");
        System.out.println("PASS "+checks+" intro, pickup, inventory, save and legacy checks; no game launched");
    }
}
