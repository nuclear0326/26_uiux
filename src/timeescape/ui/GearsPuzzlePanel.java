package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.RepairModels.Gears;

/** Independent gear puzzle content, also usable for headless previews. */
public final class GearsPuzzlePanel extends RepairPuzzlePanel {
    private Gears gears=new Gears();
    private final Board board=new Board();
    private int selected=-1,dragSize=-1;
    private Point drag;
    public GearsPuzzlePanel(Runnable onSolved,Runnable onClose){
        super(onSolved,onClose);
        JButton reset=new JButton("전체 부품 회수");reset.addActionListener(e->{if(isFinished())return;gears=new Gears();refreshPuzzle();});
        action.addActionListener(e->{if(!isFinished()&&gears.solved())solve();});
        build("시계태엽 복원","홈 크기에 맞게 배치 / 부품 선택 후 칸 클릭 또는 드래그 / 장착 부품 클릭으로 회수",board,reset);
        refreshPuzzle();
    }
    @Override protected void skipStage(){solve();}
    private void refreshPuzzle(){
        status.setText("장착 "+gears.filled()+" / 16     남은 부품: 소 "+gears.stock[0]+" / 중 "+gears.stock[1]+" / 대 "+gears.stock[2]+"     홈과 부품의 크기를 맞추세요");action.setText("수리 완료");action.setEnabled(gears.solved());board.repaint();
    }
    private final class Board extends JPanel {
        private double scale=1;private int ox,oy;
        Board(){setBackground(new Color(17,24,32));MouseAdapter mouse=new MouseAdapter(){
            public void mousePressed(MouseEvent e){if(isFinished())return;Point p=local(e);
                for(int s=0;s<3;s++)if(new Rectangle(670,120+s*135,170,120).contains(p)){selected=s;dragSize=gears.stock[s]>0?s:-1;drag=p;repaint();return;}
                int slot=slotAt(p);if(slot>=0){if(gears.placed[slot/4][slot%4]>=0)gears.remove(slot/4,slot%4);else if(selected>=0)gears.put(slot/4,slot%4,selected);refreshPuzzle();}
            }
            public void mouseDragged(MouseEvent e){if(isFinished())return;if(dragSize>=0){drag=local(e);repaint();}}
            public void mouseReleased(MouseEvent e){if(isFinished())return;if(dragSize>=0){int slot=slotAt(local(e));if(slot>=0)gears.put(slot/4,slot%4,dragSize);dragSize=-1;drag=null;refreshPuzzle();}}
        };addMouseListener(mouse);addMouseMotionListener(mouse);}
        Point local(MouseEvent e){return new Point((int)((e.getX()-ox)/scale),(int)((e.getY()-oy)/scale));}
        int slotAt(Point p){if(p.x<90||p.x>=610||p.y<20||p.y>=540)return -1;return ((p.y-20)/130)*4+(p.x-90)/130;}
        protected void paintComponent(Graphics raw){super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();scale=Math.min(getWidth()/900.0,getHeight()/560.0);ox=(int)((getWidth()-900*scale)/2);oy=(int)((getHeight()-560*scale)/2);g.translate(ox,oy);g.scale(scale,scale);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setFont(GameFonts.body(15));GearBoardRenderer.paint(g,gears,selected,dragSize,drag);g.dispose();}
    }
}
