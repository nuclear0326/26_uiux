package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Ending notice only. Completion and persistence remain owned by the caller. */
final class EndingPanel extends JPanel {
    private static final String TITLE="탈출 성공", MESSAGE="장치 복구와 최종 좌표 입력 완료!";
    final JButton confirm,dismiss;
    final Rectangle panel=new Rectangle();
    private final BufferedImage original,blurred;
    private final int header;
    private final Runnable close;
    private boolean finished;
    private Font titleFont,bodyFont;
    EndingPanel(BufferedImage background,int header,Runnable close){
        original=background;this.header=header;this.close=close;blurred=DevicePanel.blur(background,header);
        setLayout(null);setBackground(new Color(5,7,9));setCursor(GameCursors.normal());
        confirm=button("확인",true);dismiss=button("",false);
        confirm.addActionListener(e->finish());dismiss.addActionListener(e->finish());
        dismiss.setToolTipText("닫기 (Esc)");dismiss.getAccessibleContext().setAccessibleName(TITLE+" 닫기");
        add(confirm);add(dismiss);setFocusCycleRoot(true);setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"close");
        getActionMap().put("close",new AbstractAction(){public void actionPerformed(ActionEvent e){finish();}});
        getAccessibleContext().setAccessibleName(TITLE);getAccessibleContext().setAccessibleDescription(MESSAGE);
    }
    void cancelSession(){finished=true;}
    private void finish(){if(finished)return;finished=true;close.run();}
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
        int w=Math.min(520,Math.max(280,getWidth()-40)),h=Math.min(350,Math.max(300,getHeight()-header-40));
        panel.setBounds((getWidth()-w)/2,header+(getHeight()-header-h)/2,w,h);
        float scale=Math.min(1,w/520f);titleFont=GameFonts.title(29*scale);bodyFont=GameFonts.body(17*scale);
        confirm.setBounds(panel.x+w/2-90,panel.y+h-80,180,40);
        confirm.setFont(GameFonts.button(23*scale));dismiss.setBounds(panel.x+w-47,panel.y+6,40,40);
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
        center(g,MESSAGE,panel.y+150,bodyFont,DevicePanel.INK);
        center(g,"미래 연구실에서 탈출했습니다.",panel.y+178,bodyFont,DevicePanel.BODY);
        center(g,"프로토타입 엔딩",panel.y+panel.height-122,GameFonts.body(14*Math.min(1,panel.width/520f)),new Color(151,147,137));g.dispose();
    }
    private void center(Graphics2D g,String text,int y,Font font,Color color){g.setColor(color);g.setFont(font);g.drawString(text,panel.x+(panel.width-g.getFontMetrics().stringWidth(text))/2,y);}
    boolean textFits(){return getFontMetrics(titleFont).stringWidth(TITLE)<panel.width-70&&getFontMetrics(bodyFont).stringWidth(MESSAGE)<panel.width-40&&panel.y+178+getFontMetrics(bodyFont).getDescent()<panel.y+panel.height-122+10;}
}
