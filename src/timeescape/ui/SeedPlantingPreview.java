package timeescape.ui;


import javax.swing.SwingUtilities;
import timeescape.puzzle.PuzzleResult;

public final class SeedPlantingPreview {
    private SeedPlantingPreview() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PuzzleResult result = SeedPlantingDialog.play(null);
            System.out.println("퍼즐 결과: " + result);
        });
    }
}
