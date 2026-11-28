package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Shared menu confirmation surface. Callers own actions and persistence. */
class MenuConfirmPanel extends JPanel {
    private final String TITLE,MESSAGE;
    final JButton cancel,confirm,dismiss;
    final Rectangle panel=new Rectangle();
    private final BufferedImage original,blurred;
    private final int header;
    private final Runnable close;
    private boolean accepted;
    private boolean finished;
    private Font titleFont,bodyFont;
    private java.util.List<String> lines=java.util.List.of();
    MenuConfirmPanel(BufferedImage background,int header,Runnable close,String title,String message,String action){
        TITLE=title;MESSAGE=message;
        original=background;this.header=header;this.close=close;blurred=DevicePanel.blur(background,header);
        setLayout(null);setBackground(new Color(5,7,9));setCursor(GameCursors.normal());
        cancel=button("취소",false);confirm=button(action,true);dismiss=button("",false);
        cancel.addActionListener(e->finish(false));confirm.addActionListener(e->finish(true));dismiss.addActionListener(e->finish(false));
        dismiss.setToolTipText("닫기 (Esc)");dismiss.getAccessibleContext().setAccessibleName(title+" 취소");
        add(cancel);add(confirm);add(dismiss);setFocusCycleRoot(true);setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"cancel");
        getActionMap().put("cancel",new AbstractAction(){public void actionPerformed(ActionEvent e){finish(false);}});
        getAccessibleContext().setAccessibleName(TITLE);getAccessibleContext().setAccessibleDescription(MESSAGE);
    }
    boolean isConfirmed(){return accepted;}
    void cancelSession(){finished=true;}
    private void finish(boolean outcome){if(finished)return;finished=true;accepted=outcome;close.run();}
    private JButton button(String text,boolean primary){
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics raw){
            Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
            g.setColor(primary||getModel().isRollover()||isFocusOwner()?DevicePanel.GOLD:DevicePanel.INK);
            if(getText().isEmpty()){g.setStroke(new BasicStroke(2.7f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));int x=getWidth()/2,y=getHeight()/2;g.drawLine(x-5,y-5,x+5,y+5);g.drawLine(x+5,y-5,x-5,y+5);}
            else{g.setFont(getFont());FontMetrics fm=g.getFontMetrics();g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());}g.dispose();
        }};
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setRolloverEnabled(true);b.setCursor(GameCursors.interact());
        b.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"),"activate");b.getActionMap().put("activate",new AbstractAction(){public void actionPerformed(ActionEvent e){b.doClick(0);}});return b;
    }
    @Override public void doLayout(){
        int w=Math.min(520,Math.max(280,getWidth()-40)),h=Math.min(290,Math.max(260,getHeight()-header-40));
        panel.setBounds((getWidth()-w)/2,header+(getHeight()-header-h)/2,w,h);
        float scale=Math.min(1,w/520f);titleFont=GameFonts.title(29*scale);bodyFont=GameFonts.body(17*scale);lines=DevicePanel.wrap(MESSAGE,getFontMetrics(bodyFont),w-60);
        int buttonW=(w-100)/2;cancel.setBounds(panel.x+w/2-(int)(115*scale)-buttonW/2,panel.y+h-78,buttonW,40);confirm.setBounds(panel.x+w/2+(int)(115*scale)-buttonW/2,panel.y+h-78,buttonW,40);
        cancel.setFont(GameFonts.button(23*scale));confirm.setFont(GameFonts.button(23*scale));dismiss.setBounds(panel.x+w-47,panel.y+6,40,40);
    }
    @Override protected void paintComponent(Graphics raw){
        super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if(header>0)g.drawImage(original,0,0,getWidth(),header,0,0,original.getWidth(),header,null);
        g.drawImage(blurred,0,header,getWidth(),getHeight()-header,null);g.setColor(new Color(0,0,0,32));g.fillRect(0,header,getWidth(),getHeight()-header);
        g.setColor(new Color(0,0,0,30));g.fillRoundRect(panel.x-3,panel.y+5,panel.width+6,panel.height+4,24,24);
        g.setColor(new Color(5,7,9,199));g.fillRoundRect(panel.x,panel.y,panel.width,panel.height,20,20);
        g.setColor(new Color(151,151,145,90));g.drawRoundRect(panel.x,panel.y,panel.width-1,panel.height-1,20,20);
        g.setColor(new Color(155,155,148,150));g.drawLine(panel.x+panel.width/6,panel.y+88,panel.x+panel.width*5/6,panel.y+88);
        center(g,TITLE,panel.y+62,titleFont,DevicePanel.INK);
        for(int i=0;i<lines.size();i++)center(g,lines.get(i),panel.y+145+i*24,bodyFont,DevicePanel.BODY);g.dispose();
    }
    private void center(Graphics2D g,String text,int y,Font font,Color color){g.setColor(color);g.setFont(font);g.drawString(text,panel.x+(panel.width-g.getFontMetrics().stringWidth(text))/2,y);}
    boolean textFits(){return getFontMetrics(titleFont).stringWidth(TITLE)<panel.width-70&&panel.y+145+(lines.size()-1)*24+getFontMetrics(bodyFont).getDescent()<cancel.getY()-10;}
}
