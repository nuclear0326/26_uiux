package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.util.IdentityHashMap;
import javax.swing.*;
import javax.swing.text.*;
import timeescape.puzzle.present.ArchiveCodePuzzle;

/** Mouse and keyboard share the same constrained document and completion path. */
public final class ArchiveLoginPanel extends JPanel {
 final JTextField password=new JTextField(4);
 final JLabel status=new JLabel(" ",SwingConstants.CENTER);
 final JButton submit=LabTheme.button("로그인");
 final JPanel content=new JPanel(new BorderLayout(0,12));
 final CipherMemoOverlay memo=new CipherMemoOverlay(this::closeReference);
 private final IdentityHashMap<Component,Boolean> enabledBefore=new IdentityHashMap<>();
 private boolean finished;
 private int selectionStart,selectionEnd;

 public ArchiveLoginPanel(Runnable solved,Runnable close,Runnable reference){
  super(new BorderLayout());PuzzleChrome.decorate(content);
  JLayeredPane layers=new JLayeredPane(){public void doLayout(){content.setBounds(0,0,getWidth(),getHeight());memo.setBounds(0,0,getWidth(),getHeight());memo.doLayout();}};
  layers.add(content,Integer.valueOf(0));layers.add(memo,Integer.valueOf(1));add(layers);
  content.add(PuzzleWorkspace.header("연구실 PC 로그인","현재 / 연구실",close),BorderLayout.NORTH);
  JPanel center=new JPanel(new GridBagLayout());center.setOpaque(false);GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=0;c.insets=new Insets(3,12,3,12);c.fill=GridBagConstraints.HORIZONTAL;
  CipherSymbols symbols=new CipherSymbols();symbols.setPreferredSize(new Dimension(330,70));center.add(symbols,c);
  c.gridy++;password.setFont(GameFonts.body(34));password.setHorizontalAlignment(JTextField.CENTER);password.setBackground(new Color(14,24,25));password.setForeground(LabTheme.GREEN);password.setCaretColor(LabTheme.TEXT);password.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LabTheme.LINE),BorderFactory.createEmptyBorder(7,25,7,25)));password.getAccessibleContext().setAccessibleName("PC 비밀번호 네 자리");
  ((AbstractDocument)password.getDocument()).setDocumentFilter(new DocumentFilter(){public void insertString(FilterBypass b,int off,String text,AttributeSet a)throws BadLocationException{replace(b,off,0,text,a);}public void replace(FilterBypass b,int off,int len,String text,AttributeSet a)throws BadLocationException{String next=b.getDocument().getText(0,b.getDocument().getLength());next=next.substring(0,off)+(text==null?"":text)+next.substring(off+len);if(next.matches("[0-9]{0,4}"))b.replace(off,len,text,a);}});
  center.add(password,c);
  JPanel keypad=new JPanel(new GridLayout(4,3,8,7));keypad.setOpaque(false);keypad.setPreferredSize(new Dimension(330,160));
  for(String value:new String[]{"1","2","3","4","5","6","7","8","9","지우기","0","전체 삭제"}){
   JButton b=LabTheme.button(value);b.setName("key-"+value);b.setFocusable(false);b.setFont(GameFonts.button(value.length()==1?23:15));
   b.addActionListener(e->{if(finished||memo.isVisible())return;
    if(value.equals("전체 삭제"))password.setText("");
    else if(value.equals("지우기")){if(password.getSelectionStart()!=password.getSelectionEnd())password.replaceSelection("");else{int pos=password.getCaretPosition();if(pos>0){password.select(pos-1,pos);password.replaceSelection("");}}}
    else password.replaceSelection(value);
    password.requestFocusInWindow();
   });keypad.add(b);
  }
  c.gridy++;center.add(keypad,c);c.gridy++;status.setPreferredSize(new Dimension(460,38));status.setFont(GameFonts.body(15));status.setForeground(LabTheme.MUTED);center.add(status,c);
  JButton view=LabTheme.button("해독표 보기");view.addActionListener(e->{openReference();reference.run();});
  JPanel controls=new JPanel(new GridBagLayout());controls.setOpaque(false);
  GridBagConstraints actions=new GridBagConstraints();actions.weightx=1;actions.fill=GridBagConstraints.HORIZONTAL;controls.add(PuzzleWorkspace.buttons(view,submit),actions);
  center.setOpaque(true);
  content.add(PuzzleWorkspace.surface(center,"해독표를 보고 네 자리\n암호를 입력하세요.","숫자 버튼 또는 키보드로\n입력할 수 있습니다.",controls),BorderLayout.CENTER);
  Runnable attempt=()->{if(finished||memo.isVisible())return;if(ArchiveCodePuzzle.accepts(password.getText())){finished=true;submit.setEnabled(false);solved.run();}else{status.setText("<html><div style='text-align:center'>비밀번호가 일치하지 않습니다.<br>해독표를 다시 확인하세요.</div></html>");status.setForeground(new Color(234,157,118));password.selectAll();password.requestFocusInWindow();}};
  submit.addActionListener(e->attempt.run());password.addActionListener(e->attempt.run());
  getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"back");
  getActionMap().put("back",new AbstractAction(){public void actionPerformed(ActionEvent e){if(memo.isVisible())closeReference();else close.run();}});
 }
 private void disableContent(Container parent){for(Component child:parent.getComponents()){enabledBefore.put(child,child.isEnabled());child.setEnabled(false);if(child instanceof Container nested)disableContent(nested);}}
 void openReference(){if(finished||memo.isVisible())return;selectionStart=password.getSelectionStart();selectionEnd=password.getSelectionEnd();disableContent(content);memo.open();}
 void closeReference(){if(!memo.isVisible())return;memo.setVisible(false);enabledBefore.forEach(Component::setEnabled);enabledBefore.clear();password.requestFocusInWindow();password.select(selectionStart,selectionEnd);repaint();}
}
