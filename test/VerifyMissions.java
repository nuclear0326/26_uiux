import timeescape.core.*;
import timeescape.model.Era;
import timeescape.puzzle.PresentPuzzleModel;
import timeescape.ui.MissionPanel;
import timeescape.ui.InventoryPanel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class VerifyMissions {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    static void blocked(Runnable action){try{action.run();throw new AssertionError("must block");}catch(IllegalStateException expected){checks++;}}
    static void usb(GameState s){s.complete("PZ006");s.completeDiagnosis();s.collect("USB");s.complete("PZ007");}
    static void device(GameState s){s.complete("PZ008");s.collect("CASE");s.collect("CELL");s.collect("MODULE");s.complete("PZ010");s.complete("PZ011");}
    static void solve(String id,int... clicks){
        PresentPuzzleModel m=new PresentPuzzleModel(id);check(!m.solved(),id+" starts unfinished");
        for(int i=0;i<clicks.length;i++)for(int j=0;j<clicks[i];j++)m.turn(i);
        check(m.solved(),id+" solution");
    }
    public static void main(String[] args)throws Exception{
        GameState fresh=new GameState();blocked(fresh::moveToPast);blocked(fresh::completeDiagnosis);blocked(()->fresh.collect("USB"));
        for(String id:new String[]{"PZ006","PZ008","PZ007","PZ010","PZ011"})blocked(()->fresh.complete(id));
        for(boolean usbFirst:new boolean[]{true,false}){
            GameState s=new GameState();s.complete("PZ004");
            check(s.available("PZ006")&&s.available("PZ008"),"both branches open");
            if(usbFirst)usb(s);else device(s);
            check(!s.canDepart(),"single branch no departure");check(!new TimeTravelController(s).canTravel(),"controller no bypass");blocked(s::moveToPast);
            if(usbFirst)device(s);else usb(s);
            check(s.canDepart(),"both complete");check(s.has("USB"),"USB kept");
            check(!s.has("CASE")&&!s.has("CELL")&&!s.has("MODULE"),"materials installed");
            new TimeTravelController(s).travel();check(s.era()==Era.PAST,"past");check(s.has("USB"),"USB survives travel");
            s.collectSparePart();s.repairWatch();s.moveToFuture();s.collectCorePart();s.installCorePart();s.escape();check(s.isEscaped(),"existing ending");
        }
        GameState early=new GameState();early.complete("PZ004");early.collect("USB");early.collect("CASE");early.collect("CELL");early.collect("MODULE");
        check(!early.done("PZ009"),"schematic required");blocked(()->early.complete("PZ010"));early.complete("PZ008");check(early.done("PZ009"),"early parts recognized");
        early.complete("PZ006");early.completeDiagnosis();check(!early.canDepart(),"Rush Hour alone blocked");early.complete("PZ007");early.complete("PZ010");early.complete("PZ011");
        early.collect("CASE");check(!early.has("CASE"),"no duplicate installed parts");early.complete("PZ011");check(early.canDepart(),"idempotent completion");
        solve("PZ004",0,0,1,0,1);
        solve("PZ006",4,3,2,1);solve("PZ008",3,2,0,3);solve("PZ010",3,3,2);solve("PZ011",2,0,1);
        javax.swing.SwingUtilities.invokeAndWait(()->{
            try {
                GameState[] states={new GameState(),early};
                for(int i=0;i<states.length;i++){
                    MissionPanel panel=new MissionPanel();panel.setSize(330,330);panel.refresh(states[i]);panel.doLayout();
                    BufferedImage image=new BufferedImage(330,330,BufferedImage.TYPE_INT_RGB);
                    var g=image.createGraphics();panel.paint(g);g.dispose();ImageIO.write(image,"png",new File("outputs/v54-release-tests/mission-"+i+".png"));
                }
            }catch(Exception e){throw new RuntimeException(e);}
        });
        System.out.println("PASS: "+checks+" mission and puzzle checks; headless panel rendering.");
    }
}
