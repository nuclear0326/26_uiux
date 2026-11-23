package timeescape.ui;
import java.awt.*;
import javax.swing.*;
final class LabTheme {
    static final Color BACK=new Color(17,23,25), PANEL=new Color(29,37,39), LINE=new Color(72,83,81);
    static final Color GOLD=new Color(211,179,118), TEXT=new Color(235,232,217), MUTED=new Color(158,175,169), GREEN=new Color(129,212,172);
    static void quality(Graphics2D g){g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);}
    static void center(Graphics2D g,String s,int x,int y){g.drawString(s,x-g.getFontMetrics().stringWidth(s)/2,y);}
    static void screw(Graphics2D g,int x,int y){g.setColor(LINE);g.fillOval(x-4,y-4,8,8);g.setColor(BACK);g.drawLine(x-2,y+2,x+2,y-2);}
    static void styleScroll(JScrollPane scroll){
        scroll.setBorder(BorderFactory.createMatteBorder(0,1,0,0,LINE));scroll.getViewport().setBackground(BACK);
        JScrollBar bar=scroll.getVerticalScrollBar();bar.setPreferredSize(new Dimension(10,0));bar.setUnitIncrement(24);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){thumbColor=LINE;trackColor=BACK;}
            @Override protected JButton createDecreaseButton(int orientation){return zero();}
            @Override protected JButton createIncreaseButton(int orientation){return zero();}
            private JButton zero(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
    }
    static JButton button(String name){
        JButton b=new JButton(name){@Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();quality(g);g.setColor(getModel().isRollover()?new Color(67,64,48):PANEL);g.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
            g.setColor(isFocusOwner()?TEXT:GOLD);g.drawRoundRect(1,1,getWidth()-3,getHeight()-3,8,8);g.setFont(GameFonts.button(16));center(g,getText(),getWidth()/2,(getHeight()-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());g.dispose();
        }};
        b.setPreferredSize(new Dimension(128,42));b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setCursor(GameCursors.interact());return b;
    }
}
