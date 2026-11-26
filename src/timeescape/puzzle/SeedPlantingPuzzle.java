package timeescape.puzzle;

/**
 * 과거 연구실에서 씨앗을 심는 퍼즐 규칙.
 * Swing이나 GameState에 직접 의존하지 않습니다.
 */
public final class SeedPlantingPuzzle {
    public enum Seed {
        BLUE_BLOOM("푸른 씨앗"),
        RED_BLOOM("붉은 씨앗");

        private final String displayName;

        Seed(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public enum WaterAmount {
        LITTLE("조금"),
        NORMAL("보통"),
        MUCH("많이");

        private final String displayName;

        WaterAmount(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private Seed selectedSeed;
    private WaterAmount selectedWater;
    private boolean planted;

    // 첫 버전은 한 가지 올바른 조건만 두어 짧고 명확하게 만듭니다.
    private static final Seed CORRECT_SEED = Seed.BLUE_BLOOM;
    private static final WaterAmount CORRECT_WATER = WaterAmount.NORMAL;

    public void selectSeed(Seed seed) {
        selectedSeed = seed;
    }

    public void selectWater(WaterAmount water) {
        selectedWater = water;
    }

    public boolean canPlant() {
        return selectedSeed != null && selectedWater != null && !planted;
    }

    public boolean plant() {
        if (!canPlant()) {
            return false;
        }

        if (selectedSeed != CORRECT_SEED || selectedWater != CORRECT_WATER) {
            return false;
        }

        planted = true;
        return true;
    }

    public boolean isPlanted() {
        return planted;
    }

    public void reset() {
        selectedSeed = null;
        selectedWater = null;
        planted = false;
    }
}
