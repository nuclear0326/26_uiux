package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.PuzzleResult;
import timeescape.system.DeveloperMode;
/** Modal adapter for the existing PuzzleRunner contract. */
public final class FrequencyPuzzleDialog extends JDialog {
 private PuzzleResult result=PuzzleResult.CANCELLED;
 private final FrequencyPuzzlePanel panel;
 private FrequencyPuzzleDialog(Frame owner){
  super(owner,"주파수 맞추기",true);
  setUndecorated(true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setCursor(GameCursors.normal());
  panel=new FrequencyPuzzlePanel(()->{result=PuzzleResult.SOLVED;dispose();},this::dispose);setContentPane(panel);
  PuzzleChrome.bindClose(getRootPane(),panel::cancel);DeveloperMode.bind(getRootPane(),panel::developerSkip);
  setSize(900,680);setMinimumSize(new Dimension(820,620));setLocationRelativeTo(owner);
 }
 public static PuzzleResult play(Frame owner){var dialog=new FrequencyPuzzleDialog(owner);dialog.setVisible(true);return dialog.result;}
 @Override public void dispose(){if(panel!=null)panel.cancelSession();super.dispose();}
}
