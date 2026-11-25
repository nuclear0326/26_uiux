package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;

/** Frame-free cinematic text; scene remains visible above a bottom gradient. */
final class DialogueOverlay extends JComponent {
    private List<String> pages=List.of();
    private int index;
    private int visibleChars, soundLetters;
    private final Timer typing=new Timer(38,e->typeNext());
    private Runnable finished=()->{};
    DialogueOverlay(){
        typing.setInitialDelay(140);
        setOpaque(false);setFocusable(true);setVisible(false);
        addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(SwingUtilities.isLeftMouseButton(e))advance();}});
        for(String key:new String[]{"released ENTER","released SPACE"})getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key),"next");
        getActionMap().put("next",new AbstractAction(){public void actionPerformed(ActionEvent e){advance();}});
        getAccessibleContext().setAccessibleName("진행 안내");
    }
    void open(List<String> text,Runnable onFinished){
        if(text.isEmpty())throw new IllegalArgumentException("Empty dialogue");
        pages=List.copyOf(text);index=0;finished=onFinished;
        setVisible(true);beginPage();requestFocusInWindow();repaint();
    }
    String currentText(){return pages.isEmpty()?"":pages.get(index);}
    String visibleText(){return currentText().substring(0,visibleChars);}
    boolean isTyping(){return visibleChars<currentText().length();}
    private void beginPage(){visibleChars=0;soundLetters=0;updateAccessibleText();typing.restart();}
    void typeNext(){
        if(!isVisible()||!isTyping()){typing.stop();return;}
        int cp=currentText().codePointAt(visibleChars);visibleChars+=Character.charCount(cp);
        if(!Character.isWhitespace(cp)&&++soundLetters%3==1)timeescape.system.GameAudio.get().dialogueTick();
        if(!isTyping())typing.stop();repaint();
    }
    void revealAll(){visibleChars=currentText().length();typing.stop();timeescape.system.GameAudio.get().stopDialogueTick();repaint();}
    void dismiss(){typing.stop();timeescape.system.GameAudio.get().stopDialogueTick();visibleChars=0;setVisible(false);pages=List.of();finished=()->{};}
    void advance(){
        if(!isVisible())return;
        if(isTyping()){revealAll();return;}
        if(index+1<pages.size()){index++;beginPage();repaint();}
        else{Runnable callback=finished;dismiss();callback.run();}
    }
    private void updateAccessibleText(){getAccessibleContext().setAccessibleDescription(currentText());}
    @Override public void removeNotify(){dismiss();super.removeNotify();}
    private static List<String> wrap(FontMetrics fm,String text,int width){
        List<String> lines=new ArrayList<>();
        for(String paragraph:text.split("\n",-1)){
            StringBuilder line=new StringBuilder();
            for(char c:paragraph.toCharArray()){
                if(line.length()>0&&fm.stringWidth(line.toString()+c)>width){lines.add(line.toString());line.setLength(0);}
                line.append(c);
            }
            lines.add(line.toString());
        }
        return lines;
    }
    @Override protected void paintComponent(Graphics graphics){
        if(pages.isEmpty())return;
        Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);
        int w=getWidth(),h=getHeight(),margin=Math.max(32,Math.min(64,(int)(w*.04)));
        int fontSize=Math.max(20,Math.min(32,w/48));
        g.setFont(GameFonts.body(fontSize));
        // Anchor the revealed text to the bottom. A new line pushes earlier lines up
        // only when its first character appears, not when a hidden newline is reached.
        List<String> lines=wrap(g.getFontMetrics(),visibleText().replaceAll("\n+$", ""),w-2*margin);
        int lineHeight=g.getFontMetrics().getHeight()+8;
        int bottom=Math.max(36,h/18),textBottom=h-bottom-46;
        int textTop=textBottom-lines.size()*lineHeight;
        int fadeTop=Math.max(0,Math.min((int)(h*.65),textTop-70));
        g.setPaint(new LinearGradientPaint(0,fadeTop,0,Math.max(fadeTop+1,h),new float[]{0f,.4f,1f},new Color[]{new Color(0,0,0,0),new Color(0,0,0,185),new Color(0,0,0,235)}));
        g.fillRect(0,fadeTop,w,h-fadeTop);
        g.setColor(new Color(242,232,211));
        int y=textTop+g.getFontMetrics().getAscent();
        for(String line:lines){g.drawString(line,margin,y);y+=lineHeight;}
        g.setFont(GameFonts.button(Math.max(14,fontSize*.55f)));g.setColor(new Color(196,173,129));
        String hint=isTyping()?"클릭하여 전체 보기":"클릭하여 계속";int tw=g.getFontMetrics().stringWidth(hint),x=margin;
        g.drawString(hint,x,h-bottom);
        g.setStroke(new BasicStroke(1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        int ax=x+tw+14,ay=h-bottom-6;g.drawLine(ax,ay,ax+6,ay+6);g.drawLine(ax+6,ay+6,ax+12,ay);
        g.dispose();
    }
    @Override public javax.accessibility.AccessibleContext getAccessibleContext(){
        if(accessibleContext==null)accessibleContext=new AccessibleJComponent(){};
        return accessibleContext;
    }
}
