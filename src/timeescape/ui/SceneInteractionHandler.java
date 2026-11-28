package timeescape.ui;
import java.util.Objects;
import timeescape.core.GameState;
import timeescape.model.Era;

/** Scene clicks and pickup routing; puzzle completion still belongs to PuzzleRunner. */
final class SceneInteractionHandler {
    private final InteractionHost host;
    SceneInteractionHandler(InteractionHost host){this.host=Objects.requireNonNull(host);}
    void interact(String id) {
        if(host.blocked())return;
        GameState state=host.state();
        if (state.introPresent()) {
            if (id.equals("CASE") && !state.has("DEVICE")) {
                state.takePrototype();
                host.play("item_pickup");
                host.log("시간이동장치를 꺼냈습니다. 연구 노트의 보관함에서 장치를 사용하세요.");
                host.refresh();
            } else host.log(id.equals("POWER") ? "연구실 전원이 켜져 있습니다. " + state.introGoal() : state.introGoal());
            return;
        }
        if (state.era() == Era.PRESENT && !state.powered() && !id.equals("POWER")) {
            host.log("전력이 없습니다. 벽면 철제 배전함을 먼저 조사하세요.");
            return;
        }
        if (id.equals("MEMO") && state.era()==Era.PRESENT) {
            if(!state.done("ACT_GET_CIPHER")){state.complete("ACT_GET_CIPHER");host.play("item_pickup");host.log("메모 해독표를 획득했습니다. PC에서 비밀번호를 입력하세요.");host.refresh();}
            host.openMemo();return;
        }
        if (id.equals("PAST_MEMO") && state.era()==Era.PAST) {
            host.log("연구 메모: 관측 기록은 왼쪽 끝에서 두 번째. 시간 실험서는 사고 보고서 바로 왼쪽. 장치 도면은 왼쪽 끝. 식물 도감은 연구 자료가 아닙니다.");
            return;
        }
        var item = state.catalog().items().get(id);
        if (item != null && state.roomId().equals(item.room())) {
            if(!state.canCollect(id)){
                host.log("사고 로그 분석을 마친 뒤 수리에 필요한 부품을 확인하세요.");
                return;
            }
            state.collect(id);
            host.log("아이템을 확인했습니다. 연구 노트의 보유 아이템과 현재 목표를 확인하세요.");
            host.refresh();
            return;
        }
        var pending = state.nextObjectStep(id);
        if (pending.isEmpty()) {
            host.log(switch (id) {
                case "POWER" -> "전력이 복구되어 있습니다.";
                case "MEMO" -> "연구 계정 암호 해독 완료. 컴퓨터를 조사하세요.";
                case "PRESENT_PC" -> "사고 로그 분석이 완료되었습니다. " + state.deviceNext();
                case "WORKBENCH" -> "장치 제작 완료. " + state.usbNext();
                case "PAST_SPARE" -> "황동 장치: 수리에 사용할 만한 부품은 이미 챙겼습니다.";
                case "PAST_BOOKSHELF" -> "서가의 연구 기록은 이미 복원했습니다. 책상 위 메모도 다시 확인할 수 있습니다.";
                case "FUTURE_CORE" -> "시간 실험 장치: 필요한 부품을 이미 확보했습니다.";
                default -> "조사할 수 없는 대상입니다.";
            });
            return;
        }
        var step = pending.get();
        if (!state.available(step.id()) || step.automatic()) {
            if (!step.mission().isBlank()) host.log(state.missionNext(step.mission()));
            else host.log(step.prompt().isBlank() ? "선행 조건을 먼저 완료하세요." : step.prompt());
            return;
        }
        if(host.runPuzzle(step.id()) != timeescape.puzzle.PuzzleRunner.Outcome.COMPLETED) host.refresh();
    }

}
