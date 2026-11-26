package timeescape.ui;
import java.awt.*;import javax.swing.*;import timeescape.puzzle.AccidentLogPuzzle;
/** Actual shared-host panel, also renderable offscreen without launching the game. */
public final class AccidentLogPanel extends RepairPuzzlePanel {
 final AccidentLogPuzzle model=new AccidentLogPuzzle();
 final JPanel rows=new JPanel(new GridLayout(6,1,0,7));
 final JLabel phase=new JLabel(); final JButton reset=new JButton("다시 맞추기");
 public AccidentLogPanel(Runnable solved,Runnable close){super(solved,close);
  JPanel board=new JPanel(new BorderLayout(0,12));board.setOpaque(false);rows.setOpaque(false);
  phase.setFont(GameFonts.button(18));phase.setForeground(LabTheme.GOLD);board.add(phase,BorderLayout.NORTH);board.add(rows,BorderLayout.CENTER);
  build("사고 로그 분석","현재 / USB 기록 분석","기록의 단서를 읽고 순서를 복원하세요. 이후 정상 기록과 대조해 최초 이상을 찾습니다.",board,reset);
  action.addActionListener(e->verify());reset.addActionListener(e->{model.reset();refreshRows();});refreshRows();
 }
 private JLabel label(String text,int size,Color color){JLabel l=new JLabel(text);l.setFont(GameFonts.body(size));l.setForeground(color);return l;}
 void verify(){if(isFinished())return;if(model.verify()){if(model.stage()==3){solve();return;}refreshRows();}else status.setText(model.stage()==1?"순서가 맞지 않습니다. 각 기록의 이전 / 이후 단서를 다시 확인하세요.":"처음 이상이 발생한 기록을 선택하세요. 출력 상승과 정전은 이후의 결과입니다.");}
 void refreshRows(){rows.removeAll();boolean ordering=model.stage()==1;
  phase.setText(ordering?"01 / 사고 기록 순서 복원":"02 / 정상 기록과 실패 기록 대조");
  action.setText(ordering?"순서 확인":"분석 완료");status.setText(ordering?"위 / 아래 버튼으로 기록을 옮기세요. 제한 시간은 없습니다.":"처음 달라진 기록을 선택하세요. 왼쪽은 정상 기록, 오른쪽은 사고 기록입니다.");
  for(int index=0;index<6;index++){final int row=index;var record=AccidentLogPuzzle.RECORDS.get(ordering?model.order().get(index):index);
   JPanel card=new JPanel(new BorderLayout(12,3));card.setBackground(LabTheme.PANEL);
   card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(model.selected()==row?LabTheme.GOLD:new Color(69,82,82)),BorderFactory.createEmptyBorder(7,12,7,12)));
   if(ordering){JPanel copy=new JPanel(new GridLayout(2,1));copy.setOpaque(false);copy.add(label(String.format("%02d   %s",index+1,record.title()),17,LabTheme.TEXT));copy.add(label(record.clue(),14,LabTheme.MUTED));card.add(copy);
    JPanel arrows=new JPanel(new GridLayout(1,2,5,0));arrows.setOpaque(false);JButton up=new JButton("↑"),down=new JButton("↓");for(JButton b:new JButton[]{up,down})PuzzleChrome.styleButton(b);up.setEnabled(row>0);down.setEnabled(row<5);up.addActionListener(e->{model.move(row,row-1);refreshRows();});down.addActionListener(e->{model.move(row,row+1);refreshRows();});arrows.add(up);arrows.add(down);card.add(arrows,BorderLayout.EAST);
   }else{JButton choose=new JButton((model.selected()==row?"✓ ":"")+record.title());choose.setPreferredSize(new Dimension(175,40));PuzzleChrome.styleButton(choose);choose.addActionListener(e->{model.choose(row);refreshRows();});card.add(choose,BorderLayout.WEST);
    JPanel compare=new JPanel(new GridLayout(1,2,15,0));compare.setOpaque(false);compare.add(label(record.normal(),14,new Color(177,202,195)));compare.add(label(record.failed(),14,LabTheme.TEXT));card.add(compare);}
   rows.add(card);
  }revalidate();repaint();
 }
 @Override protected void skipStage(){model.skipStage();if(model.stage()==3)solve();else refreshRows();}
}
