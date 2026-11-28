package timeescape.ui;
import timeescape.core.*;
import timeescape.scene.InteractionHint;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
public final class VerifySpec101 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static boolean held(GameScenePanel p)throws Exception{var f=GameScenePanel.class.getDeclaredField("revealHeld");f.setAccessible(true);return f.getBoolean(p);}
 static void mouse(GameScenePanel p,int type,int button){p.dispatchEvent(new MouseEvent(p,type,0,0,10,10,1,false,button));}
 static void key(GameScenePanel p,String key){p.getActionMap().get(key).actionPerformed(new ActionEvent(p,0,key));}
 public static void main(String[] args)throws Exception{
  var s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");
  for(String id:List.of("CASE","CELL","MODULE")){ok(!s.canCollect(id)&&InteractionHint.of(s,id)==InteractionHint.LATER,"materials gated after power");try{s.collect(id);throw new AssertionError();}catch(IllegalStateException expected){checks++;}}
  ok(s.canCollect("USB"),"USB still obtainable before analysis");s.collect("USB");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.complete("PZ001");s.complete("PZ007");
  ok(!s.canCollect("CASE"),"copy alone does not unlock materials");s.complete("PZ021");
  for(String id:List.of("CASE","CELL","MODULE")){ok(s.canCollect(id)&&InteractionHint.of(s,id)==InteractionHint.READY,"analysis unlocks pickup and hint");s.collect(id);}
  var snapshot=s.snapshot();ok(GameState.restore(ProgressCatalog.standard(),snapshot).snapshot().equals(snapshot),"save roundtrip unchanged");s.complete("PZ008");s.complete("PZ010");s.complete("PZ011");ok(s.canDepart(),"full repair still works");
  var old=GameState.restore(ProgressCatalog.standard(),new GameState.Snapshot(2,"present_1",Set.of("PZ004","PZ008"),Set.of("CASE"),Set.of("power_restored"),Set.of("present_1"),Set.of("CASE")));
  ok(old.has("CASE")&&old.isItemUnread("CASE")&&old.canCollect("CELL"),"old owned items and repair preserved");
  SwingUtilities.invokeAndWait(()->{try{
   GameScenePanel p=new GameScenePanel(id->{throw new AssertionError("middle button must not activate object");});
   mouse(p,MouseEvent.MOUSE_PRESSED,MouseEvent.BUTTON2);ok(held(p),"middle hold reveals");
   key(p,"revealObjects");mouse(p,MouseEvent.MOUSE_RELEASED,MouseEvent.BUTTON2);ok(held(p),"Shift remains held");
   key(p,"hideObjects");ok(!held(p),"both released");
   key(p,"revealObjects");mouse(p,MouseEvent.MOUSE_PRESSED,MouseEvent.BUTTON2);key(p,"hideObjects");ok(held(p),"middle remains held");
   p.setInputBlocked(true);ok(!held(p),"overlay clears both holds");mouse(p,MouseEvent.MOUSE_PRESSED,MouseEvent.BUTTON2);ok(!held(p),"blocked input ignored");
   p.setInputBlocked(false);ok(!held(p),"unblock does not restore stale hold");mouse(p,MouseEvent.MOUSE_PRESSED,MouseEvent.BUTTON2);p.setEra(timeescape.model.Era.PRESENT);ok(!held(p),"era change clears hold");
   mouse(p,MouseEvent.MOUSE_CLICKED,MouseEvent.BUTTON2);p.removeNotify();ok(!held(p),"removal clears holds");
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" pickup gates, legacy saves, mixed input and reset checks; no game window.");
 }
}
