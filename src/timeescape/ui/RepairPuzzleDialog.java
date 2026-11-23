package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import timeescape.puzzle.RepairModels.*;
import timeescape.system.GameAudio;

/** Interactive prototype; all movable artwork uses the same coordinates as hit testing. */
public final class RepairPuzzleDialog extends JDialog {
    private final boolean gearsMode;
    private Gears gears=new Gears();
    private Wires wires=new Wires(false);
    private final JLabel status=new JLabel(" ");
    private final JButton action=new JButton();
    private final Board board=new Board();
    private boolean success;
    private int selected=-1,active=-1,dragSize=-1;
    private Point drag;
    private static final Color[] COLORS={new Color(255,151,90),new Color(88,209,229),new Color(194,148,255),new Color(244,211,91)};
    public static boolean play(Frame owner,boolean gears){RepairPuzzleDialog d=new RepairPuzzleDialog(owner,gears);d.setVisible(true);return d.success;}
    public RepairPuzzleDialog(Frame owner,boolean gears){
        super(owner,gears?"과거 | 시계태엽 고치기":"미래 | 기계 고치기",true);gearsMode=gears;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);setSize(1000,820);setMinimumSize(new Dimension(900,760));setLocationRelativeTo(owner);
        JPanel root=new JPanel(new BorderLayout(12,12));root.setBackground(new Color(17,24,32));root.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));setContentPane(root);
        JPanel header=new JPanel(new GridLayout(0,1,0,7));header.setOpaque(false);
        JLabel title=new JLabel(gears?"CLOCKWORK / 4 × 4 기어 복원":"CIRCUIT / 구형 장치 배선 복구");title.setFont(GameFonts.title(23));title.setForeground(Color.WHITE);header.add(title);
        JLabel help=new JLabel(gears?"홈 크기에 맞게 배치 · 부품 선택 후 칸 클릭 또는 드래그 · 장착 부품 클릭으로 회수":"왼쪽 단자를 클릭한 뒤 드래그 또는 인접 칸 클릭 · 기존 선 클릭으로 재선택 · 끝점부터 이어 그리기");help.setForeground(new Color(199,211,221));header.add(help);root.add(header,BorderLayout.NORTH);
        root.add(board,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(0,10));footer.setOpaque(false);status.setForeground(Color.WHITE);footer.add(status,BorderLayout.NORTH);
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.CENTER,9,0));controls.setOpaque(false);
        JButton reset=new JButton(gears?"전체 부품 회수":"현재 면 배선 초기화");reset.addActionListener(e->{if(gearsMode)this.gears=new Gears();else wires=new Wires(wires.hard);active=-1;refreshPuzzle();});controls.add(reset);
        if(!gears){JButton clear=new JButton("선 지우기");clear.addActionListener(e->{if(active>=0){wires.begin(active);refreshPuzzle();}});controls.add(clear);JButton undo=new JButton("한 칸 취소");undo.addActionListener(e->{if(active>=0){wires.undo(active);refreshPuzzle();}});controls.add(undo);}
        action.addActionListener(e->{if(gearsMode){if(this.gears.solved())finish();}else if(wires.solved()){if(wires.hard)finish();else {wires=new Wires(true);active=-1;refreshPuzzle();}}});controls.add(action);
        JButton close=new JButton("나가기");close.addActionListener(e->dispose());controls.add(close);footer.add(controls,BorderLayout.CENTER);
        JLabel note=new JLabel("미완료 상태에서 나가면 이 퍼즐은 처음부터 시작합니다.",SwingConstants.CENTER);note.setForeground(new Color(158,174,187));footer.add(note,BorderLayout.SOUTH);root.add(footer,BorderLayout.SOUTH);
        timeescape.system.DeveloperMode.bind(getRootPane(), () -> {
            if(success)return;
            if(!gearsMode&&!wires.hard){wires=new Wires(true);active=-1;refreshPuzzle();}
            else finish();
        });
        if(timeescape.system.DeveloperMode.isEnabled())setTitle(getTitle()+" [개발 모드: Ctrl+Shift+F10 단계 스킵]");
        GameAudio.get().puzzle(true);addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){GameAudio.get().puzzle(false);}});refreshPuzzle();
    }
    private void finish(){success=true;GameAudio.get().play("puzzle_success");dispose();}
    private void refreshPuzzle(){
        if(gearsMode){status.setText("장착 "+gears.filled()+" / 16     남은 부품: 소 "+gears.stock[0]+" · 중 "+gears.stock[1]+" · 대 "+gears.stock[2]+"     홈과 부품의 크기를 맞추세요");action.setText("수리 완료");action.setEnabled(gears.solved());}
        else {int count=0;for(int i=0;i<wires.rows.length;i++)if(wires.connected(i))count++;status.setText((wires.hard?"뒷면 · 어려움":"앞면 · 쉬움")+"     연결 "+count+" / "+wires.rows.length+"     통로 "+wires.coveredCells()+" / "+wires.totalCells()+"     모든 통로를 채우세요.");action.setText(wires.hard?"수리 완료":"구 뒤집기 → 어려움");action.setEnabled(wires.solved());}board.repaint();
    }
    private final class Board extends JPanel {
        private double scale=1;private int ox,oy;
        Board(){setBackground(new Color(17,24,32));MouseAdapter mouse=new MouseAdapter(){
            public void mousePressed(MouseEvent e){Point p=local(e);if(gearsMode){
                for(int s=0;s<3;s++)if(new Rectangle(670,120+s*135,170,120).contains(p)){selected=s;dragSize=gears.stock[s]>0?s:-1;drag=p;repaint();return;}
                int slot=slotAt(p);if(slot>=0){if(gears.placed[slot/4][slot%4]>=0)gears.remove(slot/4,slot%4);else if(selected>=0)gears.put(slot/4,slot%4,selected);refreshPuzzle();}
            }else{Point cell=cell(p);int picked=wires.selectAt(cell);if(picked>=0){active=picked;refreshPuzzle();return;}if(active>=0){wires.extend(active,cell);refreshPuzzle();}}}
            public void mouseDragged(MouseEvent e){Point p=local(e);if(gearsMode){if(dragSize>=0){drag=p;repaint();}}else if(active>=0){wires.extend(active,cell(p));refreshPuzzle();}}
            public void mouseReleased(MouseEvent e){if(gearsMode&&dragSize>=0){int slot=slotAt(local(e));if(slot>=0)gears.put(slot/4,slot%4,dragSize);dragSize=-1;drag=null;refreshPuzzle();}}
        };addMouseListener(mouse);addMouseMotionListener(mouse);}
        Point local(MouseEvent e){return new Point((int)((e.getX()-ox)/scale),(int)((e.getY()-oy)/scale));}
        Point cell(Point p){return new Point((int)Math.floor((p.x-190+12)/24.0),(int)Math.floor((p.y-30+12)/24.0));}
        int radius(int s){return new int[]{25,37,49}[s];}
        int slotAt(Point p){if(p.x<90||p.x>=610||p.y<20||p.y>=540)return -1;return ((p.y-20)/130)*4+(p.x-90)/130;}
        protected void paintComponent(Graphics raw){super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();scale=Math.min(getWidth()/900.0,getHeight()/560.0);ox=(int)((getWidth()-900*scale)/2);oy=(int)((getHeight()-560*scale)/2);g.translate(ox,oy);g.scale(scale,scale);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setFont(GameFonts.body(15));if(gearsMode)paintGears(g);else paintWires(g);g.dispose();}
        void paintGears(Graphics2D g){GearBoardRenderer.paint(g,gears,selected,dragSize,drag);}
        void paintWires(Graphics2D g){WireBoardRenderer.paint(g,wires,active);}
    }
}
