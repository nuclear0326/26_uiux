package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import timeescape.puzzle.present.ModuleAssemblyPuzzle;

/** Approved module artwork around the unchanged rotation model and shared lifecycle. */
public final class ModuleAssemblyPanel extends RepairPuzzlePanel {
    static final Color CONTACT=new Color(104,212,219), SOCKET=new Color(213,180,118);
    ModuleAssemblyPuzzle model=new ModuleAssemblyPuzzle();
    final Board board=new Board();
    final JButton reset=new JButton("초기화");
    public ModuleAssemblyPanel(Runnable solved,Runnable close){this(solved,close,"현재 / 연구실 작업대");}
    public ModuleAssemblyPanel(Runnable solved,Runnable close,String context){
        super(solved,close);setLayout(new BorderLayout(0,16));
        add(PuzzleWorkspace.header(model.title(),context,this::closePuzzle),BorderLayout.NORTH);
        action.setText("조립 확인");action.addActionListener(e->verify());
        reset.addActionListener(e->{if(isFinished())return;model=new ModuleAssemblyPuzzle();refresh();});
        JPanel actions=new JPanel(new BorderLayout(0,12));actions.setOpaque(false);status.setFont(GameFonts.body(15));
        actions.add(status,BorderLayout.NORTH);actions.add(PuzzleWorkspace.buttons(reset,action),BorderLayout.SOUTH);
        add(PuzzleWorkspace.surface(board,"세 부품의 접점을\n소켓 방향에 맞추세요.","부품을 클릭하면\n시계 방향으로 90° 회전합니다.",actions),BorderLayout.CENTER);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"close-module");
        getActionMap().put("close-module",new AbstractAction(){public void actionPerformed(ActionEvent e){closePuzzle();}});
        refresh();
    }
    void turn(int index){if(isFinished())return;model.turn(index);refresh();}
    void refresh(){
        int count=0;for(int i=0;i<3;i++){
            if(model.value(i)==i)count++;
            String name=model.label(i)+" / 소켓 "+new String[]{"↑","→","↓"}[i];
            board.parts[i].getAccessibleContext().setAccessibleName(name);board.parts[i].setToolTipText(name);
        }
        status.setForeground(model.solved()?LabTheme.GREEN:LabTheme.TEXT);status.setText("접점 정렬 "+count+" / 3");board.repaint();
    }
    void verify(){
        if(isFinished())return;
        if(model.solved()){
            status.setForeground(LabTheme.GREEN);status.setText("모듈 조립이 완료되었습니다.");
            reset.setEnabled(false);action.setEnabled(false);for(JButton part:board.parts)part.setEnabled(false);solve();
        }else {status.setForeground(new Color(234,157,118));status.setText("접점과 소켓 방향을 확인하세요.");}
    }
    @Override protected void skipStage(){for(int i=0;i<3;i++)while(model.value(i)!=i)model.turn(i);refresh();verify();}
    final class Board extends JPanel {
        final JButton[] parts=new JButton[3];
        Board(){
            setLayout(null);
            for(int i=0;i<parts.length;i++){
                final int index=i;
                JButton button=new JButton(){@Override protected void paintComponent(Graphics g){} };
                button.setOpaque(false);button.setContentAreaFilled(false);button.setBorderPainted(false);button.setFocusPainted(false);button.setBorder(BorderFactory.createEmptyBorder());
                button.setCursor(GameCursors.interact());button.addActionListener(e->turn(index));
                button.addFocusListener(new FocusAdapter(){public void focusGained(FocusEvent e){repaint();}public void focusLost(FocusEvent e){repaint();}});
                parts[i]=button;add(button);
            }
        }
        double scale(){return Math.min(getWidth()/940.0,getHeight()/480.0);}
        @Override public void doLayout(){double s=scale();for(int i=0;i<3;i++)parts[i].setBounds((int)((getWidth()-940*s)/2+(24+i*302)*s),(int)((getHeight()-480*s)/2+64*s),(int)(288*s),(int)(378*s));}

        protected void paintComponent(Graphics raw){
            super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
            double s=Math.min(getWidth()/940.0,getHeight()/480.0);g.translate((getWidth()-940*s)/2,(getHeight()-480*s)/2);g.scale(s,s);
            // Compact shape legend, intentionally distinct from the goal and operation instructions below.
            g.setColor(CONTACT);g.fillRoundRect(28,23,16,12,2,2);for(int x=31;x<43;x+=5)g.fillRect(x,18,2,7);
            text(g,"회전 접점",54,34,GameFonts.body(15),LabTheme.TEXT);
            g.setColor(SOCKET);g.setStroke(new BasicStroke(2));g.draw(new Line2D.Double(173,19,173,35));g.drawLine(173,35,191,35);g.drawLine(191,35,191,19);
            text(g,"고정 소켓",201,34,GameFonts.body(15),LabTheme.TEXT);
            for(int i=0;i<3;i++)module(g,i,24+i*302,64,288,378);
            g.dispose();
        }
        void module(Graphics2D g,int id,int x,int y,int w,int h){
            int value=model.value(id);boolean aligned=value==id;int cx=x+w/2,cy=y+181;
            g.setPaint(new GradientPaint(x,y,new Color(29,44,49),x,y+h,new Color(18,31,37)));g.fillRoundRect(x,y,w,h,9,9);
            g.setStroke(new BasicStroke(1));g.setColor(aligned?new Color(103,150,128):new Color(66,83,87));g.drawRoundRect(x,y,w,h,9,9);
            if(parts[id].isFocusOwner()){g.setColor(LabTheme.TEXT);g.drawRoundRect(x+4,y+4,w-8,h-8,6,6);}
            LabTheme.screw(g,x+12,y+12);LabTheme.screw(g,x+w-12,y+12);LabTheme.screw(g,x+12,y+h-12);LabTheme.screw(g,x+w-12,y+h-12);
            g.setFont(GameFonts.button(21));g.setColor(LabTheme.TEXT);LabTheme.center(g,new String[]{"케이스","전원 셀","제어 모듈"}[id],cx,y+37);
            g.setColor(new Color(53,71,75));g.drawLine(x+30,y+53,x+w-30,y+53);
            // Stationary housing has just four indexing positions, not clock-style minute ticks.
            g.setColor(new Color(8,18,23));g.fillOval(cx-104,cy-104,208,208);
            g.setColor(new Color(86,106,109));g.setStroke(new BasicStroke(2));g.drawOval(cx-104,cy-104,208,208);
            g.setColor(new Color(45,66,73));g.setStroke(new BasicStroke(10));g.drawOval(cx-94,cy-94,188,188);
            g.setColor(new Color(132,150,147));g.setStroke(new BasicStroke(2));
            for(int dir=0;dir<4;dir++){double a=dir*Math.PI/2;g.drawLine(cx+(int)(Math.sin(a)*97),cy-(int)(Math.cos(a)*97),cx+(int)(Math.sin(a)*103),cy-(int)(Math.cos(a)*103));}
            // Fixed female socket: wide U-shaped collar opening inward.
            Graphics2D fixed=(Graphics2D)g.create();fixed.translate(cx,cy);fixed.rotate(id*Math.PI/2);
            fixed.setColor(new Color(12,22,25));fixed.fillRoundRect(-26,-118,52,40,5,5);
            fixed.setColor(SOCKET);fixed.setStroke(new BasicStroke(4,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND));
            Path2D u=new Path2D.Double();u.moveTo(-24,-80);u.lineTo(-24,-115);u.lineTo(24,-115);u.lineTo(24,-80);fixed.draw(u);
            for(int xx:new int[]{-12,0,12}){fixed.setColor(new Color(88,72,45));fixed.fillRect(xx-3,-110,6,22);fixed.setColor(SOCKET);fixed.fillRect(xx-2,-107,4,13);}
            fixed.dispose();
            // Rotatable component and its three solid contacts are visually separate from housing.
            Graphics2D part=(Graphics2D)g.create();part.translate(cx,cy);part.rotate(value*Math.PI/2);
            part.setColor(new Color(0,0,0,80));part.fillOval(-76,-73,154,154);
            part.setPaint(new GradientPaint(-76,-76,new Color(53,75,78),76,76,new Color(25,44,51)));part.fillOval(-76,-76,152,152);
            part.setColor(new Color(114,142,142));part.setStroke(new BasicStroke(1.5f));part.drawOval(-76,-76,152,152);
            part.setColor(new Color(19,36,42));part.fillOval(-65,-65,130,130);
            part.setColor(new Color(104,212,219,25));part.fillRoundRect(-25,-102,50,42,7,7);
            part.setColor(CONTACT);part.fillRoundRect(-21,-84,42,22,3,3);for(int xx:new int[]{-12,0,12})part.fillRoundRect(xx-3,-103,6,25,2,2);
            part.setColor(new Color(36,71,78));part.fillRect(-15,-73,30,5);
            part.setColor(new Color(120,148,147));part.setStroke(new BasicStroke(1.5f));
            if(id==0){part.drawRoundRect(-37,-31,74,62,7,7);part.drawRoundRect(-29,-23,58,46,3,3);for(int xx:new int[]{-31,31})for(int yy:new int[]{-25,25})part.fillOval(xx-2,yy-2,4,4);}
            else if(id==1){for(int xx:new int[]{-29,4}){part.drawRoundRect(xx,-30,25,60,5,5);part.fillRect(xx+7,-35,11,5);part.drawLine(xx+7,-16,xx+18,-16);part.drawLine(xx+12,-21,xx+12,-11);part.drawLine(xx+7,17,xx+18,17);}}
            else {part.drawRect(-27,-27,54,54);part.drawRect(-18,-18,36,36);for(int q=-18;q<=18;q+=12){part.drawLine(q,-27,q,-40);part.drawLine(q,27,q,40);part.drawLine(-27,q,-40,q);part.drawLine(27,q,40,q);}}
            part.dispose();
            // Direction readouts remove ambiguity even without relying on color.
            int row=y+306;
            g.setFont(GameFonts.button(16));g.setColor(CONTACT);LabTheme.center(g,"접점  "+new String[]{"↑","→","↓","←"}[value],cx-67,row);
            g.setColor(new Color(70,87,89));g.drawLine(cx,row-16,cx,row+3);
            g.setColor(SOCKET);LabTheme.center(g,"소켓  "+new String[]{"↑","→","↓"}[id],cx+67,row);
            g.setFont(GameFonts.body(15));g.setColor(aligned?LabTheme.GREEN:LabTheme.MUTED);LabTheme.center(g,aligned?"✓  접점 일치":"정렬 필요",cx,y+345);
        }
        void text(Graphics2D g,String text,int x,int y,Font f,Color color){g.setFont(f);g.setColor(color);g.drawString(text,x,y);}
    }
}
