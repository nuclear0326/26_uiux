package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.core.*;
import timeescape.scene.*;
import timeescape.model.Era;

public final class VerifyHighlight113 {
 static int checks;static void ok(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 static java.lang.reflect.Field field(String n)throws Exception{var f=GameScenePanel.class.getDeclaredField(n);f.setAccessible(true);return f;}
 static float contrast(GameScenePanel p)throws Exception{return field("revealContrast").getFloat(p);}
 static boolean held(GameScenePanel p)throws Exception{return field("revealHeld").getBoolean(p);}
 static javax.swing.Timer timer(GameScenePanel p)throws Exception{return (javax.swing.Timer)field("hoverTimer").get(p);}
 static void tick(GameScenePanel p)throws Exception{var t=timer(p);for(var l:t.getActionListeners())l.actionPerformed(new ActionEvent(t,0,""));}
 static void key(GameScenePanel p,boolean down){p.getActionMap().get(down?"revealObjects":"hideObjects").actionPerformed(new ActionEvent(p,0,""));}
 static void middle(GameScenePanel p,boolean down){p.dispatchEvent(new MouseEvent(p,down?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_RELEASED,0,0,1000,100,1,false,MouseEvent.BUTTON2));}
 static GameState restored(){GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");return s;}
 static GameState analyzed(){GameState s=restored();for(String id:new String[]{"ACT_GET_CIPHER","PZ006","PZ001"})s.complete(id);s.collect("USB");s.complete("PZ007");s.complete("PZ021");return s;}
 static GameScenePanel scene(GameState s){GameScenePanel p=new GameScenePanel(id->{throw new AssertionError("reveal must not activate an object");});p.setSize(1920,1080);p.setEra(s.era());p.restorePowered(s.powered());p.setProgressHints(s);return p;}
 static BufferedImage draw(GameScenePanel p)throws Exception{BufferedImage im=new BufferedImage(p.getWidth(),p.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();p.printAll(g);g.dispose();return im;}
 static boolean different(BufferedImage a,BufferedImage b,Rectangle r){for(int y=Math.max(0,r.y);y<Math.min(a.getHeight(),r.y+r.height);y++)for(int x=Math.max(0,r.x);x<Math.min(a.getWidth(),r.x+r.width);x++)if(a.getRGB(x,y)!=b.getRGB(x,y))return true;return false;}
 static void motion(GameState s)throws Exception{
  var snapshot=s.snapshot();GameScenePanel p=scene(s);field("revealContrast").setFloat(p,1);field("revealHeld").setBoolean(p,true);field("particlePhase").setDouble(p,0);BufferedImage a=draw(p);field("particlePhase").setDouble(p,.8);BufferedImage b=draw(p);
  for(Hotspot h:HotspotCatalog.of(s.era()))ok(different(a,b,h.bounds())==(InteractionHint.of(s,h.id())==InteractionHint.READY),"animation only on READY: "+s.era()+"/"+h.id());
  ok(s.snapshot().equals(snapshot),"highlight never mutates progress");timer(p).stop();
 }
 static void full(GameState s,Path out,String name)throws Exception{
  GameScenePanel p=scene(s);GameHud hud=new GameHud(p,new MissionPanel(),new InventoryPanel(id->{}),()->{},()->{},()->{});hud.setSize(1440,875);VerifyAccident94.layout(hud);VerifyAccident94.layout(hud);
  field("revealContrast").setFloat(p,1);field("revealHeld").setBoolean(p,true);field("particlePhase").setDouble(p,1.2375);
  BufferedImage im=new BufferedImage(1440,875,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();hud.printAll(g);g.dispose();ImageIO.write(im,"png",out.resolve(name).toFile());timer(p).stop();
 }
 public static void main(String[] args)throws Exception{
  ok(GraphicsEnvironment.isHeadless(),"headless only");Path out=timeescape.testutil.ArtifactPaths.output("v113-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();GameState s=restored();ok(InteractionHint.of(s,"MEMO")==InteractionHint.READY&&InteractionHint.of(s,"USB")==InteractionHint.READY&&InteractionHint.of(s,"PRESENT_PC")==InteractionHint.LATER&&InteractionHint.of(s,"POWER")==InteractionHint.NONE,"all three original states");
   motion(s);motion(analyzed());motion(GameState.newGame());GameState dark=GameState.newGame();dark.takePrototype();dark.usePrototype();motion(dark);
   GameState tutorial=GameState.newGame();for(Hotspot h:HotspotCatalog.of(Era.PRESENT))ok(InteractionHint.of(tutorial,h.id())==(h.id().equals("CASE")?InteractionHint.READY:InteractionHint.NONE),"tutorial only box");
   GameState travel=analyzed();travel.complete("PZ008");for(String id:new String[]{"CASE","CELL","MODULE"})travel.collect(id);travel.complete("PZ010");travel.complete("PZ011");travel.moveToPast();motion(travel);travel.complete("PZ002");motion(travel);travel.repairWatch();travel.moveToFuture();motion(travel);travel.complete("PZ003");motion(travel);
   GameState loaded=GameState.restore(s.catalog(),s.snapshot());for(Hotspot h:HotspotCatalog.of(Era.PRESENT))ok(InteractionHint.of(s,h.id())==InteractionHint.of(loaded,h.id()),"restored hint unchanged");
   GameScenePanel p=scene(s);BufferedImage normal=draw(p);key(p,true);for(int i=0;i<5;i++)tick(p);ok(contrast(p)==1&&held(p),"Shift reaches full reveal");draw(p);Object mask=field("contrastMask").get(p);
   BufferedImage white=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_RGB);Graphics2D g=white.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1920,1080);var paint=GameScenePanel.class.getDeclaredMethod("paintContrast",Graphics2D.class);paint.setAccessible(true);paint.invoke(p,g);g.dispose();
   ok((white.getRGB(1000,100)&255)==127,"background still 50 percent");ok((white.getRGB(325,390)&255)==255&&(white.getRGB(940,540)&255)==255,"READY and LATER retain base light");ok((white.getRGB(805,365)&255)==127,"completed object dimmed with background");draw(p);ok(field("contrastMask").get(p)==mask,"contrast mask cached");
   // Release either input while the other remains held: no fade until both are released.
   middle(p,true);key(p,false);tick(p);ok(held(p)&&contrast(p)==1,"middle keeps highlight after Shift release");key(p,true);middle(p,false);tick(p);ok(held(p)&&contrast(p)==1,"Shift keeps highlight after middle release");
   key(p,false);ok(!held(p)&&contrast(p)==1,"release does not jump brightness");float previous=contrast(p);
   for(int i=1;i<=5;i++){tick(p);float value=contrast(p);ok(value<previous&&value>=0,"monotonic fade "+i);previous=value;ImageIO.write(draw(p),"png",out.resolve("release-"+i+".png").toFile());}
   ok(contrast(p)==0&&!timer(p).isRunning(),"fade fully ends and timer stops");ok(!different(normal,draw(p),new Rectangle(0,0,1920,1080)),"exact return to original background without residual overlay");
   middle(p,true);tick(p);ok(contrast(p)>0&&contrast(p)<1,"middle press fades in");middle(p,false);tick(p);ok(contrast(p)==0,"quick release cleans up");
   key(p,true);tick(p);key(p,false);tick(p);key(p,true);tick(p);ok(contrast(p)>0&&held(p),"interrupted fade can resume");p.setInputBlocked(true);ok(!held(p)&&contrast(p)==0,"menu clears reveal");key(p,true);middle(p,true);ok(!held(p)&&contrast(p)==0,"blocked input ignored");p.setInputBlocked(false);ok(!held(p),"no stale input on return");
   key(p,true);tick(p);((java.beans.PropertyChangeListener)field("windowFocusListener").get(p)).propertyChange(new java.beans.PropertyChangeEvent(p,"focusedWindow",null,null));ok(contrast(p)==0&&!held(p),"lost window focus clears reveal");key(p,true);tick(p);p.setEra(Era.PAST);ok(contrast(p)==0&&!held(p),"era transition clears reveal");timer(p).stop();
   // Entire additional overlay shares reveal alpha, including its edge outside the silhouette.
   Hotspot memo=HotspotCatalog.of(Era.PRESENT).stream().filter(h->h.id().equals("MEMO")).findFirst().orElseThrow();long last=-1;
   for(float strength:new float[]{0,.2f,.4f,.6f,.8f,1}){BufferedImage im=new BufferedImage(1920,1080,BufferedImage.TYPE_INT_ARGB);Graphics2D q=im.createGraphics();ObjectHighlightEffect.paint(q,memo,1.1,strength);q.dispose();long alpha=0;Rectangle b=memo.bounds();b.grow(20,20);for(int y=b.y;y<b.y+b.height;y++)for(int x=b.x;x<b.x+b.width;x++)alpha+=im.getRGB(x,y)>>>24;ok(strength==0?alpha==0:alpha>last,"whole effect scales with release alpha");last=alpha;}
   var cache=ObjectHighlightEffect.class.getDeclaredField("CACHE");cache.setAccessible(true);Map<?,?> cached=(Map<?,?>)cache.get(null);Object geometry=cached.get(memo);int size=cached.size();BufferedImage scratch=new BufferedImage(400,400,BufferedImage.TYPE_INT_ARGB);Graphics2D q=scratch.createGraphics();for(int i=0;i<25;i++)ObjectHighlightEffect.paint(q,memo,i*.04,1);q.dispose();ok(cached.size()==size&&cached.get(memo)==geometry,"geometry reused across frames");
   full(s,out,"01-ready-later-complete.png");full(tutorial,out,"02-tutorial.png");full(analyzed(),out,"03-after-analysis.png");
   s.complete("ACT_GET_CIPHER");GameScenePanel changed=scene(restored());field("revealContrast").setFloat(changed,1);draw(changed);changed.setProgressHints(s);ok(field("contrastMask").get(changed)==null,"progress invalidates mask");motion(s);
   System.out.println("PASS "+checks+" hint rules, tutorial, three eras, exact 50% mask, mixed holds, fade, restoration, lifecycle and cache checks; no game windows.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
