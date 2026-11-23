package timeescape.puzzle.present;
public final class ModuleAssemblyPuzzle extends CyclicValuesPuzzle {
 private static final int[] TARGET={0,1,2};
 public boolean solved(){return matches(TARGET);}
 public ModuleAssemblyPuzzle(){super("PZ010",4,new int[]{1,2,0});}
 public String title(){return "모듈 조립";}
 public String instruction(){return "부품 커넥터 방향을 소켓에 맞추세요. 케이스 소켓 ↑ / 전원 셀 소켓 → / 제어 모듈 소켓 ↓. 각 부품을 눌러 회전하세요.";}
 public String label(int i){return new String[]{"케이스","전원 셀","제어 모듈"}[i]+" : "+direction(i);}
}
