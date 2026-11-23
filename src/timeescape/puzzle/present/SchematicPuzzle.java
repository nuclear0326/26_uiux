package timeescape.puzzle.present;
public final class SchematicPuzzle extends CyclicValuesPuzzle {
 private static final int[] TARGET={1,2,3,0};
 public boolean solved(){return matches(TARGET);}
 public SchematicPuzzle(){super("PZ008",4,new int[]{2,0,3,1});}
 public String title(){return "설계도 복원";}
 public String instruction(){return "찢어진 설계도 네 조각을 회전하세요. 연결선이 [→ ↓ ← ↑] 순서가 되면 폐회로가 완성됩니다.";}
 public String label(int i){return (i+1)+" : "+direction(i);}
}
