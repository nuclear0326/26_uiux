package timeescape.ui;

import java.util.List;
import timeescape.core.ProgressCatalog;
import timeescape.model.Era;
import timeescape.puzzle.BookshelfPuzzle;
import timeescape.scene.Hotspot;
import timeescape.scene.HotspotCatalog;

/** Headless model, registry, progression and circled-shelf hit-area checks. */
public final class VerifyBookshelfPuzzle {
    private static int checks;
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }
    public static void main(String[] args) {
        BookshelfPuzzle puzzle = new BookshelfPuzzle();
        check(!puzzle.solved() && !puzzle.full(), "starts empty");
        check(!puzzle.place(0, "UNKNOWN"), "unknown volume rejected");
        puzzle.place(1, BookshelfPuzzle.OBSERVATION);
        check(!puzzle.solved(), "partial arrangement cannot solve");
        puzzle.place(0, BookshelfPuzzle.SCHEMATIC);
        puzzle.place(2, BookshelfPuzzle.TIME_EXPERIMENT);
        puzzle.place(3, BookshelfPuzzle.INCIDENT_REPORT);
        check(puzzle.full() && puzzle.solved(), "memo order solves");
        puzzle.take(BookshelfPuzzle.TIME_EXPERIMENT);
        check(!puzzle.full() && !puzzle.solved(), "moving a placed book clears the old slot");
        puzzle.place(2, BookshelfPuzzle.GARDEN_GUIDE);
        check(puzzle.full() && !puzzle.solved(), "decoy volume cannot solve");
        puzzle.reset();
        check(!puzzle.full() && BookshelfPuzzle.solution().size() == 4, "reset and four-volume solution");
        check(BookshelfPuzzle.hint(0).contains("두 번째") && BookshelfPuzzle.hint(1).contains("바로 왼쪽"), "hint ladder");

        ProgressCatalog catalog = ProgressCatalog.standard();
        check(catalog.steps().size() == 22, "new step is registered without removing existing steps");
        var step = catalog.steps().get("PZ022");
        check(step != null && step.kind().equals("bookshelf"), "progress kind registration");
        check(step.object().equals("PAST_BOOKSHELF") && step.requires().contains("GATE001"), "past shelf is gated by past unlock");
        check(step.flags().contains("archive_record_found"), "successful solve grants a persisted archive flag");
        check(PuzzleDialogs.launchers(null).containsKey("bookshelf"), "dialog launcher registered");

        List<Hotspot> past = HotspotCatalog.of(Era.PAST);
        long spineTargets = past.stream().filter(h -> h.id().equals("PAST_BOOKSHELF") && !h.label().equals("서가 속 연구 기록")).count();
        check(spineTargets == 5, "five spines expose title/sigil hover labels");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(800, 320)), "red-circled shelf center opens puzzle");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(700, 220)), "shelf hit area includes upper-left circle edge");
        check(past.stream().noneMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(650, 320)), "nearby wall does not trigger puzzle");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_MEMO") && h.contains(950, 800)), "desk memo has its own clickable hotspot");
        System.out.println("PASS " + checks + " bookshelf integration checks; no game window launched.");
    }
}
