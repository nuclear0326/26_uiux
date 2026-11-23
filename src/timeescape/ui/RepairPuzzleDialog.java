package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.PuzzleResult;
import timeescape.system.DeveloperMode;
import timeescape.system.GameAudio;

/** Common modal host. Puzzle rules, input and stage state belong only to its content panel. */
public final class RepairPuzzleDialog extends JDialog {
    @FunctionalInterface public interface PanelFactory {
        RepairPuzzlePanel create(Runnable onSolved, Runnable onClose);
    }
    private final RepairPuzzlePanel content;
    private PuzzleResult result=PuzzleResult.CANCELLED;
    private boolean closed;
    public static PuzzleResult play(Frame owner,String title,PanelFactory factory){
        RepairPuzzleDialog dialog=new RepairPuzzleDialog(owner,title,factory);
        dialog.setVisible(true);return dialog.result;
    }
    private RepairPuzzleDialog(Frame owner,String title,PanelFactory factory){
        super(owner,title,true);
        setCursor(GameCursors.normal());setUndecorated(true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(1000,820);setMinimumSize(new Dimension(900,760));setLocationRelativeTo(owner);
        content=factory.create(this::finish,this::dispose);setContentPane(content);PuzzleChrome.bindClose(getRootPane(),this::dispose);
        DeveloperMode.bind(getRootPane(),content::developerSkip);
        if(DeveloperMode.isEnabled())setTitle(getTitle()+" [개발 모드: Ctrl+Shift+F10 단계 스킵]");
        GameAudio.get().puzzle(true);
        addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){GameAudio.get().puzzle(false);}});
    }
    private void finish(){if(closed)return;result=PuzzleResult.SOLVED;GameAudio.get().play("puzzle_success");dispose();}
    @Override public void dispose(){closed=true;if(content!=null)content.cancelSession();super.dispose();}
}
