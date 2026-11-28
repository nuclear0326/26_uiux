package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.present.PresentPuzzle;
import timeescape.puzzle.present.PresentPuzzles;

/** Dialog content shared with offscreen visual verification. */
public final class PresentPuzzlePanel extends JPanel {
    private PresentPuzzle puzzle;
    private final JLabel status=new JLabel();
    private final JPanel instruments=new JPanel();
    public PresentPuzzlePanel(String id,Runnable onSolved,Runnable onClose){
        this(id,onSolved,onClose,"현재 / 연구실");
    }
    public PresentPuzzlePanel(String id,Runnable onSolved,Runnable onClose,String context){
        super(new BorderLayout(0,18));if(id.equals("PZ008")){add(new SchematicPuzzlePanel(onSolved,onClose,context),BorderLayout.CENTER);return;}if(id.equals("PZ010")){add(new ModuleAssemblyPanel(onSolved,onClose,context),BorderLayout.CENTER);return;}if(id.equals("PZ011")){add(new TimeCoordinatesPanel(onSolved,onClose,context),BorderLayout.CENTER);return;}puzzle=PresentPuzzles.create(id);
        PuzzleChrome.decorate(this);
        add(PuzzleChrome.header(puzzle.title(),context,puzzle.instruction(),onClose),BorderLayout.NORTH);
        instruments.setOpaque(false);instruments.setLayout(new GridLayout(1,puzzle.size(),12,12));add(instruments,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,12));bottom.setOpaque(false);status.setFont(GameFonts.body(15));status.setForeground(LabTheme.GREEN);bottom.add(status,BorderLayout.NORTH);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);
        JButton reset=LabTheme.button("다시 맞추기"),close=LabTheme.button("조사 종료"),check=LabTheme.button("작동 확인");
        reset.addActionListener(e->{puzzle=PresentPuzzles.create(id);refresh();});close.addActionListener(e->onClose.run());
        check.addActionListener(e->{if(puzzle.solved())onSolved.run();else{status.setForeground(new Color(234,157,118));status.setText("아직 정렬되지 않았습니다. 계기와 연결 방향을 확인하세요.");}});
        actions.add(reset);actions.add(close);actions.add(check);bottom.add(actions,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        for(int i=0;i<puzzle.size();i++)instruments.add(new Instrument(i));refresh();
    }
    private static String serial(String id){return switch(id){case "PZ004"->"POWER DISTRIBUTION";case "PZ006"->"RESEARCH ARCHIVE";case "PZ008"->"DEVICE SCHEMATIC";case "PZ010"->"MODULE ASSEMBLY";default->"TEMPORAL ALIGNMENT";};}
    private void refresh(){status.setForeground(LabTheme.GREEN);status.setText(PuzzleChrome.text(puzzle.status()));for(Component c:instruments.getComponents()){Instrument b=(Instrument)c;b.getAccessibleContext().setAccessibleName(puzzle.label(b.index));b.setToolTipText(puzzle.label(b.index));}instruments.repaint();}
    private final class Instrument extends JButton {
        final int index;
        Instrument(int index){this.index=index;setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setRolloverEnabled(true);setCursor(GameCursors.interact());addActionListener(e->{puzzle.turn(index);refresh();});}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);int w=getWidth(),h=getHeight();
            g.setPaint(new GradientPaint(0,0,new Color(43,51,50),w,h,LabTheme.PANEL));g.fillRoundRect(1,1,w-2,h-2,14,14);
            g.setStroke(new BasicStroke(isFocusOwner()?2:1));g.setColor(getModel().isRollover()||isFocusOwner()?LabTheme.GOLD:LabTheme.LINE);g.drawRoundRect(1,1,w-3,h-3,14,14);
            LabTheme.screw(g,12,12);LabTheme.screw(g,w-13,h-13);g.setFont(GameFonts.body(12));g.setColor(LabTheme.MUTED);LabTheme.center(g,String.format("%02d",index+1),w/2,24);
            switch(puzzle.id()){case "PZ004"->power(g,w,h);case "PZ006"->code(g,w,h);default->throw new IllegalStateException(puzzle.id());}g.dispose();
        }
        void power(Graphics2D g,int w,int h){
            boolean on=puzzle.value(index)==1;int cx=w/2,cy=h/2;int[] amps={2,3,5,7,11};
            g.setColor(new Color(10,17,18));g.fillRoundRect(cx-27,cy-62,54,116,14,14);
            g.setPaint(new GradientPaint(cx-24,0,new Color(91,101,99),cx+24,0,new Color(33,41,42)));g.fillRoundRect(cx-23,cy+(on?-55:5),46,43,8,8);
            g.setColor(LabTheme.TEXT);for(int j=0;j<3;j++)g.drawLine(cx-13,cy+(on?-43:17)+j*6,cx+13,cy+(on?-43:17)+j*6);
            g.setColor(on?LabTheme.GREEN:new Color(87,77,57));g.fillOval(cx-6,54,12,12);
            g.setFont(GameFonts.body(25));g.setColor(LabTheme.GOLD);LabTheme.center(g,amps[index]+" A",cx,h-64);
            g.setFont(GameFonts.body(12));g.setColor(on?LabTheme.GREEN:LabTheme.MUTED);LabTheme.center(g,on?"CONNECTED":"ISOLATED",cx,h-36);
        }
        void code(Graphics2D g,int w,int h){
            int cx=w/2,cy=h/2;g.setColor(new Color(207,194,157));g.fillRoundRect(16,42,w-32,43,3,3);g.setColor(new Color(55,55,43));g.setFont(GameFonts.body(24));LabTheme.center(g,new String[]{"□","△","☆","○"}[index],cx,72);
            g.setColor(new Color(9,17,16));g.fillRoundRect(18,cy-42,w-36,100,10,10);g.setColor(LabTheme.GREEN);g.setFont(GameFonts.body(65));LabTheme.center(g,""+puzzle.value(index),cx,cy+31);
            g.setColor(LabTheme.MUTED);g.setFont(GameFonts.body(13));LabTheme.center(g,"ROTATE  +1",cx,h-35);
        }
    }
}
