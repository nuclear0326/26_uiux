package timeescape.system;

import timeescape.core.GameState;
import timeescape.core.ProgressCatalog;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

/** UTF-8 save file; only completed milestones, never unfinished puzzle boards. */
public final class SaveStore {
    private final Path file;
    private final Path backup;
    private final ProgressCatalog catalog;
    public record Loaded(GameState state, boolean recoveredFromBackup) {}

    public SaveStore(Path directory, ProgressCatalog catalog) {
        this(directory,catalog,"progress.save");
    }
    SaveStore(Path directory, ProgressCatalog catalog, String filename) {
        if (!Set.of("progress.save","slot1.save","slot2.save","slot3.save","slot4.save").contains(filename))
            throw new IllegalArgumentException("Unknown save filename");
        this.file = directory.toAbsolutePath().resolve(filename);
        this.backup = file.resolveSibling(filename+".bak");
        this.catalog = catalog;
    }
    public static Path defaultDirectory() {
        String override = System.getProperty("timeescape.saveDir");
        if (override != null && !override.isBlank()) return Path.of(override);
        String local = System.getenv("LOCALAPPDATA");
        return (local == null || local.isBlank() ? Path.of(System.getProperty("user.home"),".timeescape")
            : Path.of(local,"TimeEscape")).resolve("save");
    }
    public Path file() { return file; }
    public boolean exists() { return Files.exists(file) || Files.exists(backup); }
    public Optional<Loaded> load() throws IOException {
        if (!Files.exists(file) && !Files.exists(backup)) return Optional.empty();
        IOException first;
        try { return Optional.of(new Loaded(read(file),false)); }
        catch (IOException e) { first=e; }
        try { return Optional.of(new Loaded(read(backup),true)); }
        catch (IOException e) { first.addSuppressed(e); throw first; }
    }
    private GameState read(Path path) throws IOException {
        if (Files.size(path)>65536) throw new IOException("Save file is too large");
        try {
            Properties p=new Properties();
            try (Reader reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)) { p.load(reader); }
            var snapshot=new GameState.Snapshot(Integer.parseInt(required(p,"schemaVersion")),
                required(p,"roomId"),ids(p,"completed"),ids(p,"items"),ids(p,"flags"),ids(p,"unlockedRooms"));
            if (!digest(payload(snapshot)).equals(required(p,"checksum"))) throw new IOException("Save checksum mismatch");
            return GameState.restore(catalog,snapshot);
        } catch (IllegalArgumentException e) { throw new IOException("Invalid or incompatible save",e); }
    }
    public void save(GameState.Snapshot snapshot) throws IOException {
        GameState.restore(catalog,snapshot); // Validate before touching either file.
        Files.createDirectories(file.getParent());
        if (Files.exists(file)) {
            boolean valid;
            try { read(file); valid=true; } catch (IOException e) { valid=false; }
            if (valid) replace(backup,Files.readAllBytes(file));
            // A damaged primary must never replace the recovery copy.
        }
        String data=payload(snapshot);
        replace(file,(data+"checksum="+digest(data)+"\n").getBytes(StandardCharsets.UTF_8));
    }
    /** An explicitly confirmed restart must not resurrect the former run via backup. */
    public void startNew(GameState.Snapshot snapshot) throws IOException {
        save(snapshot);
        replace(backup,Files.readAllBytes(file));
    }
    private static void replace(Path target,byte[] bytes) throws IOException {
        Path temporary=Files.createTempFile(target.getParent(),"progress-",".tmp");
        try {
            try (FileChannel channel=FileChannel.open(temporary,StandardOpenOption.WRITE)) {
                ByteBuffer buffer=ByteBuffer.wrap(bytes);
                while(buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            try { Files.move(temporary,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    private static String payload(GameState.Snapshot s) {
        return "schemaVersion="+s.schemaVersion()+"\nroomId="+s.roomId()+"\ncompleted="+joined(s.completed())
            +"\nitems="+joined(s.items())+"\nflags="+joined(s.flags())+"\nunlockedRooms="+joined(s.unlockedRooms())+"\n";
    }
    private static String joined(Set<String> values) { return String.join(",",new TreeSet<>(values)); }
    private static String required(Properties p,String key) {
        String value=p.getProperty(key);
        if(value==null) throw new IllegalArgumentException("Missing field: "+key);
        return value;
    }
    private static Set<String> ids(Properties p,String key) {
        String value=required(p,key);Set<String> result=new HashSet<>();
        if(!value.isEmpty()) for(String id:value.split(",",-1)) {
            if(!id.matches("[A-Za-z0-9_]+")||!result.add(id)) throw new IllegalArgumentException("Invalid ID list");
        }
        return result;
    }
    private static String digest(String data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8))); }
        catch(java.security.NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
}
