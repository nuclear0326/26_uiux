package timeescape.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/** App-local native cursors. Preview and headless checks never create native resources. */
public final class GameCursors {
    private GameCursors() {}
    public static final int SIZE = 48;
    public static Point hotspot() { return new Point(3,3); }
    private static final class Images {
        static final BufferedImage NORMAL = read("normal"), INTERACT = read("interact");
    }
    private static final class Native {
        static final Cursor NORMAL = create(false), INTERACT = create(true);
    }
    public static BufferedImage image(boolean interact) { return interact ? Images.INTERACT : Images.NORMAL; }
    public static Cursor normal() { return forInteraction(false); }
    public static Cursor interact() { return forInteraction(true); }
    public static Cursor forInteraction(boolean active) {
        if (GraphicsEnvironment.isHeadless()) return Cursor.getPredefinedCursor(active ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR);
        return active ? Native.INTERACT : Native.NORMAL;
    }
    private static BufferedImage read(String name) {
        try (var in = GameCursors.class.getResourceAsStream("/assets/cursors/"+name+".png")) {
            if(in==null) throw new IllegalStateException("Missing cursor: "+name);
            var image=ImageIO.read(in);
            if(image==null) throw new IllegalStateException("Invalid cursor: "+name);
            return image;
        } catch(IOException e) { throw new IllegalStateException(e); }
    }
    private static Cursor create(boolean active) {
        try {
            Toolkit toolkit=Toolkit.getDefaultToolkit();
            Dimension size=toolkit.getBestCursorSize(SIZE,SIZE);
            if(size.width<=0 || size.height<=0) return Cursor.getPredefinedCursor(active?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR);
            double scale=Math.min(1,Math.min(size.width/(double)SIZE,size.height/(double)SIZE));
            BufferedImage canvas=new BufferedImage(size.width,size.height,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=canvas.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(image(active),0,0,(int)(SIZE*scale),(int)(SIZE*scale),null);g.dispose();
            return toolkit.createCustomCursor(canvas,new Point((int)Math.round(3*scale),(int)Math.round(3*scale)),active?"Research nib interaction":"Research nib");
        } catch(IllegalArgumentException | java.awt.AWTError | SecurityException e) {
            return Cursor.getPredefinedCursor(active?Cursor.HAND_CURSOR:Cursor.DEFAULT_CURSOR);
        }
    }
}
