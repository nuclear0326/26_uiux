package timeescape.ui;
import java.awt.*;import javax.swing.*;import javax.swing.text.*;
import timeescape.puzzle.present.ArchiveCodePuzzle;
/** PC-only login. The owned memo supplies the mapping, never the answer here. */
public final class ArchiveLoginPanel extends JPanel {
 final JTextField password=new JTextField(4);
 final JLabel status=new JLabel("해독표의 문양을 숫자로 바꾸어 입력하세요.",SwingConstants.CENTER);
 final JButton submit=LabTheme.button("로그인");
 private boolean finished;
 public ArchiveLoginPanel(Runnable solved,Runnable close,Runnable reference){
  super(new BorderLayout(0,20));PuzzleChrome.decorate(this);
  add(PuzzleChrome.header("연구실 PC 로그인","현재 / 연구실","보관함에서 획득한 해독표를 참고해 네 자리 비밀번호를 입력하세요.",close),BorderLayout.NORTH);
  JPanel center=new JPanel(new GridBagLayout());center.setOpaque(false);GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=0;c.insets=new Insets(10,12,10,12);c.fill=GridBagConstraints.HORIZONTAL;
  JLabel symbols=new JLabel("□   △   ☆   ○",SwingConstants.CENTER);symbols.setFont(GameFonts.body(54));symbols.setForeground(LabTheme.TEXT);center.add(symbols,c);
  c.gridy++;password.setFont(GameFonts.body(40));password.setHorizontalAlignment(JTextField.CENTER);password.setBackground(new Color(14,24,25));password.setForeground(LabTheme.GREEN);password.setCaretColor(LabTheme.TEXT);password.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LabTheme.LINE),BorderFactory.createEmptyBorder(10,25,10,25)));password.getAccessibleContext().setAccessibleName("PC 비밀번호 네 자리");
  ((AbstractDocument)password.getDocument()).setDocumentFilter(new DocumentFilter(){public void insertString(FilterBypass b,int off,String text,AttributeSet a)throws BadLocationException{replace(b,off,0,text,a);}public void replace(FilterBypass b,int off,int len,String text,AttributeSet a)throws BadLocationException{String next=b.getDocument().getText(0,b.getDocument().getLength());next=next.substring(0,off)+(text==null?"":text)+next.substring(off+len);if(next.matches("[0-9]{0,4}"))b.replace(off,len,text,a);}});
  center.add(password,c);c.gridy++;status.setFont(GameFonts.body(15));status.setForeground(LabTheme.MUTED);center.add(status,c);add(center,BorderLayout.CENTER);
  JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,0));buttons.setOpaque(false);JButton view=LabTheme.button("해독표 보기"),cancel=LabTheme.button("조사 종료");view.addActionListener(e->reference.run());cancel.addActionListener(e->close.run());buttons.add(view);buttons.add(cancel);buttons.add(submit);add(buttons,BorderLayout.SOUTH);
  Runnable attempt=()->{if(finished)return;if(ArchiveCodePuzzle.accepts(password.getText())){finished=true;submit.setEnabled(false);solved.run();}else{status.setText("비밀번호가 일치하지 않습니다. 해독표를 다시 확인하세요.");status.setForeground(new Color(234,157,118));password.selectAll();password.requestFocusInWindow();}};
  submit.addActionListener(e->attempt.run());password.addActionListener(e->attempt.run());
 }
}
