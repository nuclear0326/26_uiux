package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.AccidentLogPuzzle;

/** Drag ordering and record comparison; only the final stage completes the puzzle. */
public final class AccidentLogPanel extends RepairPuzzlePanel {
 final AccidentLogPuzzle model=new AccidentLogPuzzle();
 final JPanel content=new JPanel(new BorderLayout(0,16));
 final AccidentEvidenceOverlay evidence=new AccidentEvidenceOverlay(this::closeEvidence);
 final JButton recordButton=new JButton("기록 보기");
 private final java.util.IdentityHashMap<Component,Boolean> enabledBefore=new java.util.IdentityHashMap<>();
 private Component previousFocus;
 final Board board=new Board();
 final JButton reset=new JButton("다시 맞추기");
 int source=-1,boundary=-1;Point pointer;int grabY;
 public AccidentLogPanel(Runnable solved,Runnable close){
  super(solved,close);setLayout(new BorderLayout());setBorder(BorderFactory.createEmptyBorder());PuzzleChrome.decorate(content);
  JLayeredPane layers=new JLayeredPane(){public void doLayout(){content.setBounds(0,0,getWidth(),getHeight());evidence.setBounds(0,0,getWidth(),getHeight());evidence.doLayout();}};
  layers.add(content,Integer.valueOf(0));layers.add(evidence,Integer.valueOf(1));add(layers,BorderLayout.CENTER);
  PuzzleChrome.styleButton(recordButton);recordButton.setPreferredSize(new Dimension(120,34));recordButton.addActionListener(e->openEvidence());
  content.add(PuzzleWorkspace.header("사고 로그 분석","현재 / USB 기록 분석",this::closePuzzle),BorderLayout.NORTH);
  reset.addActionListener(e->{if(isFinished()||evidence.isVisible())return;cancelDrag();model.reset();status.setText(" ");board.repaint();});
  action.addActionListener(e->verify());
  getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"cancel-drag");
  getActionMap().put("cancel-drag",new AbstractAction(){public void actionPerformed(ActionEvent e){if(isFinished())return;if(evidence.isVisible())closeEvidence();else if(source>=0)cancelDrag();else closePuzzle();}});
  refreshRows();
 }
 void verify(){
  if(isFinished()||evidence.isVisible())return;cancelDrag();
  if(model.verify()){
   status.setText(" ");
   if(model.stage()==3){action.setEnabled(false);reset.setEnabled(false);solve();return;}
   refreshRows();
  }else status.setText(model.stage()==1?"기록의 단서를 다시 확인하세요.":"최초 이상 기록을 다시 확인하세요.");
 }
 void refreshRows(){Component old=((BorderLayout)content.getLayout()).getLayoutComponent(BorderLayout.CENTER);if(old!=null)content.remove(old);
  JPanel actions=new JPanel(new BorderLayout(0,10));actions.setOpaque(false);status.setFont(GameFonts.body(14));status.setForeground(LabTheme.MUTED);actions.add(status,BorderLayout.NORTH);action.setText(model.stage()==1?"순서 확인":"분석 완료");actions.add(PuzzleWorkspace.buttons(reset,action),BorderLayout.SOUTH);
  content.add(PuzzleWorkspace.surface(board,model.stage()==1?"사고 기록을 발생 순서대로\n배치하세요.":"정상 기록과 비교해\n최초 이상을 찾으세요.",model.stage()==1?"기록을 드래그해 순서 변경\n밝은 선 위치에 놓기":"이상이 시작된 기록을 선택하고\n분석 완료를 누르세요.",actions,recordButton),BorderLayout.CENTER);revalidate();repaint();
 }
 void cancelDrag(){source=-1;boundary=-1;pointer=null;board.repaint();}
 final class Board extends JPanel {
  final int left=22,top=68,gap=8;
  int cardHeight(){return Math.max(40,(getHeight()-top-20-5*gap)/6);}
  Rectangle row(int i){return new Rectangle(left,top+i*(cardHeight()+gap),getWidth()-2*left,cardHeight());}
  int slot(Point p){if(p.x<left||p.x>getWidth()-left||p.y<top-16||p.y>getHeight()-6)return -1;return Math.max(0,Math.min(6,(int)Math.floor((p.y-top+(cardHeight()+gap)/2.0)/(cardHeight()+gap))));}
  Board(){setCursor(GameCursors.interact());setFocusable(true);addFocusListener(new FocusAdapter(){public void focusLost(FocusEvent e){cancelDrag();}});MouseAdapter drag=new MouseAdapter(){
   public void mousePressed(MouseEvent e){if(isFinished()||evidence.isVisible()||!SwingUtilities.isLeftMouseButton(e))return;requestFocusInWindow();for(int i=0;i<6;i++)if(row(i).contains(e.getPoint())){if(model.stage()==1){source=i;grabY=e.getY()-row(i).y;pointer=e.getPoint();boundary=i;}else {model.choose(i);status.setText(" ");}repaint();break;}}
   public void mouseDragged(MouseEvent e){if(isFinished()||evidence.isVisible()||source<0||(e.getModifiersEx()&MouseEvent.BUTTON1_DOWN_MASK)==0)return;pointer=e.getPoint();boundary=slot(pointer);repaint();}
   public void mouseReleased(MouseEvent e){if(!SwingUtilities.isLeftMouseButton(e))return;if(isFinished()||evidence.isVisible()||model.stage()!=1){cancelDrag();return;}if(source<0)return;int target=slot(e.getPoint());if(target>=0){int to=target>source?target-1:target;model.move(source,to);status.setText(" ");}cancelDrag();}
  };addMouseListener(drag);addMouseMotionListener(drag);}
  void text(Graphics2D g,String s,int x,int y,Font font,Color c){g.setFont(font);g.setColor(c);g.drawString(s,x,y);}
  void card(Graphics2D g,int i,Rectangle r,boolean floating){
   boolean ordering=model.stage()==1;var record=AccidentLogPuzzle.RECORDS.get(ordering?model.order().get(i):i);
   if(floating){g.setColor(new Color(0,0,0,110));g.fillRoundRect(r.x+5,r.y+7,r.width,r.height,8,8);}
   g.setColor(floating?new Color(32,54,63):new Color(25,41,49));g.fillRoundRect(r.x,r.y,r.width,r.height,6,6);
   g.setColor(floating||(!ordering&&model.selected()==i)?LabTheme.GOLD:PuzzleWorkspace.LINE);g.setStroke(new BasicStroke(floating?2:1));g.drawRoundRect(r.x,r.y,r.width,r.height,6,6);
   if(ordering){
    int cy=r.y+r.height/2;g.setColor(LabTheme.MUTED);for(int dy=-7;dy<=7;dy+=7)for(int dx=0;dx<=6;dx+=6)g.fillOval(r.x+15+dx,cy+dy-1,2,2);
    text(g,String.format("%02d",i+1),r.x+38,cy+5,GameFonts.button(16),PuzzleWorkspace.ACCENT);
    text(g,record.title(),r.x+83,cy-6,GameFonts.button(17),LabTheme.TEXT);
    text(g,record.clue(),r.x+83,cy+16,GameFonts.body(14),LabTheme.MUTED);
   }else{
    int c1=r.x+190,c2=r.x+190+(r.width-205)/2;
    text(g,record.title(),r.x+17,r.y+r.height/2+5,GameFonts.button(16),LabTheme.TEXT);
    text(g,record.normal(),c1,r.y+r.height/2+5,GameFonts.body(14),new Color(177,202,195));
    text(g,record.failed(),c2,r.y+r.height/2+5,GameFonts.body(14),LabTheme.TEXT);
   }
  }
  protected void paintComponent(Graphics raw){super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
   text(g,model.stage()==1?"01 / 사고 기록 순서 복원":"02 / 정상 기록과 실패 기록 대조",left,29,GameFonts.button(17),PuzzleWorkspace.ACCENT);
   if(model.stage()==2){text(g,"기록",left+17,53,GameFonts.body(14),LabTheme.MUTED);text(g,"정상 기록",left+190,53,GameFonts.body(14),LabTheme.MUTED);text(g,"사고 기록",left+190+(getWidth()-2*left-205)/2,53,GameFonts.body(14),LabTheme.MUTED);}
   for(int i=0;i<6;i++){if(i==source){g.setComposite(AlphaComposite.SrcOver.derive(.25f));card(g,i,row(i),false);g.setComposite(AlphaComposite.SrcOver);}else card(g,i,row(i),false);}
   if(source>=0&&pointer!=null){
    Rectangle r=row(source);r.x+=12;r.y=Math.max(top+8,Math.min(getHeight()-r.height-10,pointer.y-grabY));
    g.setComposite(AlphaComposite.SrcOver.derive(.95f));card(g,source,r,true);g.setComposite(AlphaComposite.SrcOver);
    if(boundary>=0){int y=boundary==6?row(5).y+row(5).height+4:row(boundary).y-4;g.setColor(PuzzleWorkspace.ACCENT);g.setStroke(new BasicStroke(3));g.drawLine(left,y,getWidth()-left,y);g.fillOval(left-4,y-4,8,8);}
   }g.dispose();
  }
 }
 @Override protected void skipStage(){if(evidence.isVisible())return;cancelDrag();model.skipStage();if(model.stage()==3)solve();else refreshRows();}
 void openEvidence(){
  if(isFinished()||evidence.isVisible())return;
  cancelDrag();previousFocus=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();disableContent(content);
  evidence.setVisible(true);evidence.normal.requestFocusInWindow();repaint();
 }
 private void disableContent(Container parent){for(Component c:parent.getComponents()){enabledBefore.put(c,c.isEnabled());c.setEnabled(false);if(c instanceof Container child)disableContent(child);}}
 void closeEvidence(){
  if(!evidence.isVisible())return;
  evidence.setVisible(false);enabledBefore.forEach(Component::setEnabled);enabledBefore.clear();
  Component focus=previousFocus;previousFocus=null;
  if(!isFinished()){if(focus!=null&&focus.isEnabled()&&SwingUtilities.isDescendingFrom(focus,content))focus.requestFocusInWindow();else recordButton.requestFocusInWindow();}
  repaint();
 }
 @Override public void removeNotify(){cancelDrag();closeEvidence();super.removeNotify();}
}
