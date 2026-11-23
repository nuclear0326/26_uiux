package timeescape.puzzle.present;
/** Common rule contract; implementations have no Swing or global progress dependency. */
public interface PresentPuzzle {
 String id(); int size(); int value(int index); void turn(int index);
 String title(); String instruction(); String label(int index); boolean solved();
 default String status(){return "부품을 클릭하여 조절  ·  Tab으로 선택 / Space로 회전";}
}
