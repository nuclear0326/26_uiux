package timeescape.ui;
import timeescape.core.*;import timeescape.model.Era;import timeescape.puzzle.*;import timeescape.system.*;
import java.util.*;import java.nio.file.*;import java.util.concurrent.*;
public final class VerifyInteractions97 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static final class Host implements InteractionHost,AutoCloseable {
  GameState current=GameState.newGame();boolean closing,travelling,selected;int saves,refreshes,opens,launches,memos,endings,failures;String lastPuzzle="";
  Runnable changed,intro,arrival;GameState deviceState;boolean deviceSelection;
  PuzzleResult outcome=PuzzleResult.SOLVED;final java.util.List<String> events=new ArrayList<>();
  final Path path;final SaveQueue queue;final PuzzleRunner runner;
  Host()throws Exception{path=Files.createTempDirectory("interactions97-");queue=new SaveQueue(new SaveSlots(path,ProgressCatalog.standard()));
   Map<String,PuzzleRunner.Launcher> map=new HashMap<>();for(String k:List.of("present","gears","wires","temporary_power","rush_hour","confirm","action","log_analysis"))map.put(k,step->{launches++;lastPuzzle=step.id();return outcome;});
   runner=new PuzzleRunner(map,()->current,()->!closing,step->{events.add("completed:"+step.id());refresh();});
  }
  public GameState state(){return current;}public boolean blocked(){return closing||travelling;}public boolean isCurrent(GameState s){return !closing&&current==s;}
  public boolean spareSelected(){return selected;}public void selectSpare(boolean b){selected=b;events.add("select:"+b);}
  public void log(String s){events.add("log:"+s);}public void refresh(){refreshes++;events.add("refresh");save();}
  public void save(){saves++;events.add("save");queue.saveAuto(current.snapshot(),false);}
  public void play(String s){events.add("sound:"+s);}public void audioEra(Era era){events.add("era:"+era);}
  public void openMemo(){memos++;events.add("memo");}public void closeNotebook(){events.add("closeNote");}
  public void startFailure(Runnable r){arrival=r;events.add("startFailure");}public void showFailureDialogue(){failures++;events.add("failureDialogue");}
  public void showEnding(){endings++;events.add("ending");}
  public void openDevice(GameState s,boolean selected,Runnable changed,Runnable intro){opens++;deviceState=s;deviceSelection=selected;this.changed=changed;this.intro=intro;}
  public PuzzleRunner.Outcome runPuzzle(String id){return runner.run(id);}
  void roundTrip()throws Exception{queue.barrier().get(5,TimeUnit.SECONDS);GameState loaded=new SaveSlots(path,ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state();ok(loaded.snapshot().equals(current.snapshot()),"handler save snapshot roundtrip");current=loaded;}
  public void close(){queue.close();}
 }
 static void deviceChanged(Host h,ItemUseHandler items,Runnable action){items.use("DEVICE");ok(h.deviceState==h.current,"device receives current session");action.run();int n=h.saves;h.changed.run();ok(h.saves==n+2,"normal device preserves explicit save then refresh save");}
 public static void main(String[] args)throws Exception{
  try(Host h=new Host()){
   SceneInteractionHandler scene=new SceneInteractionHandler(h);ItemUseHandler items=new ItemUseHandler(h);
   h.closing=true;scene.interact("CASE");items.use("DEVICE");ok(h.events.isEmpty()&&h.opens==0,"closing blocks both handlers");h.closing=false;h.travelling=true;scene.interact("CASE");ok(!h.current.has("DEVICE"),"travel blocks pickup");h.travelling=false;
   items.use("DEVICE");ok(h.opens==0,"missing device cannot open");items.use("CIPHER_TABLE");ok(h.memos==0,"missing memo cannot open");
   scene.interact("PRESENT_PC");ok(h.launches==0,"tutorial blocks other objects");scene.interact("CASE");ok(h.current.has("DEVICE")&&h.saves==1,"tutorial pickup and save");int n=h.saves;scene.interact("CASE");ok(h.saves==n,"duplicate tutorial pickup no save");
   items.use("DEVICE");ok(h.intro!=null,"prototype uses intro path");h.intro.run();ok(!h.current.prototypeFailed(),"failure delayed until effect completes");h.arrival.run();ok(h.current.prototypeFailed()&&!h.current.powered()&&h.failures==1,"failure remains present and shows dialogue");
   scene.interact("MEMO");ok(!h.current.has("CIPHER_TABLE"),"dark room blocks memo");scene.interact("POWER");ok(h.current.powered()&&h.lastPuzzle.equals("PZ004"),"power routes wire puzzle");
   n=h.launches;scene.interact("WORKBENCH");ok(h.launches==n&&!h.current.done("PZ008"),"repair blocked before analysis");scene.interact("MEMO");ok(h.current.has("CIPHER_TABLE")&&h.memos==1,"memo pickup viewer");n=h.saves;scene.interact("MEMO");items.use("CIPHER_TABLE");ok(h.memos==3&&h.saves==n,"reopen memo no duplicate reward/save");
   h.outcome=PuzzleResult.CANCELLED;n=h.refreshes;scene.interact("PRESENT_PC");ok(!h.current.done("PZ006")&&h.refreshes==n+1,"scene cancellation refresh preserved");h.outcome=PuzzleResult.SOLVED;
   scene.interact("PRESENT_PC");scene.interact("PRESENT_PC");ok(h.current.done("PZ001"),"PC password then Rush Hour");items.use("USB");ok(!h.current.done("PZ021"),"uncopied USB cannot analyze");scene.interact("USB");scene.interact("PRESENT_PC");ok(h.current.usbComplete(),"USB pickup and copy");
   h.outcome=PuzzleResult.CANCELLED;n=h.saves;items.use("USB");ok(!h.current.has("FAULT_REPORT")&&h.saves==n,"cancelled inventory analysis no completion save");h.outcome=PuzzleResult.SOLVED;n=h.saves;items.use("USB");ok(h.current.has("FAULT_REPORT")&&h.current.available("PZ008")&&h.saves==n+1,"analysis reward repair gate and one completion save");n=h.launches;items.use("USB");items.use("FAULT_REPORT");ok(h.launches==n,"completed USB and report only explain");h.roundTrip();
   scene.interact("WORKBENCH");for(String id:new String[]{"CASE","CELL","MODULE"})scene.interact(id);scene.interact("WORKBENCH");scene.interact("WORKBENCH");ok(h.current.canDepart(),"workbench repair remains reachable after reload");
   deviceChanged(h,items,()->h.current.moveToPast());ok(h.current.era()==Era.PAST&&h.events.contains("sound:time_travel"),"past travel feedback");scene.interact("PAST_SPARE");items.use("SPARE");ok(h.selected,"spare selected");items.use("SPARE");ok(!h.selected,"spare deselected");items.use("SPARE");
   deviceChanged(h,items,()->{ok(h.deviceSelection,"selection reaches device");h.current.repairWatch();});ok(!h.selected&&!h.current.has("SPARE")&&h.current.canEnter("future_1"),"part consumed selection reset and future unlocked");
   deviceChanged(h,items,()->h.current.moveToFuture());scene.interact("FUTURE_CORE");ok(h.lastPuzzle.equals("PZ003"),"future puzzle routing");items.use("CORE");deviceChanged(h,items,()->h.current.installCorePart());deviceChanged(h,items,()->h.current.escape());ok(h.endings==1&&h.current.isEscaped(),"ending callback");h.roundTrip();
   n=h.launches;scene.interact("FUTURE_CORE");scene.interact("unknown");ok(h.launches==n,"completed and unknown objects do not launch");
   items.use("DEVICE");Runnable old=h.changed;h.current=GameState.newGame();n=h.saves;old.run();ok(h.saves==n&&!h.current.isEscaped(),"stale device callback cannot affect replacement session");
  }
  try(Host h=new Host()){SceneInteractionHandler scene=new SceneInteractionHandler(h);ItemUseHandler items=new ItemUseHandler(h);scene.interact("CASE");items.use("DEVICE");h.intro.run();Runnable old=h.arrival;h.current=GameState.newGame();int n=h.saves;old.run();ok(h.saves==n&&!h.current.prototypeFailed(),"stale failure callback ignored");scene.interact("CASE");items.use("DEVICE");h.intro.run();h.closing=true;n=h.saves;h.arrival.run();ok(h.saves==n&&!h.current.prototypeFailed(),"closing failure callback ignored");}
  System.out.println("PASS "+checks+" scene/item routing, cancel, reward, save/load, selection, travel and stale callback checks; no game or dialog launched.");
 }
}
