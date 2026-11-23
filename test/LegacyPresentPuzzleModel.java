package timeescape.puzzle;

import java.util.Arrays;

/** Small deterministic puzzles. Progress is committed only after a solved check. */
public final class LegacyPresentPuzzleModel {
    public final String id;
    private final int[] values;
    private static final int[] POWER = {2, 3, 5, 7, 11};
    public LegacyPresentPuzzleModel(String id) {
        this.id = id;
        values = switch(id) {
            case "PZ004" -> new int[5];
            case "PZ006" -> new int[4];
            case "PZ008" -> new int[]{2, 0, 3, 1};
            case "PZ010" -> new int[]{1, 2, 0};
            case "PZ011" -> new int[]{1, 2, 3};
            default -> throw new IllegalArgumentException(id);
        };
    }
    public int size() { return values.length; }
    public int value(int i) { return values[i]; }
    public void turn(int i) {
        if (i < 0 || i >= values.length) throw new IllegalArgumentException();
        int modulo = switch(id) { case "PZ004" -> 2; case "PZ006" -> 10; default -> 4; };
        values[i] = (values[i] + 1) % modulo;
        if (id.equals("PZ011")) values[(i + 1) % 3] = (values[(i + 1) % 3] + 1) % 4;
    }
    public int power() { int sum=0; for(int i=0;i<5;i++) sum+=POWER[i]*values[i]; return sum; }
    public String title() { return switch(id) {
        case "PZ004" -> "전력 복구"; case "PZ006" -> "연구 기록 복호화";
        case "PZ008" -> "설계도 복원"; case "PZ010" -> "모듈 조립"; default -> "시간 좌표 보정";
    }; }
    public String instruction() { return switch(id) {
        case "PZ004" -> "스위치를 켜거나 끄며 전력 합계를 정확히 16으로 맞추세요. 잘못된 합계로 확인해도 다시 조절할 수 있습니다.";
        case "PZ006" -> "메모: △=3, ○=1, □=4, ☆=2. 계정 암호는 □ △ ☆ ○ 순서입니다. 각 숫자를 눌러 변경하세요.";
        case "PZ008" -> "찢어진 설계도 네 조각을 회전하세요. 연결선이 [→ ↓ ← ↑] 순서가 되면 폐회로가 완성됩니다.";
        case "PZ010" -> "부품 커넥터 방향을 소켓에 맞추세요. 케이스 소켓 ↑ / 전원 셀 소켓 → / 제어 모듈 소켓 ↓. 각 부품을 눌러 회전하세요.";
        default -> "세 링을 모두 ↑(0)에 맞추세요. 링을 누르면 해당 링과 바로 다음 링이 함께 90도 회전합니다. C의 다음은 A입니다.";
    }; }
    public String label(int i) {
        if(id.equals("PZ004")) return POWER[i] + " 전력: " + (values[i]==1 ? "ON" : "OFF");
        if(id.equals("PZ006")) return (i+1) + "번: " + values[i];
        String[] arrow={"↑", "→", "↓", "←"};
        String prefix=id.equals("PZ010") ? new String[]{"케이스", "전원 셀", "제어 모듈"}[i] : (i+1)+"";
        return prefix + " : " + arrow[values[i]];
    }
    public boolean solved() { return switch(id) {
        case "PZ004" -> power()==16;
        case "PZ006" -> Arrays.equals(values,new int[]{4,3,2,1});
        case "PZ008" -> Arrays.equals(values,new int[]{1,2,3,0});
        case "PZ010" -> Arrays.equals(values,new int[]{0,1,2});
        default -> Arrays.equals(values,new int[]{0,0,0});
    }; }
}
