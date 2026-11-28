package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import timeescape.puzzle.present.SchematicPuzzle;

/** Drag-only assembly using the shared puzzle window and one-shot completion lifecycle. */
public final class SchematicPuzzlePanel extends RepairPuzzlePanel {
    final SchematicPuzzle model=new SchematicPuzzle();
    final Board board=new Board();
    final JButton reset=new JButton("초기화");
    private final BufferedImage drawing=SchematicDrawing.create();
    int source=-1,target=-1;
    Point2D.Double pointer;
    double grabX,grabY;

    public SchematicPuzzlePanel(Runnable solved,Runnable close){this(solved,close,"현재 / 연구실 작업대");}
    public SchematicPuzzlePanel(Runnable solved,Runnable close,String context){
        super(solved,close);setLayout(new BorderLayout(0,16));
        add(PuzzleWorkspace.header(model.title(),context,this::closePuzzle),BorderLayout.NORTH);
        action.setText("복원 확인");action.addActionListener(e->verify());
        reset.addActionListener(e->{if(isFinished())return;cancelDrag();model.reset();refresh();});
        JPanel actions=new JPanel(new BorderLayout(0,12));actions.setOpaque(false);status.setFont(GameFonts.body(15));
        actions.add(status,BorderLayout.NORTH);actions.add(PuzzleWorkspace.buttons(reset,action),BorderLayout.SOUTH);
        add(PuzzleWorkspace.surface(board,"회로와 도면을 이어\n설계도를 완성하세요.","조각을 드래그해 배치하세요.\n놓인 조각도 옮길 수 있습니다.",actions),BorderLayout.CENTER);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"cancel-drag");
        getActionMap().put("cancel-drag",new AbstractAction(){public void actionPerformed(ActionEvent e){if(isFinished())return;if(source>=0)cancelDrag();else closePuzzle();}});
        refresh();
    }
    void refresh(){status.setForeground(LabTheme.TEXT);status.setText("배치 "+model.placed()+" / 9");board.repaint();}
    void verify(){
        if(isFinished())return;cancelDrag();
        if(model.solved()){status.setForeground(LabTheme.GREEN);status.setText("회로 연결이 복원되었습니다.");reset.setEnabled(false);action.setEnabled(false);solve();}
        else {status.setForeground(new Color(234,157,118));status.setText(model.placed()<9?"빈 칸에 조각을 놓으세요.":"회로 연결을 다시 확인하세요.");}
        board.repaint();
    }
    void cancelDrag(){source=-1;target=-1;pointer=null;board.repaint();}
    @Override protected void skipStage(){
        cancelDrag();for(int piece=0;piece<9;piece++)for(int cell=0;cell<18;cell++)if(model.pieceAt(cell)==piece){model.move(cell,piece);break;}verify();
    }
    @Override public void removeNotify(){cancelDrag();super.removeNotify();}

    final class Board extends JPanel {
        Board(){
            setFocusable(true);setCursor(GameCursors.interact());
            addFocusListener(new FocusAdapter(){public void focusLost(FocusEvent e){cancelDrag();}});
            addComponentListener(new ComponentAdapter(){public void componentResized(ComponentEvent e){cancelDrag();}public void componentHidden(ComponentEvent e){cancelDrag();}});
            MouseAdapter drag=new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    if(isFinished()||!SwingUtilities.isLeftMouseButton(e))return;
                    int cell=slot(e.getPoint());if(cell<0||model.pieceAt(cell)<0)return;
                    requestFocusInWindow();source=cell;target=cell;pointer=logical(e.getPoint());Rectangle r=cell(cell);grabX=pointer.x-r.x;grabY=pointer.y-r.y;repaint();
                }
                public void mouseDragged(MouseEvent e){if(isFinished()||source<0||(e.getModifiersEx()&MouseEvent.BUTTON1_DOWN_MASK)==0)return;pointer=logical(e.getPoint());target=slot(e.getPoint());repaint();}
                public void mouseReleased(MouseEvent e){
                    if(!SwingUtilities.isLeftMouseButton(e))return;if(isFinished()){cancelDrag();return;}
                    if(source<0)return;int to=slot(e.getPoint());boolean moved=model.move(source,to);cancelDrag();if(moved)refresh();
                }
            };addMouseListener(drag);addMouseMotionListener(drag);
        }
        double scale(){return Math.min(getWidth()/1040.0,getHeight()/535.0);}
        Point2D.Double logical(Point p){double s=scale();return new Point2D.Double((p.x-(getWidth()-1040*s)/2)/s,(p.y-(getHeight()-535*s)/2)/s);}
        Rectangle cell(int i){int n=i%9;return i<9?new Rectangle(36+n%3*140,60+n/3*140,140,140):new Rectangle(552+n%3*152,60+n/3*152,140,140);}
        int slot(Point p){if(scale()<=0)return -1;Point2D q=logical(p);for(int i=0;i<18;i++)if(cell(i).contains(q))return i;return -1;}
        Point center(int i){Rectangle r=cell(i);double s=scale();return new Point((int)((getWidth()-1040*s)/2+(r.getCenterX())*s),(int)((getHeight()-535*s)/2+r.getCenterY()*s));}
        @Override protected void paintComponent(Graphics raw){
            super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);double s=scale();
            g.translate((getWidth()-1040*s)/2,(getHeight()-535*s)/2);g.scale(s,s);
            g.setFont(GameFonts.button(16));g.setColor(LabTheme.TEXT);g.drawString("복원판",36,34);g.drawString("설계도 조각",552,34);
            g.setColor(new Color(40,59,65));g.drawLine(502,20,502,510);
            g.setColor(new Color(9,22,28));g.fillRect(31,55,430,430);g.setStroke(new BasicStroke(1));g.setColor(new Color(73,101,108));g.drawRect(31,55,430,430);
            for(int i=0;i<18;i++){
                Rectangle r=cell(i);g.setStroke(new BasicStroke(1));
                if(i<9){g.setColor(new Color(21,40,48));g.fillRect(r.x+2,r.y+2,136,136);g.setColor(new Color(49,72,79));g.setStroke(new BasicStroke(1,0,0,1,new float[]{4,6},0));g.drawRect(r.x+2,r.y+2,136,136);}
                else {g.setColor(new Color(24,41,47));g.drawRoundRect(r.x,r.y,140,140,5,5);}
                if(model.pieceAt(i)>=0&&source!=i)tile(g,model.pieceAt(i),r.x,r.y,!isFinished());
            }
            if(source>=0&&pointer!=null){
                if(target>=0){Rectangle r=cell(target);g.setColor(new Color(211,179,118,32));g.fillRect(r.x+1,r.y+1,138,138);g.setColor(LabTheme.GOLD);g.setStroke(new BasicStroke(2));g.drawRect(r.x+1,r.y+1,138,138);}
                int x=(int)(pointer.x-grabX),y=(int)(pointer.y-grabY);tile(g,model.pieceAt(source),x,y,true);g.setColor(LabTheme.GOLD);g.setStroke(new BasicStroke(1.5f));g.drawRect(x,y,140,140);
            }
            if(isFinished()&&model.solved()){g.setColor(LabTheme.GREEN);g.setStroke(new BasicStroke(1.5f));g.drawRect(31,55,430,430);}
            g.dispose();
        }
        private void tile(Graphics2D g,int id,int x,int y,boolean raised){
            if(raised){g.setColor(new Color(0,0,0,65));g.fillRect(x+3,y+4,140,140);}
            g.drawImage(drawing,x,y,x+140,y+140,id%3*140,id/3*140,id%3*140+140,id/3*140+140,null);
            if(raised){g.setStroke(new BasicStroke(1));g.setColor(new Color(121,149,150));g.drawRect(x,y,139,139);}
        }
    }
}
