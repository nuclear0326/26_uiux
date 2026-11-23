package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.function.*;
import javax.swing.*;

/** In-game menu: cached blurred room, translucent black panel, text-only controls. */
final class GameMenuOverlay extends JPanel {
    private final GameScenePanel scene;
    private final Runnable close,main,reset;
    private BooleanSupplier intro=()->true;
    private Consumer<Boolean> setIntro=value->{};
    private BufferedImage backdrop;
    private int cachedWidth,cachedHeight;
    private String page="menu";
    final Rectangle panel=new Rectangle();
    private final JLabel title=new JLabel("게임 메뉴",SwingConstants.CENTER);
    private final JButton dismiss=link("×","close");
    private final java.util.List<JButton> options=new java.util.ArrayList<>();
    GameMenuOverlay(GameScenePanel scene,Runnable close,Runnable main,Runnable reset){
        this.scene=scene;this.close=close;this.main=main;this.reset=reset;
        setOpaque(false);setLayout(null);setVisible(false);setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        addMouseListener(new MouseAdapter(){});addMouseMotionListener(new MouseMotionAdapter(){});addMouseWheelListener(e->e.consume());
        title.setForeground(new Color(241,229,205));add(title);dismiss.addActionListener(e->close.run());dismiss.setToolTipText("닫기 (Esc)");add(dismiss);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"back");
        getActionMap().put("back",new AbstractAction(){public void actionPerformed(ActionEvent e){back();}});
        showPage("menu");
    }
    void setTransitionSettings(BooleanSupplier get,Consumer<Boolean> set){intro=get;setIntro=set;}
    private JButton link(String label,String name){JButton b=new JButton(label){
        @Override protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);boolean hot=getModel().isRollover()||isFocusOwner();g.setColor(hot?new Color(234,196,129):new Color(239,227,203));g.setFont(getFont());FontMetrics fm=g.getFontMetrics();g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());g.dispose();}
    };b.setName(name);b.setOpaque(false);b.setBorderPainted(false);b.setContentAreaFilled(false);b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setFont(GameFonts.button(30));return b;}
    private void option(String label,String name,Runnable action){JButton b=link(label,name);b.addActionListener(e->action.run());options.add(b);add(b);}
    void showPage(String next){page=next;for(JButton b:options)remove(b);options.clear();
        switch(next){
            case "settings"->{title.setText("설정");option("시작 연출  "+(intro.getAsBoolean()?"켜짐":"꺼짐"),"intro",()->{setIntro.accept(!intro.getAsBoolean());showPage("settings");});option("돌아가기","back",()->showPage("menu"));}
            case "reset"->{title.setText("진행을 초기화할까요?");option("초기화","confirm-reset",()->{close.run();reset.run();});option("취소","cancel",()->showPage("menu"));}
            default->{title.setText("게임 메뉴");option("메인 메뉴","main",()->{close.run();main.run();});option("설정","settings",()->showPage("settings"));option("초기화","reset",()->showPage("reset"));}
        }
        revalidate();doLayout();repaint();if(isShowing())options.get(0).requestFocusInWindow();
    }
    void open(){showPage("menu");backdrop=null;setVisible(true);repaint();options.get(0).requestFocusInWindow();}
    void release(){setVisible(false);backdrop=null;}
    void back(){if(page.equals("menu"))close.run();else showPage("menu");}
    private void prepareBackdrop(){
        if(backdrop!=null&&cachedWidth==getWidth()&&cachedHeight==getHeight())return;
        cachedWidth=getWidth();cachedHeight=getHeight();int w=Math.max(1,getWidth()/3),h=Math.max(1,getHeight()/3);
        BufferedImage small=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);Graphics2D g=small.createGraphics();g.scale(w/(double)Math.max(1,scene.getWidth()),h/(double)Math.max(1,scene.getHeight()));scene.printAll(g);g.dispose();
        // Replicate edge pixels before convolution so the screen edge has no sharp unblurred strip.
        BufferedImage padded=new BufferedImage(w+8,h+8,BufferedImage.TYPE_INT_RGB);
        for(int y=0;y<h+8;y++)for(int x=0;x<w+8;x++)padded.setRGB(x,y,small.getRGB(Math.max(0,Math.min(w-1,x-4)),Math.max(0,Math.min(h-1,y-4))));
        float[] weights=new float[81];float total=0;for(int y=-4;y<=4;y++)for(int x=-4;x<=4;x++){float value=(float)Math.exp(-(x*x+y*y)/12.);weights[(y+4)*9+x+4]=value;total+=value;}for(int i=0;i<81;i++)weights[i]/=total;
        backdrop=new ConvolveOp(new Kernel(9,9,weights),ConvolveOp.EDGE_NO_OP,null).filter(padded,null).getSubimage(4,4,w,h);
    }
    @Override public void doLayout(){int w=Math.min(620,Math.max(420,(int)(getWidth()*.39))),h=Math.min(680,(int)(getHeight()*.82));panel.setBounds((getWidth()-w)/2,(getHeight()-h)/2,w,h);
        int titleSize=page.equals("reset")?26:Math.min(48,w/11);title.setFont(GameFonts.title(titleSize));title.setBounds(panel.x+24,panel.y+42,w-48,66);dismiss.setFont(GameFonts.button(30));dismiss.setBounds(panel.x+w-57,panel.y+15,40,40);
        int top=panel.y+Math.min(175,h/3),available=h-(top-panel.y)-35,step=available/options.size();for(int i=0;i<options.size();i++){JButton b=options.get(i);b.setFont(GameFonts.button(page.equals("settings")?24:36));b.setBounds(panel.x+24,top+i*step,w-48,step);}
    }
    @Override protected void paintComponent(Graphics graphics){if(getWidth()<1||getHeight()<1)return;prepareBackdrop();Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(backdrop,0,0,getWidth(),getHeight(),null);
        g.setColor(new Color(0,0,0,32));g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(0,0,0,30));g.fillRoundRect(panel.x-3,panel.y+5,panel.width+6,panel.height+4,24,24);
        g.setColor(new Color(5,7,9,199));g.fillRoundRect(panel.x,panel.y,panel.width,panel.height,20,20);
        g.setColor(new Color(151,151,145,90));g.drawRoundRect(panel.x,panel.y,panel.width-1,panel.height-1,20,20);
        g.setColor(new Color(155,155,148,150));g.drawLine(panel.x+panel.width/6,panel.y+137,panel.x+panel.width*5/6,panel.y+137);g.dispose();
    }
}
