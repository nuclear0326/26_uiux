package timeescape.ui;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import timeescape.core.GameState;

/** Window operations only; no progress mutation or persistence logic. */
interface GameView {
    CompletableFuture<Boolean> prepareGameView();
    void prepareResources();
    boolean hasGameView();
    boolean isTravelling();
    void setContinueAvailable(boolean available);
    void setInputBlocked(boolean blocked);
    void showSlots(String page);
    void showMainMenu();
    void showGame();
    void setWindowEnabled(boolean enabled);
    void disposeWindow();
    void restoreScene(GameState state);
    void refresh(GameState state,boolean selected);
    void showMessage(String text);
    void closeNotebook();
    void dismissDialogue();
    void cancelTravel();
    void openMemo();
    void startFailure(Runnable completed);
    void showOpeningTutorial();
    void showFailureDialogue();
    void openDevice(GameState state,boolean selected,Runnable changed,Runnable intro);
    void showEnding();
    void showSaveFailure(Path path);
    void showPreparationFailure();
    void showBackupRecovered();
    boolean confirmExit();
}
