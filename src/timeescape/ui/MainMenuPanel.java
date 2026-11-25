package timeescape.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

import timeescape.system.MenuImageAssets;
import timeescape.system.GameAudio;

/** Image-based menu. Draws at design resolution and letterboxes on resize. */
public final class MainMenuPanel extends JPanel {
    public interface MenuActions {
        default boolean confirmNewGame() { return true; }
        void startGame();
        void continueGame();
        void showSettings();
        void exitGame();
    }

    private static final int DESIGN_W = 1600;
    private static final int DESIGN_H = 900;
    private static final int BUTTON_X = 76;
    private static final int BUTTON_Y = 298;
    private static final int BUTTON_W = 505;
    private static final int BUTTON_H = 76;
    private static final int BUTTON_STEP = 96;
    private static final String[] LABELS = {"게임 시작", "이어하기", "설정", "종료"};
    public static final String GAME_TITLE = "TIME ESCAPE";

    private final MenuImageAssets images = new MenuImageAssets();
    private final MenuAtmosphere atmosphere = new MenuAtmosphere(images,true);
    private double elapsed, departure = -1, lightningAt = -100, nextThunder = 12, thunderDue = -1;
    private long lastTick;
    private int stormNumber;
    private boolean glassPlayed;
    private Runnable pending;
    private double frozenClock;
    private boolean preparingTransition;
    private long preparationRevision;
    java.util.concurrent.CompletableFuture<Void> prepareEffects(){return atmosphere.prepare();}
    private boolean runningTransition() { return pending != null || preparingTransition; }
    private void advance(double dt) {
        elapsed += Math.min(.1,Math.max(0,dt));
        if (pending != null) {
            double audioTime = GameAudio.get().menuTransitionSeconds();
            if (audioTime == -2) return; // Wait for soundtrack readiness without blocking Swing.
            departure = audioTime >= 0 ? Math.max(departure, audioTime) : departure + Math.max(0,dt);
            if (departure >= MenuStartTimeline.BREAK_AT && !glassPlayed) { glassPlayed=true; }
            if (departure >= MenuStartTimeline.END_AT) {
                GameAudio.get().stopMenuTransition();
                Runnable done=pending; pending=null; departure=-1; atmosphere.reset(); done.run();
            }
        } else {
            if(elapsed >= nextThunder) {
                lightningAt=elapsed;thunderDue=elapsed+1.2;
                nextThunder=elapsed+18+(stormNumber++*7%13);
            }
            if(thunderDue>=0 && elapsed>=thunderDue){thunderDue=-1;GameAudio.get().play("menu_thunder");}
        }
    }
    private void stopAnimation() {
        preparationRevision++;preparingTransition=false;
        GameAudio.get().stopMenuTransition();
        refreshTimer.stop(); lastTick=0;pending=null;departure=-1;thunderDue=-1;lightningAt=-100;nextThunder=elapsed+12;atmosphere.reset();
    }
    private final MenuActions actions;
    private final Timer refreshTimer;

    private int hovered = -1;
    private int pressed = -1;
    private boolean continueAvailable = false; // Enabled only after a valid save/session is available.
    private boolean transitionAnimation = true;

    public MainMenuPanel(MenuActions actions) {
        this.actions = actions;
        setPreferredSize(new Dimension(1280, 720));
        setBackground(new Color(35, 27, 21));
        setFocusable(true);
        setCursor(GameCursors.normal());

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int next = buttonAt(e.getPoint());
                if (hovered != next) {
                    hovered = next;
                    setCursor(GameCursors.forInteraction(next >= 0 && !runningTransition()));
                    repaint();
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                hovered = -1;
                setCursor(GameCursors.normal());
                repaint();
            }
            @Override public void mousePressed(MouseEvent e) {
                if (!javax.swing.SwingUtilities.isLeftMouseButton(e) || runningTransition()) return;
                pressed = buttonAt(e.getPoint());
                repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (!javax.swing.SwingUtilities.isLeftMouseButton(e)) return;
                int previouslyPressed = pressed;
                pressed = -1;
                repaint();
                if (previouslyPressed >= 0 && previouslyPressed == buttonAt(e.getPoint())
                    && !runningTransition()) {
                    activate(previouslyPressed);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        refreshTimer = new Timer(33, e -> {
            long now=System.nanoTime();
            if(lastTick!=0)advance((now-lastTick)/1e9);
            lastTick=now;
            repaint();
        });
        // Start only when attached and visible.
    }

    @Override public void addNotify() {
        super.addNotify();
        if (isVisible()) refreshTimer.start();
    }

    @Override public void removeNotify() {
        stopAnimation();
        super.removeNotify();
    }

    public void setContinueAvailable(boolean value) {
        continueAvailable = value;
        repaint();
    }

    public void setTransitionAnimation(boolean value) {
        transitionAnimation = value;
    }

    public boolean isTransitionAnimationEnabled() {
        return transitionAnimation;
    }

    @Override public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (refreshTimer != null) {
            if (visible && isShowing()) refreshTimer.start();
            else stopAnimation();
        }
    }

    @Override protected void paintComponent(Graphics original) {
        super.paintComponent(original);
        Graphics2D g = (Graphics2D) original.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // Background/effects share one cover transform; controls have their own safe layout.
            double cover=Math.max(getWidth()/(double)DESIGN_W,getHeight()/(double)DESIGN_H);
            Graphics2D background=(Graphics2D)g.create();
            background.translate((getWidth()-DESIGN_W*cover)*.72,(getHeight()-DESIGN_H*cover)*.5);
            background.scale(cover,cover);
            atmosphere.draw(background,elapsed,elapsed-lightningAt,departure,pending==null?MenuAtmosphere.clockSeconds():frozenClock);
            background.dispose();
            View view = view();
            if(departure<0 && getWidth()/(double)getHeight()<1.6){
                g.setPaint(new java.awt.GradientPaint(0,0,new Color(9,12,14,145),(float)(650*view.scale),0,new Color(9,12,14,0)));
                g.fillRect(0,0,getWidth(),getHeight());
            }
            g.translate(view.x, view.y);
            g.scale(view.scale, view.scale);
            g.clipRect(0,0,DESIGN_W,DESIGN_H);
            drawTitle(g);
            drawButtons(g);

        } finally {
            g.dispose();
        }
    }

    private void drawTitle(Graphics2D g) {
        if(departure>=0)return;
        g.setFont(GameFonts.title(60));
        g.setColor(new Color(237,221,189));g.drawString(GAME_TITLE,72,184);
        g.setPaint(new java.awt.GradientPaint(76,0,new Color(158,116,53,190),580,0,new Color(158,116,53,10)));
        g.fillRect(76,216,505,1);g.setColor(new Color(214,174,105));g.fillOval(331,214,5,5);
    }
    private void drawButtons(Graphics2D g) {
        if(departure>=0)return;
        g.setFont(GameFonts.button(32));
        FontMetrics fm=g.getFontMetrics();
        for(int i=0;i<LABELS.length;i++) {
            int y=BUTTON_Y+i*BUTTON_STEP;
            boolean disabled=i==1&&!continueAvailable;
            boolean active=!disabled&&(hovered==i || (hovered<0&&i==0));
            if(active) {
                g.setPaint(new java.awt.GradientPaint(BUTTON_X,y,new Color(171,107,35,pressed==i?90:58),BUTTON_X+BUTTON_W,y,new Color(90,57,25,0)));
                g.fillRect(BUTTON_X,y,BUTTON_W,BUTTON_H);
                g.setPaint(new java.awt.GradientPaint(BUTTON_X,y,new Color(225,172,84,190),BUTTON_X+BUTTON_W,y,new Color(160,110,42,10)));
                g.fillRect(BUTTON_X,y,BUTTON_W,1);g.fillRect(BUTTON_X,y+BUTTON_H-1,BUTTON_W,1);
                g.setColor(new Color(235,185,97));g.fillRect(BUTTON_X,y,2,BUTTON_H);
            } else {g.setColor(new Color(169,130,75,40));g.drawLine(BUTTON_X,y+BUTTON_H,BUTTON_X+BUTTON_W,y+BUTTON_H);}
            g.setColor(disabled?new Color(123,119,110):new Color(236,221,193));
            g.drawString(LABELS[i],BUTTON_X+40,y+(BUTTON_H-fm.getHeight())/2+fm.getAscent());
        }
    }

    public void startConfirmedNewGame() { activate(0,true); }
    private void activate(int button) { activate(button,false); }
    private void activate(int button,boolean confirmed) {
        if (runningTransition() || (button == 1 && !continueAvailable)) return;
        if (button == 0 && !confirmed && !actions.confirmNewGame()) return;
        Runnable callback;
        switch (button) {
            case 0: callback = actions::startGame; break;
            case 1: callback = actions::continueGame; break;
            case 2: callback = actions::showSettings; break;
            case 3: callback = actions::exitGame; break;
            default: return;
        }
        hovered = -1;
        pressed = -1;
        setCursor(GameCursors.normal());
        GameAudio.get().play("ui_click");
        if (transitionAnimation && button == 0) {
            preparingTransition=true;
            long revision=++preparationRevision;
            boolean attached=isShowing();
            java.util.function.BiConsumer<Void,Throwable> begin=(ignored,error)->{
                if(revision!=preparationRevision)return;
                preparingTransition=false;
                if(attached&&!isShowing())return;
                if(error!=null){javax.swing.JOptionPane.showMessageDialog(this,"시작 연출을 준비하지 못했습니다. 배포 파일을 확인해주세요.","준비 실패",javax.swing.JOptionPane.ERROR_MESSAGE);return;}
                GameAudio.get().startMenuTransition();
                lastTick=System.nanoTime();
                pending=callback;departure=0;glassPlayed=false;thunderDue=-1;frozenClock=MenuAtmosphere.clockSeconds();atmosphere.reset();
            };
            var ready=atmosphere.prepare();
            if(ready.isDone())ready.whenComplete(begin);else ready.whenCompleteAsync(begin,javax.swing.SwingUtilities::invokeLater);
        } else callback.run();
    }

    private int buttonAt(Point p) {
        View v = view();
        if (v.scale <= 0) return -1;
        double x = (p.x - v.x) / v.scale;
        double y = (p.y - v.y) / v.scale;
        for (int i = 0; i < LABELS.length; i++) {
            if (i == 1 && !continueAvailable) continue;
            Rectangle r = new Rectangle(BUTTON_X, BUTTON_Y + i * BUTTON_STEP, BUTTON_W, BUTTON_H);
            if (r.contains(x, y)) return i;
        }
        return -1;
    }

    private View view() {
        double scale = Math.min(getWidth() / (double) DESIGN_W, getHeight() / (double) DESIGN_H);
        int w = (int) Math.round(DESIGN_W * scale);
        int h = (int) Math.round(DESIGN_H * scale);
        return new View(0, (getHeight() - h) / 2, scale);
    }

    private static final class View {
        final int x, y;
        final double scale;
        View(int x, int y, double scale) { this.x = x; this.y = y; this.scale = scale; }
    }
}

