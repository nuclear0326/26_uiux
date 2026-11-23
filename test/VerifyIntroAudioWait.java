package timeescape.ui;
import javax.swing.*;import timeescape.system.GameAudio;
public class VerifyIntroAudioWait {
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
 int[] starts={0};var panel=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){starts[0]++;}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
 var activate=MainMenuPanel.class.getDeclaredMethod("activate",int.class);activate.setAccessible(true);var advance=MainMenuPanel.class.getDeclaredMethod("advance",double.class);advance.setAccessible(true);var departure=MainMenuPanel.class.getDeclaredField("departure");departure.setAccessible(true);var clock=GameAudio.class.getDeclaredField("transitionSeconds");clock.setAccessible(true);
 activate.invoke(panel,0);clock.setDouble(GameAudio.get(),-2);advance.invoke(panel,20.0);if(starts[0]!=0||departure.getDouble(panel)!=0)throw new AssertionError("pending audio must hold intro");
 clock.setDouble(GameAudio.get(),2.45);advance.invoke(panel,.1);if(departure.getDouble(panel)!=2.45||starts[0]!=0)throw new AssertionError("follow audio clock");
 clock.setDouble(GameAudio.get(),-1);advance.invoke(panel,10.0);if(starts[0]!=1)throw new AssertionError("device fallback completes");
 System.out.println("PASS 3 intro wait/audio clock/fallback integration checks; no window or audio device");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
