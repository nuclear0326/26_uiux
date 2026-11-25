package timeescape.ui;
import javax.swing.*;import java.awt.image.*;import java.nio.file.*;import javax.imageio.ImageIO;import java.security.*;import java.util.*;
public class WeatherStartupProbe {
 static long at,start;static StringBuilder rows=new StringBuilder();
 static void mark(String name){long n=System.nanoTime();rows.append(name+","+(n-at)/1e6+"\n");at=n;}
 public static void main(String[] args)throws Exception {Path out=Path.of(args[0]);Files.createDirectories(out);SwingUtilities.invokeAndWait(()->{try{
 start=at=System.nanoTime();UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());mark("Swing_LAF");
 GameFonts.installDefaults();mark("fonts");var scene=new GameScenePanel(s->{});var inventory=new InventoryPanel(s->{});var missions=new MissionPanel();var hud=new GameHud(scene,missions,inventory,()->{},()->{},()->{});mark("game_UI");
 var menu=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});mark("menu_initialization");
 menu.setSize(1500,790);var first=new BufferedImage(1500,790,BufferedImage.TYPE_INT_RGB);var fg=first.createGraphics();menu.printAll(fg);fg.dispose();mark("first_paint");rows.append("total,"+(at-start)/1e6+"\n");Files.writeString(out.resolve("timings.csv"),rows);System.out.println(out+"\n"+rows);
 if(args.length>1){var f=MainMenuPanel.class.getDeclaredField("atmosphere");f.setAccessible(true);var a=(MenuAtmosphere)f.get(menu);StringBuilder hashes=new StringBuilder();
 double[][] samples={{0,-100,-1},{.5,-100,-1},{1.2,-100,-1},{12.05,.05,-1},{12.24,.24,-1},{12.7,.7,-1},{40,-100,-1},{100,-100,-1},{2,-100,1},{2,-100,MenuStartTimeline.BREAK_AT+.3},{2,-100,MenuStartTimeline.FADE_AT+.2}};
 for(int i=0;i<samples.length;i++){var im=new BufferedImage(1600,900,BufferedImage.TYPE_INT_ARGB);var g=im.createGraphics();double[] s=samples[i];a.draw(g,s[0],s[1],s[2],36723);g.dispose();var md=MessageDigest.getInstance("SHA-256");for(int pixel:im.getRGB(0,0,1600,900,null,0,1600)){md.update((byte)(pixel>>>24));md.update((byte)(pixel>>>16));md.update((byte)(pixel>>>8));md.update((byte)pixel);}hashes.append(i+","+HexFormat.of().formatHex(md.digest())+"\n");if(i==3)ImageIO.write(im,"png",out.resolve("weather.png").toFile());}Files.writeString(out.resolve("frames.sha256"),hashes);}
 }catch(Exception e){throw new RuntimeException(e);}});System.exit(0);}
}
