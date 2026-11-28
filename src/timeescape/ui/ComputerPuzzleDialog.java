package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.PuzzleResult;
import timeescape.system.*;
/** Window lifecycle only; renderable content retains the original puzzle behavior. */
public final class ComputerPuzzleDialog extends JDialog {
 private PuzzleResult result=PuzzleResult.CANCELLED;
 private final RushHourPanel panel;
 public static PuzzleResult play(Frame owner){var d=new ComputerPuzzleDialog(owner);d.setVisible(true);return d.result;}
 private ComputerPuzzleDialog(Frame owner){
  super(owner,"진단 컴퓨터 | 데이터 추출 터미널",true);
  setUndecorated(true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(975,728);setMinimumSize(new Dimension(905,660));setLocationRelativeTo(owner);
  panel=new RushHourPanel(()->{result=PuzzleResult.SOLVED;dispose();},this::dispose);setContentPane(panel);
  PuzzleChrome.bindClose(getRootPane(),this::dispose);DeveloperMode.bind(getRootPane(),panel::skip);
  if(DeveloperMode.isEnabled())setTitle(getTitle()+" [개발 모드: Shift+8+9+0 스킵]");
  GameAudio.get().puzzle(true);addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosed(java.awt.event.WindowEvent e){GameAudio.get().puzzle(false);}});
 }
 @Override public void dispose(){if(panel!=null)panel.close();super.dispose();}
}
