package timeescape.system;

import timeescape.core.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Independent auto/manual checkpoints, all using the same safe save-file format. */
public final class SaveSlots {
    public enum Slot {
        AUTO("자동저장","progress.save"), ONE("저장소 1","slot1.save"), TWO("저장소 2","slot2.save"),
        THREE("저장소 3","slot3.save"), FOUR("저장소 4","slot4.save");
        private final String title,file;
        Slot(String title,String file){this.title=title;this.file=file;}
        public String title(){return title;}
    }
    public record Summary(Slot slot, boolean exists, boolean loadable, boolean recovered,
                          String progress, String time) {}
    private final Map<Slot,SaveStore> stores=new EnumMap<>(Slot.class);
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
    public SaveSlots(Path directory,ProgressCatalog catalog){
        for(Slot slot:Slot.values())stores.put(slot,new SaveStore(directory,catalog,slot.file));
    }
    public SaveStore store(Slot slot){return stores.get(Objects.requireNonNull(slot));}
    public boolean hasAny(){return stores.values().stream().anyMatch(SaveStore::exists);}
    public void saveManual(Slot slot,GameState.Snapshot snapshot)throws IOException{
        if(slot==Slot.AUTO)throw new IllegalArgumentException("Automatic slot is read-only in manual save UI");
        store(slot).save(snapshot);
    }
    public SaveStore.Loaded load(Slot slot)throws IOException{
        return store(slot).load().orElseThrow(()->new IOException("Empty save slot"));
    }
    public List<Summary> summaries(){
        List<Summary> result=new ArrayList<>();
        for(Slot slot:Slot.values()){
            SaveStore store=store(slot);
            if(!store.exists()){result.add(new Summary(slot,false,false,false,"비어 있음",""));continue;}
            try{
                var loaded=load(slot);var state=loaded.state();
                long puzzles=state.snapshot().completed().stream().filter(id->{
                    String kind=state.catalog().steps().get(id).kind();
                    return Set.of("present","rush_hour","gears","wires").contains(kind);
                }).count();
                Path source=loaded.recoveredFromBackup()?store.file().resolveSibling(slot.file+".bak"):store.file();
                String time=DATE.format(Files.getLastModifiedTime(source).toInstant().atZone(ZoneId.systemDefault()));
                String progress=state.locationTitle()+" · "+(state.isEscaped()?"탈출 완료":"퍼즐 "+puzzles+"개 완료");
                if(loaded.recoveredFromBackup())progress+=" · 백업";
                result.add(new Summary(slot,true,true,loaded.recoveredFromBackup(),progress,time));
            }catch(IOException e){result.add(new Summary(slot,true,false,false,"불러올 수 없는 저장 파일",""));}
        }
        return List.copyOf(result);
    }
}
