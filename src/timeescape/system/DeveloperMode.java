package timeescape.system;
import javax.swing.*;
import java.awt.event.ActionEvent;
/** Session-only tooling. Disabled on every fresh game process. */
public final class DeveloperMode {
 private static boolean enabled;
 private DeveloperMode(){}
 public static boolean isEnabled(){return enabled;}
 public static void setEnabled(boolean value){enabled=value;}
 public static void bind(JRootPane root,Runnable skip){
  root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ctrl shift F10"),"developerSkip");
  root.getActionMap().put("developerSkip",new AbstractAction(){public void actionPerformed(ActionEvent e){if(DeveloperMode.isEnabled())skip.run();}});
 }
}
