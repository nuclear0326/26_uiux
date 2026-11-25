import timeescape.core.*;
import timeescape.system.SaveStore;
import timeescape.ui.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class VerifySaveStore {
    static int checks;
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message);checks++; }
    static SaveStore store(Path path) { return new SaveStore(path,ProgressCatalog.standard()); }
    static GameState checkpoint(GameState state,Path path) throws Exception {
        store(path).save(state.snapshot());
        GameState reloaded=store(path).load().orElseThrow().state();
        check(state.snapshot().equals(reloaded.snapshot()),"all progress fields round-trip");
        return reloaded;
    }
    static void prepare(GameState s) {
        s.complete("PZ004");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");s.complete("PZ007");
        s.complete("PZ008");s.collect("CASE");s.collect("CELL");s.collect("MODULE");s.complete("PZ010");s.complete("PZ011");
    }
    static void ioFailure(IORunnable action) throws Exception {
        try { action.run(); } catch(IOException expected) {checks++;return;}
        throw new AssertionError("Expected IO failure");
    }
    interface IORunnable { void run() throws Exception; }
    static void process(String mode,Path path) throws Exception {
        String javaExe=Path.of(System.getProperty("java.home"),"bin","java.exe").toString();
        Process child=new ProcessBuilder(javaExe,"-Djava.awt.headless=true","-cp",System.getProperty("java.class.path"),
            "VerifySaveStore",mode,path.toString()).inheritIO().start();
        if(!child.waitFor(30,java.util.concurrent.TimeUnit.SECONDS)) { child.destroyForcibly();throw new AssertionError("Child timed out"); }
        check(child.exitValue()==0,"separate JVM "+mode);
    }
    public static void main(String[] args) throws Exception {
        if(args.length==2) {
            Path dir=Path.of(args[1]);
            if(args[0].equals("--write")) {
                GameState s=new GameState();prepare(s);s.moveToPast();s.collectSparePart();store(dir).save(s.snapshot());
            } else {
                GameState s=store(dir).load().orElseThrow().state();
                if(!s.roomId().equals("past_1")||!s.hasSparePart()||!s.has("USB")||!s.powered())throw new AssertionError("Restart lost state");
                s.repairWatch();s.moveToFuture();s.collectCorePart();s.installCorePart();s.escape();store(dir).save(s.snapshot());
            }
            return;
        }
        Path root=Files.createTempDirectory(timeescape.testutil.ArtifactPaths.output("v54-release-tests"),"save-tests-");
        Path path=root.resolve("roundtrip");SaveStore disk=store(path);
        check(disk.load().isEmpty()&&!Files.exists(path),"missing save does not create an empty run");
        GameState s=checkpoint(new GameState(),path);
        s.complete("PZ004");s=checkpoint(s,path);check(s.powered(),"power restoration persists");
        s.complete("PZ006");s=checkpoint(s,path);s.completeDiagnosis();s=checkpoint(s,path);
        check(!s.usbComplete(),"unfinished USB mission stays unfinished");
        s.collect("USB");s=checkpoint(s,path);s.complete("PZ007");s=checkpoint(s,path);
        s.complete("PZ008");s.collect("CASE");s.collect("CELL");s=checkpoint(s,path);
        check(!s.done("PZ009"),"partial item collection retained without premature completion");
        s.collect("MODULE");s=checkpoint(s,path);s.complete("PZ010");s=checkpoint(s,path);
        check(!s.has("CASE")&&s.has("DEVICE"),"consumed materials not restored");
        var afterAssembly=s.snapshot();s.complete("PZ010");check(afterAssembly.equals(s.snapshot()),"reload never regrants a reward");
        s.complete("PZ011");s=checkpoint(s,path);check(s.roomUnlocked("past_1"),"opened room persists");
        s.moveToPast();s=checkpoint(s,path);s.collectSparePart();s=checkpoint(s,path);
        s.repairWatch();s=checkpoint(s,path);check(!s.hasSparePart()&&s.roomUnlocked("future_1"),"repair consumption and future unlock persist");
        s.moveToFuture();s=checkpoint(s,path);s.collectCorePart();s=checkpoint(s,path);
        s.installCorePart();s=checkpoint(s,path);s.escape();s=checkpoint(s,path);
        check(s.isEscaped()&&s.has("USB")&&!s.hasCorePart(),"ending snapshot complete");
        try(var files=Files.list(path)) { check(files.noneMatch(p->p.toString().endsWith(".tmp")),"temporary files cleaned"); }

        Path recovery=root.resolve("recovery");SaveStore safe=store(recovery);GameState original=new GameState();
        safe.save(original.snapshot());original.complete("PZ004");safe.save(original.snapshot());
        Files.writeString(safe.file(),"truncated");
        var restored=safe.load().orElseThrow();check(restored.recoveredFromBackup()&&!restored.state().powered(),"corrupt primary recovers prior checkpoint");
        safe.save(restored.state().snapshot());check(!safe.load().orElseThrow().recoveredFromBackup(),"recovered snapshot repairs primary");
        Files.writeString(safe.file(),"bad");Files.writeString(recovery.resolve("progress.save.bak"),"bad backup");
        ioFailure(safe::load);check(Files.readString(safe.file()).equals("bad"),"failed load preserves damaged file");

        Path missingPrimary=root.resolve("backup-only");SaveStore backupOnly=store(missingPrimary);
        backupOnly.save(new GameState().snapshot());backupOnly.save(original.snapshot());Files.delete(backupOnly.file());
        check(backupOnly.load().orElseThrow().recoveredFromBackup(),"missing primary recovers backup");
        backupOnly.startNew(new GameState().snapshot());Files.writeString(backupOnly.file(),"bad");
        check(!backupOnly.load().orElseThrow().state().powered(),"new game also resets recovery point");

        Path failed=root.resolve("failed-write");SaveStore blocked=store(failed);blocked.save(new GameState().snapshot());
        byte[] before=Files.readAllBytes(blocked.file());Files.createDirectory(failed.resolve("progress.save.bak"));
        Files.writeString(failed.resolve("progress.save.bak/obstacle"),"occupied");
        ioFailure(()->blocked.save(original.snapshot()));check(Arrays.equals(before,Files.readAllBytes(blocked.file())),"failed backup write leaves primary intact");
        Path notDirectory=root.resolve("not-directory");Files.writeString(notDirectory,"file");
        ioFailure(()->store(notDirectory).save(original.snapshot()));

        Path checksum=root.resolve("checksum");SaveStore tampered=store(checksum);tampered.save(original.snapshot());
        Files.writeString(tampered.file(),Files.readString(tampered.file()).replace("power_restored","unknown_flag"));ioFailure(tampered::load);
        var invalid=new GameState.Snapshot(999,"present_1",Set.of(),Set.of(),Set.of(),Set.of("present_1"));
        try { GameState.restore(ProgressCatalog.standard(),invalid);throw new AssertionError("version accepted"); }
        catch(IllegalArgumentException expected){checks++;}

        process("--write",root.resolve("process"));process("--resume",root.resolve("process"));
        GameState fromProcess=store(root.resolve("process")).load().orElseThrow().state();
        check(fromProcess.isEscaped()&&fromProcess.roomId().equals("future_1"),"resume after actual process exit");

        javax.swing.SwingUtilities.invokeAndWait(()->{
            try {
                GameScenePanel scene=new GameScenePanel(id->{});scene.restorePowered(true);
                var level=GameScenePanel.class.getDeclaredField("lightLevel");level.setAccessible(true);
                var timer=GameScenePanel.class.getDeclaredField("lightingTimer");timer.setAccessible(true);
                check(level.getFloat(scene)==1&&!((javax.swing.Timer)timer.get(scene)).isRunning(),"resume lit room without replaying flicker");
                scene.restorePowered(false);check(level.getFloat(scene)==0,"fresh room stays dark");
                int[] starts={0};boolean[] allow={false};MainMenuPanel menu=new MainMenuPanel(new MainMenuPanel.MenuActions(){
                    public boolean confirmNewGame(){return allow[0];}public void startGame(){starts[0]++;}
                    public void continueGame(){}public void showSettings(){}public void exitGame(){}
                });menu.setTransitionAnimation(false);
                var activate=MainMenuPanel.class.getDeclaredMethod("activate",int.class);activate.setAccessible(true);
                activate.invoke(menu,0);check(starts[0]==0,"cancel new game keeps existing save");
                allow[0]=true;activate.invoke(menu,0);check(starts[0]==1,"confirmed new game starts");
            }catch(Exception e){throw new RuntimeException(e);}
        });
        System.out.println("PASS "+checks+" save/recovery/separate-process/resume checks. No game, JFrame or EXE launched.");
    }
}
