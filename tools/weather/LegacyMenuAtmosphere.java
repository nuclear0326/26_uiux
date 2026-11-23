package timeescape.ui;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.time.LocalTime;
import timeescape.system.MenuImageAssets;

/** Layered laboratory menu. Coordinates are independent of button hit testing. */
final class LegacyMenuAtmosphere {
    static final double CX=1263, CY=428, RX=128, RY=146;
    private final BufferedImage lit, unlit;
    private final Shape window;
    private LegacyTextureRain textureRain;
    private BufferedImage stormGlow;
    private final ClockGlassEffect glassEffect = new ClockGlassEffect();
    LegacyMenuAtmosphere(MenuImageAssets images) {
        lit=images.get("menu_background.png");unlit=images.get("menu_unlit.png");
        // Only exposed panes: keep moving water and storm light off mullions and props.
        Area rain=new Area();
        rain.add(new Area(pane(786,52,908,36,908,242,786,252)));
        rain.add(new Area(pane(930,30,1110,5,1110,224,930,240)));
        rain.add(new Area(pane(1136,0,1280,0,1280,36,1136,54)));
        rain.add(new Area(pane(1378,0,1436,0,1436,64,1378,38)));
        rain.add(new Area(pane(1462,0,1598,0,1598,90,1462,52)));
        rain.add(new Area(pane(791,273,906,265,906,293,840,310,840,369,791,374)));
        rain.add(new Area(pane(930,263,1080,245,1080,293,1017,309,1017,294,930,293)));
        rain.add(new Area(pane(1498,187,1598,173,1598,311,1555,298,1555,228,1498,228)));
        window=rain;
        textureRain=new LegacyTextureRain(lit,window);
        buildStormGlow();
    }
    private static Shape pane(double... xy){
        Path2D p=new Path2D.Double();p.moveTo(xy[0],xy[1]);
        for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();return p;
    }
    void reset(){ /* Effect is deterministic and has no per-play state. */ }
    static double clockSeconds(){LocalTime t=LocalTime.now();return t.toSecondOfDay()+t.getNano()/1e9;}
    static double flash(double t){
        if(t<0||t>1.5)return 0;
        return Math.min(1,.92*(1-Math.exp(-t*65))*Math.exp(-t*7)
            +.34*Math.exp(-Math.pow((t-.24)/.055,2)));
    }
    private void buildStormGlow(){
        stormGlow=new BufferedImage(1600,900,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<400;y++)for(int x=770;x<1600;x++){
            if(!window.contains(x,y))continue;
            double edge=1;
            for(int d=1;d<=16;d++)if(!window.contains(x-d,y)||!window.contains(x+d,y)||!window.contains(x,y-d)||!window.contains(x,y+d)){edge=(d-1)/16.;break;}
            double falloff=Math.exp(-((x-1090.)*(x-1090.)/(550*550.)+(y-70.)*(y-70.)/(380*380.)));
            int c=lit.getRGB(Math.min(lit.getWidth()-1,x*lit.getWidth()/1600),Math.min(lit.getHeight()-1,y*lit.getHeight()/900));
            int r=Math.min(255,(int)((c>>16&255)*1.35+24));
            int green=Math.min(255,(int)((c>>8&255)*1.40+30));
            int blue=Math.min(255,(int)((c&255)*1.45+35));
            stormGlow.setRGB(x,y,((int)(255*edge*falloff)<<24)|(r<<16)|(green<<8)|blue);
        }
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
        g.setComposite(AlphaComposite.SrcOver.derive((float)dip));g.drawImage(unlit,0,0,1600,900,null);g.setComposite(AlphaComposite.SrcOver);
        drawHands(g,clockTime);
        if(departure>=MenuStartTimeline.BREAK_AT)glassEffect.draw(g,departure-MenuStartTimeline.BREAK_AT);
        double flash=flash(lightningAge);
        if(flash>0&&departure<0){
            Graphics2D sky=(Graphics2D)g.create();
            sky.setComposite(AlphaComposite.SrcOver.derive((float)flash));
            sky.drawImage(stormGlow,0,0,null);sky.dispose();
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
