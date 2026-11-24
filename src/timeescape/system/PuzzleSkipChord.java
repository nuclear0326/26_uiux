package timeescape.system;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
/** Tracks physical keys, so Shift-generated characters such as * and ( do not matter. */
public final class PuzzleSkipChord {
 private final Set<Integer> down=new HashSet<>();
 private boolean fired;
 public void reset(){down.clear();fired=false;}
 public boolean accept(int event,int code,boolean shift){
  if(event==KeyEvent.KEY_RELEASED){down.remove(code);if(code==KeyEvent.VK_SHIFT)reset();else if(down.isEmpty())fired=false;return false;}
  if(event!=KeyEvent.KEY_PRESSED)return false;
  if(!shift){reset();return false;}
  if(code==KeyEvent.VK_8||code==KeyEvent.VK_9||code==KeyEvent.VK_0||code==KeyEvent.VK_NUMPAD8||code==KeyEvent.VK_NUMPAD9||code==KeyEvent.VK_NUMPAD0)down.add(code);
  if(!fired&&has(KeyEvent.VK_8,KeyEvent.VK_NUMPAD8)&&has(KeyEvent.VK_9,KeyEvent.VK_NUMPAD9)&&has(KeyEvent.VK_0,KeyEvent.VK_NUMPAD0)){fired=true;return true;}return false;
 }
 private boolean has(int a,int b){return down.contains(a)||down.contains(b);}
}
