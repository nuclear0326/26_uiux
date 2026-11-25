package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import timeescape.model.Era;
import timeescape.system.GameAudio;
import timeescape.system.ImageAssets;

/** Approved v6 audio timeline; revised failure visual stays in the unpowered present. No progress mutation here: completion belongs to GameFrame. */
final class FirstTravelOverlay extends JComponent {
    static final double LIGHT_START=.6716, LIGHT_OFF=4.6716, CUT=4.5716;
    static final double WARP=5.4716, ARRIVAL=5.9216, MOTOR_END=7.352, END=7.6;
    private BufferedImage on,off;
    private final Timer timer=new Timer(16,e->tick());
    private long generation,lastTick,waitingSince,stalledSince;
    private double seconds,lastAudio=-1;
    private boolean arrived,finished;
    private Runnable arrival,complete;
    FirstTravelOverlay(){
        setVisible(false);setOpaque(false);setFocusable(true);
        addMouseListener(new MouseAdapter(){});addMouseMotionListener(new MouseMotionAdapter(){});
    }
    void start(Runnable arrival,Runnable complete,Runnable failure){
        if(isVisible())return;
        long token=++generation;
        this.arrival=arrival;this.complete=complete;
        seconds=0;lastAudio=-1;arrived=false;finished=false;
        setVisible(true);requestFocusInWindow();
        CompletableFuture.supplyAsync(()->new BufferedImage[]{ImageAssets.background(Era.PRESENT),ImageAssets.presentOff()})
            .whenComplete((images,error)->SwingUtilities.invokeLater(()->{
                if(token!=generation)return;
                if(error!=null){cancel();failure.run();return;}
                on=images[0];off=images[1];
                GameAudio.get().startFirstTravel();
                waitingSince=lastTick=stalledSince=System.nanoTime();timer.start();repaint();
            }));
    }
    private void tick(){
        long now=System.nanoTime();double dt=(now-lastTick)/1e9;lastTick=now;
        double audio=GameAudio.get().firstTravelSeconds();
        if(audio==-2){
            if((now-waitingSince)/1e9<5)return;
            // Cancel delayed audio rather than playing it after the silent fallback began.
            GameAudio.get().stopFirstTravel();audio=-1;
        }
        if(audio>=0 && audio>lastAudio){seconds=Math.max(seconds,audio);lastAudio=audio;stalledSince=now;}
        else if(audio<0 || (now-stalledSince)/1e9>.5)seconds+=dt;
        advance(seconds);
    }
    void advance(double t){
        if(finished)return;
        seconds=t;
        if(t>=ARRIVAL&&!arrived){arrived=true;if(arrival!=null)arrival.run();}
        if(t>=END){
            finished=true;timer.stop();setVisible(false);GameAudio.get().stopFirstTravel();
            Runnable callback=complete;complete=null;arrival=null;
            if(callback!=null)callback.run();
        }
        repaint();
    }
    void cancel(){++generation;timer.stop();finished=true;complete=null;arrival=null;setVisible(false);GameAudio.get().stopFirstTravel();}
    static double darkness(double t){return t<LIGHT_START?0:t>=LIGHT_OFF?1:MenuStartTimeline.darkness(t-LIGHT_START);}
    @Override protected void paintComponent(Graphics graphics){
        if(on==null)return;
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Rectangle r=SceneViewport.cover(getWidth(),getHeight());
        // Same crop and layer order as gameplay; only illumination changes.
        g.drawImage(off,r.x,r.y,r.width,r.height,null);
        float light=(float)(1-darkness(seconds));
        if(light>0){
            g.setComposite(AlphaComposite.SrcOver.derive(light));
            g.drawImage(on,r.x,r.y,r.width,r.height,null);
        }
        g.dispose();
    }
}
