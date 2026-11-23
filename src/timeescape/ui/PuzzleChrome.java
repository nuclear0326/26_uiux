package timeescape.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/** Shared centered heading and restrained instrument frame for all puzzle hosts. */
final class PuzzleChrome {
 private PuzzleChrome(){}
 static String text(String value){return value.replace('\u00b7','/');}
 static void decorate(JPanel panel){
  panel.setBackground(LabTheme.BACK);
  panel.setBorder(new AbstractBorder(){
   public Insets getBorderInsets(Component c){return new Insets(22,30,24,30);}
   public Insets getBorderInsets(Component c,Insets i){i.set(22,30,24,30);return i;}
   public void paintBorder(Component c,Graphics raw,int x,int y,int w,int h){
    Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
    g.setColor(new Color(43,46,44));g.setStroke(new BasicStroke(8));g.drawRoundRect(x+5,y+5,w-11,h-11,13,13);
    g.setStroke(new BasicStroke(1));g.setColor(new Color(106,92,66));g.drawRoundRect(x+10,y+10,w-21,h-21,9,9);
    for(int sx:new int[]{x+17,x+w-18})for(int sy:new int[]{y+17,y+h-18})LabTheme.screw(g,sx,sy);g.dispose();
   }
  });
 }
 static JPanel header(String title,String context,String instructions,Runnable close){
  JPanel header=new JPanel(new BorderLayout(0,9));header.setOpaque(false);
  JPanel titleRow=new JPanel(new BorderLayout(12,0));titleRow.setOpaque(false);
  titleRow.add(Box.createHorizontalStrut(34),BorderLayout.WEST);
  JComponent heading=new JComponent(){
   public Dimension getPreferredSize(){return new Dimension(500,48);}
   protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
    float size=30;Font f=GameFonts.title(size);while(size>20&&g.getFontMetrics(f).stringWidth(title)>getWidth()-32)f=GameFonts.title(--size);
    g.setFont(f);int tw=g.getFontMetrics().stringWidth(title),cx=getWidth()/2,cy=getHeight()/2;
    g.setColor(new Color(98,86,63));if(cx-tw/2>35){g.drawLine(0,cy,cx-tw/2-24,cy);g.drawLine(cx+tw/2+24,cy,getWidth(),cy);}
    g.setColor(LabTheme.TEXT);LabTheme.center(g,title,cx,cy+(g.getFontMetrics().getAscent()-g.getFontMetrics().getDescent())/2);g.dispose();
   }
  };titleRow.add(heading,BorderLayout.CENTER);
  JButton dismiss=new JButton("×");dismiss.setFont(GameFonts.body(27));dismiss.setForeground(LabTheme.MUTED);dismiss.setContentAreaFilled(false);dismiss.setBorderPainted(false);dismiss.setBorder(BorderFactory.createEmptyBorder());dismiss.setMargin(new Insets(0,0,0,0));dismiss.setPreferredSize(new Dimension(34,40));dismiss.setCursor(GameCursors.interact());dismiss.setToolTipText("조사 종료 (Esc)");dismiss.getAccessibleContext().setAccessibleName("조사 종료");dismiss.addActionListener(e->close.run());titleRow.add(dismiss,BorderLayout.EAST);
  JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(titleRow,BorderLayout.CENTER);
  JLabel sub=new JLabel(text(context),SwingConstants.CENTER);sub.setFont(GameFonts.body(14));sub.setForeground(LabTheme.MUTED);top.add(sub,BorderLayout.SOUTH);header.add(top,BorderLayout.NORTH);
  JTextArea help=new JTextArea(text(instructions));help.setFont(GameFonts.body(15));help.setForeground(LabTheme.TEXT);help.setEditable(false);help.setFocusable(false);help.setOpaque(false);help.setLineWrap(true);help.setWrapStyleWord(true);help.setRows(2);help.setBorder(BorderFactory.createEmptyBorder(7,0,0,0));header.add(help,BorderLayout.CENTER);
  return header;
 }
 static void styleButton(JButton b){b.setFont(GameFonts.button(15));b.setForeground(LabTheme.TEXT);b.setBackground(LabTheme.PANEL);b.setOpaque(true);b.setFocusPainted(false);b.setCursor(GameCursors.interact());b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LabTheme.LINE),BorderFactory.createEmptyBorder(10,13,10,13)));}
 static void bindClose(JRootPane root,Runnable close){root.registerKeyboardAction(e->close.run(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);}
}
