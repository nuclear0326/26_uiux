package timeescape.ui;
import java.util.List;
import javax.swing.SwingUtilities;
import timeescape.core.GameState;
import timeescape.scene.InteractionHint;
public final class VerifyTyping86 {
 static int n;static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);n++;}
 public static void main(String[] args)throws Exception{
  SwingUtilities.invokeAndWait(()->{
   var d=new DialogueOverlay();int[] done={0};d.open(List.of("가나다\n라마","다음"),()->done[0]++);
   check(d.visibleText().isEmpty()&&d.isTyping(),"starts blank");d.typeNext();check(d.visibleText().equals("가"),"one complete Hangul character");
   d.typeNext();check(d.visibleText().equals("가나"),"left to right");d.advance();check(d.visibleText().equals("가나다\n라마")&&done[0]==0,"first click reveals only");
   d.advance();check(d.currentText().equals("다음")&&d.visibleText().isEmpty(),"next page restarts typing");
   d.advance();d.advance();d.advance();check(done[0]==1&&!d.isVisible(),"completion once");
   d.open(List.of("중단"),()->done[0]++);d.dismiss();d.typeNext();check(d.visibleText().isEmpty()&&done[0]==1,"dismiss stops text and callback");
   var s=GameState.newGame();check(InteractionHint.of(s,"CASE")==InteractionHint.READY,"only tutorial box ready");
   for(String id:List.of("POWER","PRESENT_PC","MEMO","WORKBENCH","USB","CELL","MODULE"))check(InteractionHint.of(s,id)==InteractionHint.NONE,"no tutorial light: "+id);
   s.takePrototype();check(InteractionHint.of(s,"CASE")==InteractionHint.NONE,"picked box no longer highlighted");
   s.usePrototype();check(InteractionHint.of(s,"POWER")==InteractionHint.READY,"normal highlighting resumes after failure");
  });System.out.println("PASS "+n+" typing and tutorial highlight checks; no audio device or game");
 }
}
