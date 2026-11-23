package timeescape.puzzle.present;
public final class TimeCoordinatesPuzzle extends CyclicValuesPuzzle {
 private static final int[] TARGET={0,0,0};
 public boolean solved(){return matches(TARGET);}
 public TimeCoordinatesPuzzle(){super("PZ011",4,new int[]{1,2,3});}
 public String title(){return "시간 좌표 보정";}
 public String instruction(){return "세 링을 모두 ↑(0)에 맞추세요. 링을 누르면 해당 링과 바로 다음 링이 함께 90도 회전합니다. C의 다음은 A입니다.";}
 @Override protected void afterTurn(int i){advance((i+1)%size());}
 public String label(int i){return (i+1)+" : "+direction(i);}
}
