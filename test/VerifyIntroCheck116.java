package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.system.AudioSettings;

public final class VerifyIntroCheck116 {
 static int checks;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container child)layout(child);}
 static void render(GameMenuOverlay overlay,Path file)throws Exception{layout(overlay);BufferedImage im=new BufferedImage(overlay.getWidth(),overlay.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();overlay.printAll(g);g.dispose();ImageIO.write(im,"png",file.toFile());}
 public static void main(String[] args)throws Exception{
  Path out=timeescape.testutil.ArtifactPaths.output("v116-preview");Files.createDirectories(out);AudioSettings.get().ready().get();
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();MainMenuPanel main=VerifyExit116.menu();main.setSize(1440,810);AtomicInteger changes=new AtomicInteger();GameMenuOverlay a=new GameMenuOverlay(main,()->{},()->{},()->{});a.setSize(1440,810);a.setTransitionSettings(main::isTransitionAnimationEnabled,v->{changes.incrementAndGet();main.setTransitionAnimation(v);});a.open("settings");
   ok(a.introCheck.isSelected()&&main.isTransitionAnimationEnabled()&&changes.get()==0,"open reflects model without changing it");
   int[] volumes=java.util.Arrays.stream(AudioSettings.Channel.values()).mapToInt(AudioSettings.get()::volume).toArray();
   render(a,out.resolve("settings-checked.png"));a.introCheck.doClick(0);ok(!main.isTransitionAnimationEnabled()&&!a.introCheck.isSelected()&&changes.get()==1,"mouse checkbox disables intro once");render(a,out.resolve("settings-unchecked.png"));
   a.introCheck.getActionMap().get("toggle").actionPerformed(null);ok(main.isTransitionAnimationEnabled()&&changes.get()==2,"Enter toggles selection");
   for(String binding:new String[]{"pressed SPACE","released SPACE"}){Object key=a.introCheck.getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke(binding));ok(key!=null,"Space binding available");a.introCheck.getActionMap().get(key).actionPerformed(new ActionEvent(a.introCheck,0,""));}
   ok(!main.isTransitionAnimationEnabled()&&changes.get()==3,"Space toggles exactly once");
   a.release();a.open("settings");ok(!a.introCheck.isSelected()&&changes.get()==3,"reopening retains existing in-session setting");
   GameScenePanel scene=new GameScenePanel(id->{});scene.setSize(1440,810);scene.restorePowered(true);GameMenuOverlay b=new GameMenuOverlay(scene,()->{},()->{},()->{});b.setSize(1440,810);b.setTransitionSettings(main::isTransitionAnimationEnabled,main::setTransitionAnimation);b.open("settings");ok(!b.introCheck.isSelected(),"ingame settings share same intro flag");b.introCheck.doClick(0);a.showPage("settings");ok(a.introCheck.isSelected()&&main.isTransitionAnimationEnabled(),"main menu reflects ingame toggle");
   a.showPage("menu");ok(!a.introCheck.isVisible(),"checkbox only visible in settings");a.introCheck.doClick(0);ok(main.isTransitionAnimationEnabled()&&changes.get()==3,"hidden checkbox cannot change model");a.open("settings");a.introCheck.setEnabled(false);a.introCheck.doClick(0);ok(main.isTransitionAnimationEnabled(),"disabled checkbox no action");a.introCheck.setEnabled(true);
   for(int[] size:new int[][]{{1440,810},{1020,650},{900,600}}){a.setSize(size[0],size[1]);layout(a);ok(a.panel.contains(a.introCheck.getBounds()),"checkbox inside panel");ok(a.audioSettings.getY()+a.audioSettings.getHeight()<a.introCheck.getY(),"checkbox below audio controls");ok(a.introCheck.getFontMetrics(a.introCheck.getFont()).stringWidth(a.introCheck.getText())+34<a.introCheck.getWidth(),"checkbox label fits");}
   ok(java.util.Arrays.equals(volumes,java.util.Arrays.stream(AudioSettings.Channel.values()).mapToInt(AudioSettings.get()::volume).toArray()),"intro changes never change audio values");
   a.release();b.release();System.out.println("PASS "+checks+" checkbox mouse/keyboard, shared model, reopen, hidden/disabled, layout and volume preservation checks; no game windows.");
  }catch(Exception e){throw new RuntimeException(e);}});
  AudioSettings.get().save().get();
 }
}
