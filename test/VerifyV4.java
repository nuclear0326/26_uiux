import timeescape.puzzle.FileRushHourModel;
import timeescape.core.GameState;
import timeescape.model.Era;
public final class VerifyV4 {
 public static void main(String[] args) {
   FileRushHourModel p=new FileRushHourModel();
   var steps=p.solution();
   if (steps.size()<5 || steps.size()>25) throw new AssertionError("Unexpected puzzle path: "+steps.size());
   System.out.println("Puzzle solution in "+steps.size()+" moves: "+steps);
   if(p.move("SYS",4)) throw new AssertionError("Target slipped through blockers");
   for(var step:steps){ if(!p.move(step.blockId(),step.to())) throw new AssertionError(step); }
   if(!p.solved()) throw new AssertionError("Target export not completed");
   GameState s=new GameState();
   if(s.era()!=Era.PRESENT) throw new AssertionError("Should start in present");
   boolean blocked=false;try{s.moveToPast();}catch(IllegalStateException e){blocked=true;}
   if(!blocked)throw new AssertionError("Past should be gated");
   s.complete("PZ004");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();
   s.collect("USB");s.complete("PZ007");s.complete("PZ008");
   s.collect("CASE");s.collect("CELL");s.collect("MODULE");s.complete("PZ010");s.complete("PZ011");s.moveToPast();
   blocked=false;try{s.moveToFuture();}catch(IllegalStateException e){blocked=true;}
   if(!blocked)throw new AssertionError("Future should require repair");
   s.collectSparePart();s.repairWatch();s.moveToFuture();
   s.collectCorePart();s.installCorePart();s.escape();
   if(!s.isEscaped())throw new AssertionError("Ending not reached");
   System.out.println("Stage flow passed: PRESENT -> PAST -> FUTURE -> ESCAPE");
 }
}
