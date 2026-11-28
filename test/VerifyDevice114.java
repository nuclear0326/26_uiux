package timeescape.ui;
import timeescape.core.GameState;

/** Headless device action checks for the archive-journal repair gate. */
public final class VerifyDevice114 {
    static int checks;
    static void ok(boolean condition,String message){if(!condition)throw new AssertionError(message);checks++;}
    static GameState pastWithSpare(){
        GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");
        s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");
        s.complete("PZ007");s.complete("PZ021");s.complete("PZ008");
        for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);
        s.complete("PZ010");s.complete("PZ011");s.moveToPast();s.collectSparePart();return s;
    }
    public static void main(String[] args){
        GameState state=pastWithSpare();
        ok(!state.available("ACT_REPAIR"),"spare alone does not make device action available");
        DeviceActions beforeRead=new DeviceActions(state,true,()->{},()->{},null);
        ok(!beforeRead.isActionEnabled(),"device UI blocks repair before shelf journal is read");
        state.complete("PZ022");
        ok(state.has("ARCHIVE_LOG")&&!state.available("ACT_REPAIR"),"solving bookshelf grants journal but does not yet allow repair");
        DeviceActions unread=new DeviceActions(state,true,()->{},()->{},null);
        ok(!unread.isActionEnabled(),"unread journal keeps device repair disabled");
        state.complete("ACT_READ_ARCHIVE");
        ok(state.available("ACT_REPAIR"),"reading journal makes repair available");
        int[] changed={0};
        DeviceActions ready=new DeviceActions(state,true,()->{},()->changed[0]++,null);
        ok(ready.isActionEnabled(),"selected spare enables device repair after reading");
        ready.activate();
        ok(state.isWatchRepaired()&&state.done("GATE002")&&changed[0]==1,"repair commits once and opens future gate");
        ready.activate();ok(changed[0]==1,"repeated activation is ignored");
        System.out.println("PASS "+checks+" journal-gated wrist device action checks; no game window launched.");
    }
}
