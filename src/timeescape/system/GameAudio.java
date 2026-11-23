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
    private long clockRestoreAt;
    private final boolean disabled;
    private String currentScene = "";
    private Clip terminal;
    private Clip menuThunder;
    private Clip menuStart;
    private final java.util.concurrent.CopyOnWriteArrayList<Clip> effects = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final java.util.Map<String, Long> lastEffects = new java.util.HashMap<>();
    private boolean clockSpinning;
    private boolean inPuzzle;
    private boolean muted;

    private GameAudio() {
        this(null, java.awt.GraphicsEnvironment.isHeadless());
    }

    // Package-private injection keeps verification entirely silent, with no audio device access.
    GameAudio(Function<String, Clip> factory, boolean disabled) {
        this.disabled = disabled;
        clipFactory = factory == null ? this::readClip : factory;
        audio = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "time-escape-audio"); t.setDaemon(true); return t;
        });
        audio.setRemoveOnCancelPolicy(true);
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
        return c;
    }
    private void pollClock() {
        if(clockSpinning && System.nanoTime() >= clockRestoreAt) {
            clockSpinning=false; refreshTrackGains();
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
        final boolean clock;
        float target;
        float gain;
        Track(Clip clip, float target, boolean clock) { this.clip = clip; this.target = target; this.clock = clock; }
    }

    private Clip readClip(String path) {
        try (InputStream raw = getClass().getResourceAsStream(ROOT + path)) {
            if (raw == null) {
                System.err.println("Audio resource missing: " + path);
                return null;
            }
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                Clip clip = AudioSystem.getClip();
                try { clip.open(stream); } catch (Exception failure) { clip.close(); throw failure; }
                return clip;
            }
        } catch (Exception ex) {
            // NullAudio fallback: Linux CI, headless, or unavailable Windows device.
            System.err.println("Audio unavailable (" + path + "): " + ex.getClass().getSimpleName());
            return null;
        }
    }

    private void setGain(Clip clip, float volume) {
        if (clip == null || !clip.isOpen()) return;
        try {
            FloatControl master = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (float) (20.0 * Math.log10(Math.max(0.0001, muted ? 0 : volume)));
            master.setValue(Math.max(master.getMinimum(), Math.min(master.getMaximum(), db)));
        } catch (Exception ignored) {
            // No MASTER_GAIN on some Java audio devices; do not break playback.
        }
    }

    private void loop(String path, float volume) {
        Clip c = openClip(path);
        if (c == null) return;
        Track t = new Track(c, volume, path.equals("ambience/clockwork.wav"));
        t.gain = 0;
        tracks.add(t);
        setGain(c, 0);
        c.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private float trackVolume(Track t) {
        return t.gain * (inPuzzle ? .45f : 1f) * (t.clock && clockSpinning ? .12f : 1f);
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
        if (scene.equals(currentScene)) return;
        currentScene = scene;
        for (Track t : tracks) t.target = 0f;
        // Gently crossfade the old era out while the new era fades in.
        loop("bgm/" + bgm + ".wav", BGM_VOLUME);
        if (ambience != null) loop("ambience/" + ambience + ".wav", AMBIENCE_VOLUME);
        if ("MENU".equals(scene)) loop("ambience/rain.wav", .18f);
        fading = true;
    }

    public void menu() { enqueue(() -> {
        // Prime timing-critical sounds before playback; subsequent triggers perform no file IO.
        if(menuStart==null || !menuStart.isOpen()) menuStart=openClip("sfx/menu_start.wav");
        if(menuThunder==null || !menuThunder.isOpen()) menuThunder=openClip("sfx/menu_thunder.wav");
        changeScene("MENU", "menu", null);
    }); }

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
            if(generation!=transitionGeneration.get()) return;
            transitionActive=false;
            if(menuStart==null || !menuStart.isOpen()) menuStart=openClip("sfx/menu_start.wav");
            if(generation!=transitionGeneration.get() || closed.get()) return;
            if(menuStart==null) { transitionSeconds=-1; return; }
            try {
                menuStart.stop(); menuStart.setFramePosition(0); setGain(menuStart,1f);
                menuStart.start(); transitionSeconds=0; transitionActive=true;
            } catch(RuntimeException e) { transitionSeconds=-1; transitionActive=false; }
        });
    }
    public double menuTransitionSeconds() { return transitionSeconds; }
    public void stopMenuTransition() {
        transitionGeneration.incrementAndGet(); transitionSeconds=-1;
        enqueue(() -> { transitionActive=false; if(menuStart!=null) { menuStart.stop(); menuStart.setFramePosition(0); } });
    }

    public void play(String effect) { enqueue(() -> playEffect(effect)); }
    private void playEffect(String effect) {
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
        if ("clock_spin".equals(effect)) {
            clockSpinning = true;
            refreshTrackGains();
            clockRestoreAt = System.nanoTime() + 1_900_000_000L;
        }
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
        if(menuThunder!=null){menuThunder.stop();menuThunder.close();menuThunder=null;}
        transitionActive=false;
        if(menuStart!=null){menuStart.stop();menuStart.close();menuStart=null;}
        fading = false;
        clockRestoreAt=0;
        clockSpinning = false;
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


