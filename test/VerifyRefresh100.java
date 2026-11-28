package timeescape.ui;
import timeescape.core.GameState;
import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

/** Headless scheduling and rendered parity checks. No game window. */
public final class VerifyRefresh100 {
 static int checks;
 static void ok(boolean v,String message){if(!v)throw new AssertionError(message);checks++;}
 static MissionPanel panel;
 static int refreshes;
 static GameState first,loaded;
 static void edt(Runnable action)throws Exception{SwingUtilities.invokeAndWait(action);}
 static void flush()throws Exception{edt(()->{});}
 static String text(){StringBuilder s=new StringBuilder();for(Component c:panel.getComponents())if(c instanceof JTextArea t)s.append(t.getText()).append('\n');return s.toString();}
 public static void main(String[] args)throws Exception{
  edt(()->{GameFonts.installDefaults();panel=new MissionPanel();panel.addContainerListener(new java.awt.event.ContainerAdapter(){public void componentAdded(java.awt.event.ContainerEvent e){if(e.getChild() instanceof JTextArea t && t.getText().equals("현재 목표"))refreshes++;}});first=GameState.newGame();
   panel.setLastMessage("before initialization");ok(refreshes==0,"no premature render");panel.refresh(first);
   ok(text().contains("before initialization"),"message retained before first state");
   panel.setLastMessage("completed puzzle");panel.refresh(first);
   ok(refreshes==2,"completion refreshes research once");
  });flush();
  edt(()->{ok(refreshes==2,"queued duplicate cancelled");panel.setLastMessage("only message");});flush();
  edt(()->{ok(refreshes==3&&text().contains("only message"),"message-only action still updates");
   panel.setLastMessage("older");panel.setLastMessage("latest");});flush();
  edt(()->{ok(refreshes==4&&text().contains("latest")&&!text().contains("older"),"burst renders latest once");
   panel.setLastMessage("loaded progress");loaded=GameState.newGame();loaded.takePrototype();loaded.usePrototype();panel.refresh(loaded);
  });flush();
  edt(()->{ok(refreshes==5&&text().contains("loaded progress"),"load cancels old scheduled refresh");
   MissionPanel baseline=new MissionPanel();baseline.setLastMessage("loaded progress");baseline.refresh(loaded);
   ok(Arrays.equals(VerifyViewUpdater96.pixels(panel),VerifyViewUpdater96.pixels(baseline)),"pixels equal immediate refresh baseline");
   var before=loaded.snapshot();panel.setLastMessage("next message");panel.refresh(loaded);panel.setLastMessage("last message");
   ok(loaded.snapshot().equals(before),"render does not mutate saves");
  });flush();
  edt(()->ok(refreshes==7&&text().contains("last message"),"message after full refresh not lost"));
  System.out.println("PASS "+checks+" coalescing, message-only, loaded state and pixel parity checks; no game window.");
 }
}
