package timeescape.ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import timeescape.system.MenuImageAssets;

/** Layered laboratory menu. Coordinates are independent of button hit testing. */
final class MenuAtmosphere {
    static final double CX=1263, CY=428, RX=128, RY=146;
    private final BufferedImage lit;
    private final TextureRain textureRain;
    private record Effects(BufferedImage unlit,BufferedImage stormGlow,ClockGlassEffect glassEffect){}
    private volatile Effects effects;
    private java.util.concurrent.CompletableFuture<Void> preparation;
    MenuAtmosphere(MenuImageAssets images){this(images,false);}
    MenuAtmosphere(MenuImageAssets images,boolean deferred) {
        lit=images.get("menu_background.png");textureRain=new TextureRain();
        if(!deferred)prepare().join();
    }
    synchronized java.util.concurrent.CompletableFuture<Void> prepare(){
        if(preparation==null){
            preparation=new java.util.concurrent.CompletableFuture<>();
            var result=preparation;
            Thread worker=new Thread(()->{try{
                var images=new MenuImageAssets();
                effects=new Effects(images.get("menu_unlit.png"),images.get("menu_storm_glow.png"),new ClockGlassEffect());
                result.complete(null);
            }catch(Throwable e){result.completeExceptionally(e);}},"time-escape-menu-preparation");
            worker.setDaemon(true);worker.start();
        }
        return preparation;
    }
    void reset(){ /* Effect is deterministic and has no per-play state. */ }
    static double clockSeconds(){LocalTime t=LocalTime.now();return t.toSecondOfDay()+t.getNano()/1e9;}
    static double flash(double t){
        if(t<0||t>1.5)return 0;
        return Math.min(1,.92*(1-Math.exp(-t*65))*Math.exp(-t*7)
            +.34*Math.exp(-Math.pow((t-.24)/.055,2)));
    }
    static double startLampDarkness(double t) { return MenuStartTimeline.darkness(t); }
    void draw(Graphics2D canvas,double t,double lightningAge,double departure,double clockTime) {
        Graphics2D g=(Graphics2D)canvas.create();
        g.translate(800,450);g.scale(1.018,1.018);
        g.translate(-800,-450);
        g.drawImage(lit,0,0,1600,900,null);
        textureRain.draw(g,t);
        double dip=0; // Preserve the approved stationary weather composition.
        if(departure>=0)dip=startLampDarkness(departure);
        Effects ready=effects;
        if(dip>0 && ready!=null){g.setComposite(AlphaComposite.SrcOver.derive((float)dip));g.drawImage(ready.unlit(),0,0,1600,900,null);g.setComposite(AlphaComposite.SrcOver);}
        drawHands(g,clockTime);
        if(ready!=null && departure>=MenuStartTimeline.BREAK_AT)ready.glassEffect().draw(g,departure-MenuStartTimeline.BREAK_AT);
        double flash=flash(lightningAge);
        if(ready!=null&&flash>0&&departure<0){
            Graphics2D sky=(Graphics2D)g.create();
            sky.setComposite(AlphaComposite.SrcOver.derive((float)flash));
            sky.drawImage(ready.stormGlow(),0,0,null);sky.dispose();
        }
        g.dispose();
        if(departure>=MenuStartTimeline.FADE_AT){canvas.setColor(new Color(0,0,0,(int)(255*Math.min(1,(departure-MenuStartTimeline.FADE_AT)/.8))));canvas.fillRect(0,0,1600,900);}
    }
    static void drawHands(Graphics2D g,double seconds){
        Graphics2D h=(Graphics2D)g.create();h.setComposite(AlphaComposite.SrcOver); // Solid metal hands never fade with the lamp.
        h.translate(CX,CY);h.scale(RX/146,1);
        hand(h,seconds/3600*Math.PI/6,87,6);hand(h,seconds/60*Math.PI/30,122,4);
        h.setColor(new Color(24,26,27));h.fillOval(-8,-8,16,16);h.setColor(new Color(43,46,47));h.drawOval(-6,-6,12,12);h.dispose();
    }
    private static void hand(Graphics2D g,double angle,double length,double width){
        Graphics2D h=(Graphics2D)g.create();h.rotate(angle);
        Path2D p=new Path2D.Double();p.moveTo(0,-length);p.lineTo(width,-16);p.lineTo(width*.6,17);p.lineTo(-width*.6,17);p.lineTo(-width,-16);p.closePath();
        h.setColor(new Color(17,20,19));h.fill(p);h.setColor(new Color(35,39,40));h.setStroke(new BasicStroke(.8f));h.draw(p);h.dispose();
    }
}
