package timeescape.ui;
import timeescape.core.*;import timeescape.model.Era;import timeescape.puzzle.PuzzleRunner;
import java.util.ArrayList;import java.util.List;

/** Headless inventory interaction regression for the spare-part mystery and journal unlock. */
public final class VerifyInteractions97 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static final class Host implements InteractionHost,AutoCloseable {
  GameState current;boolean selected;String last="";int refreshes,saves,opens,endings;final List<String> events=new ArrayList<>();
  GameState deviceState;boolean deviceSelection;Runnable changed=this::refresh,intro=()->{},arrival;boolean closing,travelling;
  Host(){this(GameState.newGame());}
  Host(GameState state){current=state;}
  public GameState state(){return current;}public boolean blocked(){return closing||travelling;}public boolean isCurrent(GameState s){return s==current;}
  public boolean spareSelected(){return selected;}public void selectSpare(boolean b){selected=b;}
  public void log(String s){last=s;events.add("log:"+s);}public void refresh(){refreshes++;events.add("refresh");save();}public void save(){saves++;events.add("save");}
  public void play(String s){events.add("sound:"+s);}public void audioEra(Era era){events.add("era:"+era);}public void openMemo(){events.add("memo");}public void closeNotebook(){events.add("closeNote");}
  public void startFailure(Runnable r){arrival=r;}public void showFailureDialogue(){events.add("failureDialogue");}public void showEnding(){endings++;events.add("ending");}
  public void openDevice(GameState s,boolean selected,Runnable changed,Runnable intro){opens++;deviceState=s;deviceSelection=selected;this.changed=changed;this.intro=intro;}
  public PuzzleRunner.Outcome runPuzzle(String id){return PuzzleRunner.Outcome.IGNORED;}
  void roundTrip(){events.add("roundTrip");}
  public void close(){}
 }
 static GameState readyForPast(){
  GameState s=GameState.newGame();s.takePrototype();s.usePrototype();s.complete("PZ004");
  s.complete("ACT_GET_CIPHER");s.complete("PZ006");s.completeDiagnosis();s.collect("USB");
  s.complete("PZ007");s.complete("PZ021");s.complete("PZ008");
  for(String id:new String[]{"CASE","CELL","MODULE"})s.collect(id);
  s.complete("PZ010");s.complete("PZ011");s.moveToPast();s.collectSparePart();return s;
 }
 public static void main(String[] args){
  Host host=new Host(readyForPast());ItemUseHandler items=new ItemUseHandler(host);
  items.use("SPARE");ok(!host.spareSelected()&&host.last.contains("용도를 아직 알 수 없는"),"spare purpose remains unknown before journal");
  ok(!host.current.available("ACT_REPAIR"),"repair stays blocked before journal");
  host.current.complete("PZ022");ok(host.current.has("ARCHIVE_LOG"),"solving bookshelf adds archive journal");
  items.use("ARCHIVE_LOG");ok(host.current.done("ACT_READ_ARCHIVE")&&host.current.flag("archive_log_read"),"clicking journal records it as read");
  ok(host.current.available("ACT_REPAIR")&&host.last.contains("대체 부품"),"journal reveals spare use and unlocks repair");
  items.use("SPARE");ok(host.spareSelected(),"spare can be selected after journal is read");
  host.current.repairWatch();ok(host.current.isWatchRepaired()&&host.current.canEnter("future_1"),"selected spare can complete repair after journal");
  System.out.println("PASS "+checks+" journal discovery, inventory interaction and repair gate checks");
 }
}
