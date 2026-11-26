package timeescape.ui;
import timeescape.core.GameState;
import timeescape.model.Era;
import timeescape.puzzle.PuzzleRunner;

/** UI effects needed by interactions. The frame supplies the current session on demand. */
interface InteractionHost {
    GameState state();
    boolean blocked();
    boolean isCurrent(GameState expected);
    boolean spareSelected();
    void selectSpare(boolean selected);
    void log(String text);
    void refresh();
    void save();
    void play(String sound);
    void audioEra(Era era);
    void openMemo();
    void closeNotebook();
    void startFailure(Runnable completed);
    void showFailureDialogue();
    void showEnding();
    void openDevice(GameState state,boolean selected,Runnable changed,Runnable intro);
    PuzzleRunner.Outcome runPuzzle(String id);
}
