package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import timeescape.puzzle.AccidentLogEvidence;

/** In-puzzle read-only reference overlay. The owning panel blocks puzzle input. */
final class AccidentEvidenceOverlay extends JPanel {
    final JPanel paper=new JPanel(new BorderLayout(0,18));
    final JPanel pages=new JPanel(new CardLayout());
    final JButton normal=new JButton("정상 작동 절차"),incident=new JButton("사고 기록"),dismiss=new JButton("×");
    final JLabel source=new JLabel();
    AccidentEvidenceOverlay(Runnable close){
        setLayout(new GridBagLayout());setOpaque(false);setFocusable(true);setFocusCycleRoot(true);setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        addMouseListener(new MouseAdapter(){});addMouseMotionListener(new MouseMotionAdapter(){});addMouseWheelListener(MouseWheelEvent::consume);
        paper.setBackground(new Color(17,27,32));paper.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(111,103,82)),BorderFactory.createEmptyBorder(20,26,22,26)));
        JPanel top=new JPanel(new BorderLayout(0,16));top.setOpaque(false);
        JPanel heading=new JPanel(new BorderLayout());heading.setOpaque(false);heading.add(label("기록 자료",GameFonts.title(26),LabTheme.TEXT),BorderLayout.WEST);
        dismiss.setFont(GameFonts.button(28));dismiss.setForeground(LabTheme.TEXT);dismiss.setBorder(BorderFactory.createEmptyBorder(0,8,0,0));dismiss.setContentAreaFilled(false);dismiss.setCursor(GameCursors.interact());dismiss.setToolTipText("기록 닫기");dismiss.getAccessibleContext().setAccessibleName("기록 닫기");dismiss.addActionListener(e->close.run());heading.add(dismiss,BorderLayout.EAST);top.add(heading,BorderLayout.NORTH);
        JPanel tabs=new JPanel(new GridLayout(1,2,10,0));tabs.setOpaque(false);
        for(JButton b:new JButton[]{normal,incident}){PuzzleChrome.styleButton(b);tabStyle(b);b.setPreferredSize(new Dimension(250,42));tabs.add(b);}
        normal.addActionListener(e->showPage(false));incident.addActionListener(e->showPage(true));top.add(tabs,BorderLayout.CENTER);
        source.setFont(GameFonts.body(14));source.setForeground(LabTheme.MUTED);top.add(source,BorderLayout.SOUTH);paper.add(top,BorderLayout.NORTH);
        pages.setOpaque(false);pages.add(normalPage(),"normal");pages.add(incidentPage(),"incident");paper.add(pages,BorderLayout.CENTER);
        paper.add(label("열람 중인 자료를 닫으면 분석 화면으로 돌아갑니다.",GameFonts.body(14),LabTheme.MUTED),BorderLayout.SOUTH);add(paper);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"dismiss");getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"),"dismiss");
        getActionMap().put("dismiss",new AbstractAction(){public void actionPerformed(ActionEvent e){close.run();}});
        showPage(false);setVisible(false);
    }
    void showPage(boolean accident){((CardLayout)pages.getLayout()).show(pages,accident?"incident":"normal");normal.putClientProperty("selected",!accident);incident.putClientProperty("selected",accident);normal.repaint();incident.repaint();source.setText(accident?"USB 복구 자료 / 장치별 기록 / 공통 시각 정보 일부 손상":"연구실 보관 문서 / 실험 장치 운용 지침 발췌");}
    @Override public void doLayout(){paper.setPreferredSize(new Dimension(Math.min(900,getWidth()-64),Math.min(712,getHeight()-72)));super.doLayout();}
    @Override protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();g.setColor(new Color(0,0,0,180));g.fillRect(0,0,getWidth(),getHeight());g.dispose();}
    private static JLabel label(String text,Font font,Color color){JLabel l=new JLabel(text);l.setFont(font);l.setForeground(color);return l;}
    private static JTextArea text(String text){JTextArea t=new JTextArea(text);t.setFont(GameFonts.body(16));t.setForeground(LabTheme.TEXT);t.setEditable(false);t.setFocusable(false);t.setOpaque(false);t.setLineWrap(true);t.setWrapStyleWord(true);t.setRows(text.split("\n").length);return t;}
    private static JPanel normalPage(){
        JPanel p=new JPanel(new GridLayout(6,1,0,6));p.setOpaque(false);
        for(var e:AccidentLogEvidence.PROCEDURE){
            JPanel row=new JPanel(new BorderLayout(14,0));row.setBackground(new Color(25,40,47));
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PuzzleWorkspace.LINE),BorderFactory.createEmptyBorder(6,12,6,12)));
            JLabel title=label(e.title(),GameFonts.button(17),PuzzleWorkspace.ACCENT);title.setPreferredSize(new Dimension(175,30));row.add(title,BorderLayout.WEST);
            row.add(text(e.text()),BorderLayout.CENTER);p.add(row);
        }
        return p;
    }
    private static JPanel incidentPage(){JPanel p=new JPanel(new GridLayout(6,1,0,8));p.setOpaque(false);for(var e:AccidentLogEvidence.INCIDENT){JPanel row=new JPanel(new BorderLayout(20,0));row.setBackground(new Color(25,40,47));row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(PuzzleWorkspace.LINE),BorderFactory.createEmptyBorder(12,18,12,18)));JLabel title=label(e.title(),GameFonts.button(16),LabTheme.TEXT);title.setPreferredSize(new Dimension(150,30));row.add(title,BorderLayout.WEST);row.add(label(e.text(),GameFonts.body(16),LabTheme.TEXT),BorderLayout.CENTER);p.add(row);}return p;}
    private static void tabStyle(JButton b){b.setUI(new javax.swing.plaf.basic.BasicButtonUI(){public void paint(Graphics raw,JComponent c){Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);boolean selected=Boolean.TRUE.equals(c.getClientProperty("selected"));g.setColor(selected?new Color(40,53,57):new Color(20,29,33));g.fillRoundRect(0,0,c.getWidth(),c.getHeight(),7,7);g.setColor(selected?LabTheme.GOLD:PuzzleWorkspace.LINE);g.drawRoundRect(1,1,c.getWidth()-3,c.getHeight()-3,7,7);if(selected){g.setStroke(new BasicStroke(2));g.drawLine(16,c.getHeight()-4,c.getWidth()-16,c.getHeight()-4);}g.setFont(GameFonts.button(15));g.setColor(selected?LabTheme.TEXT:LabTheme.MUTED);LabTheme.center(g,b.getText(),c.getWidth()/2,(c.getHeight()-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());g.dispose();}});}
}
