package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;import javax.swing.*;import javax.imageio.ImageIO;import java.nio.file.*;
public class VerifyFontRoles {
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container cc)layout(cc);}
 static void render(JComponent c,int w,int h,String name)throws Exception{c.setSize(w,h);layout(c);layout(c);var im=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);var g=im.createGraphics();c.printAll(g);g.dispose();ImageIO.write(im,"png",timeescape.testutil.ArtifactPaths.output("v40-font-screens/"+name+".png").toFile());}
 public static void main(String[]args)throws Exception{
  Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v40-font-screens"));
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   if(!GameFonts.title(20).getFontName().contains("Serif")||!GameFonts.body(14).getFontName().contains("Regular")||!GameFonts.button(16).getFontName().contains("SemiBold"))throw new AssertionError("Role mapping");
   String sample="TIME ESCAPE 현재 연구실 게임 시작 시간여행 장치 전력 복구 0123456789";
   for(Font f:new Font[]{GameFonts.title(20),GameFonts.body(15),GameFonts.button(16)})if(f.canDisplayUpTo(sample)!=-1)throw new AssertionError("Glyphs");
   var menu=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
   render(menu,1440,810,"menu");menu.setVisible(false);
   var game=new GameHud(new GameScenePanel(id->{}),new MissionPanel(),new InventoryPanel(id->{}),()->{},()->{},()->{});render(game,1440,810,"game");render(game,1020,650,"game-minimum");
   for(String id:new String[]{"PZ004","PZ006","PZ008","PZ010","PZ011"})render(new PresentPuzzlePanel(id,()->{},()->{}),900,640,id);
   System.out.println("PASS bundled roles/glyphs; menu/game/minimum-size and five puzzle screens rendered headlessly.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
