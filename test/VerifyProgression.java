import timeescape.core.*;
import java.io.StringReader;
import java.util.Set;

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
    public static void main(String[] args) throws Exception {
        ProgressCatalog catalog=ProgressCatalog.standard();
        check(catalog.steps().size()==23,"journal read action is registered");
        GameState state=GameState.newGame();
        state.takePrototype();state.usePrototype();state.complete("PZ004");
        state.complete("ACT_GET_CIPHER");state.complete("PZ006");state.completeDiagnosis();
        state.collect("USB");state.complete("PZ007");state.complete("PZ021");
        state.complete("PZ008");
        for(String id:new String[]{"CASE","CELL","MODULE"})state.collect(id);
        state.complete("PZ010");state.complete("PZ011");
        check(state.done("GATE001")&&state.roomUnlocked("past_1"),"past room gate opens");
        state.moveToPast();check(state.available("PZ022"),"bookshelf puzzle is available in past");
        state.collectSparePart();check(state.hasSparePart(),"brass device yields spare part");
        check(!state.available("ACT_REPAIR"),"spare part alone cannot repair the wrist device");
        blocked(state::repairWatch);
        state.complete("PZ022");
        check(state.has("ARCHIVE_LOG")&&state.flag("archive_record_found"),"solving bookshelf grants research journal");
        check(!state.available("ACT_REPAIR"),"unread journal does not unlock repair");
        state.complete("ACT_READ_ARCHIVE");
        check(state.flag("archive_log_read")&&state.available("ACT_REPAIR"),"reading journal unlocks repair");
        state.repairWatch();
        check(state.isWatchRepaired()&&state.done("GATE002")&&state.canEnter("future_1"),"repair opens future gate");
        state.moveToFuture();state.collectCorePart();state.installCorePart();state.escape();
        check(state.isEscaped()&&state.has("USB"),"full downstream scenario remains completable");
        check(state.snapshot().completed().containsAll(Set.of("PZ022","ACT_READ_ARCHIVE","ACT_REPAIR","PZ003","ACT_INSTALL","ACT_ESCAPE")),"scenario progress is persisted");
        try {
            ProgressCatalog.read(new StringReader("schema.version=999"));
            throw new AssertionError("unsupported schema accepted");
        } catch(IllegalArgumentException expected) { checks++; }
        System.out.println("PASS "+checks+" journal-gated progression and downstream checks");
    }
}
