package timeescape.ui;

import java.awt.Rectangle;

/** Shared aspect-preserving scene placement for gameplay and its cutscenes. */
final class SceneViewport {
    private SceneViewport() {}
    static Rectangle cover(int width, int height) {
        double scale = Math.max(width / 1920.0, height / 1080.0);
        int w = (int)Math.round(1920 * scale);
        int h = (int)Math.round(1080 * scale);
        return new Rectangle((width-w)/2, (height-h)/2, w, h);
    }
}
