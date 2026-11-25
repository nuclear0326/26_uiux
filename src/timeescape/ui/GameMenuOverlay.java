package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Arrays;
import java.util.function.*;
import javax.swing.*;

/** In-game menu: cached blurred room, translucent black panel, text-only controls. */
final class GameMenuOverlay extends JPanel {
    private final JComponent scene;
    private final Runnable close,main,reset;
    private BooleanSupplier intro=()->true;
    private Consumer<Boolean> setIntro=value->{};
    private BufferedImage backdrop;
    private int cachedWidth,cachedHeight;
    private String page="menu";
    private Runnable newGame=()->{};
    private final JLabel newGameMessage=new JLabel("<html><div style='text-align:center'>새 게임을 시작하면 자동저장이 초기화됩니다.<br>저장소 1~4는 그대로 유지됩니다.</div></html>",SwingConstants.CENTER);
    final HelpContent helpContent=new HelpContent();
    private Supplier<java.util.List<timeescape.system.SaveSlots.Summary>> slots=java.util.List::of;
    private Predicate<timeescape.system.SaveSlots.Slot> saveSlot=slot->false,loadSlot=slot->false;
    private java.util.function.Supplier<java.util.concurrent.CompletableFuture<java.util.List<timeescape.system.SaveSlots.Summary>>> asyncSlots;
    private java.util.function.Function<timeescape.system.SaveSlots.Slot,java.util.concurrent.CompletableFuture<Boolean>> asyncSave,asyncLoad;
    private boolean busy,loadingSlots,applyingSummaries;
    private long pageRevision;
    boolean isBusy(){return busy;}
    void setAsyncSaveSlots(java.util.function.Supplier<java.util.concurrent.CompletableFuture<java.util.List<timeescape.system.SaveSlots.Summary>>> list,
        java.util.function.Function<timeescape.system.SaveSlots.Slot,java.util.concurrent.CompletableFuture<Boolean>> save,
        java.util.function.Function<timeescape.system.SaveSlots.Slot,java.util.concurrent.CompletableFuture<Boolean>> load){asyncSlots=list;asyncSave=save;asyncLoad=load;}
    private void setBusy(boolean value){busy=value;dismiss.setEnabled(!value);for(JButton b:options)b.setEnabled(!value);}
    private timeescape.system.SaveSlots.Slot overwrite;
    private final JLabel information=new JLabel("",SwingConstants.CENTER);
    final Rectangle panel=new Rectangle();
    private final JLabel title=new JLabel("게임 메뉴",SwingConstants.CENTER);
    private final JButton dismiss=link("×","close");
    private final java.util.List<JButton> options=new java.util.ArrayList<>();
    GameMenuOverlay(JComponent scene,Runnable close,Runnable main,Runnable reset){
        this.scene=scene;this.close=close;this.main=main;this.reset=reset;
        setOpaque(false);setLayout(null);setVisible(false);setFocusCycleRoot(true);
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        addMouseListener(new MouseAdapter(){});addMouseMotionListener(new MouseMotionAdapter(){});addMouseWheelListener(e->e.consume());
        title.setForeground(new Color(241,229,205));add(title);dismiss.addActionListener(e->{if(!busy)close.run();});dismiss.setToolTipText("닫기 (Esc)");add(dismiss);
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"back");
        getActionMap().put("back",new AbstractAction(){public void actionPerformed(ActionEvent e){back();}});
        information.setForeground(new Color(185,177,161));information.setFont(GameFonts.body(14));add(information);
        newGameMessage.setFont(GameFonts.body(16));newGameMessage.setForeground(new Color(188,184,173));newGameMessage.setVisible(false);add(newGameMessage);
        helpContent.setVisible(false);add(helpContent);
        showPage("menu");
    }
    void setNewGameAction(Runnable action){newGame=action;}
    void setTransitionSettings(BooleanSupplier get,Consumer<Boolean> set){intro=get;setIntro=set;}
    void setSaveSlots(Supplier<java.util.List<timeescape.system.SaveSlots.Summary>> slots,
                      Predicate<timeescape.system.SaveSlots.Slot> save,
                      Predicate<timeescape.system.SaveSlots.Slot> load){this.slots=slots;saveSlot=save;loadSlot=load;}
    private JButton link(String label,String name){JButton b=new JButton(label){
        @Override protected void paintComponent(Graphics graphics){
            Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);
            boolean hot=isEnabled()&&(getModel().isRollover()||isFocusOwner());
            Color ink=!isEnabled()?new Color(133,128,119):hot?new Color(234,196,129):new Color(239,227,203);
            g.setColor(ink);
            if(Boolean.TRUE.equals(getClientProperty("backArrow"))){
                g.translate((getWidth()-44)/2.0,(getHeight()-38)/2.0);
                g.setStroke(new BasicStroke(3.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                java.awt.geom.Path2D curve=new java.awt.geom.Path2D.Double();
                curve.moveTo(10,34);curve.lineTo(24,34);curve.append(new java.awt.geom.Arc2D.Double(10,6,28,28,-90,180,java.awt.geom.Arc2D.OPEN),true);curve.lineTo(12,6);g.draw(curve);
                java.awt.geom.Path2D head=new java.awt.geom.Path2D.Double();
                head.moveTo(11.8,.8);head.quadTo(13,-.1,13,1.5);head.lineTo(13,10.5);head.quadTo(13,12.1,11.8,11.2);head.lineTo(4.8,7.3);head.quadTo(3,6,4.8,4.7);head.lineTo(11.8,.8);head.closePath();g.fill(head);
                g.dispose();return;
            }
            g.setFont(getFont());FontMetrics fm=g.getFontMetrics();
            String detail=(String)getClientProperty("detail"),time=(String)getClientProperty("time");
            if(detail==null)g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()-fm.getHeight())/2+fm.getAscent());
            else{
                int top=(getHeight()-62)/2;
                g.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,top+24);
                g.setFont(GameFonts.body(14));g.setColor(isEnabled()?new Color(183,177,164):new Color(120,118,112));
                g.drawString(detail,(getWidth()-g.getFontMetrics().stringWidth(detail))/2,top+43);
                if(time!=null)g.drawString(time,(getWidth()-g.getFontMetrics().stringWidth(time))/2,top+61);
            }
            g.dispose();
        }
    };b.setName(name);b.setOpaque(false);b.setBorderPainted(false);b.setContentAreaFilled(false);b.setFocusPainted(false);b.setCursor(GameCursors.interact());b.setFont(GameFonts.button(30));return b;}
    private void option(String label,String name,Runnable action){JButton b=link(label,name);
        if(label.equals("돌아가기")){b.setText("");b.putClientProperty("backArrow",true);b.setToolTipText("돌아가기 (Esc)");b.getAccessibleContext().setAccessibleName("돌아가기");}b.addActionListener(e->{if(!busy)action.run();});options.add(b);add(b);}
    void showPage(String next){if(busy)return;long revision=++pageRevision;page=next;newGameMessage.setVisible(next.equals("newgame"));helpContent.setVisible(next.equals("help"));information.setText("");for(JButton b:options)remove(b);options.clear();
        switch(next){
            case "newgame"->{title.setText("새 게임을 시작할까요?");option("취소","cancel-newgame",close);option("시작","confirm-newgame",()->{if(!isVisible())return;close.run();newGame.run();});}
            case "help"->{title.setText("조작 도움말");information.setText("<html><div style='text-align:center'>?버튼을 클릭시 조작 도움말을<br>다시 볼 수 있습니다.</div></html>");option("닫기","help-close",close);}
            case "save","load","ingameload"->{
                boolean saving=next.equals("save");title.setText(saving?"저장":next.equals("ingameload")?"불러오기":"이어하기");
                information.setText(saving?"자동저장은 자동 갱신 · 저장소를 눌러 저장":"불러올 저장소를 선택하세요");
                if(asyncSlots!=null&&!applyingSummaries){
                    loadingSlots=true;
                    slots=()->java.util.Arrays.stream(timeescape.system.SaveSlots.Slot.values())
                        .map(slot->new timeescape.system.SaveSlots.Summary(slot,false,false,false,"확인 중…", "")).toList();
                }
                for(var summary:slots.get()){
                    var slot=summary.slot();
                    option(slot.title(),"slot-"+slot.name(),()->{
                        if(saving){
                            if(summary.exists()){overwrite=slot;showPage("overwrite");}
                            else saveSelected(slot);
                        }else loadSelected(slot);
                    });
                    JButton button=options.get(options.size()-1);
                    button.putClientProperty("detail",summary.progress());button.putClientProperty("time",summary.time());
                    button.setToolTipText(summary.slot().title()+" · "+summary.progress()+" · "+summary.time());
                    button.getAccessibleContext().setAccessibleDescription(button.getToolTipText());
                    button.setEnabled(!loadingSlots&&(saving?slot!=timeescape.system.SaveSlots.Slot.AUTO:summary.loadable()));
                }
                option("돌아가기","back",()->{if(saving||next.equals("ingameload"))showPage("menu");else close.run();});
            }
            case "overwrite"->{title.setText(overwrite.title()+"에 덮어쓸까요?");option("저장","confirm-save",()->saveSelected(overwrite));option("취소","cancel",()->showPage("save"));}
            case "settings"->{title.setText("설정");option("시작 연출  "+(intro.getAsBoolean()?"켜짐":"꺼짐"),"intro",()->{setIntro.accept(!intro.getAsBoolean());showPage("settings");});option("돌아가기","back",()->showPage("menu"));}
            default->{title.setText("게임 메뉴");option("저장","save",()->showPage("save"));option("불러오기","load",()->showPage("ingameload"));option("설정","settings",()->showPage("settings"));option("메인 메뉴","main",()->{close.run();main.run();});}
        }
        revalidate();doLayout();repaint();if(isShowing())focusFirst();
        if(asyncSlots!=null&&!applyingSummaries&&(next.equals("save")||next.equals("load")||next.equals("ingameload"))){
            asyncSlots.get().whenCompleteAsync((list,error)->{
                if(revision!=pageRevision||!isVisible())return;
                loadingSlots=false;
                if(error!=null){information.setText("저장소를 확인하지 못했습니다. 다시 열어주세요.");return;}
                String status=information.getText();slots=()->list;applyingSummaries=true;
                try{showPage(next);information.setText(status);}finally{applyingSummaries=false;}
            },SwingUtilities::invokeLater);
        }
    }
    private void focusFirst(){options.stream().filter(JButton::isEnabled).findFirst().ifPresent(JButton::requestFocusInWindow);}
    private void loadSelected(timeescape.system.SaveSlots.Slot slot){
        if(asyncLoad==null){if(loadSlot.test(slot))close.run();return;}
        String previous=page;setBusy(true);information.setText("불러오는 중…");
        asyncLoad.apply(slot).whenCompleteAsync((ok,error)->{
            setBusy(false);
            if(error==null&&Boolean.TRUE.equals(ok)){close.run();}
            else{showPage(previous);information.setText("불러오지 못했습니다. 현재 진행은 유지됩니다.");}
        },SwingUtilities::invokeLater);
    }
    private void saveSelected(timeescape.system.SaveSlots.Slot slot){
        if(asyncSave!=null){
            setBusy(true);information.setText("저장 중…");
            asyncSave.apply(slot).whenCompleteAsync((ok,error)->{
                setBusy(false);showPage("save");
                information.setText(error==null&&Boolean.TRUE.equals(ok)?slot.title()+"에 저장했습니다.":"저장하지 못했습니다. 다시 시도해주세요.");
            },SwingUtilities::invokeLater);return;
        }
        boolean success=saveSlot.test(slot);showPage("save");
        information.setText(success?slot.title()+"에 저장했습니다.":"저장하지 못했습니다. 다시 시도해주세요.");
    }
    void open(){open("menu");}
    void open(String page){showPage(page);backdrop=null;setVisible(true);repaint();focusFirst();}
    void release(){pageRevision++;setVisible(false);backdrop=null;}
    void back(){if(busy)return;if(page.equals("menu")||page.equals("load")||page.equals("help")||page.equals("newgame"))close.run();else showPage(page.equals("overwrite")?"save":"menu");}
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
    @Override public void doLayout(){int w=Math.min(620,Math.max(420,(int)(getWidth()*.39))),h=Math.min(680,(int)(getHeight()*.82));boolean slotPage=page.equals("save")||page.equals("load")||page.equals("ingameload");
        if(slotPage)h=Math.min(760,Math.max(0,getHeight()-24));
        if(page.equals("help"))h=Math.min(680,Math.max(h,Math.min(getHeight()-24,590)));
        if(page.equals("newgame")){w=Math.min(520,Math.max(1,getWidth()-40));h=Math.min(290,Math.max(1,getHeight()-40));}
        panel.setBounds((getWidth()-w)/2,(getHeight()-h)/2,w,h);
        if(page.equals("newgame")){
            title.setFont(GameFonts.title(29));title.setBounds(panel.x+30,panel.y+30,w-60,46);
            dismiss.setFont(GameFonts.button(26));dismiss.setBounds(panel.x+w-46,panel.y+9,36,36);
            information.setBounds(0,0,0,0);newGameMessage.setBounds(panel.x+26,panel.y+108,w-52,62);
            int buttonWidth=(w-100)/2;
            for(int i=0;i<options.size();i++){JButton b=options.get(i);b.setFont(GameFonts.button(23));b.setBounds(panel.x+40+i*(buttonWidth+20),panel.y+h-76,buttonWidth,44);}
            return;
        }
        int titleSize=(page.equals("reset")||page.equals("overwrite"))?26:Math.min(48,w/11);title.setFont(GameFonts.title(titleSize));title.setBounds(panel.x+24,panel.y+42,w-48,66);dismiss.setFont(GameFonts.button(30));dismiss.setBounds(panel.x+w-57,panel.y+15,40,40);
        information.setBounds(panel.x+20,panel.y+108,w-40,24);
        if(page.equals("help")){
            information.setBounds(panel.x+20,panel.y+108,w-40,42);
            helpContent.setBounds(panel.x+40,panel.y+158,w-80,h-238);
            JButton closeButton=options.get(0);closeButton.setFont(GameFonts.button(24));closeButton.setBounds(panel.x+30,panel.y+h-70,w-60,40);
            return;
        }
        if(slotPage){
            int rows=Math.max(1,options.size()-1),top=panel.y+150,rowHeight=(h-228)/rows;
            for(int i=0;i<options.size();i++){
                JButton b=options.get(i);b.setFont(GameFonts.button(24));
                if(i==options.size()-1)b.setBounds(panel.x+24,panel.y+h-60,w-48,40);
                else b.setBounds(panel.x+24,top+i*rowHeight+(i>0?16:0),w-48,rowHeight);
            }
            return;
        }
        int top=panel.y+Math.min(175,h/3),available=h-(top-panel.y)-35,step=available/options.size();for(int i=0;i<options.size();i++){JButton b=options.get(i);b.setFont(GameFonts.button((page.equals("settings")||page.equals("save")||page.equals("load")||page.equals("ingameload"))?24:36));b.setBounds(panel.x+24,top+i*step,w-48,step);}
    }
    @Override protected void paintComponent(Graphics graphics){if(getWidth()<1||getHeight()<1)return;prepareBackdrop();Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(backdrop,0,0,getWidth(),getHeight(),null);
        g.setColor(new Color(0,0,0,32));g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(new Color(0,0,0,30));g.fillRoundRect(panel.x-3,panel.y+5,panel.width+6,panel.height+4,24,24);
        g.setColor(new Color(5,7,9,199));g.fillRoundRect(panel.x,panel.y,panel.width,panel.height,20,20);
        g.setColor(new Color(151,151,145,90));g.drawRoundRect(panel.x,panel.y,panel.width-1,panel.height-1,20,20);
        g.setColor(new Color(155,155,148,150));int ruleY=panel.y+(page.equals("newgame")?88:page.equals("help")?154:137);g.drawLine(panel.x+panel.width/6,ruleY,panel.x+panel.width*5/6,ruleY);
        if((page.equals("save")||page.equals("load")||page.equals("ingameload"))&&options.size()>2){
            JButton auto=options.get(0);int y=auto.getY()+auto.getHeight()+8,cx=panel.x+panel.width/2;
            g.setColor(new Color(190,179,157,160));g.setStroke(new BasicStroke(1.2f));g.drawLine(cx-38,y,cx+38,y);
        }
        g.dispose();
    }
}
