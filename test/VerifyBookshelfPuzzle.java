package timeescape.ui;

import java.util.List;
import timeescape.core.ProgressCatalog;
import timeescape.model.Era;
import timeescape.puzzle.BookshelfPuzzle;
import timeescape.scene.Hotspot;
import timeescape.scene.HotspotCatalog;

/** Headless model, registry, progression and past-room hit-area checks. */
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
        check(catalog.steps().size() == 23, "archive reading step is registered");
        var step = catalog.steps().get("PZ022");
        check(step != null && step.kind().equals("bookshelf"), "progress kind registration");
        check(step.object().equals("PAST_BOOKSHELF") && step.requires().contains("GATE001"), "past shelf is gated by past unlock");
        check(step.flags().contains("archive_record_found") && step.grant().contains("ARCHIVE_LOG"), "solving the shelf grants the archive journal");
        var read = catalog.steps().get("ACT_READ_ARCHIVE");
        check(read != null && read.requires().contains("PZ022") && read.items().contains("ARCHIVE_LOG"), "journal read action requires the reward");
        var repair = catalog.steps().get("ACT_REPAIR");
        check(repair.requires().containsAll(List.of("PZ002", "ACT_READ_ARCHIVE")), "device repair waits for journal read and spare puzzle");
        check(repair.items().containsAll(List.of("SPARE", "ARCHIVE_LOG")) && repair.consume().contains("SPARE"), "repair consumes the spare part after journal unlock");
        check(PuzzleDialogs.launchers(null).containsKey("bookshelf"), "dialog launcher registered");

        List<Hotspot> past = HotspotCatalog.of(Era.PAST);
        check(past.stream().filter(h -> h.id().equals("PAST_BOOKSHELF")).count() == 1, "bookshelf hover and glint use a single shelf hotspot");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.label().equals("연구 기록서 정리")), "shelf hover label is unified");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(800, 320)), "shelf center opens puzzle");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(700, 250)), "shelf hotspot covers the books");
        check(past.stream().noneMatch(h -> h.id().equals("PAST_BOOKSHELF") && h.contains(650, 320)), "nearby wall does not trigger puzzle");
        check(past.stream().anyMatch(h -> h.id().equals("PAST_MEMO") && h.contains(1500, 950)), "desk memo click and Shift glint target the memo");
        System.out.println("PASS " + checks + " bookshelf integration checks; no game window launched.");
    }
}
