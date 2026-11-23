package timeescape.core;

import timeescape.model.Era;

/** 인벤토리 손목 장치를 통해서만 실행되는 시간 이동 규칙. */
public final class TimeTravelController {
    private final GameState state;

    public TimeTravelController(GameState state) { this.state = state; }

    public boolean canTravel() {
        return (state.era() == Era.PRESENT && state.canDepart())
            || (state.era() == Era.PAST && state.isWatchRepaired());
    }

    public String destinationLabel() {
        if (state.era() == Era.PRESENT) return "저장된 시간 좌표: 과거";
        if (state.era() == Era.PAST) return "수정 좌표: 미래";
        return "최종 좌표 입력 및 탈출";
    }

    public void travel() {
        if (state.era() == Era.PRESENT) state.moveToPast();
        else if (state.era() == Era.PAST) state.moveToFuture();
        else throw new IllegalStateException("미래에서는 일반 시간 이동이 불가능합니다.");
    }
}
