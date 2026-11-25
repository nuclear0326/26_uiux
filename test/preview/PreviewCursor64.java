import timeescape.ui.*;
import timeescape.core.*;
import timeescape.model.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
public class PreviewCursor64 {
 static void layout(Container c){c.doLayout();for(Component x:c.getComponents())if(x instanceof Container child)layout(child);}
 public static void main(String[] args)throws Exception {SwingUtilities.invokeAndWait(()->{try{
 GameFonts.installDefaults();GameState state=new GameState();state.complete("PZ004");
 GameScenePanel scene=new GameScenePanel(x->{});scene.setEra(Era.PRESENT);scene.restorePowered(true);scene.setProgressHints(state);
 GameHud hud=new GameHud(scene,new MissionPanel(),new InventoryPanel(x->{}),()->{},()->{},()->{});hud.setSize(1440,900);layout(hud);
 BufferedImage base=new BufferedImage(1440,900,BufferedImage.TYPE_INT_RGB);Graphics2D g=base.createGraphics();hud.printAll(g);g.dispose();
 File dir=timeescape.testutil.ArtifactPaths.file("v64-preview");dir.mkdirs();
 for(boolean active:new boolean[]{false,true}){
 BufferedImage out=new BufferedImage(1440,1020,BufferedImage.TYPE_INT_RGB);g=out.createGraphics();g.drawImage(base,0,0,null);
 int x=active?245:650,y=active?380:650;g.drawImage(GameCursors.image(active),x-3,y-3,null);
 g.setColor(new Color(24,25,25));g.fillRect(0,900,1440,120);g.setColor(new Color(236,222,194));g.setFont(GameFonts.button(24));g.drawString(active?"파란빛 펜촉 · 구멍과 아래쪽 띠 발광":"03 연구용 펜촉 · 기본 상태",36,949);
 g.setFont(GameFonts.body(17));g.drawString("실제 게임 화면의 헤드리스 렌더 + 적용 커서 합성 / 오른쪽은 2배 확대",36,985);
 g.drawImage(GameCursors.image(active),1290,912,96,96,null);g.dispose();
 ImageIO.write(out,"png",new File(dir,active?"cursor-interact.png":"cursor-normal.png"));
 }
 if(GameCursors.image(false).getWidth()!=48||GameCursors.image(true).getWidth()!=48)throw new AssertionError();
 if(GameCursors.normal()==null||GameCursors.interact()==null)throw new AssertionError();
 System.out.println("PASS: actual HUD rendered headlessly, cursor assets loaded, safe headless fallback. No game launched.");
 }catch(Exception e){throw new RuntimeException(e);}});}
}
