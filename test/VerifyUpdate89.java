package timeescape.ui;
import timeescape.core.*;import timeescape.system.*;import timeescape.puzzle.*;import java.util.*;import java.nio.file.*;import javax.swing.*;import java.awt.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
public final class VerifyUpdate89 {
 static int checks;static void ok(boolean v,String s){if(!v)throw new AssertionError(s);checks++;}
 static void layout(Container c){c.doLayout();for(var x:c.getComponents())if(x instanceof Container n)layout(n);}
 public static void main(String[] args)throws Exception{
  String original=System.getProperty("jpackage.app-version"),override=System.getProperty("timeescape.saveDir");
  try{System.clearProperty("timeescape.saveDir");System.setProperty("jpackage.app-version","88.0");Path old=SaveStore.defaultDirectory();System.setProperty("jpackage.app-version","89.0");Path fresh=SaveStore.defaultDirectory();ok(!old.equals(fresh)&&fresh.endsWith("v89.0"),"release isolation");ok(fresh.equals(SaveStore.defaultDirectory()),"restart keeps release path");System.setProperty("timeescape.saveDir","test-override");ok(SaveStore.defaultDirectory().equals(Path.of("test-override")),"override retained");}finally{if(original==null)System.clearProperty("jpackage.app-version");else System.setProperty("jpackage.app-version",original);if(override==null)System.clearProperty("timeescape.saveDir");else System.setProperty("timeescape.saveDir",override);}
  SwingUtilities.invokeAndWait(()->{try{
   var catalog=ProgressCatalog.standard();ok(catalog.steps().get("PZ004").kind().equals("wires"),"power uses wires");ok(catalog.steps().get("PZ003").kind().equals("temporary_power")&&catalog.steps().get("PZ003").name().contains("(임시)"),"future placeholder");
   var s=GameState.newGame();s.takePrototype();s.usePrototype();int[] saved={0};var store=new SaveStore(Files.createTempDirectory("update89-"),catalog);
   var runner=new PuzzleRunner(Map.of("wires",step->{boolean[] solved={false};var panel=new WiresPuzzlePanel(()->solved[0]=true,()->{});panel.developerSkip();ok(!solved[0]&&!s.powered()&&saved[0]==0,"easy gives no power or save");panel.developerSkip();ok(solved[0],"hard completes");panel.developerSkip();return PuzzleResult.from(solved[0]);}),()->s,()->true,step->{saved[0]++;try{store.save(s.snapshot());}catch(Exception e){throw new RuntimeException(e);}});
   ok(runner.run("PZ004")==PuzzleRunner.Outcome.COMPLETED&&s.powered()&&saved[0]==1,"power and autosave once");ok(store.load().orElseThrow().state().powered(),"restored power");
   GameFonts.installDefaults();var scene=new GameScenePanel(id->{});scene.setEra(timeescape.model.Era.PRESENT);scene.restorePowered(true);var missions=new MissionPanel();var inv=new InventoryPanel(id->{});var hud=new GameHud(scene,missions,inv,()->{},()->{},()->{});
   hud.showOpeningTutorial(java.util.List.of("첫 안내"));ok(hud.dialogue.isVisible()&&!hud.gameMenu.isVisible(),"dialogue first");hud.dialogue.revealAll();hud.dialogue.advance();ok(hud.gameMenu.isVisible()&&hud.gameMenu.helpContent.isVisible(),"help after tutorial");hud.closeGameMenu();ok(!hud.gameMenu.isVisible(),"help closes");hud.showDialogue(java.util.List.of("일반 안내"));hud.dialogue.revealAll();hud.dialogue.advance();ok(!hud.gameMenu.isVisible(),"ordinary dialogue does not reopen help");hud.openHelp();
   Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v89-preview"));
   hud.setSize(1484,842);layout(hud);layout(hud);var img=new BufferedImage(1484,842,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();hud.printAll(g);g.dispose();ImageIO.write(img,"png",timeescape.testutil.ArtifactPaths.output("v89-preview/help.png").toFile());
   hud.removeNotify();System.out.println("PASS "+checks+" release saves, wiring stages, completion and tutorial checks");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
