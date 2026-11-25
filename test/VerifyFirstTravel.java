package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.model.Era;
import timeescape.system.*;

/** Offscreen only. No game window, sound device or real save access. */
public final class VerifyFirstTravel {
 static int checks;
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static void field(Object target,String name,Object value)throws Exception{var f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container n)layout(n);}
 public static void main(String[] args)throws Exception{
  Path out=timeescape.testutil.ArtifactPaths.output("v83-travel");Files.createDirectories(out);
  var state=GameState.newGame();state.takePrototype();
  var saves=new SaveStore(Files.createTempDirectory("travel-verification-"),ProgressCatalog.standard());
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();var scene=new GameScenePanel(id->{});scene.setEra(Era.PRESENT);scene.restorePowered(true);
   var missions=new MissionPanel();missions.refresh(state);var inv=new InventoryPanel(id->{});inv.refresh(state,false);
   var hud=new GameHud(scene,missions,inv,()->{},()->{},()->{});hud.setSize(1280,720);layout(hud);layout(hud);
   int[] done={0},arrivals={0};
   hud.startFirstTravel(()->done[0]++);
   check(hud.isTravelling()&&!hud.noteButton.isEnabled(),"input blocked while resources prepare");
   hud.openHelp();hud.openNotebook("inventory");check(!hud.gameMenu.isVisible()&&!hud.notebook.isVisible(),"overlays blocked");
   hud.cancelFirstTravel();check(!hud.isTravelling()&&done[0]==0,"cancel has no completion");
   var t=hud.travel;
   field(t,"on",ImageAssets.background(Era.PRESENT));field(t,"off",ImageAssets.presentOff());
   field(t,"finished",false);field(t,"arrived",false);
   field(t,"arrival",(Runnable)()->{arrivals[0]++;hud.setLocation("현재","연구실");});
   field(t,"complete",(Runnable)()->{done[0]++;new TimeTravelController(state).travel();try{saves.save(state.snapshot());}catch(Exception e){throw new RuntimeException(e);}});
   t.setVisible(true);
   double[] times={.2,2.0,4.8,5.7,6.3,7.4};
   for(int i=0;i<times.length;i++){
    t.advance(times[i]);BufferedImage image=new BufferedImage(1280,720,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();hud.printAll(g);g.dispose();ImageIO.write(image,"png",out.resolve("stage-"+i+".png").toFile());
   }
   check(arrivals[0]==1&&done[0]==0,"arrival once; tail continues before commit");
   check(state.era()==Era.PRESENT,"no early progress mutation");
   check(Math.abs((FirstTravelOverlay.ARRIVAL-.2)/7.152-.8)<1e-8,"arrival at motor 80 percent");
   check(FirstTravelOverlay.MOTOR_END>FirstTravelOverlay.ARRIVAL,"motor tail after arrival");
   check(FirstTravelOverlay.darkness(0)==0&&FirstTravelOverlay.darkness(4.8)==1,"power off timeline");
   t.advance(FirstTravelOverlay.END);t.advance(30);
   check(done[0]==1&&!t.isVisible(),"completion exactly once");
   check(state.era()==Era.PRESENT&&state.flag("device_broken")&&!state.roomUnlocked("past_1")&&!state.powered(),"failure stays dark without past unlock");
   check(saves.load().orElseThrow().state().snapshot().equals(state.snapshot()),"save persists completed destination");
   hud.removeNotify();
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" travel checks and six production HUD snapshots; no game or audio device");
 }
}
