package timeescape.system;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
/** Development shortcuts scoped to a visible, focused puzzle window. */
public final class DeveloperMode {
 private static boolean enabled;
 // Temporary design-review build switch. Turn off before a normal release.
 public static final boolean TEMPORARY_PUZZLE_CHEAT=true;
 private DeveloperMode(){}
 public static boolean isEnabled(){return enabled;}
 public static void setEnabled(boolean value){enabled=value;}
 public static void bind(JRootPane root,Runnable stageSkip){
  if(!TEMPORARY_PUZZLE_CHEAT||Boolean.TRUE.equals(root.getClientProperty("temporaryPuzzleCheat")))return;
  root.putClientProperty("temporaryPuzzleCheat",Boolean.TRUE);
  KeyboardFocusManager manager=KeyboardFocusManager.getCurrentKeyboardFocusManager();PuzzleSkipChord chord=new PuzzleSkipChord();
  KeyEventDispatcher dispatcher=e->{
   Component focus=manager.getFocusOwner();Window owner=SwingUtilities.getWindowAncestor(root);
   if(!root.isShowing()||owner==null||manager.getFocusedWindow()!=owner||focus==null||SwingUtilities.getRootPane(focus)!=root){chord.reset();return false;}
   if(chord.accept(e.getID(),e.getKeyCode(),e.isShiftDown())){stageSkip.run();return true;}return false;
  };
  PropertyChangeListener focusReset=e->{if(e.getNewValue()!=SwingUtilities.getWindowAncestor(root))chord.reset();};
  boolean[] installed={false};
  Runnable update=()->{boolean showing=root.isShowing();if(showing&&!installed[0]){manager.addKeyEventDispatcher(dispatcher);manager.addPropertyChangeListener("focusedWindow",focusReset);installed[0]=true;}else if(!showing&&installed[0]){manager.removeKeyEventDispatcher(dispatcher);manager.removePropertyChangeListener("focusedWindow",focusReset);installed[0]=false;chord.reset();}};
  root.addHierarchyListener(e->{if((e.getChangeFlags()&(HierarchyEvent.SHOWING_CHANGED|HierarchyEvent.DISPLAYABILITY_CHANGED))!=0)update.run();});update.run();
 }
}

