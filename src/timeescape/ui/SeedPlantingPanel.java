package timeescape.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import timeescape.puzzle.SeedPlantingPuzzle;
import timeescape.puzzle.SeedPlantingPuzzle.Seed;
import timeescape.puzzle.SeedPlantingPuzzle.WaterAmount;

/** Themed, reusable content panel for the seed planting minigame. */
public final class SeedPlantingPanel extends JPanel {
    private final SeedPlantingPuzzle puzzle = new SeedPlantingPuzzle();
    private final Runnable onSolved;
    private final JLabel status = new JLabel("씨앗과 물의 양을 선택하세요.", SwingConstants.CENTER);

    private final ChoiceCard blueSeed = new ChoiceCard("푸른 씨앗", "푸른빛 꽃이 피는 종자", "01");
    private final ChoiceCard redSeed = new ChoiceCard("붉은 씨앗", "붉은빛 꽃이 피는 종자", "02");
    private final ChoiceCard littleWater = new ChoiceCard("조금", "토양 표면만 적심", "LOW");
    private final ChoiceCard normalWater = new ChoiceCard("보통", "기록된 표준 공급량", "MID");
    private final ChoiceCard muchWater = new ChoiceCard("많이", "토양에 물이 고일 정도", "HIGH");

    public SeedPlantingPanel(Runnable onSolved, Runnable onClose) {
        super(new BorderLayout(0, 18));
        this.onSolved = onSolved;
        PuzzleChrome.decorate(this);

        add(PuzzleChrome.header(
                "씨앗 심기",
                "과거 / 연구실",
                "연구 기록을 바탕으로 씨앗과 물의 양을 선택하세요.\n푸른 조명 아래, 기록된 양만큼 물을 주어라.",
                onClose
        ), BorderLayout.NORTH);
        add(buildChoices(), BorderLayout.CENTER);
        add(buildFooter(onClose), BorderLayout.SOUTH);

        ButtonGroup seedGroup = new ButtonGroup();
        seedGroup.add(blueSeed);
        seedGroup.add(redSeed);

        ButtonGroup waterGroup = new ButtonGroup();
        waterGroup.add(littleWater);
        waterGroup.add(normalWater);
        waterGroup.add(muchWater);

        blueSeed.addActionListener(event -> puzzle.selectSeed(Seed.BLUE_BLOOM));
        redSeed.addActionListener(event -> puzzle.selectSeed(Seed.RED_BLOOM));
        littleWater.addActionListener(event -> puzzle.selectWater(WaterAmount.LITTLE));
        normalWater.addActionListener(event -> puzzle.selectWater(WaterAmount.NORMAL));
        muchWater.addActionListener(event -> puzzle.selectWater(WaterAmount.MUCH));
    }

    private JPanel buildChoices() {
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 16));
        content.setOpaque(false);

        JPanel seedSection = new JPanel(new BorderLayout(0, 8));
        seedSection.setOpaque(false);
        seedSection.add(sectionTitle("01  씨앗 선택"), BorderLayout.NORTH);
        JPanel seeds = new JPanel(new GridLayout(1, 2, 14, 0));
        seeds.setOpaque(false);
        seeds.add(blueSeed);
        seeds.add(redSeed);
        seedSection.add(seeds, BorderLayout.CENTER);

        JPanel waterSection = new JPanel(new BorderLayout(0, 8));
        waterSection.setOpaque(false);
        waterSection.add(sectionTitle("02  물 공급량"), BorderLayout.NORTH);
        JPanel water = new JPanel(new GridLayout(1, 3, 14, 0));
        water.setOpaque(false);
        water.add(littleWater);
        water.add(normalWater);
        water.add(muchWater);
        waterSection.add(water, BorderLayout.CENTER);

        content.add(seedSection);
        content.add(waterSection);
        return content;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(GameFonts.button(15));
        label.setForeground(LabTheme.GOLD);
        return label;
    }

    private JPanel buildFooter(Runnable onClose) {
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        status.setFont(GameFonts.body(14));
        status.setForeground(LabTheme.MUTED);
        footer.add(status, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton closeButton = LabTheme.button("조사 종료");
        JButton plantButton = LabTheme.button("씨앗 심기");
        closeButton.addActionListener(event -> onClose.run());
        plantButton.addActionListener(event -> plantSeed());
        actions.add(closeButton);
        actions.add(plantButton);
        footer.add(actions, BorderLayout.SOUTH);
        return footer;
    }

    private void plantSeed() {
        if (!puzzle.canPlant()) {
            status.setForeground(new Color(234, 157, 118));
            status.setText("씨앗과 물의 양을 모두 선택하세요.");
            return;
        }

        if (puzzle.plant()) {
            status.setForeground(LabTheme.GREEN);
            status.setText("씨앗을 심었습니다. 현재 시간대에서 자란 모습을 확인하세요.");
            onSolved.run();
            return;
        }

        status.setForeground(new Color(234, 157, 118));
        status.setText("조건이 맞지 않습니다. 연구 기록을 다시 살펴보세요.");
    }

    /** A project-styled selectable card, avoiding platform-default radio controls. */
    private static final class ChoiceCard extends JToggleButton {
        private final String title;
        private final String detail;
        private final String marker;

        ChoiceCard(String title, String detail, String marker) {
            this.title = title;
            this.detail = detail;
            this.marker = marker;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setCursor(GameCursors.interact());
            setPreferredSize(new Dimension(180, 108));
            getAccessibleContext().setAccessibleName(title + ": " + detail);
        }

        @Override
        protected void paintComponent(Graphics raw) {
            Graphics2D g = (Graphics2D) raw.create();
            LabTheme.quality(g);
            int width = getWidth();
            int height = getHeight();
            boolean selected = isSelected();
            boolean active = selected || getModel().isRollover() || isFocusOwner();

            g.setColor(selected ? new Color(45, 58, 54)
                    : getModel().isRollover() ? new Color(36, 46, 45) : LabTheme.PANEL);
            g.fillRoundRect(1, 1, width - 3, height - 3, 12, 12);
            g.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
            g.setColor(active ? LabTheme.GOLD : LabTheme.LINE);
            g.drawRoundRect(1, 1, width - 3, height - 3, 12, 12);
            LabTheme.screw(g, 12, 12);
            LabTheme.screw(g, width - 13, height - 13);

            g.setFont(GameFonts.body(12));
            g.setColor(selected ? LabTheme.GREEN : LabTheme.MUTED);
            LabTheme.center(g, marker, width / 2, 31);

            g.setFont(GameFonts.button(18));
            g.setColor(LabTheme.TEXT);
            LabTheme.center(g, title, width / 2, height / 2 + 5);

            g.setFont(GameFonts.body(12));
            g.setColor(LabTheme.MUTED);
            LabTheme.center(g, detail, width / 2, height - 22);

            if (selected) {
                g.setColor(LabTheme.GREEN);
                g.fillOval(width - 29, 20, 8, 8);
            }
            g.dispose();
        }
    }
}
