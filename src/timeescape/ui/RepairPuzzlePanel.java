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
        setBackground(new Color(17,24,32));
        setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
    }
    protected final void build(String heading, String instructions, JComponent board, JButton... controls) {
        JPanel header=new JPanel(new GridLayout(0,1,0,7));header.setOpaque(false);
        JLabel title=new JLabel(heading);title.setFont(GameFonts.title(23));title.setForeground(Color.WHITE);header.add(title);
        JLabel help=new JLabel(instructions);help.setForeground(new Color(199,211,221));header.add(help);add(header,BorderLayout.NORTH);
        add(board,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(0,10));footer.setOpaque(false);status.setForeground(Color.WHITE);footer.add(status,BorderLayout.NORTH);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.CENTER,9,0));buttons.setOpaque(false);
        for(JButton control:controls)buttons.add(control);
        buttons.add(action);
        JButton close=new JButton("나가기");close.addActionListener(e->{if(!finished){finished=true;onClose.run();}});buttons.add(close);
        footer.add(buttons,BorderLayout.CENTER);
        JLabel note=new JLabel("미완료 상태에서 나가면 이 퍼즐은 처음부터 시작합니다.",SwingConstants.CENTER);note.setForeground(new Color(158,174,187));footer.add(note,BorderLayout.SOUTH);add(footer,BorderLayout.SOUTH);
    }
    protected final boolean isFinished(){return finished;}
    protected final void solve(){if(!finished){finished=true;onSolved.run();}}
    public final void cancelSession(){finished=true;}
    public final void developerSkip(){if(!finished)skipStage();}
    protected abstract void skipStage();
}
