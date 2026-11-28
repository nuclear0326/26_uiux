package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.present.PresentPuzzles;
import timeescape.system.DeveloperMode;
public final class PresentPuzzleDialog extends JDialog {
    private boolean success;
    public static timeescape.puzzle.PuzzleResult play(Frame owner,String id){return play(owner,id,(id.equals("PZ008")||id.equals("PZ010")||id.equals("PZ011"))?"현재 / 연구실 작업대":"현재 / 연구실");}
    public static timeescape.puzzle.PuzzleResult play(Frame owner,String id,String context){if(id.equals("PZ008"))return RepairPuzzleDialog.play(owner,"설계도 복원",(solved,close)->new SchematicPuzzlePanel(solved,close,context));if(id.equals("PZ010"))return RepairPuzzleDialog.play(owner,"모듈 조립",(solved,close)->new ModuleAssemblyPanel(solved,close,context));if(id.equals("PZ011"))return RepairPuzzleDialog.play(owner,"시간 좌표 보정",(solved,close)->new TimeCoordinatesPanel(solved,close,context));PresentPuzzleDialog d=new PresentPuzzleDialog(owner,id,context);d.setVisible(true);return timeescape.puzzle.PuzzleResult.from(d.success);}
    private PresentPuzzleDialog(Frame owner,String id,String context){
        super(owner,PresentPuzzles.create(id).title(),true);setCursor(GameCursors.normal());setUndecorated(true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setContentPane(id.equals("PZ006")?new ArchiveLoginPanel(this::finish,this::dispose,()->{}):new PresentPuzzlePanel(id,this::finish,this::dispose,context));
        DeveloperMode.bind(getRootPane(),this::finish);getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        if(DeveloperMode.isEnabled())setTitle(getTitle()+" [Shift+8+9+0 스킵]");setSize(900,680);setMinimumSize(new Dimension(820,620));setLocationRelativeTo(owner);
    }
    private void finish(){success=true;dispose();}
}
