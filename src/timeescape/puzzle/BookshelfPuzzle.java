package timeescape.puzzle;

import java.util.List;
import java.util.Set;

/** Rule model for restoring the four research volumes in the past-era archive. */
public final class BookshelfPuzzle {
    public static final String SCHEMATIC = "SCHEMATIC";
    public static final String OBSERVATION = "OBSERVATION";
    public static final String TIME_EXPERIMENT = "TIME_EXPERIMENT";
    public static final String INCIDENT_REPORT = "INCIDENT_REPORT";
    public static final String GARDEN_GUIDE = "GARDEN_GUIDE";
    private static final List<String> ORDER = List.of(SCHEMATIC, OBSERVATION, TIME_EXPERIMENT, INCIDENT_REPORT);
    private static final Set<String> VALID = Set.of(SCHEMATIC, OBSERVATION, TIME_EXPERIMENT, INCIDENT_REPORT, GARDEN_GUIDE);
    private final String[] slots = new String[4];

    public static List<String> books() { return List.of(SCHEMATIC, OBSERVATION, TIME_EXPERIMENT, INCIDENT_REPORT, GARDEN_GUIDE); }
    public static List<String> solution() { return ORDER; }
    public static String title(String id) {
        return switch (id) {
            case SCHEMATIC -> "장치 도면";
            case OBSERVATION -> "관측 기록";
            case TIME_EXPERIMENT -> "시간 실험";
            case INCIDENT_REPORT -> "사고 보고서";
            case GARDEN_GUIDE -> "식물 도감";
            default -> "알 수 없는 책";
        };
    }
    public static String symbol(String id) {
        return switch (id) {
            case SCHEMATIC -> "⚙";
            case OBSERVATION -> "☆";
            case TIME_EXPERIMENT -> "⌛";
            case INCIDENT_REPORT -> "△";
            case GARDEN_GUIDE -> "❧";
            default -> "?";
        };
    }
    public static String detail(String id) {
        return switch (id) {
            case SCHEMATIC -> "제어 장치의 회로와 부품 배치를 그린 도면입니다. 표지 안쪽에 황동 톱니 도장이 있습니다.";
            case OBSERVATION -> "실험 전후의 계기 수치가 기록된 관측 일지입니다. 별 모양 책갈피가 꽂혀 있습니다.";
            case TIME_EXPERIMENT -> "시간 좌표를 다룬 연구서입니다. 모래시계 문양이 표지에 새겨져 있습니다.";
            case INCIDENT_REPORT -> "장치 이상과 중단 경위를 기록한 보고서입니다. 경고 삼각형이 표지에 찍혀 있습니다.";
            case GARDEN_GUIDE -> "연구실 화분과 식물을 정리한 도감입니다. 실험 자료가 아닌 듯합니다.";
            default -> "";
        };
    }

    public String at(int slot) { checkSlot(slot); return slots[slot]; }
    public boolean contains(String id) {
        for (String placed : slots) if (id.equals(placed)) return true;
        return false;
    }
    public boolean take(String id) {
        if (!VALID.contains(id)) return false;
        for (int i = 0; i < slots.length; i++) if (id.equals(slots[i])) { slots[i] = null; return true; }
        return true;
    }
    public boolean place(int slot, String id) {
        checkSlot(slot);
        if (!VALID.contains(id)) return false;
        take(id);
        slots[slot] = id;
        return true;
    }
    public boolean full() {
        for (String id : slots) if (id == null) return false;
        return true;
    }
    public boolean solved() {
        if (!full()) return false;
        for (int i = 0; i < slots.length; i++) if (!ORDER.get(i).equals(slots[i])) return false;
        return true;
    }
    public void reset() { java.util.Arrays.fill(slots, null); }
    public static String hint(int level) {
        return switch (level) {
            case 0 -> "관측 기록은 왼쪽 끝에서 두 번째 칸입니다.";
            case 1 -> "시간 실험서는 사고 보고서 바로 왼쪽에 놓입니다.";
            default -> "장치 도면은 왼쪽 끝입니다. 식물 도감은 연구 자료가 아닙니다.";
        };
    }
    private static void checkSlot(int slot) {
        if (slot < 0 || slot >= 4) throw new IndexOutOfBoundsException("slot: " + slot);
    }
}
