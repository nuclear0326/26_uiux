package timeescape.system;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
/** Global audio preferences, independent of release-specific progress saves. */
public final class AudioSettings {
 public enum Channel { BGM, AMBIENCE, SFX }
 private static final AudioSettings INSTANCE=new AudioSettings(defaultFile());
 public static AudioSettings get(){return INSTANCE;}
 private final Path file;private final int[] values={100,100,100};
 private final ExecutorService io=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"audio-settings-io");t.setDaemon(true);return t;});
 private final CompletableFuture<Void> ready;
 private volatile String error="";
 public AudioSettings(Path file){this.file=file;ready=CompletableFuture.runAsync(this::load,io);}
 static Path defaultFile(){String override=System.getProperty("timeescape.settingsDir");String local=System.getenv("LOCALAPPDATA");Path root=override!=null?Path.of(override):local==null||local.isBlank()?Path.of(System.getProperty("user.home"),".timeescape"):Path.of(local,"TimeEscape");return root.resolve("audio.properties");}
 public CompletableFuture<Void> ready(){return ready;}
 public synchronized int volume(Channel c){return values[c.ordinal()];}
 public synchronized void set(Channel c,int value){values[c.ordinal()]=Math.max(0,Math.min(100,value));}
 public float multiplier(Channel c){return volume(c)/100f;}
 public String error(){return error;}
 private void load(){try{if(!Files.exists(file))return;Properties p=new Properties();try(var reader=Files.newBufferedReader(file)){p.load(reader);}synchronized(this){for(Channel c:Channel.values()){try{set(c,Integer.parseInt(p.getProperty(c.name(),"100")));}catch(NumberFormatException ignored){}}}}catch(IOException e){error="음량 설정을 읽지 못했습니다.";}}
 public CompletableFuture<Void> save(){return ready.thenRunAsync(()->{try{Properties p=new Properties();synchronized(this){for(Channel c:Channel.values())p.setProperty(c.name(),Integer.toString(volume(c)));}Files.createDirectories(file.toAbsolutePath().getParent());Path temp=Files.createTempFile(file.toAbsolutePath().getParent(),"audio-",".tmp");try{try(var out=Files.newBufferedWriter(temp)){p.store(out,"TIME ESCAPE audio volumes");}try{Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}error="";}finally{Files.deleteIfExists(temp);}}catch(IOException e){error="음량 설정을 저장하지 못했습니다.";throw new CompletionException(e);}},io);}
}
