package timeescape.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import timeescape.core.GameSession;
import timeescape.core.GameState;
import timeescape.system.DialogueCatalog;

/** Window composition, visual operations and screen navigation only. */
public final class GameFrame extends JFrame implements GameView {
    private static final String MENU="menu",GAME="game";
    private final CardLayout cardLayout=new CardLayout();
    private final JPanel cards=new JPanel(cardLayout),gameRoot=new JPanel(new BorderLayout());
    private final GamePreparation preparation=new GamePreparation();
    private final JLabel progress=new JLabel();
    private final GameController controller;
    private final MainMenuPanel mainMenu;
    private GameMenuOverlay continueSlots;
    private GameScenePanel scene;
    private GameHud hud;
    private GameViewUpdater viewUpdater;

    public GameFrame(){
        super("TIME ESCAPE");setCursor(GameCursors.normal());setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1020,650));setSize(1500,880);setLocationRelativeTo(null);
        controller=new GameController(this,new GameSession(),PuzzleDialogs.launchers(this));
        mainMenu=new MainMenuPanel(controller);cards.add(mainMenu,MENU);cards.add(gameRoot,GAME);
        setContentPane(cards);cardLayout.show(cards,MENU);controller.initialize();
        addWindowListener(new WindowAdapter(){
            @Override public void windowOpened(WindowEvent e){controller.windowOpened();}
            @Override public void windowClosing(WindowEvent e){dispose();}
        });
    }
    @Override public void dispose(){if(controller==null)super.dispose();else controller.requestClose();}
    @Override public void disposeWindow(){super.dispose();}
    @Override public void setWindowEnabled(boolean enabled){setEnabled(enabled);}
    @Override public void prepareResources(){mainMenu.prepareEffects();preparation.prepare();}
    @Override public CompletableFuture<Boolean> prepareGameView(){
        return preparation.readyOnEdt().thenApply(weather->{
            if(controller.isClosing())return false;if(hud==null)initUI(weather);return true;
        });
    }
    private void initUI(LabWindowWeather weather){
        scene=new GameScenePanel(controller::sceneClicked,weather);
        InventoryPanel inventory=new InventoryPanel(controller::itemClicked);
        MissionPanel missions=new MissionPanel();inventory.setReadListener(controller::save);
        hud=new GameHud(scene,missions,inventory,()->hud.openHelp(),controller::resetGame,controller::showMainMenu);
        viewUpdater=new GameViewUpdater(scene,missions,hud,inventory,progress);
        hud.setTransitionSettings(mainMenu::isTransitionAnimationEnabled,mainMenu::setTransitionAnimation);
        hud.setAsyncSaveSlots(controller::summaries,controller::saveManualSlot,controller::loadSlot);
        gameRoot.add(hud,BorderLayout.CENTER);gameRoot.revalidate();
    }
    private GameMenuOverlay ensureContinueSlots(){
        if(continueSlots==null){
            continueSlots=new GameMenuOverlay(mainMenu,this::closeContinueSlots,()->{},()->{});
            continueSlots.setAsyncSaveSlots(controller::summaries,slot->CompletableFuture.completedFuture(false),controller::loadSlot);
            continueSlots.setNewGameAction(mainMenu::startConfirmedNewGame);
            continueSlots.setTransitionSettings(mainMenu::isTransitionAnimationEnabled,mainMenu::setTransitionAnimation);
            setGlassPane(continueSlots);
        }return continueSlots;
    }
    private void closeContinueSlots(){
        if(continueSlots==null||continueSlots.isBusy())return;continueSlots.release();setInputBlocked(false);
        if(mainMenu.isShowing())mainMenu.requestFocusInWindow();else if(scene!=null)scene.requestFocusInWindow();
    }
    @Override public void showSlots(String page){ensureContinueSlots().open(page);}
    @Override public void showMainMenu(){cardLayout.show(cards,MENU);}
    @Override public void showGame(){cardLayout.show(cards,GAME);}
    @Override public void setContinueAvailable(boolean available){mainMenu.setContinueAvailable(available);}
    @Override public void setInputBlocked(boolean blocked){if(scene!=null)scene.setInputBlocked(blocked);}
    @Override public boolean hasGameView(){return hud!=null;}
    @Override public boolean isTravelling(){return hud!=null&&hud.isTravelling();}
    @Override public void restoreScene(GameState state){scene.setEra(state.era());scene.restorePowered(state.powered());}
    @Override public void refresh(GameState state,boolean selected){viewUpdater.refresh(state,selected);}
    @Override public void showMessage(String text){if(hud!=null)hud.showMessage(text);}
    @Override public void closeNotebook(){hud.closeNotebook();}
    @Override public void dismissDialogue(){hud.dismissDialogue();}
    @Override public void cancelTravel(){if(hud!=null)hud.cancelFirstTravel();}
    @Override public void openMemo(){hud.openCipherMemo();}
    @Override public void startFailure(Runnable completed){hud.startFirstTravel(completed);}
    @Override public void showOpeningTutorial(){hud.showOpeningTutorial(DialogueCatalog.standard().scene("intro.start"));}
    @Override public void showFailureDialogue(){hud.showDialogue(DialogueCatalog.standard().scene("intro.failure"));}
    @Override public void openDevice(GameState state,boolean selected,Runnable changed,Runnable intro){
        if(intro==null)new DeviceDialog(this,state,selected,changed).setVisible(true);
        else new DeviceDialog(this,state,selected,changed,intro).setVisible(true);
    }
    @Override public void showEnding(){EndingDialog.show(this);}
    @Override public void showSaveFailure(Path path){JOptionPane.showMessageDialog(this,
        "진행 상황을 저장하지 못했습니다.\n"+path+"\n저장 위치의 쓰기 권한과 남은 공간을 확인해주세요. 현재 게임은 계속 열려 있습니다.","저장 실패",JOptionPane.WARNING_MESSAGE);}
    @Override public void showPreparationFailure(){JOptionPane.showMessageDialog(this,"게임 리소스를 준비하지 못했습니다. 배포 파일을 확인해주세요.","준비 실패",JOptionPane.ERROR_MESSAGE);}
    @Override public void showBackupRecovered(){JOptionPane.showMessageDialog(this,"선택한 저장소의 이전 백업으로 복구했습니다.","저장 복구",JOptionPane.INFORMATION_MESSAGE);}
    @Override public boolean confirmExit(){return ExitConfirmDialog.confirm(this);}
}
