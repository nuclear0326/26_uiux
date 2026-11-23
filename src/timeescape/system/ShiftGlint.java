package timeescape.system;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Frames recorded in Effekseer 1.80.8. Loaded once, no runtime native dependency. */
public final class ShiftGlint {
    private static final BufferedImage[] FRAMES = load();
    private ShiftGlint() {}
    private static BufferedImage[] load() {
        try (var input = ShiftGlint.class.getResourceAsStream("/assets/effects/shift_glint.png")) {
            if (input == null) throw new IllegalStateException("Missing Shift glow sheet");
            BufferedImage sheet = ImageIO.read(input);
            if (sheet == null || sheet.getWidth()!=320 || sheet.getHeight()!=288)
                throw new IllegalStateException("Invalid Shift glow sheet");
            BufferedImage[] frames = new BufferedImage[90];
            for(int i=0;i<90;i++) frames[i]=sheet.getSubimage(i%10*32,i/10*32,32,32);
            return frames;
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    public static BufferedImage frame(int index) { return FRAMES[index]; }
}
