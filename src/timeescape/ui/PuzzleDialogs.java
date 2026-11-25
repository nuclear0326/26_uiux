package timeescape.ui;

import java.awt.Frame;
import java.util.Map;
import javax.swing.JOptionPane;
import timeescape.puzzle.PuzzleResult;
import timeescape.puzzle.PuzzleRunner.Launcher;

/** Register UI implementations here; GameFrame does not branch on puzzle kinds. */
final class PuzzleDialogs {
    private PuzzleDialogs() { }
    static Map<String, Launcher> launchers(Frame owner) {
        return Map.of(
            "present", step -> PresentPuzzleDialog.play(owner, step.id()),
            "gears", step -> RepairPuzzleDialog.play(owner, "과거 | 시계태엽 고치기", GearsPuzzlePanel::new),
            "wires", step -> RepairPuzzleDialog.play(owner, "현재 | 배전함 배선 복구", WiresPuzzlePanel::new),
            "temporary_power", step -> PresentPuzzleDialog.play(owner, "PZ004", "미래 / 전원 켜기 (임시)"),
            "rush_hour", step -> ComputerPuzzleDialog.play(owner),
            "confirm", step -> PuzzleResult.from(JOptionPane.showConfirmDialog(owner,
                "USB에 연구 데이터를 복사할까요?", "데이터 복사", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION),
            "action", step -> PuzzleResult.SOLVED
        );
    }
}
