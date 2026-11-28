package timeescape.puzzle;
import java.util.*;
/** Session-only rules. Progress is committed by PuzzleRunner after both stages. */
public final class AccidentLogPuzzle {
 public record Record(String title,String clue,String normal,String failed) {}
 public static final List<Record> RECORDS=List.of(
  new Record("장치 가동", "전원 셀 접속 신호와 초기화 완료 표시가 남아 있다.", "전원 셀 연결 / 초기화 정상", "전원 셀 연결 / 초기화 정상"),
  new Record("목표 좌표 입력", "목표 좌표가 저장되었고, 입력 데이터 검사는 일치했다.", "목표 시간 좌표 입력 완료", "목표 시간 좌표 입력 완료"),
  new Record("좌표 보정 검사", "보정값 차이 +0.18과 출력 제한 해제 상태가 저장되었다.", "차이 +0.02 / 출력 제한 유지", "차이 +0.18 / 출력 제한 해제"),
  new Record("출력 상승", "출력 계측기에 정격의 180%가 기록되었다.", "정격 출력 100% / 안정", "정격 출력 180% / 과부하"),
  new Record("보호 회로 작동", "과부하 보호 신호가 켜져 있고 차단기 접점은 열려 있다.", "과부하 없음 / 회로 유지", "과부하 감지 / 차단기 개방"),
  new Record("연구실 정전", "연구실 전원이 차단되었고 이동 확인 신호는 기록되지 않았다.", "연구실 전원 정상 유지", "연구실 전원 상실 / 이동 실패")
 );
 private final List<Integer> order=new ArrayList<>(List.of(4,1,5,0,3,2));
 private int stage=1,selected=-1;
 public int stage(){return stage;} public List<Integer> order(){return List.copyOf(order);}
 public void move(int from,int to){if(stage!=1||from<0||to<0||from>=6||to>=6)return;Integer v=order.remove(from);order.add(to,v);}
 public void choose(int row){if(stage==2&&row>=0&&row<6)selected=row;}
 public int selected(){return selected;}
 public boolean verify(){if(stage==1){for(int i=0;i<6;i++)if(order.get(i)!=i)return false;stage=2;return true;}if(stage==2&&selected==2){stage=3;return true;}return false;}
 public void reset(){if(stage==1){order.clear();order.addAll(List.of(4,1,5,0,3,2));}else if(stage==2)selected=-1;}
 public void skipStage(){if(stage==1){order.clear();for(int i=0;i<6;i++)order.add(i);stage=2;}else if(stage==2){selected=2;stage=3;}}
}
