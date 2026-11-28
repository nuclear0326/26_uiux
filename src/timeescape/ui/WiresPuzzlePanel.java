package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.RepairModels.Wires;

/** Wire selection and easy-to-hard progression, independent of its host window. */
public final class WiresPuzzlePanel extends RepairPuzzlePanel {
    private Wires wires=new Wires(false);
    private final Board board=new Board();
    private final JLabel selection=new JLabel(" ");
    private static final String[] COLOR_NAMES={"주황색","하늘색","보라색","노란색"};
    private int active=-1;
    public WiresPuzzlePanel(Runnable onSolved,Runnable onClose){
        super(onSolved,onClose);
        JButton reset=new JButton("초기화");
        reset.addActionListener(e->{if(isFinished())return;wires=new Wires(wires.hard);active=-1;refreshPuzzle();});
        action.addActionListener(e->{if(!isFinished()&&wires.solved()){if(wires.hard)solve();else nextStage();}});
        add(PuzzleWorkspace.header("배선 복구","현재 / 연구실 배전함",this::closePuzzle),BorderLayout.NORTH);
        JPanel controls=new JPanel(new BorderLayout(0,5));controls.setOpaque(false);
        status.setFont(GameFonts.body(15));status.setForeground(LabTheme.TEXT);controls.add(status,BorderLayout.NORTH);
        JPanel selected=new JPanel(new BorderLayout());selected.setOpaque(false);selected.setPreferredSize(new Dimension(220,25));selection.setFont(GameFonts.body(14));selection.setForeground(PuzzleWorkspace.ACCENT);selected.add(selection,BorderLayout.WEST);controls.add(selected,BorderLayout.CENTER);
        controls.add(PuzzleWorkspace.buttons(reset,action),BorderLayout.SOUTH);
        add(PuzzleWorkspace.surface(board,"같은 색 단자를 연결하고\n모든 통로를 채우세요.","단자에서 드래그해 연결\n기존 선을 눌러 이어 그리기\n직전 칸으로 되돌아가면 취소\n왼쪽 점 더블클릭: 선 초기화",controls),BorderLayout.CENTER);
        JLabel note=new JLabel("나가면 배선 진행이 초기화됩니다.");note.setFont(GameFonts.body(14));note.setForeground(LabTheme.MUTED);add(note,BorderLayout.SOUTH);
        refreshPuzzle();
    }
    private void nextStage(){wires=new Wires(true);active=-1;refreshPuzzle();}
    @Override protected void skipStage(){if(wires.hard)solve();else nextStage();}
    private void refreshPuzzle(){
        int count=0;for(int i=0;i<wires.rows.length;i++)if(wires.connected(i))count++;
        status.setText("연결 "+count+" / "+wires.rows.length+"     통로 "+wires.coveredCells()+" / "+wires.totalCells());
        selection.setText(active>=0?COLOR_NAMES[active]+" 선 선택됨":" ");
        action.setText(wires.hard?"수리 완료":"다음 단계");action.setEnabled(wires.solved());board.repaint();
    }
    private final class Board extends JPanel {
        private double scale=1,ox,oy;
        Board(){MouseAdapter mouse=new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                if(isFinished()||!SwingUtilities.isLeftMouseButton(e))return;
                Point local=local(e);
                // Only the left terminal disc resets a circuit. A normal click keeps its route.
                if(e.getClickCount()==2){
                    for(int i=0;i<wires.rows.length;i++){
                        Point start=wires.start(i);double dx=local.x-(190+start.x*24),dy=local.y-(30+start.y*24);
                        if(dx*dx+dy*dy<=11*11){active=i;wires.begin(i);refreshPuzzle();return;}
                    }
                }
                Point cell=cell(local);int picked=wires.selectAt(cell);
                if(picked>=0){active=picked;refreshPuzzle();return;}
                if(active>=0){wires.extend(active,cell);refreshPuzzle();}
            }
            public void mouseDragged(MouseEvent e){if(!isFinished()&&active>=0&&(e.getModifiersEx()&MouseEvent.BUTTON1_DOWN_MASK)!=0){wires.extend(active,cell(local(e)));refreshPuzzle();}}
        };addMouseListener(mouse);addMouseMotionListener(mouse);}
        Point local(MouseEvent e){return new Point((int)Math.round((e.getX()-ox)/scale),(int)Math.round((e.getY()-oy)/scale));}
        Point cell(Point p){return new Point((int)Math.floor((p.x-190+12)/24.0),(int)Math.floor((p.y-30+12)/24.0));}
        protected void paintComponent(Graphics raw){
            super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
            g.setFont(GameFonts.button(16));g.setColor(PuzzleWorkspace.ACCENT);g.drawString(wires.hard?"02 / 어려움":"01 / 쉬움",22,32);
            scale=Math.max(.01,Math.min((getWidth()-40)/510.0,(getHeight()-90)/510.0));
            ox=(getWidth()-510*scale)/2-175*scale;oy=(getHeight()-510*scale)/2+8-15*scale;
            g.translate(ox,oy);g.scale(scale,scale);WireBoardRenderer.paintBoard(g,wires,active);g.dispose();
        }
    }
}
