package timeescape.ui;

import java.awt.*;
import javax.swing.*;

/** Shared layout and one-shot lifecycle; each puzzle owns its rules and input. */
public abstract class RepairPuzzlePanel extends JPanel {
    protected final JLabel status = new JLabel(" ");
    protected final JButton action = new JButton();
    private final Runnable onSolved, onClose;
    private boolean finished;

    protected RepairPuzzlePanel(Runnable onSolved, Runnable onClose) {
        super(new BorderLayout(12,12));
        this.onSolved=onSolved; this.onClose=onClose;
        PuzzleChrome.decorate(this);
    }
    protected final void build(String heading, String instructions, JComponent board, JButton... controls) {
        add(PuzzleChrome.header(heading,heading.contains("시계")?"과거 / 4 × 4 기어 복원":"미래 / 구형 장치 배선 복구",instructions,this::closePuzzle),BorderLayout.NORTH);
        add(board,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(0,10));footer.setOpaque(false);status.setForeground(LabTheme.TEXT);status.setFont(GameFonts.body(14));footer.add(status,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER,9,0));buttons.setOpaque(false);
        for(JButton control:controls){PuzzleChrome.styleButton(control);buttons.add(control);}
        PuzzleChrome.styleButton(action);
        buttons.add(action);
        JButton close=new JButton("조사 종료");PuzzleChrome.styleButton(close);close.addActionListener(e->closePuzzle());buttons.add(close);
        footer.add(buttons,BorderLayout.CENTER);
        JLabel note=new JLabel("미완료 상태에서 나가면 이 퍼즐은 처음부터 시작합니다.",SwingConstants.CENTER);note.setFont(GameFonts.body(14));note.setForeground(new Color(158,174,187));footer.add(note,BorderLayout.SOUTH);add(footer,BorderLayout.SOUTH);
    }
    private void closePuzzle(){if(!finished){finished=true;onClose.run();}}
    protected final boolean isFinished(){return finished;}
    protected final void solve(){if(!finished){finished=true;onSolved.run();}}
    public final void cancelSession(){finished=true;}
    public final void developerSkip(){if(!finished)skipStage();}
    protected abstract void skipStage();
}
