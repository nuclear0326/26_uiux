package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.FrequencyPuzzle;
/** Example only: not registered in the production game. */
public final class FrequencyPuzzlePanel extends JPanel {
 private final FrequencyPuzzle model=new FrequencyPuzzle();
 private final JLabel value=new JLabel("",SwingConstants.CENTER);
 private final JLabel status=new JLabel("다이얼을 조절하세요.");
 private final Runnable solvedCallback,closeCallback;
 private boolean finished;
 public FrequencyPuzzlePanel(Runnable onSolved,Runnable onClose){
  super(new BorderLayout(12,18));solvedCallback=onSolved;closeCallback=onClose;
  PuzzleChrome.decorate(this);
  add(PuzzleChrome.header("주파수 맞추기","현재 / 연구실","눈금을 7에 맞추세요. 예제에서는 정답을 공개합니다.",this::cancel),BorderLayout.NORTH);
  JPanel board=new JPanel(new BorderLayout(12,12));board.setOpaque(false);
  value.setFont(GameFonts.title(52));value.setForeground(LabTheme.TEXT);board.add(value,BorderLayout.CENTER);
  JPanel controls=new JPanel(new FlowLayout());controls.setOpaque(false);
  controls.add(button("감소",()->{model.turn(-1);refresh();}));controls.add(button("증가",()->{model.turn(1);refresh();}));board.add(controls,BorderLayout.SOUTH);add(board,BorderLayout.CENTER);
  JPanel footer=new JPanel(new BorderLayout(0,12));footer.setOpaque(false);status.setFont(GameFonts.body(15));status.setForeground(LabTheme.MUTED);footer.add(status,BorderLayout.NORTH);
  JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT));actions.setOpaque(false);actions.add(button("다시 맞추기",()->{model.reset();status.setText("다이얼을 조절하세요.");refresh();}));actions.add(button("조사 종료",this::cancel));actions.add(button("작동 확인",()->{if(model.solved())finish();else status.setText("아직 주파수가 맞지 않습니다.");}));footer.add(actions,BorderLayout.SOUTH);add(footer,BorderLayout.SOUTH);refresh();
 }
 private JButton button(String name,Runnable action){JButton b=new JButton(name);PuzzleChrome.styleButton(b);b.addActionListener(e->{if(!finished)action.run();});return b;}
 private void refresh(){value.setText(Integer.toString(model.value()));}
 private void finish(){if(finished)return;finished=true;solvedCallback.run();}
 public void cancel(){if(finished)return;finished=true;closeCallback.run();}
 public void cancelSession(){finished=true;/* Stop owned timers/workers here if added. */}
 public void developerSkip(){finish();}
}
