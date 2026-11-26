package timeescape.ui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;
import timeescape.core.GameSession;
import timeescape.core.GameState;
import timeescape.model.Era;
import timeescape.puzzle.PuzzleRunner;
import timeescape.system.AudioSettings;
import timeescape.system.GameAudio;
import timeescape.system.SaveSlots;

/** Coordinates user actions and lifecycle; the view never owns progress or saves. */
final class GameController implements InteractionHost, MainMenuPanel.MenuActions {
    private final GameView view;
    private final GameSession session;
    private final PuzzleRunner puzzles;
    private final SceneInteractionHandler sceneInteractions;
    private final ItemUseHandler itemUses;
    private final Supplier<CompletableFuture<Void>> saveSettings;
    private final Runnable shutdownAudio;
    private boolean closing,starting,spareSelected;

    GameController(GameView view,GameSession session,Map<String,PuzzleRunner.Launcher> launchers){
        this(view,session,launchers,()->AudioSettings.get().save(),()->GameAudio.get().shutdown());
    }
    GameController(GameView view,GameSession session,Map<String,PuzzleRunner.Launcher> launchers,
                   Supplier<CompletableFuture<Void>> saveSettings,Runnable shutdownAudio){
        this.view=Objects.requireNonNull(view);this.session=Objects.requireNonNull(session);
        this.saveSettings=Objects.requireNonNull(saveSettings);this.shutdownAudio=Objects.requireNonNull(shutdownAudio);
        puzzles=new PuzzleRunner(launchers,session::state,()->!closing,this::onPuzzleCompleted);
        sceneInteractions=new SceneInteractionHandler(this);itemUses=new ItemUseHandler(this);
    }
    void initialize(){view.setContinueAvailable(session.anySave());updateSaveAvailability();SwingUtilities.invokeLater(()->{if(!closing)GameAudio.get().menu();});}
    void windowOpened(){if(!closing)view.prepareResources();}
    boolean isClosing(){return closing;}
    void sceneClicked(String id){sceneInteractions.interact(id);}
    void itemClicked(String id){itemUses.use(id);}
    CompletableFuture<List<SaveSlots.Summary>> summaries(){return session.summaries();}

    void requestClose(){
        if(closing)return;closing=true;view.setWindowEnabled(false);view.cancelTravel();
        session.saveAuto().thenCompose(v->saveSettings.get()).whenComplete((ignored,error)->{
            if(error!=null){closing=false;view.setWindowEnabled(true);view.showSaveFailure(session.autoSaveFile());return;}
            session.close();shutdownAudio.run();view.disposeWindow();
        });
    }
    CompletableFuture<Boolean> loadSlot(SaveSlots.Slot slot){
        if(closing)return CompletableFuture.completedFuture(false);
        return session.prepareLoad(slot).thenCompose(result->view.prepareGameView().thenApply(ready->{
            if(closing||!ready||!session.applyLoaded(result))return false;
            spareSelected=false;view.dismissDialogue();view.restoreScene(session.state());
            log(slot.title()+"에서 진행을 불러왔습니다.");refresh();view.showGame();audioEra(session.state().era());
            if(result.recoveredFromBackup())view.showBackupRecovered();return true;
        }));
    }
    CompletableFuture<Boolean> saveManualSlot(SaveSlots.Slot slot){
        if(closing)return CompletableFuture.completedFuture(false);
        return session.saveManual(slot).thenApply(ok->{if(!closing&&ok)view.setContinueAvailable(session.anySave());return ok;});
    }
    @Override public void save(){
        if(closing||!session.started())return;
        session.saveAuto().whenComplete((ignored,error)->{
            if(closing)return;if(error!=null)view.showSaveFailure(session.autoSaveFile());
            else view.setContinueAvailable(session.anySave());
        });
    }
    private void updateSaveAvailability(){session.refreshAvailability().thenRun(()->{if(!closing)view.setContinueAvailable(session.anySave());});}
    void resetGame(){
        if(closing)return;session.startNew();view.restoreScene(session.state());spareSelected=false;view.closeNotebook();
        log(session.state().introGoal());refresh();play("ui_click");view.showOpeningTutorial();
    }
    void showMainMenu(){view.closeNotebook();view.setContinueAvailable(session.anySave());updateSaveAvailability();view.showMainMenu();GameAudio.get().menu();}
    @Override public boolean confirmNewGame(){
        if(closing||starting)return false;if(!session.autoExists())return true;
        view.setInputBlocked(true);view.showSlots("newgame");return false;
    }
    @Override public void startGame(){
        if(closing||starting)return;starting=true;
        view.prepareGameView().whenComplete((ready,error)->{
            starting=false;if(closing)return;if(error!=null){view.showPreparationFailure();return;}if(!ready)return;
            session.startNew();view.restoreScene(session.state());spareSelected=false;view.setContinueAvailable(true);
            log(session.state().introGoal());refresh();view.showGame();audioEra(session.state().era());view.showOpeningTutorial();
        });
    }
    @Override public void continueGame(){if(closing||starting)return;save();view.setInputBlocked(true);view.showSlots("load");}
    @Override public void showSettings(){if(closing||starting)return;view.setInputBlocked(true);view.showSlots("settings");}
    @Override public void exitGame(){if(view.confirmExit())requestClose();}

    @Override public GameState state(){return session.state();}
    @Override public boolean blocked(){return closing||!view.hasGameView()||view.isTravelling();}
    @Override public boolean isCurrent(GameState expected){return !closing&&session.isCurrent(expected);}
    @Override public boolean spareSelected(){return spareSelected;}
    @Override public void selectSpare(boolean selected){spareSelected=selected;}
    @Override public void log(String text){view.showMessage(text);}
    @Override public void refresh(){view.refresh(session.state(),spareSelected);save();}
    @Override public void play(String sound){GameAudio.get().play(sound);}
    @Override public void audioEra(Era era){GameAudio.get().era(era);}
    @Override public void openMemo(){view.openMemo();}
    @Override public void closeNotebook(){view.closeNotebook();}
    @Override public void startFailure(Runnable completed){view.startFailure(completed);}
    @Override public void showFailureDialogue(){view.showFailureDialogue();}
    @Override public void showEnding(){view.showEnding();}
    @Override public void openDevice(GameState state,boolean selected,Runnable changed,Runnable intro){view.openDevice(state,selected,changed,intro);}
    @Override public PuzzleRunner.Outcome runPuzzle(String id){return puzzles.run(id);}

    /** Exactly one progress notification and autosave path for every solved puzzle kind. */
    private void onPuzzleCompleted(timeescape.core.ProgressCatalog.Step step) {
            log(switch (step.id()) {
                case "PZ001" -> session.state().prototypeFailed()?"사고 로그 복구 완료. 데이터를 USB에 복사하세요.":"연구 데이터 복구 완료. USB에 복사하세요.";
                case "PZ004" -> "전력이 복구되었습니다. 컴퓨터에서 사고 로그를 확보하세요.";
                case "PZ006" -> "연구 계정 접근 권한을 얻었습니다.";
                case "PZ007" -> session.state().prototypeFailed()?"사고 로그를 USB에 확보했습니다. USB를 사용해 사고 기록을 분석하세요.":"USB 확보 미션 완료. USB는 미래까지 보관됩니다.";
                case "PZ021" -> "좌표 보정 불일치가 최초 원인입니다. 분석 보고서를 획득했습니다. 작업대에서 장치 수리를 시작하세요.";
                case "PZ002" -> "과거 작업대에서 '대체 부품'을 획득했습니다. 인벤토리에서 부품을 선택하고 손목 장치를 여세요.";
                case "PZ003" -> "중앙 시간 장치에서 '미래 핵심 부품'을 확보했습니다. 인벤토리의 손목 장치에 장착하세요.";
                default -> step.mission().isBlank() ? step.name() + " 완료" : session.state().missionNext(step.mission());
            });
            if (step.id().equals("PZ002")) play("item_pickup");
            if (step.id().equals("PZ003")) play("core_pickup");
        refresh();
    }
}
