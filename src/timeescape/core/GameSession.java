package timeescape.core;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import timeescape.system.SaveQueue;
import timeescape.system.SaveSlots;
import timeescape.system.SaveStore;

/** Owns the active progress and save workflow. Call mutations on the Swing EDT.
 * File IO stays in SaveQueue; loading is committed only after the view is ready.
 */
public final class GameSession implements AutoCloseable {
    public static final class PendingLoad {
        private final GameSession owner;
        private final long revision;
        private final SaveStore.Loaded loaded;
        private PendingLoad(GameSession owner,long revision,SaveStore.Loaded loaded){this.owner=owner;this.revision=revision;this.loaded=loaded;}
        public boolean recoveredFromBackup(){return loaded.recoveredFromBackup();}
    }
    private final SaveSlots slots;
    private final SaveQueue queue;
    private GameState state=GameState.newGame();
    private boolean started,replaceSave,closed,autoExists=true,anySave;
    private long sessionRevision,availabilityRevision;

    public GameSession(){this(SaveStore.defaultDirectory());}
    public GameSession(Path directory){slots=new SaveSlots(Objects.requireNonNull(directory),ProgressCatalog.standard());queue=new SaveQueue(slots);}
    public GameState state(){return state;}
    public boolean started(){return started;}
    public boolean autoExists(){return autoExists;}
    public boolean anySave(){return anySave;}
    public boolean isCurrent(GameState expected){return !closed&&state==expected;}
    public Path autoSaveFile(){return slots.store(SaveSlots.Slot.AUTO).file();}
    private void ensureOpen(){if(closed)throw new IllegalStateException("Session closed");}

    public void startNew(){ensureOpen();state=GameState.newGame();started=true;replaceSave=true;sessionRevision++;availabilityRevision++;}
    public CompletableFuture<Void> saveAuto(){
        ensureOpen();if(!started)return queue.barrier();
        availabilityRevision++;boolean reset=replaceSave;replaceSave=false;
        return queue.saveAuto(state.snapshot(),reset).thenRun(()->{if(!closed){autoExists=true;anySave=true;}});
    }
    public CompletableFuture<Boolean> saveManual(SaveSlots.Slot slot){
        ensureOpen();if(!started)return CompletableFuture.completedFuture(false);
        availabilityRevision++;
        return queue.saveManual(slot,state.snapshot()).thenApply(ok->{if(!closed&&ok)anySave=true;return ok;});
    }
    public CompletableFuture<PendingLoad> prepareLoad(SaveSlots.Slot slot){
        ensureOpen();long revision=++sessionRevision;
        return saveAuto().thenCompose(ignored->queue.load(slot)).thenApply(loaded->new PendingLoad(this,revision,loaded));
    }
    public boolean applyLoaded(PendingLoad pending){
        Objects.requireNonNull(pending);
        if(closed||pending.owner!=this||pending.revision!=sessionRevision)return false;
        state=pending.loaded.state();started=true;replaceSave=false;sessionRevision++;availabilityRevision++;return true;
    }
    public CompletableFuture<List<SaveSlots.Summary>> summaries(){ensureOpen();return queue.summaries();}
    public CompletableFuture<Void> refreshAvailability(){
        ensureOpen();long revision=++availabilityRevision;
        return queue.summaries().thenAccept(list->{
            if(closed||revision!=availabilityRevision)return;
            autoExists=list.stream().anyMatch(v->v.slot()==SaveSlots.Slot.AUTO&&v.exists());
            anySave=list.stream().anyMatch(SaveSlots.Summary::exists);
        });
    }
    /** Called only after the final progress and settings saves succeed. */
    @Override public void close(){if(closed)return;closed=true;sessionRevision++;availabilityRevision++;queue.close();}
}
