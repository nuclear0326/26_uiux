package timeescape.system;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;
import timeescape.model.Era;

/**
 * TIME ESCAPE Java Swing 사운드 계층. 외부 오디오 라이브러리 없이 PCM WAV 사용.
 * 게임 진행 로직, 기존 UI, 오브젝트 판정과 독립된 선택형 오디오 서비스다.
 * 장치에 출력 포트가 없거나 소리가 비활성화돼도 게임은 정상 동작한다.
 */
public final class GameAudio {
    private static final GameAudio INSTANCE = new GameAudio();
    public static GameAudio get() { return INSTANCE; }

    private static final String ROOT = "/assets/audio/";
    private static final float BGM_VOLUME = .43f;
    private static final float AMBIENCE_VOLUME = .44f;
    private static final float SFX_VOLUME = .60f;

    private final List<Track> tracks = new ArrayList<>();
    private final ScheduledThreadPoolExecutor audio;
    private final Function<String, Clip> clipFactory;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong transitionGeneration = new AtomicLong();
    private volatile double transitionSeconds = -1;
    private boolean transitionActive;
    private boolean fading;
    private final boolean disabled;
    private String currentScene = "";
    private Clip terminal;
    private Clip menuThunder;
    private Clip menuStart;
    private Clip firstTravel;
    private Clip dialogueKey;
    private final AtomicLong dialogueGeneration=new AtomicLong();
    private boolean travelling;
    private final AtomicLong travelGeneration=new AtomicLong();
    private volatile double travelSeconds=-1;
    private final java.util.concurrent.CopyOnWriteArrayList<Clip> effects = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final java.util.Map<String, Long> lastEffects = new java.util.HashMap<>();
    private boolean inPuzzle;
    private boolean muted;
    private final java.util.Map<Clip,AudioSettings.Channel> channels=new java.util.IdentityHashMap<>();
    private final java.util.Map<Clip,Float> baseGains=new java.util.IdentityHashMap<>();
    public void refreshVolumes(){enqueue(()->{for(var entry:new java.util.ArrayList<>(baseGains.entrySet())){if(entry.getKey().isOpen())setGain(entry.getKey(),entry.getValue());else{baseGains.remove(entry.getKey());channels.remove(entry.getKey());}}});}
    private final long[] recoveryDelays;
    private long sceneGeneration;
    private String sceneBgm, sceneAmbience;
    private ScheduledFuture<?> sceneRetry, transitionRetry;

    private GameAudio() {
        this(null, java.awt.GraphicsEnvironment.isHeadless());
    }

    // Package-private injection keeps verification entirely silent, with no audio device access.
    GameAudio(Function<String, Clip> factory, boolean disabled) {
        this(factory, disabled, new long[]{150,400,1000,2000});
    }
    GameAudio(Function<String, Clip> factory, boolean disabled, long[] recoveryDelays) {
        this.recoveryDelays = recoveryDelays.clone();
        this.disabled = disabled;
        clipFactory = factory == null ? this::readClip : factory;
        audio = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "time-escape-audio"); t.setDaemon(true); return t;
        });
        audio.setRemoveOnCancelPolicy(true);
        AudioSettings.get().ready().thenRun(this::refreshVolumes);
        audio.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        audio.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        if (!disabled) {
            audio.scheduleWithFixedDelay(() -> safely(this::pollClock), 0, 10, TimeUnit.MILLISECONDS);
            audio.scheduleWithFixedDelay(() -> { if(fading) safely(this::animate); }, 45, 45, TimeUnit.MILLISECONDS);
        }
    }

    private void safely(Runnable action) {
        try { action.run(); } catch (RuntimeException e) {
            System.err.println("Audio command failed: " + e.getClass().getSimpleName());
        }
    }
    private void enqueue(Runnable action) {
        if (closed.get() || disabled) return;
        try { audio.execute(() -> { if(!closed.get()) safely(action); }); }
        catch (RejectedExecutionException ignored) { }
    }
    private Clip openClip(String path) {
        if(closed.get() || disabled) return null;
        Clip c;
        try { c = clipFactory.apply(path); }
        catch (RuntimeException e) {
            System.err.println("Audio preparation failed: " + e.getClass().getSimpleName());
            return null;
        }
        if(closed.get() && c != null) { c.close(); return null; }
        if(c!=null){channels.entrySet().removeIf(e->!e.getKey().isOpen());baseGains.entrySet().removeIf(e->!e.getKey().isOpen());channels.put(c,path.startsWith("bgm/")?AudioSettings.Channel.BGM:path.startsWith("ambience/")?AudioSettings.Channel.AMBIENCE:AudioSettings.Channel.SFX);}
        return c;
    }
    private void pollClock() {
        if(travelling && firstTravel!=null) {
            try { travelSeconds=firstTravel.isOpen() ? firstTravel.getMicrosecondPosition()/1_000_000.0 : -1; }
            catch(RuntimeException ignored){travelSeconds=-1;}
        }
        if(!transitionActive) return;
        Clip c=menuStart;
        double seconds=-1;
        try {
            if(c!=null && c.isOpen() && (c.isRunning() || c.getFramePosition()>=c.getFrameLength()))
                seconds=c.getMicrosecondPosition()/1_000_000.0;
        } catch(RuntimeException ignored) { /* Device loss falls back to the UI timeline. */ }
        transitionSeconds=seconds;
    }

    private static final class Track {
        final Clip clip;
        final String path;
        float target;
        float gain;
        Track(Clip clip, float target, String path) { this.clip = clip; this.target = target; this.path = path; }
    }

    private Clip readClip(String path) {
        String stage="resource", format="unknown";
        try (InputStream raw = getClass().getResourceAsStream(ROOT + path)) {
            if (raw == null) {
                System.err.println("Audio resource missing: " + path);
                return null;
            }
            stage="decode";
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                format=stream.getFormat().toString();stage="select-line";
                // Request the original WAV format, not getClip()'s unspecified stereo template.
                var info=new javax.sound.sampled.DataLine.Info(Clip.class,stream.getFormat());
                Clip clip = (Clip) AudioSystem.getLine(info);
                stage="open-line";
                try { clip.open(stream); } catch (Exception failure) { clip.close(); throw failure; }
                return clip;
            }
        } catch (Exception ex) {
            // NullAudio fallback: Linux CI, headless, or unavailable Windows device.
            System.err.println("Audio unavailable (" + path + ", stage="+stage+", format="+format+"): " + ex.getClass().getSimpleName()+": "+ex.getMessage());
            if (Boolean.getBoolean("timeescape.audioDiagnostics")) ex.printStackTrace();
            return null;
        }
    }

    private void setGain(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) return;
        baseGains.put(clip,volume);
        volume *= AudioSettings.get().multiplier(channels.getOrDefault(clip,AudioSettings.Channel.SFX));
        try {
            if(clip.isControlSupported(javax.sound.sampled.BooleanControl.Type.MUTE))((javax.sound.sampled.BooleanControl)clip.getControl(javax.sound.sampled.BooleanControl.Type.MUTE)).setValue(muted||volume==0);
            FloatControl master = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (float) (20.0 * Math.log10(Math.max(0.0001, muted ? 0 : volume)));
            master.setValue(Math.max(master.getMinimum(), Math.min(master.getMaximum(), db)));
        } catch (Exception ignored) {
            // No MASTER_GAIN on some Java audio devices; do not break playback.
        }
    }

    private boolean loop(String path, float volume) {
        for(Track t:tracks)if(t.path.equals(path)&&t.target>0&&t.clip.isOpen())return true;
        Clip c = openClip(path);
        if (c == null) return false;
        try {
            setGain(c, 0);c.loop(Clip.LOOP_CONTINUOUSLY);
            Track t = new Track(c, volume, path);t.gain=0;tracks.add(t);fading=true;
            return true;
        } catch(RuntimeException error) { c.close();return false; }
    }

    private float trackVolume(Track t) {
        return t.gain * (travelling ? 0f : inPuzzle ? .45f : 1f);
    }

    private void refreshTrackGains() {
        for (Track t : tracks) setGain(t.clip, trackVolume(t));
    }

    private void animate() {
        boolean moving = false;
        Iterator<Track> it = tracks.iterator();
        while (it.hasNext()) {
            Track t = it.next();
            t.gain += (t.target - t.gain) * .25f;
            if (Math.abs(t.target - t.gain) <= .012f) t.gain = t.target;
            setGain(t.clip, trackVolume(t));
            if (t.target == 0 && t.gain == 0) {
                t.clip.stop(); t.clip.close(); it.remove();
            } else if (t.gain != t.target) moving = true;
        }
        if (!moving) fading = false;
    }

    private void changeScene(String scene, String bgm, String ambience) {
        if(!scene.equals(currentScene)){
            currentScene=scene;
            for(Track t:tracks)t.target=0f;
            fading=true;
        }
        sceneBgm="bgm/"+bgm+".wav";
        sceneAmbience="MENU".equals(scene)?"ambience/rain.wav":ambience==null?null:"ambience/"+ambience+".wav";
        if(sceneRetry!=null)sceneRetry.cancel(false);
        prepareScene(++sceneGeneration,0);
    }
    /** Retry only persistent scene/preloaded sounds, never a late one-shot effect. */
    private void prepareScene(long generation,int attempt){
        if(closed.get()||generation!=sceneGeneration)return;
        boolean ready=true;
        if("MENU".equals(currentScene)){
            if(menuStart==null||!menuStart.isOpen())menuStart=openClip("sfx/menu_start.wav");
            ready=menuStart!=null;
        }
        if("MENU".equals(currentScene)||"PRESENT".equals(currentScene)){
            if(menuThunder==null||!menuThunder.isOpen())menuThunder=openClip("sfx/menu_thunder.wav");
            ready=(menuThunder!=null)&&ready;
        }
        ready=loop(sceneBgm,BGM_VOLUME)&&ready;
        if(sceneAmbience!=null)ready=loop(sceneAmbience,"MENU".equals(currentScene)?.18f:AMBIENCE_VOLUME)&&ready;
        if(!ready&&attempt<recoveryDelays.length&&!closed.get())
            sceneRetry=audio.schedule(()->safely(()->prepareScene(generation,attempt+1)),recoveryDelays[attempt],TimeUnit.MILLISECONDS);
        else if(!ready&&!closed.get())System.err.println("Audio scene preparation exhausted retries: "+currentScene);
    }

    public void menu() { enqueue(() -> changeScene("MENU", "menu", null)); }

    public void era(Era era) { enqueue(() -> changeEra(era)); }
    private void changeEra(Era era) {
        switch (era) {
            case PRESENT -> changeScene("PRESENT", "present", "rain");
            case PAST -> changeScene("PAST", "past", null);
            case FUTURE -> changeScene("FUTURE", "future", "reactor");
        }
    }

    public void puzzle(boolean opening) { enqueue(() -> changePuzzle(opening)); }
    private void changePuzzle(boolean opening) {
        if (inPuzzle == opening) return;
        inPuzzle = opening;
        if (opening) {
            terminal = openClip("ambience/terminal.wav");
            if (terminal != null) {
                setGain(terminal, .26f);
                terminal.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } else {
            if (terminal != null) { terminal.stop(); terminal.close(); terminal = null; }
        }
        for (Track t : tracks) setGain(t.clip, trackVolume(t));
    }

    /** -2 means waiting for audio preparation: the UI remains responsive but holds the intro at zero. */
    public void startMenuTransition() {
        long generation=transitionGeneration.incrementAndGet();
        if(disabled || closed.get()) { transitionSeconds=-1; return; }
        transitionSeconds=-2;
        enqueue(() -> {
            if(transitionRetry!=null)transitionRetry.cancel(false);
            prepareTransition(generation,0);
        });
    }
    private void prepareTransition(long generation,int attempt){
        if(generation!=transitionGeneration.get()||closed.get())return;
        transitionActive=false;
        if(menuStart==null||!menuStart.isOpen())menuStart=openClip("sfx/menu_start.wav");
        if(generation!=transitionGeneration.get()||closed.get())return;
        if(menuStart!=null){
            try{
                menuStart.stop();menuStart.setFramePosition(0);setGain(menuStart,1f);
                menuStart.start();transitionSeconds=0;transitionActive=true;return;
            }catch(RuntimeException error){menuStart.close();menuStart=null;}
        }
        if(attempt<recoveryDelays.length)
            transitionRetry=audio.schedule(()->safely(()->prepareTransition(generation,attempt+1)),recoveryDelays[attempt],TimeUnit.MILLISECONDS);
        else transitionSeconds=-1; // Device unavailable: keep the existing silent UI fallback.
    }
    public double menuTransitionSeconds() { return transitionSeconds; }
    public void stopMenuTransition() {
        transitionGeneration.incrementAndGet(); transitionSeconds=-1;
        enqueue(() -> { if(transitionRetry!=null)transitionRetry.cancel(false); transitionActive=false; if(menuStart!=null) { menuStart.stop(); menuStart.setFramePosition(0); } });
    }

    /** All decoding stays on the audio worker. -2 holds the visual at its first frame. */
    public void startFirstTravel() {
        long generation=travelGeneration.incrementAndGet();
        if(disabled||closed.get()){travelSeconds=-1;return;}
        travelSeconds=-2;
        enqueue(()->{
            if(generation!=travelGeneration.get())return;
            travelling=true;refreshTrackGains();
            if(firstTravel!=null){firstTravel.stop();firstTravel.close();firstTravel=null;}
            Clip clip=openClip("sfx/first_travel.wav");
            if(generation!=travelGeneration.get()||closed.get()){if(clip!=null)clip.close();return;}
            firstTravel=clip;
            if(clip==null){travelSeconds=-1;return;}
            try{setGain(clip,1f);clip.start();travelSeconds=0;}
            catch(RuntimeException error){clip.close();firstTravel=null;travelSeconds=-1;}
        });
    }
    public double firstTravelSeconds(){return travelSeconds;}
    public void stopFirstTravel(){
        travelGeneration.incrementAndGet();travelSeconds=-1;
        enqueue(()->{travelling=false;if(firstTravel!=null){firstTravel.stop();firstTravel.close();firstTravel=null;}refreshTrackGains();});
    }

    public void dialogueTick(){
        long token=dialogueGeneration.get();
        enqueue(()->{
            if(travelling||token!=dialogueGeneration.get())return;
            if(dialogueKey==null||!dialogueKey.isOpen())dialogueKey=openClip("sfx/dialogue_key.wav");
            if(dialogueKey==null||token!=dialogueGeneration.get())return;
            dialogueKey.stop();dialogueKey.setFramePosition(0);setGain(dialogueKey,.09f);dialogueKey.start();
        });
    }
    public void stopDialogueTick(){dialogueGeneration.incrementAndGet();enqueue(()->{if(dialogueKey!=null)dialogueKey.stop();});}
    public void play(String effect) { enqueue(() -> playEffect(effect)); }
    private void playEffect(String effect) {
        if(travelling)return;
        if ("menu_thunder".equals(effect) && menuThunder!=null && menuThunder.isOpen()) {
            try { menuThunder.stop(); menuThunder.setFramePosition(0); setGain(menuThunder,SFX_VOLUME); menuThunder.start(); }
            catch(RuntimeException ex) { menuThunder.close(); menuThunder=null; }
            return;
        }
        long now = System.nanoTime();
        Long previous = lastEffects.get(effect);
        if (previous != null && now - previous < 75_000_000L) return;
        lastEffects.put(effect, now);
        // Bound short-effect overlap during repeated file movement.
        if (effects.size() >= 8) {
            var snapshot = effects.iterator();
            if (snapshot.hasNext()) { Clip oldest = snapshot.next(); if (effects.remove(oldest)) { oldest.stop(); oldest.close(); } }
        }
        Clip c = openClip("sfx/" + effect + ".wav");
        if (c == null) return;
        effects.add(c);
        setGain(c, SFX_VOLUME);
        c.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                enqueue(() -> { effects.remove(c); c.close(); });
            }
        });
        try { c.start(); } catch (RuntimeException ex) { effects.remove(c); c.close(); }
    }

    public void shutdown() {
        if(!closed.compareAndSet(false,true)) return;
        transitionGeneration.incrementAndGet(); transitionSeconds=-1;
        audio.execute(() -> safely(this::closeAll));
        audio.shutdown();
    }
    private void closeAll() {
        dialogueGeneration.incrementAndGet();if(dialogueKey!=null){dialogueKey.stop();dialogueKey.close();dialogueKey=null;}
        travelGeneration.incrementAndGet();travelling=false;travelSeconds=-1;
        if(firstTravel!=null){firstTravel.stop();firstTravel.close();firstTravel=null;}
        sceneGeneration++;
        if(sceneRetry!=null)sceneRetry.cancel(false);
        if(transitionRetry!=null)transitionRetry.cancel(false);
        if(menuThunder!=null){menuThunder.stop();menuThunder.close();menuThunder=null;}
        transitionActive=false;
        if(menuStart!=null){menuStart.stop();menuStart.close();menuStart=null;}
        fading = false;
        inPuzzle = false;
        for (Clip effect : effects) { effect.stop(); effect.close(); }
        effects.clear();
        lastEffects.clear();
        if (terminal != null) { terminal.stop(); terminal.close(); terminal = null; }
        for (Track t : tracks) { t.clip.stop(); t.clip.close(); }
        tracks.clear();
        currentScene = "";
    }
}


