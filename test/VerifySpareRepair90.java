package timeescape.ui;
import timeescape.core.*;import timeescape.system.*;import timeescape.model.Era;import java.nio.file.*;
public final class VerifySpareRepair90 {
 static int n;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 public static void main(String[] args)throws Exception{
  var s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");s.complete("PZ007");s.complete("PZ021");s.complete("PZ008");for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);s.complete("PZ010");s.complete("PZ011");s.moveToPast();
  ok(!s.available("ACT_REPAIR")&&!s.canEnter("future_1"),"no part no repair or future");
  try{s.repairWatch();throw new AssertionError("repair should require part and journal");}catch(IllegalStateException expected){n++;}
  s.collectSparePart();ok(s.hasSparePart()&&!s.available("ACT_REPAIR"),"spare alone does not unlock repair");
  s.complete("PZ022");ok(s.has("ARCHIVE_LOG")&&!s.available("ACT_REPAIR"),"shelf puzzle grants unread journal but repair stays gated");
  s.complete("ACT_READ_ARCHIVE");ok(s.available("ACT_REPAIR"),"reading journal unlocks repair with the spare");
  var store=new SaveStore(Files.createTempDirectory("spare90-"),ProgressCatalog.standard());store.save(s.snapshot());s=store.load().orElseThrow().state();ok(s.available("ACT_REPAIR"),"existing save resumes before repair");
  s.repairWatch();ok(s.done("ACT_REPAIR")&&s.isWatchRepaired(),"repair committed");ok(!s.hasSparePart()&&!s.isItemUnread("SPARE"),"part consumed once");ok(s.done("GATE002")&&s.canEnter("future_1"),"future unlock follows repair");
  var done=s.snapshot();s.repairWatch();ok(done.equals(s.snapshot()),"duplicate repair unchanged");
  store.save(s.snapshot());s=store.load().orElseThrow().state();ok(s.isWatchRepaired()&&!s.hasSparePart()&&s.canEnter("future_1"),"save retains part and gate states");
  new TimeTravelController(s).travel();ok(s.era()==Era.FUTURE,"future travel succeeds");s.collectCorePart();ok(s.hasCorePart(),"future puzzle reward");s.installCorePart();s.escape();ok(s.isEscaped(),"downstream completion remains reachable");
  System.out.println("PASS "+n+" journal-gated repair, save reload, future and ending checks");
 }
}
