import timeescape.core.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class VerifyProgression {
    static int checks;
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    static void blocked(Runnable action) {
        try { action.run(); } catch (IllegalStateException expected) { checks++; return; }
        throw new AssertionError("Action should be blocked");
    }
    static ProgressCatalog parse(String data) throws Exception {
        return ProgressCatalog.read(new StringReader(data));
    }
    static void invalid(String data) throws Exception {
        try { parse(data); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Invalid catalog accepted");
    }
    static void usb(GameState state) {
        state.complete("PZ006"); state.completeDiagnosis(); state.collect("USB"); state.complete("PZ007");
    }
    static void device(GameState state) {
        state.complete("PZ008"); state.collect("CASE"); state.collect("CELL"); state.collect("MODULE");
        state.complete("PZ010"); state.complete("PZ011");
    }
    public static void main(String[] args) throws Exception {
        String data;
        try (var in=VerifyProgression.class.getResourceAsStream("/assets/progression/game.properties")) {
            data=new String(in.readAllBytes(),StandardCharsets.UTF_8);
        }
        ProgressCatalog original=parse(data);
        check(original.steps().size()==15,"all legacy puzzle/action/gate states defined");
        for (boolean firstUsb : new boolean[]{true,false}) {
            GameState state=new GameState(original);
            check(state.roomId().equals("present_1"),"initial room");
            blocked(()->state.enterRoom("past_1")); blocked(()->state.complete("PZ002"));
            blocked(()->state.collect("SPARE")); blocked(()->state.enterRoom("unknown"));
            state.complete("PZ004"); check(state.flag("power_restored"),"environment reward");
            if(firstUsb) usb(state); else device(state);
            check(!state.roomUnlocked("past_1"),"AND gate stays locked after one branch");
            if(firstUsb) device(state); else usb(state);
            check(state.done("GATE001")&&state.roomUnlocked("past_1"),"automatic room unlock");
            check(state.has("DEVICE")&&!state.has("CASE"),"assembly reward and cost");
            check(state.missionCompletedCount("M_USB")==3&&state.missionCompletedCount("M_DEVICE")==4,"UI counts");
            var snap=state.snapshot(); state.moveToPast();
            check(snap.roomId().equals("present_1"),"snapshot detached");
            blocked(()->state.enterRoom("present_1")); // Existing direction remains unchanged.
            state.collectSparePart(); state.repairWatch(); state.collectSparePart();
            check(state.done("PZ002")&&!state.has("SPARE"),"reward cannot be farmed after consumption");
            state.moveToFuture(); state.collectCorePart(); state.installCorePart(); state.collectCorePart();
            check(!state.has("CORE"),"core cannot be farmed");
            state.escape(); check(state.isEscaped()&&state.has("USB"),"ending and retained USB");
            check(state.snapshot().completed().containsAll(Set.of("ACT_REPAIR","PZ003","ACT_INSTALL","ACT_ESCAPE")),"uniform IDs");
            state.complete("ACT_ESCAPE");check(state.isEscaped(),"completion idempotent");
        }
        String reordered=data.replace("step.PZ001.order=20","step.PZ001.order=1")
            .replace("step.PZ011.order=40","step.PZ011.order=1");
        GameState state=new GameState(parse(reordered));state.complete("PZ004");
        check(state.nextStep("M_USB").orElseThrow().id().equals("PZ006"),"display order does not bypass dependencies");
        check(state.nextObjectStep("WORKBENCH").orElseThrow().id().equals("PZ008"),"workbench follows prerequisites");
        usb(state);device(state);check(state.canDepart(),"reordered data preserves gameplay");

        String extended=data.replace("rooms=present_1,past_1,future_1","rooms=present_1,past_1,future_1,present_2")
            .replace("room.present_1.exits=past_1","room.present_1.exits=past_1,present_2")
            .replace("items=USB,CASE,CELL,MODULE,DEVICE,SPARE,CORE","items=USB,CASE,CELL,MODULE,DEVICE,SPARE,CORE,KEY")
            .replace("steps=PZ004,","steps=PZ_EXTRA,PZ_INSIDE,PZ004,") + """

            room.present_2.era=PRESENT
            room.present_2.title=현재 · 숨겨진 방
            room.present_2.exits=present_1
            step.PZ_EXTRA.order=5
            step.PZ_EXTRA.name=숨겨진 방 열기
            step.PZ_EXTRA.room=present_1
            step.PZ_EXTRA.kind=action
            step.PZ_EXTRA.object=HIDDEN_DOOR
            step.PZ_EXTRA.requires=PZ004
            step.PZ_EXTRA.grant=KEY
            step.PZ_EXTRA.unlock=present_2
            step.PZ_INSIDE.order=10
            step.PZ_INSIDE.name=숨겨진 방 장치
            step.PZ_INSIDE.room=present_2
            step.PZ_INSIDE.kind=action
            step.PZ_INSIDE.requires=PZ_EXTRA
            step.PZ_INSIDE.items=KEY
            step.PZ_INSIDE.consume=KEY
            step.PZ_INSIDE.flags=hidden_machine_repaired
            """;
        state=new GameState(parse(extended)); final GameState expanded=state;
        blocked(()->expanded.enterRoom("present_2"));state.complete("PZ004");state.complete("PZ_EXTRA");
        check(state.has("KEY")&&state.roomUnlocked("present_2"),"new data grants item and room");
        blocked(()->expanded.complete("PZ_INSIDE"));state.enterRoom("present_2");state.complete("PZ_INSIDE");
        check(!state.has("KEY")&&state.flag("hidden_machine_repaired"),"new room item and environment rule");
        state.enterRoom("present_1");check(state.done("PZ_INSIDE"),"configured return retains completion");
        check(state.nextObjectStep("WORKBENCH").orElseThrow().id().equals("PZ008"),"extension leaves existing objects intact");
        try { state.snapshot().items().add("KEY");throw new AssertionError("mutable snapshot"); }
        catch(UnsupportedOperationException expected){checks++;}

        invalid(data.replace("step.PZ001.requires=PZ006","step.PZ001.requires=unknown"));
        invalid(data.replace("step.PZ006.requires=PZ004","step.PZ006.requires=PZ001"));
        invalid(data.replace("steps=PZ004,","steps=PZ004,PZ004,"));
        invalid(data.replace("step.PZ001.room=present_1","step.PZ001.room=unknown"));
        invalid(data.replace("step.PZ010.items=CASE,CELL,MODULE","step.PZ010.items=CASE,CELL"));
        invalid(data.replace("schema.version=1","schema.version=999"));
        System.out.println("PASS "+checks+" progression/schema/extension checks; no game launched.");
    }
}
