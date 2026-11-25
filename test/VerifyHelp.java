package timeescape.ui;
import java.awt.*;import java.awt.image.*;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;import timeescape.core.GameState;
public class VerifyHelp {
 static int checks;static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);checks++;}
 static void layout(Container p){p.doLayout();for(Component c:p.getComponents())if(c instanceof Container x)layout(x);}
 static JButton find(Container p,String text){for(Component c:p.getComponents()){if(c instanceof JButton b&&(text.equals(b.getText())||text.equals(b.getName())))return b;if(c instanceof Container x){JButton b=find(x,text);if(b!=null)return b;}}return null;}
 static void render(GameHud h,int w,int ht,String name)throws Exception{h.setSize(w,ht);for(int i=0;i<3;i++)layout(h);BufferedImage im=new BufferedImage(w,ht,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();h.printAll(g);g.dispose();ImageIO.write(im,"png",timeescape.testutil.ArtifactPaths.output("v54-help/"+name+".png").toFile());}
 static void alternatives()throws Exception{
  BufferedImage im=new BufferedImage(1120,330,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();LabTheme.quality(g);g.setColor(new Color(14,17,19));g.fillRect(0,0,1120,330);
  for(int i=0;i<3;i++){int y=18+i*104;g.setColor(new Color(27,25,22));g.fillRoundRect(18,y,1084,88,8,8);g.setColor(new Color(186,173,150));g.setFont(GameFonts.body(14));g.drawString(new String[]{"1  시대 배지형 · 추천","2  두 줄 정보형","3  자연스러운 문구형"}[i],36,y+21);g.setColor(new Color(236,222,194));g.setFont(GameFonts.title(22));g.drawString("TIME ESCAPE",38,y+60);
   g.setColor(new Color(105,93,72));g.drawLine(235,y+32,235,y+70);
   if(i==0){g.setColor(new Color(65,55,37));g.fillRoundRect(262,y+31,74,38,10,10);g.setColor(new Color(220,187,125));g.setFont(GameFonts.button(18));g.drawString("현재",283,y+57);g.setColor(new Color(236,222,194));g.setFont(GameFonts.title(22));g.drawString("연구실",357,y+59);}
   if(i==1){g.setFont(GameFonts.body(14));g.setColor(new Color(164,157,143));g.drawString("시간",263,y+40);g.drawString("장소",365,y+40);g.setColor(new Color(236,222,194));g.setFont(GameFonts.button(20));g.drawString("현재",263,y+65);g.drawString("연구실",365,y+65);}
   if(i==2){g.setFont(GameFonts.title(22));g.setColor(new Color(236,222,194));g.drawString("현재의 연구실",263,y+59);}
   g.setFont(GameFonts.button(18));g.setColor(new Color(236,222,194));g.drawString("연구 노트     ?",870,y+57);g.setStroke(new BasicStroke(2));for(int k=0;k<3;k++)g.drawLine(1000,y+43+k*7,1022,y+43+k*7);
  }g.dispose();ImageIO.write(im,"png",timeescape.testutil.ArtifactPaths.output("v54-help/header-alternatives.png").toFile());
 }
 public static void main(String[] args)throws Exception{Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v54-help"));SwingUtilities.invokeAndWait(()->{try{
  GameFonts.installDefaults();GameState s=new GameState();s.complete("PZ004");GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.restorePowered(true);MissionPanel m=new MissionPanel();m.refresh(s);InventoryPanel inv=new InventoryPanel(id->{});inv.refresh(s,false);GameHud[] ref={null};GameHud hud=new GameHud(scene,m,inv,()->ref[0].openHelp(),()->{},()->{});ref[0]=hud;hud.setLocation("현재","연구실");
  render(hud,1440,810,"header-scene");check(hud.location.getText().equals("TIME ESCAPE")&&hud.timeValue.getText().equals("현재")&&hud.placeValue.getText().equals("연구실"),"approved header text");
  find(hud,"?").doClick();check(hud.gameMenu.helpContent.isVisible(),"help button opens themed content");check(hud.gameMenu.isVisible(),"help overlay open");check(!hud.noteButton.isEnabled(),"note blocked behind help");
  scene.getActionMap().get("revealObjects").actionPerformed(null);var held=GameScenePanel.class.getDeclaredField("revealHeld");held.setAccessible(true);check(!held.getBoolean(scene),"Shift blocked");
  for(int[] size:new int[][]{{1440,810},{1020,650},{1280,720},{1920,1080}}){render(hud,size[0],size[1],"help-"+size[0]);HelpContent body=hud.gameMenu.helpContent;
   check(hud.gameMenu.panel.contains(body.getBounds()),"body within modal");
   for(Rectangle line:body.guideLines){check(Math.abs(line.getCenterX()-body.getWidth()/2.)<=.5,"guide centered");check(new Rectangle(0,0,body.getWidth(),body.getHeight()).contains(line),"guide text not clipped");}
   check(hud.location.getFontMetrics(hud.location.getFont()).stringWidth(hud.location.getText())<=hud.location.getWidth(),"header fits");
   for(JLabel field:new JLabel[]{hud.timeValue,hud.placeValue})check(field.getFontMetrics(field.getFont()).stringWidth(field.getText())<=field.getWidth(),"two-line header value fits");
  }
  hud.gameMenu.back();check(!hud.gameMenu.isVisible()&&hud.noteButton.isEnabled(),"Esc closes help and restores input");hud.openHelp();find(hud.gameMenu,"help-close").doClick();check(!hud.gameMenu.isVisible(),"close text works");hud.openHelp();find(hud.gameMenu,"close").doClick();check(!hud.gameMenu.isVisible(),"X works");
  for(String era:new String[]{"과거","미래"}){hud.setLocation(era,"연구실");check(hud.timeValue.getText().equals(era)&&hud.placeValue.getText().equals("연구실"),"era text updates");}
  hud.openGameMenu();check(!hud.gameMenu.helpContent.isVisible()&&find(hud.gameMenu,"save")!=null,"menu intact after help");hud.closeGameMenu();hud.removeNotify();alternatives();System.out.println("PASS "+checks+" help/header/input/layout checks; no game window.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
