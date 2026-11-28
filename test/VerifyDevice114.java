package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;import timeescape.core.GameState;

/** Headless device action checks for the archive-journal repair gate. */
public final class VerifyDevice114 {
    static int checks;
    static void ok(boolean condition,String message){if(!condition)throw new AssertionError(message);checks++;}
    /** Past-ready progression state, still in the present before time travel. */
    static GameState ready(){
        GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");
        s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");
        s.complete("PZ007");s.complete("PZ021");s.complete("PZ008");
        for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);
        s.complete("PZ010");s.complete("PZ011");return s;
    }
    static GameState pastWithSpare(){GameState s=ready();s.moveToPast();s.collectSparePart();return s;}
    static void layout(Container c){c.doLayout();for(Component a:c.getComponents())if(a instanceof Container child)layout(child);}
    /** Shared scene preview image used by ending and USB dialog tests. */
    static BufferedImage room(GameState s,int w,int h,boolean notebook){
        GameFonts.installDefaults();
        GameScenePanel scene=new GameScenePanel(id->{});scene.setEra(s.era());scene.restorePowered(s.powered());scene.setProgressHints(s);
        InventoryPanel inventory=new InventoryPanel(id->{});inventory.refresh(s,false);
        GameHud hud=new GameHud(scene,new MissionPanel(),inventory,()->{},()->{},()->{});
        hud.setLocation(s.era()==timeescape.model.Era.PAST?"과거":s.era()==timeescape.model.Era.FUTURE?"미래":"현재","연구실");
        hud.setSize(w,h);layout(hud);layout(hud);if(notebook){hud.openNotebook("inventory");layout(hud);}
        BufferedImage image=DevicePanel.capture(hud);
        if(notebook){hud.closeNotebook();BufferedImage clean=DevicePanel.capture(hud);for(int y=65;y<h;y++)for(int x=0;x<w;x++)if(clean.getRGB(x,y)!=image.getRGB(x,y))throw new AssertionError("snapshot excludes notebook");}
        return image;
    }
    public static void main(String[] args){
        GameState state=pastWithSpare();
        ok(!state.available("ACT_REPAIR"),"spare alone does not make device action available");
        DeviceActions beforeRead=new DeviceActions(state,true,()->{},()->{},null);
        ok(!beforeRead.isActionEnabled(),"device UI blocks repair before shelf journal is read");
        state.complete("PZ022");
        ok(state.has("ARCHIVE_LOG")&&!state.available("ACT_REPAIR"),"solving bookshelf grants journal but does not yet allow repair");
        DeviceActions unread=new DeviceActions(state,true,()->{},()->{},null);
        ok(!unread.isActionEnabled(),"unread journal keeps device repair disabled");
        state.complete("ACT_READ_ARCHIVE");
        ok(state.available("ACT_REPAIR"),"reading journal makes repair available");
        int[] changed={0};DeviceActions readyAction=new DeviceActions(state,true,()->{},()->changed[0]++,null);
        ok(readyAction.isActionEnabled(),"selected spare enables device repair after reading");readyAction.activate();
        ok(state.isWatchRepaired()&&state.done("GATE002")&&changed[0]==1,"repair commits once and opens future gate");
        readyAction.activate();ok(changed[0]==1,"repeated activation is ignored");
        System.out.println("PASS "+checks+" journal-gated wrist device action checks; no game window launched.");
    }
}
