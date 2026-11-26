package timeescape.ui;

import java.util.Objects;
import javax.swing.JLabel;
import timeescape.core.GameState;
import timeescape.model.Era;

/** Refreshes existing views on the EDT. Does not own progress or request saves.
 * The current state is passed on each call so loading a save cannot leave stale references.
 */
final class GameViewUpdater {
    private final GameScenePanel scene;
    private final MissionPanel missions;
    private final GameHud hud;
    private final InventoryPanel inventory;
    private final JLabel progress;

    GameViewUpdater(GameScenePanel scene, MissionPanel missions, GameHud hud,
                    InventoryPanel inventory, JLabel progress) {
        this.scene=Objects.requireNonNull(scene);
        this.missions=Objects.requireNonNull(missions);
        this.hud=Objects.requireNonNull(hud);
        this.inventory=Objects.requireNonNull(inventory);
        this.progress=Objects.requireNonNull(progress);
    }

    void refresh(GameState state, boolean spareSelected) {
        Objects.requireNonNull(state);
        scene.setEra(state.era());
        scene.setProgressHints(state);
        scene.setPowered(state.powered());
        missions.refresh(state);
        hud.setLocation(state.era().title(), state.locationTitle().replaceFirst("^.*? · ", ""));
        progress.setText("    " + stageText(state));
        inventory.refresh(state, spareSelected);
    }

    private static String stageText(GameState state) {
        if (state.introScenario()) return state.introGoal();
        if (state.isEscaped()) return "탈출 완료";
        if (state.era() == Era.PRESENT) return state.canDepart() ? "장치 수리 완료 / 과거 이동 가능" : state.powered() ? "사고 로그 확보 → 분석 → 장치 수리" : "벽면 철제 배전함에서 전력 복구 필요";
        if (state.era() == Era.PAST) return state.isWatchRepaired() ? "장치 복구 완료 · 미래 이동 가능" : "대체 부품으로 장치 수리 필요";
        return state.isCoreInstalled() ? "최종 좌표 입력 가능" : "미래 핵심 부품 확보 및 수리";
    }

}
