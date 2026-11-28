package timeescape.puzzle;

import java.util.List;

/** Ordered normal procedure and device observations for comparing the experiment. */
public final class AccidentLogEvidence {
    public record Entry(String title,String text) {}
    public static final List<Entry> PROCEDURE=List.of(
        new Entry("01. 장치 가동","전원 셀이 연결되고 장치 초기화가 완료됩니다."),
        new Entry("02. 목표 좌표 입력","이동할 시간 좌표가 저장되고, 입력 데이터가 정상인지 확인합니다."),
        new Entry("03. 좌표 보정 검사","목표값과 측정값의 차이를 확인합니다.\n정상 범위는 −0.05에서 +0.05까지이며, 범위를 벗어나면 출력을 높이지 않습니다."),
        new Entry("04. 출력 상승","검사를 통과하면 출력을 정격의 100%까지 높입니다.\n출력은 이 범위 안에서 안정적으로 유지되어야 합니다."),
        new Entry("05. 보호 회로 확인","과부하가 없으면 전원 연결을 유지합니다.\n과부하가 발생하면 차단기를 열어 장치와 연구실의 전원을 차단합니다."),
        new Entry("06. 최종 작동 확인","정상 작동 시 연구실의 전원은 유지됩니다.\n전원 차단이나 이동 실패가 기록되었다면 앞 단계에서 발생한 이상을 확인해야 합니다.")
    );
    // Grouped by device, deliberately not chronological.
    public static final List<Entry> INCIDENT=List.of(
        new Entry("출력 계측기","최대 출력 180% / 측정 종료 직전 값 보존"),
        new Entry("입력 저장부","목표 좌표 수신 완료 / 저장 데이터 검사 일치"),
        new Entry("전원 감시부","연구실 전원 차단 / 이동 확인 신호 없음"),
        new Entry("보정 제어부","보정값 차이 +0.18 / 출력 제한 해제"),
        new Entry("기동 기록부","전원 셀 접속 확인 / 초기화 완료"),
        new Entry("차단기 접점","접점 열림 / 과부하 보호 신호 기록")
    );
    private AccidentLogEvidence() {}
}
