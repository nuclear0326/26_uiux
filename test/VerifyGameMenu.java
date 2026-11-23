package timeescape.ui;
import java.awt.*;import java.awt.event.*;import java.awt.image.*;import java.nio.file.*;import javax.imageio.ImageIO;import javax.swing.*;import timeescape.core.GameState;
public class VerifyGameMenu {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container n)layout(n);}
 static JButton find(Container c,String name){for(Component child:c.getComponents()){if(child instanceof JButton b&&name.equals(b.getName()))return b;if(child instanceof Container n){JButton b=find(n,name);if(b!=null)return b;}}return null;}
 static void render(GameHud hud,int w,int h,String name)throws Exception{hud.setSize(w,h);for(int i=0;i<3;i++)layout(hud);BufferedImage im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);hud.printAll(g);g.dispose();ImageIO.write(im,"png",Path.of("outputs/v54-menu/"+name+".png").toFile());}
 public static void main(String[]args)throws Exception{Files.createDirectories(Path.of("outputs/v54-menu"));SwingUtilities.invokeAndWait(()->{try{
  GameFonts.installDefaults();GameState state=new GameState();state.complete("PZ004");int[] calls={0,0,0};boolean[] intro={true};GameScenePanel scene=new GameScenePanel(id->calls[2]++);scene.setEra(state.era());scene.setPowered(true);var finish=GameScenePanel.class.getDeclaredMethod("finishLighting");finish.setAccessible(true);finish.invoke(scene);
  MissionPanel missions=new MissionPanel();missions.refresh(state);InventoryPanel inv=new InventoryPanel(id->{});inv.refresh(state,false);GameHud hud=new GameHud(scene,missions,inv,()->{},()->calls[1]++,()->calls[0]++);hud.setTransitionSettings(()->intro[0],v->intro[0]=v);hud.setLocationTitle("현재 · 연구실");render(hud,1440,810,"baseline");hud.openNotebook("records");hud.openGameMenu();render(hud,1440,810,"game-menu");
  check(hud.gameMenu.isVisible(),"menu opens");check(!hud.notebook.isVisible(),"notebook closes");check(!hud.noteButton.isEnabled(),"background control blocked");
  scene.getActionMap().get("revealObjects").actionPerformed(null);var reveal=GameScenePanel.class.getDeclaredField("revealHeld");reveal.setAccessible(true);check(!reveal.getBoolean(scene),"Shift blocked under menu");
  Component target=SwingUtilities.getDeepestComponentAt(hud.layers,20,200);check(target==hud.gameMenu,"overlay intercepts outside click");
  find(hud.gameMenu,"settings").doClick();render(hud,1440,810,"settings");find(hud.gameMenu,"intro").doClick();check(!intro[0],"existing intro setting changes");find(hud.gameMenu,"intro").doClick();check(intro[0],"setting restored");hud.gameMenu.back();
  check(find(hud.gameMenu,"reset")==null,"reset removed");
  String[] order={"save","load","settings","main"};for(int i=1;i<order.length;i++)check(find(hud.gameMenu,order[i-1]).getY()<find(hud.gameMenu,order[i]).getY(),"requested menu order");
  var slots=new timeescape.system.SaveSlots(Files.createTempDirectory(Path.of("outputs/v54-menu"),"slot-test-"),timeescape.core.ProgressCatalog.standard());
  slots.store(timeescape.system.SaveSlots.Slot.AUTO).save(state.snapshot());slots.saveManual(timeescape.system.SaveSlots.Slot.ONE,new GameState().snapshot());boolean[] fail={true};
  hud.setSaveSlots(slots::summaries,slot->false,slot->{if(fail[0])return false;try{var loaded=slots.load(slot).state();scene.restorePowered(loaded.powered());missions.refresh(loaded);inv.refresh(loaded,false);calls[1]++;return true;}catch(Exception ex){throw new RuntimeException(ex);}});
  find(hud.gameMenu,"load").doClick();render(hud,1440,810,"ingame-load");check(find(hud.gameMenu,"slot-AUTO").isEnabled(),"auto available in-game");check(!find(hud.gameMenu,"slot-TWO").isEnabled(),"empty slots disabled");
  find(hud.gameMenu,"back").doClick();check(hud.gameMenu.isVisible()&&find(hud.gameMenu,"load")!=null,"back returns to menu without loading");check(calls[1]==0,"cancel preserves current state");
  find(hud.gameMenu,"load").doClick();hud.gameMenu.back();check(hud.gameMenu.isVisible()&&find(hud.gameMenu,"save")!=null,"Esc returns to game menu");
  find(hud.gameMenu,"load").doClick();find(hud.gameMenu,"slot-ONE").doClick();check(hud.gameMenu.isVisible()&&!hud.noteButton.isEnabled()&&calls[1]==0,"failed load keeps overlay and state");
  fail[0]=false;find(hud.gameMenu,"slot-ONE").doClick();check(calls[1]==1&&!hud.gameMenu.isVisible()&&hud.noteButton.isEnabled(),"successful load closes overlay and unlocks controls");
  var powered=GameScenePanel.class.getDeclaredField("powered");powered.setAccessible(true);check(!powered.getBoolean(scene),"saved dark background restored");

  hud.openGameMenu();find(hud.gameMenu,"main").doClick();check(calls[0]==1&&!hud.gameMenu.isVisible(),"main menu callback");
  hud.openGameMenu();find(hud.gameMenu,"close").doClick();check(!hud.gameMenu.isVisible(),"X closes");hud.openGameMenu();hud.gameMenu.back();check(!hud.gameMenu.isVisible(),"Esc closes root");
  for(int[] size:new int[][]{{1020,650},{1280,720},{1920,1080},{2560,1080}}){hud.openGameMenu();render(hud,size[0],size[1],"menu-"+size[0]);Rectangle r=hud.gameMenu.panel;check(r.x>=0&&r.y>=0&&r.x+r.width<=hud.layers.getWidth()&&r.y+r.height<=hud.layers.getHeight(),"within bounds");check(Math.abs(r.getCenterX()-hud.layers.getWidth()/2.)<1,"centered");for(String id:new String[]{"save","load","settings","main"}){JButton b=find(hud.gameMenu,id);check(b.getFontMetrics(b.getFont()).stringWidth(b.getText())<=b.getWidth(),"text fits");}hud.closeGameMenu();}
  System.out.println("PASS "+checks+" game-menu controls/load/settings/input/layout checks; no game main, JFrame, audio or EXE launched.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
