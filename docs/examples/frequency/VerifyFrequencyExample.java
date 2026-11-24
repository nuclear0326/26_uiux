package timeescape.ui;
import timeescape.puzzle.FrequencyPuzzle;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
public final class VerifyFrequencyExample {
 static int checks;
 static void check(boolean ok){if(!ok)throw new AssertionError();checks++;}
 static JButton button(Container c,String name){for(Component x:c.getComponents()){if(x instanceof JButton b&&name.equals(b.getText()))return b;if(x instanceof Container a){JButton b=button(a,name);if(b!=null)return b;}}return null;}
 static void click(Container c,String name){JButton b=button(c,name);if(b==null)throw new AssertionError(name);b.doClick(0);}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container a)layout(a);}
 public static void main(String[] args)throws Exception{
  FrequencyPuzzle m=new FrequencyPuzzle();check(!m.solved());m.turn(-1);check(m.value()==0);for(int i=0;i<7;i++)m.turn(1);check(m.solved());m.turn(100);check(m.value()==10);m.reset();check(m.value()==0&&!m.solved());
  SwingUtilities.invokeAndWait(()->{
   int[] count={0,0};var p=new FrequencyPuzzlePanel(()->count[0]++,()->count[1]++);click(p,"작동 확인");check(count[0]==0);for(int i=0;i<7;i++)click(p,"증가");click(p,"작동 확인");click(p,"작동 확인");p.developerSkip();check(count[0]==1&&count[1]==0);
   var cancelled=new FrequencyPuzzlePanel(()->count[0]++,()->count[1]++);cancelled.cancel();cancelled.cancel();cancelled.developerSkip();check(count[1]==1&&count[0]==1);
   var preview=new FrequencyPuzzlePanel(()->{},()->{});preview.setSize(900,680);layout(preview);var image=new BufferedImage(900,680,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();preview.printAll(g);g.dispose();check(preview.getWidth()==900);

  });
  System.out.println("PASS "+checks+" example checks; model, one-shot completion, cancellation, headless render. No game window.");
 }
}
