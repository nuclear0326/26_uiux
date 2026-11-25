package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Uses the production menu rain engine in three uniformly scaled window bays. */
final class LabWindowWeather {
    private final LabWindowMask mask = new LabWindowMask();
    private final TextureRain rain = new TextureRain();
    private final BufferedImage stormOn,stormOff;
    private final Bay[] bays={new Bay(781,967,164,0),new Bay(996,1213,144,1.7),new Bay(1248,1464,120,3.4)};
    private static final int PANE_W=180,PANE_H=400;
    private static final class Bay {
        final double x,y,scale,phase;
        final BufferedImage image=new BufferedImage(PANE_W,PANE_H,BufferedImage.TYPE_INT_ARGB);
        final int[] pixels=((java.awt.image.DataBufferInt)image.getRaster().getDataBuffer()).getData();
        final int[] on=new int[PANE_W*PANE_H],off=new int[PANE_W*PANE_H];
        Bay(double left,double right,double top,double offset){x=left;y=top;scale=(right-left)/PANE_W;phase=offset;}
    }
    LabWindowWeather() {
        try {stormOn=load("lab_storm_on.png");stormOff=load("lab_storm_off.png");}
        catch(IOException e){throw new IllegalStateException("Cannot load lab lightning",e);}
        // Only small background samples for accurate additive compositing, not rain extraction.
        BufferedImage on=timeescape.system.ImageAssets.background(timeescape.model.Era.PRESENT);
        BufferedImage off=timeescape.system.ImageAssets.presentOff();
        for(Bay bay:bays)for(int y=0;y<PANE_H;y++)for(int x=0;x<PANE_W;x++){
            double px=bay.x+x*bay.scale,py=bay.y+y*bay.scale;
            int index=y*PANE_W+x;
            bay.on[index]=sample(on,px,py);bay.off[index]=sample(off,px,py);
        }
    }
    private static int sample(BufferedImage im,double x,double y){
        int px=Math.max(0,Math.min(im.getWidth()-1,(int)(x*im.getWidth()/1672.)));
        int py=Math.max(0,Math.min(im.getHeight()-1,(int)(y*im.getHeight()/941.)));
        return im.getRGB(px,py);
    }
    private static int channel(int a,int b,int shift,float power){return (int)((a>>shift&255)*(1-power)+(b>>shift&255)*power);}
    private void drawRain(Graphics2D g,double seconds,float power){
        for(Bay bay:bays){
            rain.updateDistantDelta(seconds+bay.phase);
            java.util.Arrays.fill(bay.pixels,0);
            for(int y=0;y<PANE_H;y++)for(int x=0;x<PANE_W;x++){
                int source=y*rain.W+930+x,index=y*PANE_W+x;
                double change=Math.max(0,rain.delta[source]);
                if(change<.1)continue;
                int r=channel(bay.off[index],bay.on[index],16,power),green=channel(bay.off[index],bay.on[index],8,power),b=channel(bay.off[index],bay.on[index],0,power);
                double dr=Math.min(255-r,change*.80),dg=Math.min(255-green,change*.97),db=Math.min(255-b,change*1.08);
                double alpha=Math.max(dr/Math.max(1,255-r),Math.max(dg/Math.max(1,255-green),db/Math.max(1,255-b)));
                if(alpha<=0)continue;
                int ai=Math.min(255,(int)Math.ceil(alpha*255));alpha=ai/255.;
                bay.pixels[index]=(ai<<24)|(TextureRain.clamp(r+dr/alpha)<<16)|(TextureRain.clamp(green+dg/alpha)<<8)|TextureRain.clamp(b+db/alpha);
            }
            Graphics2D pane=(Graphics2D)g.create();pane.translate(bay.x,bay.y);pane.scale(bay.scale,bay.scale);
            pane.drawImage(bay.image,0,0,null);pane.dispose();
        }
    }
    private static BufferedImage load(String name)throws IOException {
        try(var in=LabWindowWeather.class.getResourceAsStream("/assets/effects/"+name)) {
            if(in==null)throw new IOException("Missing "+name);
            BufferedImage im=ImageIO.read(in);if(im==null)throw new IOException("Invalid "+name);return im;
        }
    }
    boolean contains(double x,double y){return mask.contains(x,y);}
    void draw(Graphics2D canvas,double seconds,double lightningAge){draw(canvas,seconds,lightningAge,1);}
    void draw(Graphics2D canvas,double seconds,double lightningAge,float power) {
        Graphics2D g=(Graphics2D)canvas.create();g.scale(1920.0/1672,1080.0/941);g.clip(mask.glass);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawRain(g,seconds,power);
        float flash=(float)MenuAtmosphere.flash(lightningAge);
        if(flash>0){
            // Match the current power state; never stamp a lit photo patch over the dark room.
            g.setComposite(AlphaComposite.SrcOver.derive(flash*(1-power)));g.drawImage(stormOff,760,110,null);
            if(power>0){g.setComposite(AlphaComposite.SrcOver.derive(flash*power));g.drawImage(stormOn,760,110,null);}
        }
        g.dispose();
    }
}
