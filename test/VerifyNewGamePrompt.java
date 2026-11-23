package timeescape.ui;
import java.awt.*;import java.awt.image.*;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;
public class VerifyNewGamePrompt {
 static int checks;static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static JButton button(GameMenuOverlay p,String name){for(Component c:p.getComponents())if(c instanceof JButton b&&name.equals(b.getName()))return b;throw new AssertionError(name);}
 public static void main(String[] args)throws Exception{Files.createDirectories(Path.of("outputs/v57-newgame"));SwingUtilities.invokeAndWait(()->{try{
  GameFonts.installDefaults();int[] starts={0},prompts={0};GameMenuOverlay[] holder={null};
  MainMenuPanel menu=new MainMenuPanel(new MainMenuPanel.MenuActions(){public boolean confirmNewGame(){prompts[0]++;holder[0].open("newgame");return false;}public void startGame(){starts[0]++;}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
  menu.setTransitionAnimation(false);menu.setSize(1440,810);
  GameMenuOverlay popup=new GameMenuOverlay(menu,()->holder[0].release(),()->{},()->{});holder[0]=popup;popup.setNewGameAction(menu::startConfirmedNewGame);popup.setSize(1440,810);
  var activate=MainMenuPanel.class.getDeclaredMethod("activate",int.class);activate.setAccessible(true);
  activate.invoke(menu,0);check(popup.isVisible()&&starts[0]==0,"confirmation before new game");
  button(popup,"cancel-newgame").doClick();check(!popup.isVisible()&&starts[0]==0,"cancel does not start");
  activate.invoke(menu,0);popup.back();check(!popup.isVisible()&&starts[0]==0,"Esc cancels");
  activate.invoke(menu,0);button(popup,"close").doClick();check(!popup.isVisible()&&starts[0]==0,"X cancels");
  activate.invoke(menu,0);JButton start=button(popup,"confirm-newgame");start.doClick();start.doClick();check(starts[0]==1&&!popup.isVisible(),"confirmed start only once");check(prompts[0]==4,"confirmation not recursively requested");
  menu.setTransitionAnimation(true);activate.invoke(menu,0);button(popup,"confirm-newgame").doClick();check(starts[0]==1,"approved start waits for existing intro");
  var advance=MainMenuPanel.class.getDeclaredMethod("advance",double.class);advance.setAccessible(true);advance.invoke(menu,MenuStartTimeline.END_AT+1);check(starts[0]==2,"existing intro reaches game start");advance.invoke(menu,1.0);check(starts[0]==2,"intro completes once");
  for(int[] size:new int[][]{{1020,650},{1440,810},{1920,1080}}){menu.setSize(size[0],size[1]);popup.setSize(size[0],size[1]);popup.open("newgame");popup.doLayout();check(popup.panel.width<=520&&popup.panel.height<=290,"compact prompt");
   for(Component c:popup.getComponents())if(c instanceof JButton b&&b.getParent()==popup){check(popup.panel.contains(b.getBounds()),"button inside prompt");check(b.getFontMetrics(b.getFont()).stringWidth(b.getText())<=b.getWidth(),"button label fits");}
   BufferedImage im=new BufferedImage(size[0],size[1],BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();popup.printAll(g);g.dispose();ImageIO.write(im,"png",Path.of("outputs/v57-newgame/prompt-"+size[0]+".png").toFile());popup.release();
  }
  System.out.println("PASS "+checks+" compact prompt/confirmation/intro checks; no game window launched.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}


