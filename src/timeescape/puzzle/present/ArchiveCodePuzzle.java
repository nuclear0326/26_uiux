package timeescape.puzzle.present;
public final class ArchiveCodePuzzle extends CyclicValuesPuzzle {
 private static final int[] TARGET={4,3,2,1};
 public boolean solved(){return matches(TARGET);}
 public ArchiveCodePuzzle(){super("PZ006",10,new int[4]);}
 public String title(){return "연구 기록 복호화";}
 public String instruction(){return "메모: △=3, ○=1, □=4, ☆=2. 계정 암호는 □ △ ☆ ○ 순서입니다. 각 숫자를 눌러 변경하세요.";}
 public String label(int i){return (i+1)+"번: "+value(i);}
}
