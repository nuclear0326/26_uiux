package timeescape.system;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import timeescape.model.Era;

/** src/assets/images 내의 실사 배경을 읽는다. 이미지 교체 시 파일명만 유지하면 된다. */
public final class ImageAssets {
    private static final Map<Era, BufferedImage> CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private ImageAssets() { }

    private static BufferedImage presentOff;
    public static synchronized BufferedImage presentOff() {
        if (presentOff == null) {
            try (InputStream in = ImageAssets.class.getResourceAsStream("/assets/images/present_off.png")) {
                if (in == null) throw new IllegalStateException("소등 배경 없음");
                presentOff = ImageIO.read(in);
                if (presentOff == null) throw new IllegalStateException("소등 배경 읽기 실패");
            } catch (IOException ex) { throw new IllegalStateException(ex); }
        }
        return presentOff;
    }

    public static BufferedImage background(Era era) {
        return CACHE.computeIfAbsent(era, e -> {
            String path = "/assets/images/" + e.imageName();
            try (InputStream in = ImageAssets.class.getResourceAsStream(path)) {
                if (in == null) throw new IllegalStateException("배경 이미지 없음: " + path);
                return ImageIO.read(in);
            } catch (IOException ex) {
                throw new IllegalStateException("이미지 로드 실패: " + path, ex);
            }
        });
    }
}
