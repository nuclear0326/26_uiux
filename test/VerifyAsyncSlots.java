package timeescape.ui;
import javax.swing.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import timeescape.system.*;import java.awt.*;import java.awt.image.*;
public class VerifyAsyncSlots {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static JButton button(GameMenuOverlay p,String n){for(Component c:p.getComponents())if(c instanceof JButton b&&n.equals(b.getName()))return b;throw new AssertionError(n);}
 static void edt(Runnable r)throws Exception{SwingUtilities.invokeAndWait(r);}static void flush()throws Exception{edt(()->{});edt(()->{});edt(()->{});}
 static java.util.List<SaveSlots.Summary> list(){return Arrays.stream(SaveSlots.Slot.values()).map(s->new SaveSlots.Summary(s,s==SaveSlots.Slot.AUTO||s==SaveSlots.Slot.ONE,s==SaveSlots.Slot.AUTO||s==SaveSlots.Slot.ONE,false,"현재 · 연구실 · 퍼즐 1개 완료","2026.11.23 12:00:00")).toList();}
 public static void main(String[] args)throws Exception{
  AtomicReference<GameMenuOverlay> ref=new AtomicReference<>();AtomicReference<CompletableFuture<java.util.List<SaveSlots.Summary>>> listing=new AtomicReference<>();AtomicReference<CompletableFuture<Boolean>> action=new AtomicReference<>();AtomicInteger calls=new AtomicInteger(),closed=new AtomicInteger();
  edt(()->{GameFonts.installDefaults();JPanel scene=new JPanel();scene.setSize(1440,810);GameMenuOverlay p=new GameMenuOverlay(scene,()->{closed.incrementAndGet();ref.get().release();},()->{},()->{});ref.set(p);p.setSize(1440,810);p.setAsyncSaveSlots(()->{var f=new CompletableFuture<java.util.List<SaveSlots.Summary>>();listing.set(f);return f;},slot->{calls.incrementAndGet();var f=new CompletableFuture<Boolean>();action.set(f);return f;},slot->{calls.incrementAndGet();var f=new CompletableFuture<Boolean>();action.set(f);return f;});p.open("save");check(!button(p,"slot-ONE").isEnabled(),"pending list disables slots");});
  var p=ref.get();listing.get().complete(list());flush();edt(()->{check(!button(p,"slot-AUTO").isEnabled(),"auto read only");check(button(p,"slot-TWO").isEnabled(),"empty slot save enabled");
   JPanel referenceScene=new JPanel();referenceScene.setSize(1440,810);GameMenuOverlay reference=new GameMenuOverlay(referenceScene,()->{},()->{},()->{});reference.setSize(1440,810);reference.setSaveSlots(VerifyAsyncSlots::list,slot->true,slot->true);reference.open("save");
   BufferedImage left=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB),right=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB);var gl=left.createGraphics();p.printAll(gl);gl.dispose();var gr=right.createGraphics();reference.printAll(gr);gr.dispose();
   check(Arrays.equals(left.getRGB(0,0,1440,810,null,0,1440),right.getRGB(0,0,1440,810,null,0,1440)),"ready async page pixel-identical to original synchronous layout");
button(p,"slot-TWO").doClick();check(p.isBusy(),"saving busy");button(p,"slot-TWO").doClick();p.back();button(p,"close").doClick();check(calls.get()==1&&closed.get()==0,"duplicate/escape/X blocked during write");});
  action.get().complete(true);flush();edt(()->check(!p.isBusy(),"save completion unlocks"));listing.get().complete(list());flush();
  edt(()->p.showPage("ingameload"));listing.get().complete(list());flush();edt(()->{check(!button(p,"slot-TWO").isEnabled(),"empty load disabled");button(p,"slot-ONE").doClick();check(p.isBusy(),"load pending");});action.get().completeExceptionally(new java.io.IOException("simulated"));flush();edt(()->check(!p.isBusy()&&closed.get()==0,"load error stays open"));listing.get().complete(list());flush();
  edt(()->button(p,"slot-ONE").doClick());action.get().complete(true);flush();edt(()->check(closed.get()==1&&!p.isVisible(),"successful load closes"));
  edt(()->p.open("save"));var stale=listing.get();edt(p::back);stale.complete(list());flush();edt(()->check(button(p,"save")!=null,"stale list cannot navigate back to slots"));
  edt(()->p.showPage("save"));listing.get().complete(list());flush();edt(()->{button(p,"slot-ONE").doClick();check(button(p,"confirm-save")!=null,"overwrite confirmation kept");button(p,"confirm-save").doClick();});action.get().complete(false);flush();edt(()->check(!p.isBusy()&&p.isVisible(),"save failure unlocks for retry"));listing.get().complete(list());flush();
  edt(()->{var actual=new BufferedImage(1440,810,BufferedImage.TYPE_INT_RGB);var g=actual.createGraphics();p.printAll(g);g.dispose();check(p.panel.width>0,"actual async slot layout rendered");});
  System.out.println("PASS "+checks+" async slot menu checks; no game window");
 }
}
