package timeescape.ui;

import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.ClockFocusPuzzle;

/** Dial-setting minigame. The journal supplies the target time; success is one-shot. */
public final class ClockFocusPanel extends RepairPuzzlePanel {
    private final ClockFocusPuzzle model = new ClockFocusPuzzle();
    private final JLabel reading = new JLabel("12:00", SwingConstants.CENTER);
    private final Dial dial = new Dial();

    public ClockFocusPanel(Runnable solved, Runnable close) {
        super(solved, close);
        JButton hourBack = new JButton("시침 −");
        JButton hourForward = new JButton("시침 +");
        JButton minuteBack = new JButton("분침 −5분");
        JButton minuteForward = new JButton("분침 +5분");
        JButton reset = new JButton("초기화");
        hourBack.addActionListener(e -> changeHour(-1));
        hourForward.addActionListener(e -> changeHour(1));
        minuteBack.addActionListener(e -> changeMinute(-5));
        minuteForward.addActionListener(e -> changeMinute(5));
        reset.addActionListener(e -> { if (!isFinished()) { model.setHour(12); model.setMinute(0); refresh(); } });
        action.setText("시각 확인");
        action.addActionListener(e -> verify());
        JPanel controls = new JPanel(new GridLayout(2, 2, 10, 8));
        controls.setOpaque(false);
        controls.add(hourBack); controls.add(hourForward);
        controls.add(minuteBack); controls.add(minuteForward);
        JPanel board = new JPanel(new BorderLayout(8, 8));
        board.setOpaque(false);
        reading.setFont(GameFonts.title(34)); reading.setForeground(LabTheme.TEXT);
        board.add(dial, BorderLayout.CENTER); board.add(reading, BorderLayout.SOUTH);
        build("시계 초점", "현재 / 연구실 시계 잠금장치",
            "연구 기록일지의 실험 시각에 맞추세요. 목표 시각은 10:10입니다. 시침과 분침을 각각 조정한 뒤 확인하세요.",
            board, hourBack, hourForward, minuteBack, minuteForward, reset);
        refresh();
    }
    private void changeHour(int delta) {
        if (isFinished()) return;
        model.setHour((model.hour() - 1 + delta + 12) % 12 + 1); refresh();
    }
    private void changeMinute(int delta) {
        if (isFinished()) return;
        model.setMinute((model.minute() + delta + 60) % 60); refresh();
    }
    private void refresh() { reading.setText(model.formatted()); status.setText("두 시계가 같은 순간을 가리키도록 맞추세요."); dial.repaint(); }
    private void verify() {
        if (isFinished()) return;
        if (model.solved()) { status.setText("시계 기록이 열렸습니다."); solve(); }
        else status.setText("아직 일치하지 않습니다. 시침과 분침을 다시 확인하세요.");
    }
    @Override protected void skipStage() { model.setHour(10); model.setMinute(10); refresh(); verify(); }

    private final class Dial extends JPanel {
        Dial() { setOpaque(false); setPreferredSize(new Dimension(300, 260)); }
        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create(); LabTheme.quality(g);
            int size = Math.max(120, Math.min(getWidth(), getHeight()) - 28);
            int cx = getWidth()/2, cy = getHeight()/2, r=size/2;
            g.setColor(new Color(25, 37, 41)); g.fillOval(cx-r, cy-r, size, size);
            g.setColor(new Color(198, 165, 105)); g.setStroke(new BasicStroke(3)); g.drawOval(cx-r, cy-r, size, size);
            for (int i=0;i<12;i++) {
                double a=Math.PI*2*i/12-Math.PI/2;
                int x1=cx+(int)((r-13)*Math.cos(a)), y1=cy+(int)((r-13)*Math.sin(a));
                int x2=cx+(int)((r-25)*Math.cos(a)), y2=cy+(int)((r-25)*Math.sin(a));
                g.setColor(new Color(225,205,164)); g.setStroke(new BasicStroke(2)); g.drawLine(x1,y1,x2,y2);
            }
            double ma=Math.PI*2*model.minute()/60-Math.PI/2;
            double ha=Math.PI*2*((model.hour()%12)+model.minute()/60.0)/12-Math.PI/2;
            g.setColor(new Color(104,212,219)); g.setStroke(new BasicStroke(3)); g.drawLine(cx,cy,cx+(int)((r-34)*Math.cos(ma)),cy+(int)((r-34)*Math.sin(ma)));
            g.setColor(new Color(234,196,129)); g.setStroke(new BasicStroke(5)); g.drawLine(cx,cy,cx+(int)((r-62)*Math.cos(ha)),cy+(int)((r-62)*Math.sin(ha)));
            g.setColor(Color.WHITE); g.fillOval(cx-5,cy-5,10,10); g.dispose();
        }
    }
}
