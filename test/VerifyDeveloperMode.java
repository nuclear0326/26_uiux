import javax.swing.*;
import timeescape.system.DeveloperMode;
public class VerifyDeveloperMode {
 public static void main(String[] args)throws Exception{
  SwingUtilities.invokeAndWait(()->{
   int[] count={0};JRootPane pane=new JRootPane();DeveloperMode.bind(pane,()->count[0]++);
   Object key=pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(KeyStroke.getKeyStroke("ctrl shift F10"));
   if(!"developerSkip".equals(key)||DeveloperMode.isEnabled())throw new AssertionError("default/binding");
   Action action=pane.getActionMap().get(key);action.actionPerformed(null);
   if(count[0]!=0)throw new AssertionError("skip enabled by default");
   DeveloperMode.setEnabled(true);action.actionPerformed(null);
   if(count[0]!=1)throw new AssertionError("skip disabled");
   DeveloperMode.setEnabled(false);action.actionPerformed(null);
   if(count[0]!=1)throw new AssertionError("disable ignored");
  });
  System.out.println("Developer skip: default off, shortcut, enabled callback and disable passed.");
 }
}
