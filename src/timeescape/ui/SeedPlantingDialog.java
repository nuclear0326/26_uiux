package timeescape.ui;

import java.awt.Dimension;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import timeescape.puzzle.PuzzleResult;

/** Themed modal host for the past-era seed planting puzzle. */
public final class SeedPlantingDialog extends JDialog {
    private boolean solved;

    private SeedPlantingDialog(Frame owner) {
        super(owner, "씨앗 심기", true);
        setUndecorated(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(new SeedPlantingPanel(this::finish, this::dispose));
        PuzzleChrome.bindClose(getRootPane(), this::dispose);
        getRootPane().registerKeyboardAction(
                event -> finish(),
                KeyStroke.getKeyStroke("ctrl ENTER"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        setSize(900, 650);
        setMinimumSize(new Dimension(820, 600));
        setLocationRelativeTo(owner);
    }

    private void finish() {
        solved = true;
        dispose();
    }

    public static PuzzleResult play(Frame owner) {
        SeedPlantingDialog dialog = new SeedPlantingDialog(owner);
        dialog.setVisible(true);
        return PuzzleResult.from(dialog.solved);
    }
}
