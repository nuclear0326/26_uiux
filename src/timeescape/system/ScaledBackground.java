package timeescape.system;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
/** One-entry scaled background cache: bounded memory, invalidated on source/size change. */
public final class ScaledBackground {
    private BufferedImage source;
    private BufferedImage scaled;
    public BufferedImage get(BufferedImage image, int width, int height) {
        if (width <= 0 || height <= 0) return null;
        if (scaled == null || source != image || scaled.getWidth() != width || scaled.getHeight() != height) {
            clear();
            source = image;
            scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
        }
        return scaled;
    }
    public void clear() {
        if (scaled != null) scaled.flush();
        scaled = null;
        source = null;
    }
}
