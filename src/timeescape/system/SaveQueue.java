package timeescape.system;

import java.util.List;
import java.util.concurrent.*;
import javax.swing.SwingUtilities;
import timeescape.core.GameState;

/** All save-directory IO has one owner. Results are published on Swing's EDT. */
public final class SaveQueue implements AutoCloseable {
    @FunctionalInterface public interface Work<T> { T run() throws Exception; }
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "time-escape-save"); t.setDaemon(true); return t;
    });
    private final SaveSlots slots;
    private GameState.Snapshot lastSaved;
    private boolean resetPending;
    public SaveQueue(SaveSlots slots) { this.slots=slots; }
    public <T> CompletableFuture<T> submit(Work<T> work) {
        CompletableFuture<T> result=new CompletableFuture<>();
        try { worker.execute(() -> {
            try { T value=work.run(); SwingUtilities.invokeLater(() -> result.complete(value)); }
            catch(Exception e) { SwingUtilities.invokeLater(() -> result.completeExceptionally(e)); }
        }); } catch(RejectedExecutionException e) { SwingUtilities.invokeLater(() -> result.completeExceptionally(e)); }
        return result;
    }
    public CompletableFuture<Void> saveAuto(GameState.Snapshot snapshot, boolean newGame) {
        return submit(() -> {
            if(newGame) { resetPending=true; lastSaved=null; }
            if(resetPending) slots.store(SaveSlots.Slot.AUTO).startNew(snapshot);
            else if(!snapshot.equals(lastSaved)) slots.store(SaveSlots.Slot.AUTO).save(snapshot);
            resetPending=false; lastSaved=snapshot; return null;
        });
    }
    public CompletableFuture<Boolean> saveManual(SaveSlots.Slot slot, GameState.Snapshot snapshot) {
        return submit(() -> { slots.saveManual(slot,snapshot); return true; });
    }
    public CompletableFuture<SaveStore.Loaded> load(SaveSlots.Slot slot) {
        return submit(() -> {
            var loaded=slots.load(slot);
            resetPending=false;
            lastSaved=slot==SaveSlots.Slot.AUTO&&!loaded.recoveredFromBackup()?loaded.state().snapshot():null;
            return loaded;
        });
    }
    public CompletableFuture<List<SaveSlots.Summary>> summaries() { return submit(slots::summaries); }
    public CompletableFuture<Void> barrier() { return submit(() -> null); }
    /** Call after the final save succeeds; queued work is never discarded. */
    @Override public void close() { worker.shutdown(); }
}
