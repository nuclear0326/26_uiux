package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;
import javax.swing.*;

/** Menu-style device surface. Snapshot/blur is prepared once; actions stay outside rendering. */
final class DevicePanel extends JPanel {
    static final Color INK=new Color(241,229,205),BODY=new Color(188,184,173),GOLD=new Color(234,196,129);
    final Rectangle panel=new Rectangle();
    final JButton close,action,dismiss;
    private final BufferedImage original,blurred;
    private final int headerHeight;
    private final String status,body;
    private final boolean enabled;
    private Font titleFont,statusFont,bodyFont;
    private java.util.List<String> lines=java.util.List.of();
    private int bodyTop,lineHeight;

    DevicePanel(BufferedImage background,int headerHeight,DeviceActions actions,Runnable cancel){
        this.original=background;this.headerHeight=Math.max(0,headerHeight);blurred=blur(background,this.headerHeight);
        String[] parts=actions.describe().split("\n",2);
        status=parts[0].replace("장치 상태: ","").replace("대체 부품: ","대체 부품 ");
        body=parts.length>1?parts[1].strip().replace("뒤 작업대에서","뒤\n작업대에서"):"";enabled=actions.isActionEnabled();
        setLayout(null);setBackground(new Color(5,7,9));setCursor(GameCursors.normal());
        close=textButton("닫기",false);action=textButton(actions.actionName(),true);dismiss=textButton("",false);
        action.setEnabled(enabled);close.addActionListener(e->cancel.run());dismiss.addActionListener(e->cancel.run());action.addActionListener(e->actions.activate());
        dismiss.getAccessibleContext().setAccessibleName("시간여행 장치 닫기");dismiss.setToolTipText("닫기 (Esc)");
        add(close);add(action);add(dismiss);setFocusCycleRoot(true);setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"close");
        getActionMap().put("close",new AbstractAction(){public void actionPerformed(ActionEvent e){cancel.run();}});
        getAccessibleContext().setAccessibleName("시간여행 장치");getAccessibleContext().setAccessibleDescription(actions.describe());
    }
    private JButton textButton(String text,boolean primary){
        JButton b=new JButton(text){@Override protected void paintComponent(Graphics raw){
            Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
            g.setColor(!isEnabled()?new Color(113,110,104):primary||getModel().isRollover()||isFocusOwner()?GOLD:INK);
            if(getText().isEmpty()){
                g.setStroke(new BasicStroke(2.7f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));int x=getWidth()/2,y=getHeight()/2;
                g.drawLine(x-5,y-5,x+5,y+5);g.drawLine(x+5,y-5,x-5,y+5);
            }else{g.setFont(getFont());FontMetrics fm=g.getFontMetrics();g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());}
            g.dispose();
        }};
        b.setOpaque(false);b.setContentAreaFilled(false);b.setBorderPainted(false);b.setFocusPainted(false);b.setRolloverEnabled(true);b.setCursor(GameCursors.interact());return b;
    }
    static BufferedImage blur(BufferedImage image,int header){
        int w=Math.max(1,image.getWidth()/3),h=Math.max(1,(image.getHeight()-header)/3);
        BufferedImage small=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=small.createGraphics();
        g.drawImage(image,0,0,w,h,0,header,image.getWidth(),image.getHeight(),null);g.dispose();
        BufferedImage padded=new BufferedImage(w+8,h+8,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<h+8;y++)for(int x=0;x<w+8;x++)padded.setRGB(x,y,small.getRGB(Math.max(0,Math.min(w-1,x-4)),Math.max(0,Math.min(h-1,y-4))));
        float[] weights=new float[81];float total=0;
        for(int y=-4;y<=4;y++)for(int x=-4;x<=4;x++){float v=(float)Math.exp(-(x*x+y*y)/12.);weights[(y+4)*9+x+4]=v;total+=v;}
        for(int i=0;i<81;i++)weights[i]/=total;
        return new ConvolveOp(new Kernel(9,9,weights),ConvolveOp.EDGE_NO_OP,null).filter(padded,null).getSubimage(4,4,w,h);
    }
    static java.util.List<String> wrap(String text,FontMetrics fm,int width){
        var lines=new ArrayList<String>();
        for(String paragraph:text.split("\\n+|(?<=\\.) +")){
            String remaining=paragraph.strip();
            while(!remaining.isEmpty()){
                int end=remaining.length();while(end>1&&fm.stringWidth(remaining.substring(0,end))>width)end--;
                if(end<remaining.length()){int space=remaining.lastIndexOf(' ',end);if(space>0)end=space;}
                lines.add(remaining.substring(0,end).strip());remaining=remaining.substring(end).strip();
            }
        }
        return lines;
    }
    @Override public void doLayout(){
        int w=Math.min(580,Math.max(280,getWidth()-40));
        double scale=Math.min(1,w/580.0);titleFont=GameFonts.title((float)(32*scale));statusFont=GameFonts.button((float)(21*scale));bodyFont=GameFonts.body((float)(17*scale));
        lineHeight=Math.max(24,(int)(28*scale));lines=wrap(body,getFontMetrics(bodyFont),w-120);
        int h=Math.max(390,274+lines.size()*lineHeight);h=Math.min(h,Math.max(300,getHeight()-headerHeight-24));
        panel.setBounds((getWidth()-w)/2,headerHeight+(getHeight()-headerHeight-h)/2,w,h);
        bodyTop=panel.y+184+Math.max(0,(3-lines.size())*12)-getFontMetrics(bodyFont).getAscent();
        int buttonWidth=(w-100)/2;close.setBounds(panel.x+w/2-(int)(145*scale)-buttonWidth/2,panel.y+h-86,buttonWidth,54);action.setBounds(panel.x+w/2+(int)(115*scale)-buttonWidth/2,panel.y+h-86,buttonWidth,54);
        for(JButton b:new JButton[]{close,action}){float size=(float)(23*scale);while(size>14&&getFontMetrics(GameFonts.button(size)).stringWidth(b.getText())>buttonWidth-8)size--;b.setFont(GameFonts.button(size));}
        dismiss.setBounds(panel.x+w-47,panel.y+6,40,40);
    }
    @Override protected void paintComponent(Graphics raw){
        super.paintComponent(raw);Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if(headerHeight>0)g.drawImage(original,0,0,getWidth(),headerHeight,0,0,original.getWidth(),headerHeight,null);
        g.drawImage(blurred,0,headerHeight,getWidth(),getHeight()-headerHeight,null);
        g.setColor(new Color(0,0,0,32));g.fillRect(0,headerHeight,getWidth(),getHeight()-headerHeight);
        g.setColor(new Color(0,0,0,30));g.fillRoundRect(panel.x-3,panel.y+5,panel.width+6,panel.height+4,24,24);
        g.setColor(new Color(5,7,9,199));g.fillRoundRect(panel.x,panel.y,panel.width,panel.height,20,20);
        g.setColor(new Color(151,151,145,90));g.drawRoundRect(panel.x,panel.y,panel.width-1,panel.height-1,20,20);
        g.setColor(new Color(155,155,148,150));g.drawLine(panel.x+panel.width/6,panel.y+88,panel.x+panel.width*5/6,panel.y+88);
        center(g,"시간여행 장치",panel.y+61,titleFont,INK);center(g,status,panel.y+139,statusFont,enabled?GOLD:BODY);
        for(int i=0;i<lines.size();i++)center(g,lines.get(i),bodyTop+i*lineHeight+getFontMetrics(bodyFont).getAscent(),bodyFont,BODY);
        g.dispose();
    }
    private void center(Graphics2D g,String text,int y,Font font,Color color){g.setFont(font);g.setColor(color);g.drawString(text,panel.x+(panel.width-g.getFontMetrics().stringWidth(text))/2,y);}
    boolean textFits(){return bodyTop+lines.size()*lineHeight<=close.getY()-8&&getFontMetrics(statusFont).stringWidth(status)<=panel.width-48;}
    /** Snapshot the room behind the notebook, preserving the sharp HUD header. */
    static BufferedImage capture(JComponent root){
        BufferedImage image=new BufferedImage(Math.max(1,root.getWidth()),Math.max(1,root.getHeight()),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();root.printAll(g);
        GameScenePanel scene=findScene(root);
        if(scene!=null){Point at=SwingUtilities.convertPoint(scene,0,0,root);Graphics2D room=(Graphics2D)g.create(at.x,at.y,scene.getWidth(),scene.getHeight());scene.printAll(room);room.dispose();}
        g.dispose();return image;
    }
    static GameScenePanel findScene(Container root){for(Component c:root.getComponents()){if(c instanceof GameScenePanel scene)return scene;if(c instanceof Container child){GameScenePanel scene=findScene(child);if(scene!=null)return scene;}}return null;}
}
