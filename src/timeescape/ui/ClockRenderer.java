package timeescape.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import timeescape.system.MenuImageAssets;

/** Draws clock hands over the dial already embedded in the menu background. */
public final class ClockRenderer {
    // Design coordinates in the 1600x900 reference background.
    public static final int CENTER_X = 1088;
    public static final int CENTER_Y = 430;
    public static final int SPRITE_SIZE = 372;
    // Both 400x400 transparent sprites are normalized to pivot (200,200).
    // Background art's existing dial center is (1088,430). Do not use bounding-box center.

    private final BufferedImage hour;
    private final BufferedImage minute;
    private final BufferedImage centerCap;

    public ClockRenderer(MenuImageAssets images) {
        hour = images.get("clock_hour_hand.png");
        minute = images.get("clock_minute_hand.png");
        centerCap = images.get("clock_center_cap.png");
    }

    /** @param secondsOfDay local wall-clock time in seconds (can exceed 86400 in animation). */
    public void draw(Graphics2D g, double secondsOfDay) {
        double hours = secondsOfDay / 3600.0;
        double minutes = secondsOfDay / 60.0;
        drawRotated(g, hour, Math.toRadians(hours * 30));
        drawRotated(g, minute, Math.toRadians(minutes * 6));
        // A single, complete decorative cap conceals both hand roots.
        final int capSize = 44;
        g.drawImage(centerCap, CENTER_X - capSize / 2, CENTER_Y - capSize / 2,
                capSize, capSize, null);
    }

    private void drawRotated(Graphics2D canvas, BufferedImage hand, double radians) {
        Graphics2D g = (Graphics2D) canvas.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            AffineTransform transform = new AffineTransform();
            transform.translate(CENTER_X, CENTER_Y);
            transform.rotate(radians); // Java's screen coordinate system => clockwise positive.
            transform.translate(-SPRITE_SIZE / 2.0, -SPRITE_SIZE / 2.0);
            transform.scale(SPRITE_SIZE / (double) hand.getWidth(), SPRITE_SIZE / (double) hand.getHeight());
            g.drawImage(hand, transform, null);
        } finally {
            g.dispose();
        }
    }
}
