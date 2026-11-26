package timeescape.ui;
import timeescape.core.*;import timeescape.system.*;import timeescape.puzzle.*;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;import javax.swing.SwingUtilities;
public final class VerifyController99 {
 static int checks;static void ok(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static <T>T edt(Callable<T> c)throws Exception{FutureTask<T> f=new FutureTask<>(c);SwingUtilities.invokeAndWait(f);return f.get();}
 static <T>T waitFor(CompletableFuture<T> f)throws Exception{return f.get(8,TimeUnit.SECONDS);}
 static final class View implements GameView {
  CompletableFuture<Boolean> ready=CompletableFuture.completedFuture(true);boolean has,travel,enabled=true,disposed,continueEnabled,exit=true;int prepares,resources,refreshes,tutorials,failures,backups,saveErrors,prepareErrors,ends;String page="",screen="",last="";GameState shown,device;boolean selection;Runnable changed,intro,arrival;
  public CompletableFuture<Boolean> prepareGameView(){prepares++;return ready.thenApply(v->{has=v;return v;});}public void prepareResources(){resources++;}public boolean hasGameView(){return has;}public boolean isTravelling(){return travel;}
  public void setContinueAvailable(boolean b){continueEnabled=b;}public void setInputBlocked(boolean b){}public void showSlots(String p){page=p;}public void showMainMenu(){screen="menu";}public void showGame(){screen="game";}public void setWindowEnabled(boolean b){enabled=b;}public void disposeWindow(){disposed=true;}
  public void restoreScene(GameState s){shown=s;}public void refresh(GameState s,boolean selected){shown=s;selection=selected;refreshes++;}public void showMessage(String s){last=s;}public void closeNotebook(){}public void dismissDialogue(){}public void cancelTravel(){travel=false;}public void openMemo(){}public void startFailure(Runnable r){arrival=r;}public void showOpeningTutorial(){tutorials++;}public void showFailureDialogue(){failures++;}
  public void openDevice(GameState s,boolean b,Runnable changed,Runnable intro){device=s;selection=b;this.changed=changed;this.intro=intro;}public void showEnding(){ends++;}public void showSaveFailure(Path p){saveErrors++;}public void showPreparationFailure(){prepareErrors++;}public void showBackupRecovered(){backups++;}public boolean confirmExit(){return exit;}
 }
 static final class Fixture implements AutoCloseable {
  final Path dir;final GameSession session;final View view=new View();final GameController c;final AtomicInteger shutdown=new AtomicInteger();final AtomicBoolean failSettings=new AtomicBoolean();
  Fixture()throws Exception{dir=Files.createTempDirectory("controller99-");session=edt(()->new GameSession(dir));Map<String,PuzzleRunner.Launcher> kinds=new HashMap<>();for(String k:List.of("present","gears","wires","temporary_power","rush_hour","confirm","action","log_analysis"))kinds.put(k,s->PuzzleResult.SOLVED);
   c=edt(()->new GameController(view,session,kinds,()->failSettings.getAndSet(false)?CompletableFuture.failedFuture(new IllegalStateException("settings failure")):CompletableFuture.completedFuture(null),shutdown::incrementAndGet));}
  void drain()throws Exception{waitFor(edt(session::saveAuto));edt(()->null);}
  public void close()throws Exception{edt(()->{session.close();return null;});}
 }
 public static void main(String[] args)throws Exception{
  for(var f:GameFrame.class.getDeclaredFields())ok(!Set.of(GameState.class,GameSession.class,SaveQueue.class).contains(f.getType()),"frame contains no progress/save owner: "+f.getName());
  try(Fixture x=new Fixture()){
   edt(()->{x.c.initialize();x.c.windowOpened();return null;});waitFor(edt(x.session::refreshAvailability));ok(x.view.resources==1,"resources requested after window open");ok(edt(x.c::confirmNewGame),"empty saves bypass confirmation");
   x.view.ready=new CompletableFuture<>();edt(()->{x.c.startGame();x.c.startGame();return null;});ok(x.view.prepares==1&&!x.session.started(),"duplicate start blocked during UI preparation");edt(()->{x.view.ready.complete(true);return null;});x.drain();ok(x.session.started()&&x.view.screen.equals("game")&&x.view.tutorials==1&&x.view.shown==x.session.state(),"start state view and tutorial");
   ok(!edt(x.c::confirmNewGame)&&x.view.page.equals("newgame"),"existing auto asks confirmation");
   edt(()->{x.c.sceneClicked("CASE");x.c.itemClicked("DEVICE");x.view.intro.run();x.view.arrival.run();return null;});ok(x.session.state().prototypeFailed()&&x.view.failures==1,"actual controller routes prototype failure");
   edt(()->{x.c.sceneClicked("POWER");x.c.sceneClicked("MEMO");x.c.sceneClicked("PRESENT_PC");x.c.sceneClicked("PRESENT_PC");x.c.sceneClicked("USB");x.c.sceneClicked("PRESENT_PC");return null;});int before=x.view.refreshes;
   edt(()->{x.c.itemClicked("USB");return null;});x.drain();ok(x.session.state().done("PZ021")&&x.session.state().has("FAULT_REPORT")&&x.view.refreshes==before+1,"final puzzle commits one refresh");ok(new SaveSlots(x.dir,ProgressCatalog.standard()).load(SaveSlots.Slot.AUTO).state().available("PZ008"),"real controller autosaves repair unlock");
   waitFor(edt(()->x.c.saveManualSlot(SaveSlots.Slot.ONE)));GameState prior=x.session.state();edt(()->{x.c.resetGame();return null;});x.drain();ok(!x.c.isCurrent(prior)&&x.view.tutorials==2,"reset replaces session and reopens tutorial");
   x.view.ready=new CompletableFuture<>();CompletableFuture<Boolean> load=edt(()->x.c.loadSlot(SaveSlots.Slot.ONE));waitFor(edt(x.session::summaries));ok(!load.isDone()&&!x.session.state().done("PZ021"),"load waits for view preparation");edt(()->{x.view.ready.complete(true);return null;});ok(waitFor(load)&&x.session.state().done("PZ021")&&x.view.shown==x.session.state(),"load applies and refreshes latest state");x.drain();
   edt(()->{x.c.showMainMenu();x.c.continueGame();return null;});ok(x.view.screen.equals("menu")&&x.view.page.equals("load"),"menu and continue routing");edt(()->{x.c.showSettings();return null;});ok(x.view.page.equals("settings"),"settings routing");
   x.failSettings.set(true);edt(()->{x.c.requestClose();return null;});x.drain();ok(!x.c.isClosing()&&x.view.enabled&&!x.view.disposed&&x.view.saveErrors==1,"failed final settings save keeps window/session open");
   edt(()->{x.c.requestClose();return null;});for(int i=0;i<50&&!x.view.disposed;i++){Thread.sleep(20);edt(()->null);}ok(x.view.disposed&&x.shutdown.get()==1,"successful final save closes once");edt(()->{x.c.requestClose();return null;});ok(x.shutdown.get()==1,"duplicate close ignored");
  }
  try(Fixture x=new Fixture()){
   x.view.ready=CompletableFuture.failedFuture(new IllegalStateException("resource failure"));edt(()->{x.c.startGame();return null;});ok(x.view.prepareErrors==1&&!x.session.started(),"preparation failure leaves menu state");x.view.ready=CompletableFuture.completedFuture(true);edt(()->{x.c.startGame();return null;});x.drain();ok(x.session.started(),"start retry succeeds");
   waitFor(edt(()->x.c.saveManualSlot(SaveSlots.Slot.ONE)));GameState state=x.session.state();x.view.ready=CompletableFuture.failedFuture(new IllegalStateException("view load failure"));try{waitFor(edt(()->x.c.loadSlot(SaveSlots.Slot.ONE)));throw new AssertionError("load should fail");}catch(ExecutionException expected){checks++;}ok(x.session.state()==state,"failed view preparation leaves current progress unchanged");
  }
  try(Fixture x=new Fixture()){
   x.view.ready=new CompletableFuture<>();edt(()->{x.c.startGame();x.c.requestClose();return null;});for(int i=0;i<50&&!x.view.disposed;i++){Thread.sleep(20);edt(()->null);}edt(()->{x.view.ready.complete(true);return null;});ok(!x.session.started()&&x.view.tutorials==0&&x.view.disposed,"late start completion ignored after close");ok(!Files.exists(x.session.autoSaveFile()),"menu-only close writes no progress");
  }
  System.out.println("PASS "+checks+" controller composition, start/load/reset, handler completion, save failure/retry and close checks; no game window.");
 }
}
