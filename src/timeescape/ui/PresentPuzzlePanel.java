package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import timeescape.puzzle.PresentPuzzleModel;

/** Dialog content shared with offscreen visual verification. */
public final class PresentPuzzlePanel extends JPanel {
    private PresentPuzzleModel puzzle;
    private final JLabel status=new JLabel();
    private final JPanel instruments=new JPanel();
    public PresentPuzzlePanel(String id,Runnable onSolved,Runnable onClose){
        super(new BorderLayout(0,18));puzzle=new PresentPuzzleModel(id);
        setBackground(LabTheme.BACK);setBorder(BorderFactory.createEmptyBorder(24,30,24,30));
        JPanel header=new JPanel(new BorderLayout(0,8));header.setOpaque(false);
        JLabel serial=new JLabel("TIME ESCAPE  /  "+serial(id));serial.setForeground(LabTheme.GOLD);serial.setFont(GameFonts.body(13));header.add(serial,BorderLayout.NORTH);
        JLabel title=new JLabel(puzzle.title());title.setForeground(LabTheme.TEXT);title.setFont(GameFonts.title(27));header.add(title,BorderLayout.CENTER);
        JTextArea instructions=new JTextArea(puzzle.instruction());instructions.setForeground(LabTheme.MUTED);instructions.setOpaque(false);instructions.setEditable(false);instructions.setFocusable(false);instructions.setLineWrap(true);instructions.setWrapStyleWord(true);instructions.setFont(GameFonts.body(15));instructions.setRows(3);header.add(instructions,BorderLayout.SOUTH);add(header,BorderLayout.NORTH);
        instruments.setOpaque(false);instruments.setLayout(id.equals("PZ008")?new GridLayout(2,2,14,14):new GridLayout(1,puzzle.size(),12,12));add(instruments,BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,12));bottom.setOpaque(false);status.setFont(GameFonts.body(15));status.setForeground(LabTheme.GREEN);bottom.add(status,BorderLayout.NORTH);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));actions.setOpaque(false);
        JButton reset=LabTheme.button("다시 맞추기"),close=LabTheme.button("조사 종료"),check=LabTheme.button("작동 확인");
        reset.addActionListener(e->{puzzle=new PresentPuzzleModel(id);refresh();});close.addActionListener(e->onClose.run());
        check.addActionListener(e->{if(puzzle.solved())onSolved.run();else{status.setForeground(new Color(234,157,118));status.setText("아직 정렬되지 않았습니다. 계기와 연결 방향을 확인하세요.");}});
        actions.add(reset);actions.add(close);actions.add(check);bottom.add(actions,BorderLayout.SOUTH);add(bottom,BorderLayout.SOUTH);
        for(int i=0;i<puzzle.size();i++)instruments.add(new Instrument(i));refresh();
    }
    private static String serial(String id){return switch(id){case "PZ004"->"POWER DISTRIBUTION";case "PZ006"->"RESEARCH ARCHIVE";case "PZ008"->"DEVICE SCHEMATIC";case "PZ010"->"MODULE ASSEMBLY";default->"TEMPORAL ALIGNMENT";};}
    private void refresh(){status.setForeground(LabTheme.GREEN);status.setText(puzzle.id.equals("PZ004")?"출력 "+puzzle.power()+" / 16  ·  필요한 회로만 연결하세요.":"부품을 클릭하여 조절  ·  Tab으로 선택 / Space로 회전");for(Component c:instruments.getComponents()){Instrument b=(Instrument)c;b.getAccessibleContext().setAccessibleName(puzzle.label(b.index));b.setToolTipText(puzzle.label(b.index));}instruments.repaint();}
    private final class Instrument extends JButton {
        final int index;
        Instrument(int index){this.index=index;setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setRolloverEnabled(true);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));addActionListener(e->{puzzle.turn(index);refresh();});}
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);int w=getWidth(),h=getHeight();
            g.setPaint(new GradientPaint(0,0,new Color(43,51,50),w,h,LabTheme.PANEL));g.fillRoundRect(1,1,w-2,h-2,14,14);
            g.setStroke(new BasicStroke(isFocusOwner()?2:1));g.setColor(getModel().isRollover()||isFocusOwner()?LabTheme.GOLD:LabTheme.LINE);g.drawRoundRect(1,1,w-3,h-3,14,14);
            LabTheme.screw(g,12,12);LabTheme.screw(g,w-13,h-13);g.setFont(GameFonts.body(12));g.setColor(LabTheme.MUTED);LabTheme.center(g,String.format("%02d",index+1),w/2,24);
            switch(puzzle.id){case "PZ004"->power(g,w,h);case "PZ006"->code(g,w,h);case "PZ008"->schematic(g,w,h);case "PZ010"->module(g,w,h);default->clock(g,w,h);}g.dispose();
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
        void schematic(Graphics2D g,int w,int h){
            g.setColor(new Color(24,53,61));g.fillRect(22,33,w-44,h-56);g.setColor(new Color(40,76,82));for(int x=22;x<w-22;x+=18)g.drawLine(x,33,x,h-23);for(int y=33;y<h-23;y+=18)g.drawLine(22,y,w-23,y);
            int cx=w/2,cy=h/2+2;Graphics2D p=(Graphics2D)g.create();p.translate(cx,cy);p.rotate(puzzle.value(index)*Math.PI/2);p.setColor(new Color(158,214,210));p.setStroke(new BasicStroke(3));p.drawLine(0,27,0,-27);p.drawLine(0,-27,-10,-14);p.drawLine(0,-27,10,-14);p.drawOval(-18,12,36,14);p.dispose();
            g.setFont(GameFonts.body(13));g.setColor(LabTheme.TEXT);g.drawString("연결 기준 "+new String[]{"→","↓","←","↑"}[index],32,h-30);
        }
        void dial(Graphics2D g,int cx,int cy,int radius){
            g.setColor(new Color(11,19,21));g.fillOval(cx-radius,cy-radius,radius*2,radius*2);g.setColor(LabTheme.GOLD);g.setStroke(new BasicStroke(2));g.drawOval(cx-radius,cy-radius,radius*2,radius*2);
            for(int k=0;k<60;k++){double a=k*Math.PI/30;int inner=radius-(k%5==0?12:5);g.setColor(k%5==0?LabTheme.GOLD:LabTheme.LINE);g.drawLine(cx+(int)(Math.sin(a)*inner),cy-(int)(Math.cos(a)*inner),cx+(int)(Math.sin(a)*(radius-3)),cy-(int)(Math.cos(a)*(radius-3)));}
            double a=puzzle.value(index)*Math.PI/2;g.setColor(LabTheme.GREEN);g.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(cx,cy,cx+(int)(Math.sin(a)*(radius-22)),cy-(int)(Math.cos(a)*(radius-22)));g.setColor(LabTheme.GOLD);g.fillOval(cx-5,cy-5,10,10);
        }
        void module(Graphics2D g,int w,int h){
            int cx=w/2,cy=h/2-8,radius=Math.min(w/2-24,h/2-72);dial(g,cx,cy,radius);g.setColor(LabTheme.GOLD);double a=index*Math.PI/2;int x=cx+(int)(Math.sin(a)*(radius+9)),y=cy-(int)(Math.cos(a)*(radius+9));g.fillRect(x-5,y-5,10,10);
            g.setFont(GameFonts.body(17));g.setColor(LabTheme.TEXT);LabTheme.center(g,new String[]{"케이스","전원 셀","제어 모듈"}[index],cx,h-44);g.setFont(GameFonts.body(13));g.setColor(LabTheme.MUTED);LabTheme.center(g,"소켓 "+new String[]{"↑","→","↓"}[index]+" 에 접점 정렬",cx,h-22);
        }
        void clock(Graphics2D g,int w,int h){
            int cx=w/2,cy=h/2,radius=Math.min(w/2-22,h/2-60);dial(g,cx,cy,radius);g.setFont(GameFonts.body(17));g.setColor(LabTheme.GOLD);LabTheme.center(g,"XII",cx,cy-radius-10);
            g.setFont(GameFonts.body(15));g.setColor(LabTheme.TEXT);LabTheme.center(g,"링 "+(char)('A'+index)+"  ↔  링 "+(char)('A'+(index+1)%3),cx,h-42);g.setFont(GameFonts.body(13));g.setColor(LabTheme.MUTED);LabTheme.center(g,"두 바늘을 함께 회전",cx,h-21);
        }
    }
}
