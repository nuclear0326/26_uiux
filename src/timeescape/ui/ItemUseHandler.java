package timeescape.ui;
import java.util.Objects;
import timeescape.core.GameState;
import timeescape.model.Era;

/** Inventory actions. Delayed callbacks only affect the session that opened them. */
final class ItemUseHandler {
    private final InteractionHost host;
    ItemUseHandler(InteractionHost host){this.host=Objects.requireNonNull(host);}
    void use(String id) {
        if(host.blocked())return;
        GameState state=host.state();
        switch (id) {
            case "CIPHER_TABLE" -> { if(state.has(id))host.openMemo(); }
            case "SPARE" -> {
                host.selectSpare(!host.spareSelected());
                host.play("file_select");
                host.log(host.spareSelected() ? "대체 부품 선택됨. 손목 시간여행 장치를 클릭하면 수리할 수 있습니다." : "대체 부품 선택을 해제했습니다.");
                host.refresh();
            }
            case "CORE" -> { host.play("file_select"); host.log("미래 핵심 부품을 확보했습니다. 손목 장치를 클릭해 장착할 수 있습니다."); }
            case "FAULT_REPORT" -> host.log("분석 결과: 좌표 보정값 불일치로 출력 제한이 해제되어 과부하와 정전이 발생했습니다. 작업대에서 설계도를 복원하고 제어 장치를 수리하세요.");
            case "USB" -> {
                if(state.available("PZ021")){host.closeNotebook();host.runPuzzle("PZ021");}
                else host.log(state.done("PZ021")?"사고 분석이 완료되었습니다. USB는 이후에도 사용할 수 있도록 보관합니다.":state.usbComplete()?"현재 연구실의 PC에서 로그를 분석하세요.":"컴퓨터에서 복구 데이터를 USB에 복사하세요.");
            }
            case "DEVICE" -> {
                if (!state.has("DEVICE")) { host.log("먼저 시간이동장치를 획득하세요."); return; }
                if (state.introPresent() || state.firstTravelComplete()) {
                    Era before = state.era();
                    host.openDevice(state, host.spareSelected(), () -> {
                        if(!host.isCurrent(state))return;
                        if (before != state.era()) {
                            host.closeNotebook();
                            host.audioEra(state.era());
                            host.log("장치가 고장나 예상과 다른 과거에 도착했습니다.");
                        }
                        host.refresh();
                    }, () -> {
                        if(!host.isCurrent(state))return;
                        GameState departing=state;
                        host.startFailure(()->{
                            if(!host.isCurrent(departing))return;
                            state.usePrototype();
                            host.selectSpare(false);
                            host.audioEra(state.era());
                            host.log(state.introGoal());
                            host.refresh();
                            host.showFailureDialogue();
                        });
                    });
                    return;
                }
                host.play("ui_open");
                Era beforeEra = state.era();
                boolean repairedBefore = state.isWatchRepaired();
                boolean installedBefore = state.isCoreInstalled();
                boolean escapedBefore = state.isEscaped();
                host.openDevice(state, host.spareSelected(), () -> {
                        if(!host.isCurrent(state))return;
                    host.save();
                    host.selectSpare(false);
                    if (!escapedBefore && state.isEscaped()) {
                        host.play("ending");
                        host.log("최종 좌표 입력 성공! 붕괴하는 미래 연구실에서 탈출했습니다.");
                        host.showEnding();
                    } else if (!installedBefore && state.isCoreInstalled()) {
                        host.play("device_repair");
                        host.log("미래 핵심 부품을 장착했습니다. 장치를 다시 열어 최종 좌표를 입력하세요.");
                    } else if (beforeEra == Era.PRESENT && state.era() == Era.PAST) {
                        host.play("time_travel");
                        host.audioEra(state.era());
                        host.log("과거 연구실로 이동했습니다. 작업대 위 황동 장치를 조사해 보세요.");
                    } else if (!repairedBefore && state.isWatchRepaired()) {
                        host.play("device_repair");
                        host.log("손목 장치를 임시 수리했습니다. 장치를 다시 열어 미래로 이동하세요.");
                    } else if (beforeEra == Era.PAST && state.era() == Era.FUTURE) {
                        host.play("time_travel");
                        host.audioEra(state.era());
                        host.log("미래 연구실에 도착했습니다. 중앙 시간 실험 장치를 조사하세요.");
                    }
                    host.refresh();
                }, null);
            }
            default -> { }
        }
    }}
