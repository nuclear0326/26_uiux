package timeescape.ui;
import timeescape.core.GameState;
import timeescape.core.TimeTravelController;
import timeescape.model.Era;

/** Existing device rules, independent of window creation for regression verification. */
final class DeviceActions {
    private final GameState state;
    private final boolean spareSelected;
    private final Runnable close,onChange,introTravel;
    private boolean closed;
    DeviceActions(GameState state,boolean selected,Runnable close,Runnable changed,Runnable intro){
        this.state=state;spareSelected=selected;this.close=close;onChange=changed;introTravel=intro;
    }
    void cancel(){closed=true;}
    void activate(){
        if(closed||!isActionEnabled())return;
        closed=true;
        if(state.introPresent()&&introTravel!=null){close.run();introTravel.run();return;}
        execute();close.run();onChange.run();
    }
    String describe() {
        if(state.prototypeFailed() && state.era()==Era.PAST && !state.isWatchRepaired()) return state.hasSparePart()
            ? "대체 부품: 확보됨\n\n보관함에서 대체 부품을 선택한 뒤 장착하세요. 장착하면 미래 좌표가 개방됩니다."
            : "과거 좌표 이동 완료\n\n미래 좌표를 개방하려면 추가 부품이 필요합니다. 작업대의 황동 장치를 조사해 대체 부품을 확보하세요.";
        if (state.introPresent()) return "장치 상태: 시험 작동 대기\n\n상자에서 꺼낸 시간이동장치입니다. 사용 버튼을 눌러 시험 작동을 시작하세요.";
        if(state.prototypeFailed() && state.era()==Era.PRESENT) return state.canDepart()?"로그 분석 및 장치 수리 완료\n\n과거 좌표로 이동할 준비가 되었습니다.":"장치 상태: 시험 작동 실패\n현재 위치: 현재 연구실\n\n전원을 복구하고 컴퓨터의 사고 로그를 확보한 뒤 작업대에서 장치를 수리하세요.";
        if (state.firstTravelComplete()) return "장치 상태: 이동 중 고장\n도착 시점: 과거\n\n설정과 다른 시간에 도착했습니다. 이동 기능이 손상되어 장치 수리가 필요합니다.";
        if (state.era() == Era.PRESENT) {
            return "USB 확보: " + (state.usbComplete() ? "완료" : "미완료")
                + "\n장치 제작: " + (state.deviceComplete() ? "완료" : "미완료")
                + "\n\n" + (state.canDepart() ? "사고 분석과 장치 수리 완료. 과거로 이동할 수 있습니다." : "사고 로그 분석 후 장치 수리를 마쳐야 과거로 이동할 수 있습니다.");
        }
        if (state.era() == Era.PAST && !state.isWatchRepaired()) {
            return "장치 상태: 심각하게 파손됨\n과거 좌표 이동: 완료\n\n과거 작업대의 황동 장치에서 대체 부품을 찾고, 인벤토리에서 부품을 선택한 뒤 이 장치를 다시 클릭하세요.";
        }
        if (state.era() == Era.PAST) {
            return "장치 상태: 임시 복구 완료\n사용 가능한 기능: 수정된 미래 좌표로 이동\n\n이제 미래의 연구실로 이동할 수 있습니다.";
        }
        return state.isCoreInstalled()
            ? "핵심 부품: 장착 완료\n장치 상태: 최종 복구\n\n최종 시간 좌표를 입력하여 연구실을 탈출하세요."
            : state.hasCorePart()
                ? "핵심 부품: 확보됨\n장치 상태: 추가 수리 필요\n\n확보한 부품을 손목 장치에 장착할 수 있습니다."
                : "장치 상태: 미래 핵심 부품 필요\n연구실 상태: 사고 직후 봉쇄\n\n중앙 시간 실험 장치에서 필요한 핵심 부품을 확보하세요.";
    }

    String actionName() {
        if (state.introPresent()) return "사용";
        if (state.firstTravelComplete()) return "수리 필요";
        if (state.era() == Era.PRESENT) return "과거로 시간 이동";
        if (state.era() == Era.PAST && !state.isWatchRepaired()) return "대체 부품 장착";
        if (state.era() == Era.PAST) return "미래로 시간 이동";
        if (state.isCoreInstalled()) return "최종 좌표 입력 및 탈출";
        return "미래 핵심 부품 장착";
    }

    boolean isActionEnabled() {
        if (state.introPresent()) return state.canDepart();
        if (state.firstTravelComplete()) return false;
        if (state.era() == Era.PRESENT) return state.canDepart();
        if (state.era() == Era.PAST && !state.isWatchRepaired()) return state.available("ACT_REPAIR") && spareSelected;
        if (state.era() == Era.PAST) return state.canEnter("future_1");
        return state.hasCorePart() || state.isCoreInstalled();
    }

    private void execute() {
        if (state.era() == Era.PRESENT) new TimeTravelController(state).travel();
        else if (state.era() == Era.PAST && !state.isWatchRepaired()) state.repairWatch();
        else if (state.era() == Era.PAST) new TimeTravelController(state).travel();
        else if (!state.isCoreInstalled()) state.installCorePart();
        else state.escape();
    }
}
