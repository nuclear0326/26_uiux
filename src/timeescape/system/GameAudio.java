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
import javax.swing.Timer;
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
    private final Timer fade;
    private String currentScene = "";
    private Clip terminal;
    private Clip menuThunder;
    private Clip menuStart;
    private final java.util.concurrent.CopyOnWriteArrayList<Clip> effects = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final java.util.Map<String, Long> lastEffects = new java.util.HashMap<>();
    private boolean clockSpinning;
    private final Timer clockRestore = new Timer(1900, e -> {
        clockSpinning = false;
        refreshTrackGains();
    });
    private boolean inPuzzle;
    private boolean muted;

    private GameAudio() {
        fade = new Timer(45, e -> animate());
        clockRestore.setRepeats(false);
    }

    private static final class Track {
        final Clip clip;
        final boolean clock;
        float target;
        float gain;
        Track(Clip clip, float target, boolean clock) { this.clip = clip; this.target = target; this.clock = clock; }
    }

    private Clip openClip(String path) {
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
        if (!moving) fade.stop();
    }

    private void changeScene(String scene, String bgm, String ambience) {
        if (scene.equals(currentScene)) return;
        currentScene = scene;
        for (Track t : tracks) t.target = 0f;
        // Gently crossfade the old era out while the new era fades in.
        loop("bgm/" + bgm + ".wav", BGM_VOLUME);
        if (ambience != null) loop("ambience/" + ambience + ".wav", AMBIENCE_VOLUME);
        if ("MENU".equals(scene)) loop("ambience/rain.wav", .18f);
        fade.start();
    }

    public void menu() {
        changeScene("MENU", "menu", null);
        if (!java.awt.GraphicsEnvironment.isHeadless() && (menuThunder==null || !menuThunder.isOpen()))
            menuThunder=openClip("sfx/menu_thunder.wav");
    }

    public void era(Era era) {
        switch (era) {
            case PRESENT -> changeScene("PRESENT", "present", "rain");
            case PAST -> changeScene("PAST", "past", null);
            case FUTURE -> changeScene("FUTURE", "future", "reactor");
        }
    }

    public void puzzle(boolean opening) {
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

    /** One continuous soundtrack is the master clock for the start visuals. */
    public void startMenuTransition() {
        stopMenuTransition();
        if (java.awt.GraphicsEnvironment.isHeadless()) return;
        menuStart = openClip("sfx/menu_start.wav");
        if (menuStart == null) return;
        try { setGain(menuStart, 1f); menuStart.start(); }
        catch (RuntimeException ex) { stopMenuTransition(); }
    }

    public double menuTransitionSeconds() {
        if (menuStart == null || !menuStart.isOpen()) return -1;
        // If an audio device stops unexpectedly, let the UI clock finish safely.
        if (!menuStart.isRunning() && menuStart.getFramePosition() < menuStart.getFrameLength()) return -1;
        return menuStart.getMicrosecondPosition() / 1_000_000.0;
    }

    public void stopMenuTransition() {
        if (menuStart != null) { menuStart.stop(); menuStart.close(); menuStart = null; }
    }

    public void play(String effect) {
        if (java.awt.GraphicsEnvironment.isHeadless()) return;
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
            clockRestore.restart();
        }
        setGain(c, SFX_VOLUME);
        c.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                effects.remove(c);
                c.close();
            }
        });
        try { c.start(); } catch (RuntimeException ex) { effects.remove(c); c.close(); }
    }

    public void shutdown() {
        if(menuThunder!=null){menuThunder.stop();menuThunder.close();menuThunder=null;}
        stopMenuTransition();
        fade.stop();
        clockRestore.stop();
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


