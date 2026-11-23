package timeescape;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import timeescape.ui.GameFrame;

/** Eclipse 실행 클래스. JDK 17 이상, Java Swing만 사용. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { }
            timeescape.ui.GameFonts.installDefaults();
            new GameFrame().setVisible(true);
        });
    }
}
