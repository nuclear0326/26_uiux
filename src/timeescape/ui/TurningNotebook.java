package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** The complete paper sheet, including title, tabs, content and outer edge, turns together. */
final class TurningNotebook extends NotebookStyle.Paper {
    private BufferedImage before,after;
    private float progress=1;
    private boolean reverse;
    private long started;
    final Timer turnTimer=new Timer(16,e->setTurnProgress((float)((System.nanoTime()-started)/460_000_000.0)));
    void turnTo(Runnable change,boolean backwards,boolean animate){
        finishTurn();
        BufferedImage old=animate&&getWidth()>0&&getHeight()>0?capture():null;
        change.run();layoutTree(this);
        if(old!=null){BufferedImage next=capture();before=old;after=next;reverse=backwards;progress=0;started=System.nanoTime();turnTimer.start();}
        repaint();
    }
    private static void layoutTree(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container n)layoutTree(n);}
    private BufferedImage capture(){BufferedImage image=new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);super.paint(g);g.dispose();return image;}
    void setTurnProgress(float value){progress=Math.max(0,Math.min(1,value));if(progress>=1)finishTurn();repaint();}
    void finishTurn(){turnTimer.stop();progress=1;before=null;after=null;repaint();}
    boolean isTurning(){return before!=null;}
    @Override public void paint(Graphics graphics){
        if(before==null||after==null){super.paint(graphics);return;}
        Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);int w=getWidth()-5,h=getHeight()-6;
        float t=progress*progress*(3-2*progress);int remaining=Math.round(w*(1-t));
        g.drawImage(after,0,0,null);
        int edge=reverse?w-remaining:remaining;
        Graphics2D old=(Graphics2D)g.create();old.clipRect(reverse?edge:0,0,remaining,getHeight());old.drawImage(before,0,0,null);old.dispose();
        if(t>0&&t<1){
            int curl=Math.max(1,(int)(Math.sin(Math.PI*t)*w*.28));int x=reverse?edge:edge-curl;
            g.setPaint(new GradientPaint(reverse?edge:edge,0,new Color(39,25,12,90),reverse?edge-curl*.65f:edge+curl*.65f,0,new Color(39,25,12,0)));
            g.fillRect(reverse?edge-curl:edge,2,curl,h-3);
            Polygon fold=new Polygon(new int[]{x,x+curl,x+curl-3,x+3},new int[]{7,0,h,h-7},4);
            Graphics2D sheet=(Graphics2D)g.create();sheet.clip(fold);
            sheet.drawImage(NotebookStyle.PAPER_IMAGE,x,0,curl,h,null);
            sheet.setPaint(new GradientPaint(x,0,reverse?new Color(68,44,19,100):new Color(255,248,223,65),x+curl,0,reverse?new Color(255,248,223,65):new Color(68,44,19,100)));
            sheet.fillRect(x,0,curl,h);sheet.dispose();
            g.setColor(new Color(102,72,35,125));g.drawLine(reverse?x:x+curl,2,reverse?x:x+curl,h-3);
        }
        g.dispose();
    }
    @Override public void removeNotify(){finishTurn();super.removeNotify();}
}
