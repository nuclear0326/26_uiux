package timeescape.ui;
import java.awt.*;
import java.awt.image.*;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.model.Era;
public class PreviewPuzzle65 {
 static int checks;
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container a)layout(a);}
 static void inspect(Container c){for(Component x:c.getComponents()){
  if(x instanceof JButton b&&b.isVisible()&&b.getWidth()>0){if(b.getText()!=null&&!b.getText().isEmpty()) {int w=b.getFontMetrics(b.getFont()).stringWidth(b.getText());if(w>b.getWidth()-b.getInsets().left-b.getInsets().right)throw new AssertionError("Button clipped: "+b.getText());}checks++;}
  if(x instanceof Container a)inspect(a);
 }}
 static BufferedImage render(JPanel p,int w,int h){p.setSize(w,h);layout(p);inspect(p);BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=out.createGraphics();p.printAll(g);g.dispose();return out;}
 public static void main(String[] args)throws Exception{SwingUtilities.invokeAndWait(()->{try{
 GameFonts.installDefaults();Path dir=Path.of("outputs/v65-preview");Files.createDirectories(dir);
 String[] ids={"PZ004","PZ006","PZ008","PZ010","PZ011","gears","wires"};
 BufferedImage sheet=new BufferedImage(1800,2100,BufferedImage.TYPE_INT_RGB);Graphics2D sg=sheet.createGraphics();sg.setColor(new Color(12,17,19));sg.fillRect(0,0,1800,2100);
 for(int i=0;i<ids.length;i++){
 JPanel p=i<5?new PresentPuzzlePanel(ids[i],()->{},()->{}):i==5?new GearsPuzzlePanel(()->{},()->{}):new WiresPuzzlePanel(()->{},()->{});
 render(p,i<5?820:900,i<5?620:760);
 BufferedImage shot=render(p,1000,760);ImageIO.write(shot,"png",dir.resolve(ids[i]+".png").toFile());
 sg.drawImage(shot,20+(i%3)*600,65+(i/3)*680,560,426,null);sg.setColor(LabTheme.TEXT);sg.setFont(GameFonts.button(22));sg.drawString(ids[i],24+(i%3)*600,45+(i/3)*680);
 if(i==0){GameScenePanel scene=new GameScenePanel(x->{});scene.setEra(Era.PRESENT);scene.restorePowered(false);scene.setSize(1440,900);BufferedImage full=new BufferedImage(1440,900,BufferedImage.TYPE_INT_RGB);Graphics2D g=full.createGraphics();scene.printAll(g);g.drawImage(shot,220,70,null);g.dispose();ImageIO.write(full,"png",dir.resolve("power-in-game.png").toFile());}
 }
 sg.dispose();ImageIO.write(sheet,"png",dir.resolve("all-puzzles.png").toFile());
 final int[] closed={0};JPanel test=new PresentPuzzlePanel("PZ004",()->{},()->closed[0]++);clickClose(test);if(closed[0]!=1)throw new AssertionError("close callback");
 System.out.println("PASS "+checks+" button layout checks at minimum and preview sizes, close callback; seven panels rendered, no game window/audio.");
 }catch(Exception e){throw new RuntimeException(e);}});}
 static void clickClose(Container c){for(Component x:c.getComponents()){if(x instanceof JButton b&&"×".equals(b.getText())){b.doClick();return;}if(x instanceof Container a)clickClose(a);}}
}
