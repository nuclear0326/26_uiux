package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.RepairModels.Wires;

/** Owns wire selection, paths, and easy-to-hard progression independently of the host window. */
public final class WiresPuzzlePanel extends RepairPuzzlePanel {
    private Wires wires=new Wires(false);
    private final Board board=new Board();
    private int active=-1;
    public WiresPuzzlePanel(Runnable onSolved,Runnable onClose){
        super(onSolved,onClose);
        JButton reset=new JButton("현재 면 배선 초기화");reset.addActionListener(e->{if(isFinished())return;wires=new Wires(wires.hard);active=-1;refreshPuzzle();});
        JButton clear=new JButton("선 지우기");clear.addActionListener(e->{if(!isFinished()&&active>=0){wires.begin(active);refreshPuzzle();}});
        JButton undo=new JButton("한 칸 취소");undo.addActionListener(e->{if(!isFinished()&&active>=0){wires.undo(active);refreshPuzzle();}});
        action.addActionListener(e->{if(!isFinished()&&wires.solved()){if(wires.hard)solve();else nextStage();}});
        build("배선 복구","왼쪽 단자를 클릭한 뒤 드래그 또는 인접 칸 클릭 / 기존 선 클릭으로 재선택 / 끝점부터 이어 그리기",board,reset,clear,undo);
        refreshPuzzle();
    }
    private void nextStage(){wires=new Wires(true);active=-1;refreshPuzzle();}
    @Override protected void skipStage(){if(wires.hard)solve();else nextStage();}
    private void refreshPuzzle(){
        int count=0;for(int i=0;i<wires.rows.length;i++)if(wires.connected(i))count++;
        status.setText((wires.hard?"뒷면 / 어려움":"앞면 / 쉬움")+"     연결 "+count+" / "+wires.rows.length+"     통로 "+wires.coveredCells()+" / "+wires.totalCells()+"     모든 통로를 채우세요.");action.setText(wires.hard?"수리 완료":"구 뒤집기 → 어려움");action.setEnabled(wires.solved());board.repaint();
    }
    private final class Board extends JPanel {
        private double scale=1;private int ox,oy;
        Board(){setBackground(new Color(17,24,32));MouseAdapter mouse=new MouseAdapter(){
            public void mousePressed(MouseEvent e){if(isFinished())return;Point cell=cell(local(e));int picked=wires.selectAt(cell);if(picked>=0){active=picked;refreshPuzzle();return;}if(active>=0){wires.extend(active,cell);refreshPuzzle();}}
            public void mouseDragged(MouseEvent e){if(!isFinished()&&active>=0){wires.extend(active,cell(local(e)));refreshPuzzle();}}
        };addMouseListener(mouse);addMouseMotionListener(mouse);}
        Point local(MouseEvent e){return new Point((int)((e.getX()-ox)/scale),(int)((e.getY()-oy)/scale));}
        Point cell(Point p){return new Point((int)Math.floor((p.x-190+12)/24.0),(int)Math.floor((p.y-30+12)/24.0));}
        protected void paintComponent(Graphics raw){super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();scale=Math.min(getWidth()/900.0,getHeight()/560.0);ox=(int)((getWidth()-900*scale)/2);oy=(int)((getHeight()-560*scale)/2);g.translate(ox,oy);g.scale(scale,scale);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setFont(GameFonts.body(15));WireBoardRenderer.paint(g,wires,active);g.dispose();}
    }
}
