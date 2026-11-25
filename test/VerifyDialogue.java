package timeescape.ui;

import timeescape.system.DialogueCatalog;
import timeescape.core.GameState;
import timeescape.model.Era;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.imageio.ImageIO;

public final class VerifyDialogue {
    static int checks;
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
    static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container container)layout(container);}
    static void render(GameHud hud,int w,int h,Path output)throws Exception{
        hud.setSize(w,h);layout(hud);layout(hud);
        BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        var g=image.createGraphics();hud.printAll(g);g.dispose();ImageIO.write(image,"png",output.toFile());
    }
    public static void main(String[] args)throws Exception{
        var parsed=DialogueCatalog.parse(new StringReader("\uFEFF# comment\n[intro.start]\n첫 줄\n둘째 줄\n---\n다음\n[other]\n내용\n"));
        check(parsed.scene("intro.start").equals(List.of("첫 줄\n둘째 줄","다음")),"UTF8, BOM, comments, page order and newline");
        for(String invalid:List.of("text", "[x]\n", "[x]\none\n---\n", "[x]\none\n[x]\ntwo")){
            try{DialogueCatalog.parse(new StringReader(invalid));throw new AssertionError("invalid accepted");}catch(IOException expected){checks++;}
        }
        List<String> opening=DialogueCatalog.standard().scene("intro.start");check(opening.size()==3,"three opening pages");
        Path dir=Path.of("outputs/dialogue-preview");Files.createDirectories(dir);
        SwingUtilities.invokeAndWait(()->{try{
            GameState state=GameState.newGame();var before=state.snapshot();
            var scene=new GameScenePanel(id->{});scene.setEra(Era.PRESENT);scene.restorePowered(true);scene.setProgressHints(state);
            var missions=new MissionPanel();missions.refresh(state);var inventory=new InventoryPanel(id->{});inventory.refresh(state,false);
            GameHud hud=new GameHud(scene,missions,inventory,()->{},()->{},()->{});
            hud.showDialogue(opening);check(hud.dialogue.isVisible()&&!hud.noteButton.isEnabled(),"opening visible and HUD disabled");
            hud.openNotebook("inventory");hud.openGameMenu();hud.openHelp();
            check(!hud.notebook.isVisible()&&!hud.gameMenu.isVisible(),"other overlays blocked");
            hud.showMessage("hidden toast");check(!hud.toast.isVisible(),"no competing notification");
            hud.dialogue.revealAll();render(hud,1500,880,dir.resolve("opening.png"));render(hud,1020,650,dir.resolve("opening-small.png"));
            hud.dialogue.getActionMap().get("next").actionPerformed(null);check(hud.dialogue.currentText().equals(opening.get(1)),"keyboard advance");
            hud.dialogue.revealAll();render(hud,1500,880,dir.resolve("opening-box.png"));
            hud.dialogue.advance();hud.dialogue.revealAll();hud.dialogue.advance();
            check(!hud.dialogue.isVisible()&&hud.noteButton.isEnabled(),"finish restores controls");
            check(state.snapshot().equals(before),"dialogue does not grant items or alter progress");
            AtomicInteger ends=new AtomicInteger();hud.dialogue.open(List.of("one"),ends::incrementAndGet);hud.dialogue.advance();hud.dialogue.advance();check(ends.get()==1,"completion once");
            hud.showDialogue(opening);hud.dismissDialogue();check(!hud.dialogue.isVisible()&&hud.noteButton.isEnabled(),"load/reset dismissal restores controls");
            hud.removeNotify();
        }catch(Exception e){throw new RuntimeException(e);}});
        System.out.println("PASS "+checks+" dialogue parsing, input and lifecycle checks; headless previews rendered; no game window");
    }
}
