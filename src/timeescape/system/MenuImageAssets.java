package timeescape.system;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/** Loads assets from src/assets on the Java classpath, not absolute disk paths. */
public final class MenuImageAssets {
    private final Map<String, BufferedImage> cache = new HashMap<>();

    public BufferedImage get(String fileName) {
        return cache.computeIfAbsent(fileName, this::load);
    }

    private BufferedImage load(String fileName) {
        String path = "/assets/images/menu/" + fileName;
        URL resource = MenuImageAssets.class.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("Image resource missing: " + path);
        }
        try {
            BufferedImage result = ImageIO.read(resource);
            if (result == null) throw new IOException("Unknown image format");
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read image resource: " + path, e);
        }
    }
}
