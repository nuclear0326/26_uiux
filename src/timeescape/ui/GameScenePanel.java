package timeescape.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



import java.awt.image.BufferedImage;

import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import timeescape.model.Era;
import timeescape.scene.Hotspot;
import timeescape.scene.HotspotCatalog;
import timeescape.system.ImageAssets;

/**
 * 게임 배경 + 보이지 않는 클릭 영역.
 * 마우스가 오브젝트 위에 있을 때만 해당 부분을 부드럽고 약하게 밝힌다.
 * 별도 이미지 오브젝트가 없어도 원본 배경을 바꾸지 않는다.
 */
public final class GameScenePanel extends JPanel {
    private static final int IMAGE_W = 1920;
    private static final int IMAGE_H = 1080;
    private final timeescape.system.ScaledBackground backgroundCache = new timeescape.system.ScaledBackground();
    private final timeescape.system.ScaledBackground offBackgroundCache = new timeescape.system.ScaledBackground();
    private boolean powered;
    private boolean inputBlocked;
    public void setInputBlocked(boolean value){inputBlocked=value;if(value)clearHighlights();}
    private float lightLevel;
    private long lightStarted;
    private final Timer lightingTimer = new Timer(33, e -> updateLighting());
    public void setPowered(boolean value) {
        if (powered == value) return;
        powered = value;
        lightingTimer.stop();
        lightLevel = 0;
        if (value) { lightStarted = System.nanoTime(); lightingTimer.start(); }
        repaint();
    }
    /** Resume an already-restored room without replaying its power-on cutscene. */
    public void restorePowered(boolean value) {
        powered = value;
        finishLighting();
        repaint();
    }
    private void updateLighting() {
        double seconds = (System.nanoTime() - lightStarted) / 1_000_000_000.0;
        lightLevel = timeescape.system.PowerRestoration.level(seconds);
        if (seconds >= timeescape.system.PowerRestoration.DURATION) lightingTimer.stop();
        repaint();
    }
    private void finishLighting() {
        lightingTimer.stop();
        lightLevel = powered ? 1 : 0;
    }
    private Era era = Era.PAST;
    private Hotspot hovered;
    private Hotspot fadingZone;
    private float glow = 0f;
    private boolean revealHeld;
    private double particlePhase;
    private float revealContrast;
    private BufferedImage contrastMask;
    private Era contrastMaskEra;
    private void paintContrast(Graphics2D g) {
        if (revealContrast <= 0) return;
        if (contrastMask == null || contrastMaskEra != era) {
            contrastMask = new BufferedImage(960,540,BufferedImage.TYPE_INT_ARGB);
            Graphics2D m = contrastMask.createGraphics();
            m.scale(.5,.5);
            m.setColor(new Color(0,0,0,128));
            m.fillRect(0,0,1920,1080);
            m.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            m.setColor(Color.WHITE);
            // Gentle feather outside each object; the core retains its actual lighting.
            for (Hotspot h : HotspotCatalog.of(era)) {
                for (int width=52;width>=2;width-=2) {
                    m.setComposite(java.awt.AlphaComposite.DstOut.derive(.10f));
                    m.setStroke(new BasicStroke(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    m.draw(h.shape());
                }
                m.setComposite(java.awt.AlphaComposite.DstOut);
                m.fill(h.shape());
            }
            m.dispose();contrastMaskEra=era;
        }
        g.setComposite(java.awt.AlphaComposite.SrcOver.derive(revealContrast));
        g.drawImage(contrastMask,0,0,1920,1080,null);
        g.setComposite(java.awt.AlphaComposite.SrcOver);
    }
    private final Consumer<String> onHotspotClick;
    private final Timer hoverTimer;
    private final PropertyChangeListener windowFocusListener = e -> clearHighlights();

    private void clearHighlights() {
        revealHeld = false;
        revealContrast = 0f;
        hovered = null;
        fadingZone = null;
        glow = 0f;
        hoverTimer.stop();
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    @Override public void addNotify() {
        super.addNotify();
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addPropertyChangeListener("focusedWindow", windowFocusListener);
    }

    @Override public void removeNotify() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .removePropertyChangeListener("focusedWindow", windowFocusListener);
        clearHighlights();
        finishLighting();
        backgroundCache.clear();
        offBackgroundCache.clear();
        super.removeNotify();
    }

    public GameScenePanel(Consumer<String> onHotspotClick) {
        this.onHotspotClick = onHotspotClick;
        setBackground(new Color(8, 11, 16));
        setPreferredSize(new Dimension(1120, 700));
        setFocusable(true);

        hoverTimer = new Timer(40, e -> {
            particlePhase += .045;
            revealContrast = revealHeld ? Math.min(1f,revealContrast+.2f) : Math.max(0f,revealContrast-.20001f);
            float target = hovered == null ? 0f : 1f;
            glow += (target - glow) * .26f;
            if (Math.abs(glow - target) < .025f) {
                glow = target;
                if (target == 0f) fadingZone = null;
                if (!revealHeld && revealContrast == 0f && hovered == null) ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) { clearHighlights(); finishLighting(); }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                if(inputBlocked)return;
                Hotspot next = hitTest(e.getPoint());
                if (next != hovered) {
                    hovered = next;
                    if (next != null) {
                        fadingZone = next;
                        if (glow <= .025f) glow = 0f;
                    }
                    setCursor(Cursor.getPredefinedCursor(next == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    hoverTimer.start();
                    repaint();
                }
            }
            @Override public void mouseClicked(MouseEvent e) {
                if(inputBlocked)return;
                if (e.getButton() != MouseEvent.BUTTON1) return;
                Hotspot target = hitTest(e.getPoint());
                if (target != null) onHotspotClick.accept(target.id());
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = null;
                setCursor(Cursor.getDefaultCursor());
                hoverTimer.start();
            }
        };
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        // Hold Shift to reveal objects; release or focus loss clears the hint.
        var keys = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        keys.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), "revealObjects");
        keys.put(KeyStroke.getKeyStroke("pressed SHIFT"), "revealObjects");
        keys.put(KeyStroke.getKeyStroke("released SHIFT"), "hideObjects");
        keys.put(KeyStroke.getKeyStroke("shift released SHIFT"), "hideObjects");
        getActionMap().put("revealObjects", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if(inputBlocked)return;
                revealHeld = true;
                hoverTimer.start();
                repaint();
            }
        });
        getActionMap().put("hideObjects", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                revealHeld = false;
                hoverTimer.start();
                repaint();
            }
        });
    }
    public void setEra(Era next) {
        if (this.era != next) {
            this.era = next;
            if (next != Era.PRESENT) finishLighting();
            revealHeld = false;
            revealContrast = 0f;
            hovered = null;
            fadingZone = null;
            glow = 0f;
            hoverTimer.stop();
            setCursor(Cursor.getDefaultCursor());
        }
        repaint();
    }

    private Rectangle displayRect() {
        double scale = Math.max((double) getWidth() / IMAGE_W, (double) getHeight() / IMAGE_H);
        int w = (int) Math.round(IMAGE_W * scale);
        int h = (int) Math.round(IMAGE_H * scale);
        return new Rectangle((getWidth() - w) / 2, (getHeight() - h) / 2, w, h);
    }

    private Hotspot hitTest(Point p) {
        Rectangle screen = displayRect();
        if (screen.width <= 0 || screen.height <= 0 || !screen.contains(p)) return null;
        int x = (int) ((p.x - screen.x) * (double) IMAGE_W / screen.width);
        int y = (int) ((p.y - screen.y) * (double) IMAGE_H / screen.height);
        for (Hotspot hotspot : HotspotCatalog.of(era)) {
            if (hotspot.contains(x, y)) return hotspot;
        }
        return null;
    }

    /** 원형 코어 / 모서리가 둥근 작은 부품 / CRT 화면: 각각 대상에 맞춰 빛난다. */
    private Shape glowShape(Hotspot h) { return h.shape(); }

    private Color glowColor() {
        return switch (era) {
            case PAST -> new Color(255, 204, 121);
            case PRESENT -> new Color(222, 193, 135);
            case FUTURE -> new Color(151, 219, 255);
        };
    }

    /** Stationary Effekseer sprites, staggered so objects shimmer independently. */
    private void paintParticles(Graphics2D g, Hotspot hotspot, float strength) {
        Rectangle b = hotspot.bounds();
        int seed = hotspot.id().hashCode() & 0x7fffffff;
        Graphics2D light = (Graphics2D)g.create();
        light.clip(hotspot.shape());
        light.setComposite(java.awt.AlphaComposite.SrcOver.derive(Math.min(1f, strength)));
        int placed = 0;
        for (int i = 0; i < 32 && placed < 6; i++) {
            double u = .12 + (((seed % 97 + i * 37) % 101) / 100.0) * .76;
            double v = .12 + (((seed % 71 + i * 53) % 103) / 102.0) * .76;
            int x = (int)(b.x + u * b.width), y = (int)(b.y + v * b.height);
            if (!hotspot.shape().contains(x, y)) continue;
            int frame = Math.floorMod((int)(particlePhase / .045 * 1.2) + seed % 90 + placed * 16, 96);
            int size = 54 + placed % 2 * 10;
            if (frame < 90) light.drawImage(timeescape.system.ShiftGlint.frame(frame), x-size/2, y-size/2, size, size, null);
            placed++;
        }
        light.dispose();
    }

    /** Draw labels in screen coordinates so text stays readable when the scene shrinks. */
    private void paintObjectLabel(Graphics2D g, Hotspot h, Rectangle screen) {
        Rectangle b = h.bounds();
        g.setFont(GameFonts.body(16));
        var fm = g.getFontMetrics();
        int width = fm.stringWidth(h.label()) + 24;
        int height = fm.getHeight() + 14;
        int center = screen.x + (int)(b.getCenterX() * screen.width / IMAGE_W);
        int x = Math.max(8, Math.min(getWidth() - width - 8, center - width / 2));
        int y = Math.max(8, screen.y + b.y * screen.height / IMAGE_H - height - 12);
        g.setColor(new Color(5, 12, 20, 235));
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(glowColor());
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, width, height, 12, 12);
        g.setColor(Color.WHITE);
        g.drawString(h.label(), x + 12, y + 7 + fm.getAscent());
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        Rectangle r = displayRect();
        BufferedImage image = ImageAssets.background(era);
        // Cache at device resolution to retain sharpness on HiDPI displays.
        int pixelW = (int)Math.ceil(r.width * Math.abs(g2.getTransform().getScaleX()));
        int pixelH = (int)Math.ceil(r.height * Math.abs(g2.getTransform().getScaleY()));
        if (era == Era.PRESENT && lightLevel < 1) {
            g2.drawImage(offBackgroundCache.get(ImageAssets.presentOff(), pixelW, pixelH), r.x, r.y, r.width, r.height, null);
            if (lightLevel > 0) {
                g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(lightLevel));
                g2.drawImage(backgroundCache.get(image, pixelW, pixelH), r.x, r.y, r.width, r.height, null);
                g2.setComposite(java.awt.AlphaComposite.SrcOver);
            }
        } else {
            g2.drawImage(backgroundCache.get(image, pixelW, pixelH), r.x, r.y, r.width, r.height, null);
        }

        Graphics2D overlay = (Graphics2D) g2.create();
        overlay.translate(r.x, r.y);
        overlay.scale(r.width / (double) IMAGE_W, r.height / (double) IMAGE_H);
        overlay.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintContrast(overlay);
        if (revealContrast > 0) {
            for (Hotspot zone : HotspotCatalog.of(era)) paintParticles(overlay, zone, revealContrast);
        } else if (fadingZone != null && glow > 0f) {
            paintParticles(overlay, fadingZone, glow * .65f);
        }
        overlay.dispose();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!revealHeld && hovered != null) {
            paintObjectLabel(g2, hovered, r);
        }        g2.dispose();
    }
}



