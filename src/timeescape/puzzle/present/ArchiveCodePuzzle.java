package timeescape.puzzle.present;
public final class ArchiveCodePuzzle extends CyclicValuesPuzzle {
 private static final int[] TARGET={7,1,3,5};
 public static boolean accepts(String value){return "7135".equals(value);}
 public boolean solved(){return matches(TARGET);}
 public ArchiveCodePuzzle(){super("PZ006",10,new int[4]);}
 public String title(){return "연구실 PC 로그인";}
 public String instruction(){return "계정 문양은 □ △ ☆ ○ 순서입니다. 보관함의 해독표를 참고해 비밀번호를 입력하세요.";}
 public String label(int i){return (i+1)+"번: "+value(i);}
}
