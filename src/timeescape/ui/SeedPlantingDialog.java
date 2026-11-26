package timeescape.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import timeescape.puzzle.PuzzleResult;
import timeescape.puzzle.SeedPlantingPuzzle;
import timeescape.puzzle.SeedPlantingPuzzle.Seed;
import timeescape.puzzle.SeedPlantingPuzzle.WaterAmount;

/** Swing dialog for the past-era seed planting minigame. */
public final class SeedPlantingDialog extends JDialog {
    private final SeedPlantingPuzzle puzzle = new SeedPlantingPuzzle();

    private final JRadioButton blueSeedButton =
            new JRadioButton(Seed.BLUE_BLOOM.displayName());
    private final JRadioButton redSeedButton =
            new JRadioButton(Seed.RED_BLOOM.displayName());

    private final JRadioButton littleWaterButton =
            new JRadioButton(WaterAmount.LITTLE.displayName());
    private final JRadioButton normalWaterButton =
            new JRadioButton(WaterAmount.NORMAL.displayName());
    private final JRadioButton muchWaterButton =
            new JRadioButton(WaterAmount.MUCH.displayName());

    private PuzzleResult result = PuzzleResult.CANCELLED;

    private SeedPlantingDialog(Frame owner) {
        super(owner, "과거 · 씨앗 심기", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JLabel instruction = new JLabel(
                "<html><div style='text-align:center;'>"
                + "연구 기록을 읽고 알맞은 씨앗과 물의 양을 선택하세요.<br>"
                + "푸른 조명 아래, 기록된 양만큼 물을 주어라."
                + "</div></html>",
                SwingConstants.CENTER
        );
        add(instruction, BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(2, 1, 8, 8));

        ButtonGroup seedGroup = new ButtonGroup();
        seedGroup.add(blueSeedButton);
        seedGroup.add(redSeedButton);

        JPanel seedPanel = new JPanel(new FlowLayout());
        seedPanel.add(new JLabel("씨앗:"));
        seedPanel.add(blueSeedButton);
        seedPanel.add(redSeedButton);

        ButtonGroup waterGroup = new ButtonGroup();
        waterGroup.add(littleWaterButton);
        waterGroup.add(normalWaterButton);
        waterGroup.add(muchWaterButton);

        JPanel waterPanel = new JPanel(new FlowLayout());
        waterPanel.add(new JLabel("물의 양:"));
        waterPanel.add(littleWaterButton);
        waterPanel.add(normalWaterButton);
        waterPanel.add(muchWaterButton);

        choices.add(seedPanel);
        choices.add(waterPanel);
        add(choices, BorderLayout.CENTER);

        JButton plantButton = new JButton("심기");
        JButton cancelButton = new JButton("나가기");

        plantButton.addActionListener(event -> plantSeed());
        cancelButton.addActionListener(event -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancelButton);
        buttons.add(plantButton);
        add(buttons, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(plantButton);
        getRootPane().registerKeyboardAction(
                event -> dispose(),
                javax.swing.KeyStroke.getKeyStroke("ESCAPE"),
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setSize(560, 300);
        setLocationRelativeTo(owner);
    }

    private void plantSeed() {
        if (blueSeedButton.isSelected()) {
            puzzle.selectSeed(Seed.BLUE_BLOOM);
        } else if (redSeedButton.isSelected()) {
            puzzle.selectSeed(Seed.RED_BLOOM);
        }

        if (littleWaterButton.isSelected()) {
            puzzle.selectWater(WaterAmount.LITTLE);
        } else if (normalWaterButton.isSelected()) {
            puzzle.selectWater(WaterAmount.NORMAL);
        } else if (muchWaterButton.isSelected()) {
            puzzle.selectWater(WaterAmount.MUCH);
        }

        if (!puzzle.canPlant()) {
            JOptionPane.showMessageDialog(
                    this,
                    "씨앗과 물의 양을 모두 선택하세요.",
                    "선택이 필요합니다",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (puzzle.plant()) {
            JOptionPane.showMessageDialog(
                    this,
                    "씨앗을 심었습니다.\n현재 시간대에서 자란 모습을 확인하세요.",
                    "심기 완료",
                    JOptionPane.INFORMATION_MESSAGE
            );
            result = PuzzleResult.SOLVED;
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "기록과 맞지 않는 조건입니다. 연구 메모를 다시 확인하세요.",
                "조건 확인",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static PuzzleResult play(Frame owner) {
        SeedPlantingDialog dialog = new SeedPlantingDialog(owner);
        dialog.setVisible(true);
        return dialog.result;
    }
}
