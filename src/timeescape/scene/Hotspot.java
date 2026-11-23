package timeescape.scene;
import java.awt.Rectangle;
import java.awt.Shape;
/** Shared object outline for clicks, glow and F3, in 1920x1080 coordinates. */
public record Hotspot(String id, String label, Shape shape) {
    public Rectangle bounds() { return shape.getBounds(); }
    public boolean contains(int x, int y) { return shape.contains(x, y); }
}
