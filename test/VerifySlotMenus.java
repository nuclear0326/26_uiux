package timeescape.ui;

import timeescape.system.SaveSlots;
import timeescape.system.SaveSlots.Slot;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;

public class VerifySlotMenus {
    static int checks;
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
    static JButton button(Container parent,String name){for(Component c:parent.getComponents())if(c instanceof JButton b&&name.equals(b.getName()))return b;throw new AssertionError(name);}
    static SaveSlots.Summary summary(Slot slot,boolean exists){return new SaveSlots.Summary(slot,exists,exists,false,exists?"현재 · 연구실 · 퍼즐 2개 완료":"비어 있음",exists?"2026.11.23 15:42:30":"");}
    static void render(GameMenuOverlay menu,int width,int height,String file)throws Exception{
        menu.setSize(width,height);menu.doLayout();BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();menu.printAll(g);g.dispose();ImageIO.write(image,"png",timeescape.testutil.ArtifactPaths.output("v54-slots",file+".png").toFile());
        for(Component c:menu.getComponents())if(c instanceof JButton b){
            check(menu.panel.contains(b.getBounds()),"button stays inside panel");
            check(b.getFontMetrics(b.getFont()).stringWidth(b.getText())<b.getWidth(),"button title fits");
            if(b.getClientProperty("detail")!=null)check(b.getHeight()>=62,"three lines fit vertically");
            for(String key:new String[]{"detail","time"})if(b.getClientProperty(key) instanceof String value)
                check(b.getFontMetrics(GameFonts.body(14)).stringWidth(value)<b.getWidth(),"slot metadata fits");
        }
    }
    public static void main(String[] args)throws Exception{
        Files.createDirectories(timeescape.testutil.ArtifactPaths.output("v54-slots"));
        SwingUtilities.invokeAndWait(()->{try{
            GameFonts.installDefaults();GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(timeescape.model.Era.PRESENT);scene.restorePowered(true);scene.setSize(1440,810);
            int[] saves={0},loads={0},closes={0};Slot[] selected={null};boolean[] fail={false};
            java.util.List<SaveSlots.Summary> data=new ArrayList<>(java.util.List.of(summary(Slot.AUTO,true),summary(Slot.ONE,false),summary(Slot.TWO,true),summary(Slot.THREE,false),summary(Slot.FOUR,true)));
            GameMenuOverlay menu=new GameMenuOverlay(scene,()->closes[0]++,()->{},()->{});
            menu.setSaveSlots(()->data,slot->{saves[0]++;selected[0]=slot;if(fail[0])return false;data.set(slot.ordinal(),summary(slot,true));return true;},slot->{loads[0]++;selected[0]=slot;return !fail[0];});
            menu.setSize(1440,810);menu.open();button(menu,"save").doClick();
            check(!button(menu,"slot-AUTO").isEnabled(),"manual save cannot overwrite automatic slot");
            check(button(menu,"slot-ONE").isEnabled(),"empty manual slot can be saved");
            check(button(menu,"slot-AUTO").getY()<button(menu,"slot-ONE").getY(),"automatic appears above manual slots");
            button(menu,"slot-ONE").doClick();check(saves[0]==1&&selected[0]==Slot.ONE,"click empty slot saves immediately");
            button(menu,"slot-ONE").doClick();check(saves[0]==1,"occupied slot requires overwrite confirmation");
            button(menu,"cancel").doClick();check(saves[0]==1,"cancel preserves manual slot");
            button(menu,"slot-ONE").doClick();button(menu,"confirm-save").doClick();check(saves[0]==2,"confirmed overwrite once");
            for(int[] size:new int[][]{{1020,595},{1440,810},{1920,1080},{2560,1080}})render(menu,size[0],size[1],"save-"+size[0]);
            fail[0]=true;button(menu,"slot-TWO").doClick();button(menu,"confirm-save").doClick();check(menu.isVisible()&&closes[0]==0,"failed save stays on slots screen");
            menu.back();check(button(menu,"save").isEnabled(),"Esc returns to game menu");
            data.set(1,summary(Slot.ONE,false));menu.open("load");check(!button(menu,"slot-ONE").isEnabled(),"empty load slot disabled");
            button(menu,"slot-TWO").doClick();check(loads[0]==1&&closes[0]==0,"failed load does not close chooser");
            fail[0]=false;button(menu,"slot-TWO").doClick();check(loads[0]==2&&selected[0]==Slot.TWO&&closes[0]==1,"chosen slot loads and closes chooser");
            menu.open("load");render(menu,1440,810,"continue-slots");menu.back();check(closes[0]==2,"Esc cancels continue chooser without loading");
            data.set(1,new SaveSlots.Summary(Slot.ONE,true,false,false,"불러올 수 없는 저장 파일",""));menu.open("load");check(!button(menu,"slot-ONE").isEnabled()&&button(menu,"slot-AUTO").isEnabled(),"corrupt slot does not disable good auto slot");
            menu.open("save");button(menu,"slot-THREE").doClick();check(selected[0]==Slot.THREE,"slot 3 save dispatch");
            button(menu,"slot-FOUR").doClick();button(menu,"confirm-save").doClick();check(selected[0]==Slot.FOUR,"slot 4 overwrite dispatch");
            menu.open("load");button(menu,"slot-THREE").doClick();check(selected[0]==Slot.THREE,"slot 3 load dispatch");
            menu.open("load");button(menu,"slot-FOUR").doClick();check(selected[0]==Slot.FOUR,"slot 4 load dispatch");
            menu.release();
            MainMenuPanel mainMenu=new MainMenuPanel(new MainMenuPanel.MenuActions(){public void startGame(){}public void continueGame(){}public void showSettings(){}public void exitGame(){}});
            mainMenu.setSize(1440,810);
            data.set(1,summary(Slot.ONE,true));data.set(2,summary(Slot.TWO,false));
            GameMenuOverlay chooser=new GameMenuOverlay(mainMenu,()->{},()->{},()->{});
            chooser.setSaveSlots(()->data,slot->false,slot->true);chooser.open("load");
            render(chooser,1440,810,"continue-slots");chooser.release();
            System.out.println("PASS "+checks+" slot menu interaction/layout checks; no window launched.");
        }catch(Exception e){throw new RuntimeException(e);}});
    }
}
