package timeescape.ui;
import timeescape.core.*;import java.awt.*;import java.awt.image.BufferedImage;import java.util.*;import javax.swing.*;
public final class VerifyViewUpdater96 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static Object field(Object o,String key)throws Exception{var f=o.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(o);}
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container p)layout(p);}
 static int[] pixels(JComponent c){c.setSize(430,950);layout(c);layout(c);BufferedImage b=new BufferedImage(430,950,BufferedImage.TYPE_INT_RGB);Graphics2D g=b.createGraphics();c.paint(g);g.dispose();return b.getRGB(0,0,430,950,null,0,430);}
 static final class Views {
  final GameScenePanel scene=new GameScenePanel(id->{});final MissionPanel mission=new MissionPanel();final InventoryPanel inventory=new InventoryPanel(id->{});final JLabel progress=new JLabel();
  final GameHud hud=new GameHud(scene,mission,inventory,()->{},()->{},()->{});
  final GameViewUpdater updater=new GameViewUpdater(scene,mission,hud,inventory,progress);
  void direct(GameState state,boolean selected){scene.setEra(state.era());scene.setProgressHints(state);scene.setPowered(state.powered());mission.refresh(state);hud.setLocation(state.era().title(),state.locationTitle().replaceFirst("^.*? · ",""));inventory.refresh(state,selected);}
  void close(){hud.removeNotify();scene.removeNotify();}
 }
 public static void main(String[] args)throws Exception{
  java.util.List<GameState.Snapshot> stages=new ArrayList<>();GameState s=GameState.newGame();stages.add(s.snapshot());s.takePrototype();s.usePrototype();stages.add(s.snapshot());s.complete("PZ004");s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.complete("PZ001");s.collect("USB");s.complete("PZ007");stages.add(s.snapshot());s.complete("PZ021");stages.add(s.snapshot());s.complete("PZ008");for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);s.complete("PZ010");s.complete("PZ011");s.moveToPast();s.collectSparePart();stages.add(s.snapshot());s.repairWatch();s.moveToFuture();stages.add(s.snapshot());s.collectCorePart();s.installCorePart();s.escape();stages.add(s.snapshot());stages.add(stages.get(2));
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();Views actual=new Views(),baseline=new Views();
   for(var snapshot:stages){GameState state=GameState.restore(ProgressCatalog.standard(),snapshot);boolean selected=state.hasSparePart();actual.updater.refresh(state,selected);baseline.direct(state,selected);
    ok(state.snapshot().equals(snapshot),"refresh cannot change progress or read markers");
    ok(field(actual.scene,"era")==state.era()&&field(actual.scene,"powered").equals(state.powered()),"scene follows latest loaded state");
    ok(field(actual.scene,"hints").equals(field(baseline.scene,"hints")),"interaction hints unchanged");
    ok(actual.hud.timeValue.getText().equals(baseline.hud.timeValue.getText())&&actual.hud.placeValue.getText().equals(baseline.hud.placeValue.getText()),"location unchanged");
    ok(Arrays.equals(pixels(actual.mission),pixels(baseline.mission)),"research text and layout pixel parity");
    ok(Arrays.equals(pixels(actual.inventory),pixels(baseline.inventory)),"inventory design and selection pixel parity");
    actual.updater.refresh(state,selected);ok(state.snapshot().equals(snapshot),"repeated update remains read-only");
   }
   String text="";for(Component c:actual.mission.getComponents())if(c instanceof JTextArea t){text+=t.getText()+"\n";ok(t.getX()==0,"left aligned objectives");}
   ok(text.contains("PC 또는 보관함의 USB")&&!text.contains("사고 로그 확보 → USB 분석"),"reloaded earlier save restores current objective");
   actual.close();baseline.close();System.out.println("PASS "+checks+" view parity, state replacement, read-only progress and layout checks; no game window.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
