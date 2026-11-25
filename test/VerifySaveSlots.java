import timeescape.core.*;
import timeescape.system.*;
import timeescape.system.SaveSlots.Slot;
import java.nio.file.*;
import java.util.*;

public class VerifySaveSlots {
    static int checks;
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    public static void main(String[] args)throws Exception{
        if(args.length==1){
            SaveSlots slots=new SaveSlots(Path.of(args[0]),ProgressCatalog.standard());
            if(!slots.load(Slot.ONE).state().powered()||slots.load(Slot.ONE).state().done("PZ006"))throw new AssertionError("manual 1 lost checkpoint");
            if(!slots.load(Slot.TWO).state().done("PZ006"))throw new AssertionError("manual 2 lost checkpoint");
            if(!slots.load(Slot.THREE).state().powered()||!slots.load(Slot.FOUR).state().done("PZ006"))throw new AssertionError("manual 3/4 process restore");
            return;
        }
        Path dir=Files.createTempDirectory(timeescape.testutil.ArtifactPaths.output("v54-release-tests"),"slots-");
        SaveSlots slots=new SaveSlots(dir,ProgressCatalog.standard());
        check(!slots.hasAny(),"empty slots");
        check(slots.summaries().stream().map(SaveSlots.Summary::slot).toList().equals(List.of(Slot.AUTO,Slot.ONE,Slot.TWO,Slot.THREE,Slot.FOUR)),"automatic slot first");
        check(slots.summaries().stream().noneMatch(SaveSlots.Summary::loadable),"empty slots disabled for loading");
        GameState state=new GameState();state.complete("PZ004");
        new SaveStore(dir,ProgressCatalog.standard()).save(state.snapshot());
        check(slots.load(Slot.AUTO).state().powered(),"v45 progress.save loads as auto without migration");
        slots.saveManual(Slot.ONE,state.snapshot());var first=state.snapshot();
        state.complete("PZ006");slots.saveManual(Slot.TWO,state.snapshot());var second=state.snapshot();
        slots.saveManual(Slot.THREE,first);slots.saveManual(Slot.FOUR,second);
        check(slots.load(Slot.THREE).state().snapshot().equals(first),"manual 3 roundtrip");
        check(slots.load(Slot.FOUR).state().snapshot().equals(second),"manual 4 roundtrip");
        slots.store(Slot.AUTO).save(state.snapshot());
        check(slots.load(Slot.ONE).state().snapshot().equals(first),"auto and manual 2 never overwrite manual 1");
        check(slots.load(Slot.TWO).state().snapshot().equals(second),"manual 2 captures selected checkpoint");
        byte[] manual1=Files.readAllBytes(slots.store(Slot.ONE).file()),manual2=Files.readAllBytes(slots.store(Slot.TWO).file());
        slots.store(Slot.AUTO).startNew(new GameState().snapshot());
        check(Arrays.equals(manual1,Files.readAllBytes(slots.store(Slot.ONE).file()))&&Arrays.equals(manual2,Files.readAllBytes(slots.store(Slot.TWO).file())),"new game/reset only resets automatic slot");
        check(slots.load(Slot.THREE).state().snapshot().equals(first)&&slots.load(Slot.FOUR).state().snapshot().equals(second),"new game keeps slots 3 and 4");
        check(slots.summaries().get(1).time().matches("\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}"),"save timestamp shown");
        check(slots.summaries().get(1).progress().contains("퍼즐 1개 완료"),"completed puzzle count shown");
        check(slots.summaries().get(2).progress().contains("현재 · 연구실"),"location shown");
        try{slots.saveManual(Slot.AUTO,second);throw new AssertionError("manual automatic write accepted");}
        catch(IllegalArgumentException expected){checks++;}
        Process child=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java.exe").toString(),
            "-Djava.awt.headless=true","-cp",System.getProperty("java.class.path"),"VerifySaveSlots",dir.toString()).inheritIO().start();
        check(child.waitFor(30,java.util.concurrent.TimeUnit.SECONDS)&&child.exitValue()==0,"manual slots restored in separate JVM");
        slots.saveManual(Slot.ONE,second);Files.writeString(slots.store(Slot.ONE).file(),"damaged");
        check(slots.load(Slot.ONE).recoveredFromBackup()&&slots.load(Slot.ONE).state().snapshot().equals(first),"each manual slot uses its own backup");
        check(slots.summaries().get(1).recovered()&&slots.summaries().get(1).loadable(),"backup recovery shown as loadable");
        check(slots.load(Slot.TWO).state().snapshot().equals(second),"other slot unaffected by corruption");
        Files.writeString(dir.resolve("slot1.save.bak"),"damaged backup");
        check(!slots.summaries().get(1).loadable()&&slots.summaries().get(1).exists(),"corrupt slot shown distinctly from empty");
        check(slots.summaries().get(0).loadable()&&slots.summaries().get(2).loadable(),"one corrupt slot does not block others");
        System.out.println("PASS "+checks+" slot isolation/legacy compatibility/backup/process checks.");
    }
}
