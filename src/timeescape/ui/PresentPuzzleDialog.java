package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.PresentPuzzleModel;
import timeescape.system.DeveloperMode;
public final class PresentPuzzleDialog extends JDialog {
    private boolean success;
    public static boolean play(Frame owner,String id){PresentPuzzleDialog d=new PresentPuzzleDialog(owner,id);d.setVisible(true);return d.success;}
    private PresentPuzzleDialog(Frame owner,String id){
        super(owner,new PresentPuzzleModel(id).title(),true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setContentPane(new PresentPuzzlePanel(id,this::finish,this::dispose));
        DeveloperMode.bind(getRootPane(),this::finish);getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        if(DeveloperMode.isEnabled())setTitle(getTitle()+" [Ctrl+Shift+F10 스킵]");setSize(900,680);setMinimumSize(new Dimension(820,620));setLocationRelativeTo(owner);
    }
    private void finish(){success=true;dispose();}
}
