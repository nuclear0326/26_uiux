package timeescape.core;

import java.util.*;
import timeescape.model.Era;

/** Mutable progress; content rules and disk persistence are separate services. */
public final class GameState {
    private final ProgressCatalog catalog;
    private final Set<String> completed = new HashSet<>();
    private final Set<String> items = new HashSet<>();
    private final Set<String> unreadItems = new HashSet<>();
    private final Set<String> flags = new HashSet<>();
    private final Set<String> unlockedRooms = new HashSet<>();
    private String roomId;

    public GameState() { this(ProgressCatalog.standard()); }
    /** New scenario entry. The plain constructor remains the legacy/save hydration baseline. */
    public static GameState newGame() {
        GameState state = new GameState();
        state.complete("INTRO_START");
        return state;
    }
    public boolean introScenario() { return done("INTRO_START"); }
    public boolean introPresent() { return introScenario() && era() == Era.PRESENT && !done("ACT_FIRST_TRAVEL") && !done("ACT_START_FAILURE"); }
    public boolean firstTravelComplete() { return introScenario() && done("ACT_FIRST_TRAVEL"); }
    public boolean prototypeFailed() { return done("ACT_START_FAILURE"); }
    public String introGoal() {
        if(prototypeFailed() && era()==Era.PAST) return "장치 수리 후 과거에 도착했습니다. 과거 연구실을 조사하세요.";
        if(prototypeFailed() && era()==Era.PRESENT) return !powered()?"시간이동에 실패했습니다. 배전함에서 연구실 전력을 복구하세요.":canDepart()?"로그 분석과 장치 수리가 끝났습니다. 보관함의 장치로 과거로 이동하세요.":"사고 로그를 확보하고 USB로 분석한 뒤 작업대에서 장치를 수리하세요.";
        return introPresent() ? (has("DEVICE")
            ? "연구 노트의 보관함에서 시간이동장치를 사용하세요."
            : "왼쪽 보관 상자에서 시간이동장치를 꺼내세요.")
            : "예상과 다른 과거에 도착했습니다. 고장난 장치와 연구실을 조사하세요.";
    }
    public void takePrototype() { complete("TUT001"); }
    /** The prototype fails in the present; later repairs unlock the normal past gate. */
    public void usePrototype() {
        if (!introPresent() || !available("ACT_START_FAILURE"))
            throw new IllegalStateException("먼저 상자에서 시간이동장치를 꺼내세요.");
        complete("ACT_START_FAILURE");
    }
    public GameState(ProgressCatalog catalog) {
        this.catalog = Objects.requireNonNull(catalog);
        roomId = catalog.initialRoom();
        unlockedRooms.add(roomId);
        settleAutomaticSteps();
    }
    public ProgressCatalog catalog() { return catalog; }
    public String roomId() { return roomId; }
    public Era era() { return catalog.rooms().get(roomId).era(); }
    public String locationTitle() { return catalog.rooms().get(roomId).title(); }
    public boolean has(String id) { return items.contains(id); }
    public boolean isItemUnread(String id) { return unreadItems.contains(id); }
    public boolean markItemRead(String id) { return unreadItems.remove(id); }
    private void acquire(String id) { if(items.add(id)) unreadItems.add(id); }
    public boolean flag(String id) { return flags.contains(id); }
    public boolean roomUnlocked(String id) { return unlockedRooms.contains(id); }
    public boolean done(String id) {
        if (id.equals("PZ005")) id = "M_DEVICE"; // Legacy alias; never a second progress entry.
        final String key = id;
        if (catalog.missions().stream().anyMatch(m -> m.id().equals(key)))
            return completed.containsAll(catalog.mission(key).completion());
        return completed.contains(id);
    }
    public boolean available(String id) {
        if(id.equals("ACT_FIRST_TRAVEL"))return false; // Retired action retained for save hydration only.
        if (!introScenario() && (id.equals("TUT001") || id.equals("ACT_FIRST_TRAVEL") || id.equals("ACT_START_FAILURE"))) return false;
        if (introPresent() && !Set.of("TUT001", "ACT_START_FAILURE").contains(id)) return false;
        // Only the retired direct-to-past opening remains gated. The current failure
        // scenario resumes normal repair and future unlock after reaching the past.
        if (firstTravelComplete() && !prototypeFailed() && Set.of("ACT_REPAIR", "GATE002").contains(id)) return false;
        ProgressCatalog.Step step = catalog.steps().get(id);
        return step != null && !completed.contains(id) && roomId.equals(step.room())
            && (step.mission().isBlank() || !missionLocked(step.mission()))
            && completed.containsAll(step.requires()) && items.containsAll(step.items());
    }
    public void complete(String id) {
        if (!catalog.steps().containsKey(id)) throw new IllegalArgumentException("Unknown step: " + id);
        if (completed.contains(id)) return;
        if (!available(id)) throw new IllegalStateException("선행 조건을 먼저 완료하세요: " + id);
        apply(catalog.steps().get(id));
        settleAutomaticSteps();
    }
    private void apply(ProgressCatalog.Step step) {
        if (!completed.add(step.id())) return;
        items.removeAll(step.consume());
        unreadItems.removeAll(step.consume());
        step.grant().forEach(this::acquire);
        flags.addAll(step.flags());
        unlockedRooms.addAll(step.unlock());
    }
    private void settleAutomaticSteps() {
        boolean changed;
        do {
            changed = false;
            for (ProgressCatalog.Step step : catalog.steps().values()) {
                if (step.automatic() && available(step.id())) { apply(step); changed = true; }
            }
        } while (changed);
    }
    public void collect(String id) {
        if (introPresent()) throw new IllegalStateException(introGoal());
        ProgressCatalog.Item item = catalog.items().get(id);
        if (item == null) throw new IllegalArgumentException("Unknown item: " + id);
        if (!roomId.equals(item.room()) || !completed.containsAll(item.requires()))
            throw new IllegalStateException("이 장소에서 아직 획득할 수 없는 아이템입니다: " + id);
        if (!item.blockedBy().isBlank() && completed.contains(item.blockedBy())) return;
        acquire(id);
        settleAutomaticSteps();
    }
    public boolean canEnter(String destination) {
        return unlockedRooms.contains(destination) && catalog.rooms().get(roomId).exits().contains(destination);
    }
    public void enterRoom(String destination) {
        if (!canEnter(destination)) throw new IllegalStateException("이동할 수 없는 방입니다: " + destination);
        roomId = destination;
        settleAutomaticSteps();
    }
    public boolean missionLocked(String id) {
        // Saves already inside the repair branch keep their earned progress.
        if(id.equals("M_DEVICE") && done("PZ008"))return false;
        return !completed.containsAll(catalog.mission(id).requires());
    }
    public int missionCompletedCount(String id) {
        return (int) catalog.missionSteps(id).stream().filter(s -> done(s.id())).count();
    }
    public Optional<ProgressCatalog.Step> nextStep(String mission) {
        List<ProgressCatalog.Step> pending = catalog.missionSteps(mission).stream().filter(s -> !done(s.id())).toList();
        // Select a ready dependency, not merely the next display-order number.
        return pending.stream().filter(s -> completed.containsAll(s.requires())).findFirst()
            .or(() -> pending.stream().findFirst());
    }
    public String missionNext(String id) {
        if(prototypeFailed() && powered() && id.equals("M_USB") && done("PZ006") && !done("PZ001")) return "컴퓨터 접근 퍼즐을 해결해 사고 로그를 복구하세요.";
        if(id.equals("M_USB") && powered() && !has("CIPHER_TABLE") && !done("PZ006"))return "연구 메모를 조사해 해독표를 획득하세요.";
        if (missionLocked(id)) return catalog.mission(id).requires().stream().filter(key -> !done(key))
            .sorted().map(key -> catalog.steps().get(key).prompt()).findFirst().orElse("");
        if (done(id)) return catalog.mission(id).finishedText();
        return nextStep(id).map(step -> !items.containsAll(step.items()) && !step.itemPrompt().isBlank()
            ? step.itemPrompt() : step.prompt()).orElse("");
    }
    public List<ProgressCatalog.Step> objectSteps(String object) {
        return catalog.steps().values().stream()
            .filter(s -> s.room().equals(roomId) && s.object().equals(object) && !done(s.id()))
            .sorted(Comparator.comparingInt(ProgressCatalog.Step::order).thenComparing(ProgressCatalog.Step::id)).toList();
    }
    public Optional<ProgressCatalog.Step> nextObjectStep(String object) {
        List<ProgressCatalog.Step> pending = objectSteps(object);
        return pending.stream().filter(s -> completed.containsAll(s.requires())).findFirst()
            .or(() -> pending.stream().findFirst());
    }

    /** Detached ID-based snapshot for inspection and save-file persistence. */
    public record Snapshot(int schemaVersion, String roomId, Set<String> completed,
                           Set<String> items, Set<String> flags, Set<String> unlockedRooms, Set<String> unreadItems) {
        public Snapshot(int version,String room,Set<String> completed,Set<String> items,Set<String> flags,Set<String> rooms) { this(version,room,completed,items,flags,rooms,Set.of()); }
        public Snapshot {
            completed = Set.copyOf(completed); items = Set.copyOf(items);
            flags = Set.copyOf(flags); unlockedRooms = Set.copyOf(unlockedRooms); unreadItems = Set.copyOf(unreadItems);
        }
    }
    public Snapshot snapshot() { return new Snapshot(2,roomId,completed,items,flags,unlockedRooms,unreadItems); }

    /** Restore values directly: never replay rewards, consumption or completion callbacks. */
    public static GameState restore(ProgressCatalog catalog, Snapshot snapshot) {
        if (snapshot.schemaVersion() != 1 && snapshot.schemaVersion() != 2) throw new IllegalArgumentException("Unsupported save version");
        if (!catalog.rooms().containsKey(snapshot.roomId())
            || !snapshot.unlockedRooms().contains(snapshot.roomId())
            || !snapshot.unlockedRooms().contains(catalog.initialRoom())
            || !catalog.rooms().keySet().containsAll(snapshot.unlockedRooms())
            || !catalog.steps().keySet().containsAll(snapshot.completed())
            || !catalog.items().keySet().containsAll(snapshot.items()))
            throw new IllegalArgumentException("Save references unavailable content");
        if (!snapshot.items().containsAll(snapshot.unreadItems()) || (snapshot.schemaVersion()==1 && !snapshot.unreadItems().isEmpty())) throw new IllegalArgumentException("Invalid unread items");
        Set<String> knownFlags = new HashSet<>();
        for (var step : catalog.steps().values()) knownFlags.addAll(step.flags());
        if (!knownFlags.containsAll(snapshot.flags())) throw new IllegalArgumentException("Unknown environment state");
        GameState result = new GameState(catalog);
        result.roomId = snapshot.roomId();
        result.completed.clear(); result.completed.addAll(snapshot.completed());
        result.items.clear(); result.items.addAll(snapshot.items());
        result.unreadItems.clear(); result.unreadItems.addAll(snapshot.unreadItems());
        result.flags.clear(); result.flags.addAll(snapshot.flags());
        result.unlockedRooms.clear(); result.unlockedRooms.addAll(snapshot.unlockedRooms());
        return result;
    }

    // Compatibility facade: existing UI and its design remain unchanged.
    public boolean powered() { return (flag("intro_power_on") && !prototypeFailed()) || flag("power_restored"); }
    public boolean usbComplete() { return done("PZ007"); }
    public boolean deviceComplete() { return done("M_DEVICE"); }
    public boolean canDepart() { return introPresent() ? available("ACT_START_FAILURE") : usbComplete() && deviceComplete(); }
    public String usbNext() { return missionNext("M_USB"); }
    public String deviceNext() { return missionNext("M_DEVICE"); }
    public boolean hasSparePart() { return has("SPARE"); }
    public boolean isWatchRepaired() { return flag("watch_repaired"); }
    public boolean isDiagnosisComplete() { return done("PZ001"); }
    public boolean hasCorePart() { return has("CORE"); }
    public boolean isCoreInstalled() { return flag("core_installed"); }
    public boolean isEscaped() { return flag("escaped"); }
    public void completeDiagnosis() { complete("PZ001"); }
    public void collectSparePart() { complete("PZ002"); }
    public void repairWatch() { complete("ACT_REPAIR"); }
    public void collectCorePart() { complete("PZ003"); }
    public void installCorePart() { complete("ACT_INSTALL"); }
    public void escape() { complete("ACT_ESCAPE"); }
    public void moveToPast() {
        if (introPresent()) { usePrototype(); return; }
        if (era() != Era.PRESENT) throw new IllegalStateException("현재에서만 과거로 이동할 수 있습니다.");
        enterRoom("past_1");
    }
    public void moveToFuture() {
        if (era() != Era.PAST) throw new IllegalStateException("과거에서만 미래로 이동할 수 있습니다.");
        enterRoom("future_1");
    }
}
