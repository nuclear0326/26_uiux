import java.util.*;
import timeescape.core.*;

public final class VerifyCompletionReservation {
    static int checks;
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
    static class Scheduler implements CompletionReservation.Scheduler {
        final List<Runnable> queued=new ArrayList<>(); int cancelled;
        public Runnable schedule(int delay,Runnable callback) {
            check(delay==600,"existing completion delay retained");queued.add(callback);return ()->cancelled++;
        }
        void fire(int index){queued.get(index).run();} // Deliberately deliver cancelled/stale events too.
    }
    public static void main(String[] args) {
        Scheduler scheduler=new Scheduler();CompletionReservation reservation=new CompletionReservation(scheduler);
        int[] calls={0};boolean[] solved={true};
        reservation.schedule(600,()->solved[0],()->calls[0]++);
        reservation.schedule(600,()->true,()->calls[0]++);
        check(scheduler.queued.size()==1,"only one pending completion");
        reservation.cancel();solved[0]=false;scheduler.fire(0);
        check(calls[0]==0&&scheduler.cancelled==1,"reset cancels timer and stale event");
        solved[0]=true;reservation.schedule(600,()->solved[0],()->calls[0]++);
        scheduler.fire(0);check(calls[0]==0,"old completion cannot finish new attempt");
        scheduler.fire(1);scheduler.fire(1);check(calls[0]==1,"new solve completes exactly once");
        reservation.schedule(600,()->false,()->calls[0]++);scheduler.fire(2);
        check(calls[0]==1,"completion rechecks validity");
        reservation.schedule(600,()->true,()->calls[0]++);reservation.close();scheduler.fire(3);
        check(calls[0]==1,"closing before timeout gives no reward");
        reservation.schedule(600,()->true,()->calls[0]++);check(scheduler.queued.size()==4,"disposed session cannot schedule");

        GameState state=new GameState();state.complete("PZ004");state.complete("ACT_GET_CIPHER");state.complete("PZ006");
        Scheduler realStateQueue=new Scheduler();CompletionReservation session=new CompletionReservation(realStateQueue);
        session.schedule(600,()->true,state::completeDiagnosis);session.cancel();realStateQueue.fire(0);
        check(!state.done("PZ001"),"cancel does not mutate real game progress");
        session.schedule(600,()->true,state::completeDiagnosis);realStateQueue.fire(1);realStateQueue.fire(1);
        check(state.done("PZ001")&&!state.usbComplete(),"valid completion grants correct step only");
        System.out.println("PASS "+checks+" Rush Hour completion lifecycle checks; no dialog launched.");
    }
}
