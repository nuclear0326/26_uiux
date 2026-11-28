package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import timeescape.puzzle.present.TimeCoordinatesPuzzle;

/** Approved coordinate design around the unchanged cyclic linked-rotation model. */
public final class TimeCoordinatesPanel extends RepairPuzzlePanel {
 static final Color HAND=new Color(104,212,219), TARGET=new Color(213,180,118);
 TimeCoordinatesPuzzle model=new TimeCoordinatesPuzzle();
 int selected;
 final Board board=new Board();
 final JLabel link=new JLabel();
 final JButton reset=new JButton("초기화");
 public TimeCoordinatesPanel(Runnable solved,Runnable close){this(solved,close,"현재 / 연구실 작업대");}
 public TimeCoordinatesPanel(Runnable solved,Runnable close,String context){
  super(solved,close);setLayout(new BorderLayout(0,16));
  add(PuzzleWorkspace.header(model.title(),context,this::closePuzzle),BorderLayout.NORTH);
  JPanel actions=new JPanel(new BorderLayout(0,12));actions.setOpaque(false);
  JPanel info=new JPanel(new GridLayout(2,1,0,6));info.setOpaque(false);
  status.setFont(GameFonts.body(15));link.setFont(GameFonts.body(14));info.add(status);info.add(link);actions.add(info,BorderLayout.NORTH);
  action.setText("보정 확인");action.addActionListener(e->verify());
  reset.addActionListener(e->{if(isFinished())return;model=new TimeCoordinatesPuzzle();selected=0;refresh();});
  actions.add(PuzzleWorkspace.buttons(reset,action),BorderLayout.SOUTH);
  add(PuzzleWorkspace.surface(board,"세 바늘을 모두 위쪽의\n목표 표시에 맞추세요.","누른 링과 다음 링이 함께\n시계 방향으로 90° 회전합니다.\nC의 다음은 A입니다.",actions),BorderLayout.CENTER);
  getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"close-coordinates");
  getActionMap().put("close-coordinates",new AbstractAction(){public void actionPerformed(ActionEvent e){closePuzzle();}});
  refresh();
 }
 void select(int index){if(isFinished())return;selected=index;updateLink();board.repaint();}
 void turn(int index){if(isFinished())return;model.turn(index);selected=index;refresh();}
 void updateLink(){
  link.setForeground(model.solved()?LabTheme.MUTED:HAND);
  link.setText(model.solved()?"세 링이 목표 방향과 일치합니다.":(char)('A'+selected)+" 선택 시 "+(char)('A'+selected)+" + "+(char)('A'+(selected+1)%3)+" 회전");
 }
 void refresh(){
  int count=0;for(int i=0;i<3;i++){
   if(model.value(i)==0)count++;
   String name="링 "+(char)('A'+i)+" / 현재 "+new String[]{"↑","→","↓","←"}[model.value(i)]+" / 목표 ↑ / "+(char)('A'+(i+1)%3)+" 함께 회전";
   board.rings[i].getAccessibleContext().setAccessibleName(name);board.rings[i].setToolTipText(name);
  }
  status.setForeground(model.solved()?LabTheme.GREEN:LabTheme.TEXT);status.setText("방향 일치 "+count+" / 3");updateLink();board.repaint();
 }
 void verify(){
  if(isFinished())return;
  if(model.solved()){
   status.setForeground(LabTheme.GREEN);status.setText("시간 좌표 보정이 완료되었습니다.");
   action.setEnabled(false);reset.setEnabled(false);for(JButton ring:board.rings)ring.setEnabled(false);solve();
  }else{status.setForeground(new Color(234,157,118));status.setText("세 바늘의 방향을 확인하세요.");}
 }
 @Override protected void skipStage(){
  // Only 4^3 click sequences: retain the coupled rule even for the developer shortcut.
  for(int a=0;a<4;a++)for(int b=0;b<4;b++)for(int c=0;c<4;c++){
   if((model.value(0)+a+c)%4!=0||(model.value(1)+a+b)%4!=0||(model.value(2)+b+c)%4!=0)continue;
   int[] turns={a,b,c};for(int i=0;i<3;i++)for(int n=0;n<turns[i];n++)model.turn(i);refresh();verify();return;
  }
  throw new IllegalStateException("Unreachable coordinate state");
 }
 final class Board extends JPanel {
  final JButton[] rings=new JButton[3];
  Board(){
   setLayout(null);
   for(int i=0;i<3;i++){
    final int index=i;
    JButton button=new JButton(){@Override protected void paintComponent(Graphics g){} };
    button.setOpaque(false);button.setContentAreaFilled(false);button.setBorderPainted(false);button.setFocusPainted(false);button.setBorder(BorderFactory.createEmptyBorder());
    button.setCursor(GameCursors.interact());button.addActionListener(e->turn(index));
    button.addMouseListener(new MouseAdapter(){public void mouseEntered(MouseEvent e){select(index);}});
    button.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){select(index);repaint();}public void focusLost(FocusEvent e){repaint();}});
    rings[i]=button;add(button);
   }
  }
  double scale(){return Math.min(getWidth()/940.0,getHeight()/480.0);}
  @Override public void doLayout(){double s=scale();for(int i=0;i<3;i++)rings[i].setBounds((int)((getWidth()-940*s)/2+(24+i*302)*s),(int)((getHeight()-480*s)/2+64*s),(int)(288*s),(int)(378*s));}

  protected void paintComponent(Graphics raw){
   super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
   double s=Math.min(getWidth()/940.0,getHeight()/480.0);g.translate((getWidth()-940*s)/2,(getHeight()-480*s)/2);g.scale(s,s);
   g.setColor(HAND);g.setStroke(new BasicStroke(3));g.drawLine(28,35,28,20);g.drawLine(28,20,23,26);g.drawLine(28,20,33,26);
   text(g,"현재 바늘",45,33,GameFonts.body(15),LabTheme.TEXT);
   g.setColor(TARGET);g.fillRect(177,20,16,5);g.setStroke(new BasicStroke(2));g.drawLine(185,25,185,34);text(g,"목표 방향",204,33,GameFonts.body(15),LabTheme.TEXT);
   for(int i=0;i<3;i++)ring(g,i,24+i*302,64,288,378);
   g.dispose();
  }
  void ring(Graphics2D g,int id,int x,int y,int w,int h){
   int cx=x+w/2,cy=y+184;boolean aligned=model.value(id)==0;boolean linked=!model.solved()&&(id==selected||id==(selected+1)%3);
   g.setPaint(new GradientPaint(x,y,new Color(29,44,49),x,y+h,new Color(18,31,37)));g.fillRoundRect(x,y,w,h,9,9);
   g.setStroke(new BasicStroke(linked?1.6f:1));g.setColor(aligned?new Color(103,150,128):linked?new Color(92,163,171):new Color(66,83,87));g.drawRoundRect(x,y,w,h,9,9);
   if(rings[id].isFocusOwner()){g.setColor(LabTheme.TEXT);g.drawRoundRect(x+4,y+4,w-8,h-8,6,6);}
   if(linked&&id==selected){g.setColor(new Color(104,212,219,45));g.fillRoundRect(x+1,y+1,w-2,52,9,9);}
   LabTheme.screw(g,x+12,y+12);LabTheme.screw(g,x+w-12,y+12);LabTheme.screw(g,x+12,y+h-12);LabTheme.screw(g,x+w-12,y+h-12);
   g.setFont(GameFonts.button(21));g.setColor(LabTheme.TEXT);LabTheme.center(g,"링 "+(char)('A'+id),cx,y+36);
   g.setColor(new Color(53,71,75));g.setStroke(new BasicStroke(1));g.drawLine(x+30,y+53,x+w-30,y+53);
   // Target is fixed on the outer frame; it never looks like a second moving needle.
   g.setFont(GameFonts.button(15));g.setColor(TARGET);LabTheme.center(g,"목표 ↑",cx,y+81);
   g.setColor(new Color(8,18,23));g.fillOval(cx-97,cy-97,194,194);
   g.setColor(new Color(83,103,107));g.setStroke(new BasicStroke(2));g.drawOval(cx-97,cy-97,194,194);
   g.setColor(new Color(43,64,72));g.setStroke(new BasicStroke(8));g.drawOval(cx-87,cy-87,174,174);
   g.setColor(new Color(117,139,138));g.setStroke(new BasicStroke(2));
   for(int dir=0;dir<4;dir++){double a=dir*Math.PI/2;g.drawLine(cx+(int)(Math.sin(a)*79),cy-(int)(Math.cos(a)*79),cx+(int)(Math.sin(a)*94),cy-(int)(Math.cos(a)*94));}
   g.setColor(TARGET);g.fillRect(cx-8,cy-99,16,5);g.setStroke(new BasicStroke(2));g.drawLine(cx,cy-94,cx,cy-84);
   // Subtle axis markings make all four possible directions easy to compare.
   g.setStroke(new BasicStroke(1,0,0,1,new float[]{2,6},0));g.setColor(new Color(82,111,117,95));g.drawLine(cx-65,cy,cx+65,cy);g.drawLine(cx,cy-65,cx,cy+65);
   Graphics2D hand=(Graphics2D)g.create();hand.translate(cx,cy);hand.rotate(model.value(id)*Math.PI/2);
   Path2D needle=new Path2D.Double();needle.moveTo(0,-78);needle.lineTo(7,-55);needle.lineTo(3,-55);needle.lineTo(3,16);needle.lineTo(-3,16);needle.lineTo(-3,-55);needle.lineTo(-7,-55);needle.closePath();
   hand.setColor(new Color(0,0,0,90));hand.translate(2,3);hand.fill(needle);hand.translate(-2,-3);hand.setColor(HAND);hand.fill(needle);hand.dispose();
   g.setColor(new Color(13,28,33));g.fillOval(cx-9,cy-9,18,18);g.setColor(HAND);g.setStroke(new BasicStroke(1.5f));g.drawOval(cx-9,cy-9,18,18);
   g.setFont(GameFonts.button(16));g.setColor(HAND);LabTheme.center(g,"현재 "+new String[]{"↑","→","↓","←"}[model.value(id)],cx-67,y+309);
   g.setColor(new Color(70,87,89));g.drawLine(cx,y+293,cx,y+312);g.setColor(TARGET);LabTheme.center(g,"목표 ↑",cx+67,y+309);
   g.setFont(GameFonts.body(15));g.setColor(aligned?LabTheme.GREEN:linked?HAND:LabTheme.MUTED);LabTheme.center(g,aligned?"✓  방향 일치":linked?(id==selected?"선택한 링":"함께 회전하는 링"):"보정 필요",cx,y+346);
  }
  void text(Graphics2D g,String t,int x,int y,Font f,Color c){g.setFont(f);g.setColor(c);g.drawString(t,x,y);}
 }
}
