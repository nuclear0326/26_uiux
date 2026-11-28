package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Native checkbox semantics with the menu's dark/gold visual treatment. */
final class MenuCheckBox extends JCheckBox {
    MenuCheckBox(String text){
        super(text);setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setRolloverEnabled(true);setFont(GameFonts.button(20));setCursor(GameCursors.interact());
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"),"toggle");getActionMap().put("toggle",new AbstractAction(){public void actionPerformed(ActionEvent e){doClick(0);}});
    }
    @Override protected void paintComponent(Graphics raw){
        Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);g.setFont(getFont());FontMetrics fm=g.getFontMetrics();
        int box=22,gap=12,group=box+gap+fm.stringWidth(getText()),x=(getWidth()-group)/2,y=(getHeight()-box)/2;
        Color ink=!isEnabled()?new Color(113,110,104):isSelected()||getModel().isRollover()||isFocusOwner()?DevicePanel.GOLD:DevicePanel.INK;
        g.setColor(new Color(10,13,15,180));g.fillRoundRect(x,y,box,box,4,4);g.setColor(ink);g.setStroke(new BasicStroke(1.4f));g.drawRoundRect(x,y,box,box,4,4);
        if(isSelected()){g.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.drawLine(x+5,y+11,x+9,y+16);g.drawLine(x+9,y+16,x+18,y+6);}
        g.drawString(getText(),x+box+gap,(getHeight()-fm.getHeight())/2+fm.getAscent());
        if(isFocusOwner()){g.setStroke(new BasicStroke(1));g.setColor(new Color(234,196,129,100));g.drawRoundRect(x-8,y-7,group+16,box+14,8,8);}g.dispose();
    }
}
