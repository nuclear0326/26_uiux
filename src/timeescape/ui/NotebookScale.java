package timeescape.ui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
/** Scales real Swing layout and fonts, keeping native input and scrolling coordinates. */
final class NotebookScale {
 private NotebookScale(){}
 static double of(Component c){for(Component p=c;p!=null;p=p.getParent())if(p instanceof TurningNotebook n)return n.uiScale();return 1;}
 static Dimension size(Dimension d,double s){return new Dimension(d.width==Integer.MAX_VALUE?d.width:(int)Math.round(d.width*s),d.height==Integer.MAX_VALUE?d.height:(int)Math.round(d.height*s));}
 static void apply(Component c,double s){
  if(c instanceof JComponent j){
   if(j.getClientProperty("note.baseFont")==null&&j.getFont()!=null)j.putClientProperty("note.baseFont",j.getFont());
   Font f=(Font)j.getClientProperty("note.baseFont");if(f!=null)j.setFont(f.deriveFont((float)(f.getSize2D()*s)));
   j.putClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
   for(String type:new String[]{"preferred","minimum","maximum"}){
    String key="note."+type;Dimension d=(Dimension)j.getClientProperty(key);
    boolean set=type.equals("preferred")?j.isPreferredSizeSet():type.equals("minimum")?j.isMinimumSizeSet():j.isMaximumSizeSet();
    if(d==null&&set){d=type.equals("preferred")?j.getPreferredSize():type.equals("minimum")?j.getMinimumSize():j.getMaximumSize();j.putClientProperty(key,d);}
    if(d!=null){Dimension v=size(d,s);if(type.equals("preferred"))j.setPreferredSize(v);else if(type.equals("minimum"))j.setMinimumSize(v);else j.setMaximumSize(v);}
   }
   if(j.getBorder() instanceof EmptyBorder e){Insets i=(Insets)j.getClientProperty("note.insets");if(i==null){i=e.getBorderInsets();j.putClientProperty("note.insets",i);}j.setBorder(BorderFactory.createEmptyBorder((int)Math.round(i.top*s),(int)Math.round(i.left*s),(int)Math.round(i.bottom*s),(int)Math.round(i.right*s)));}
   if(j.getLayout() instanceof BorderLayout b){Dimension d=(Dimension)j.getClientProperty("note.gaps");if(d==null){d=new Dimension(b.getHgap(),b.getVgap());j.putClientProperty("note.gaps",d);}b.setHgap((int)Math.round(d.width*s));b.setVgap((int)Math.round(d.height*s));}
  }
  if(c instanceof Container p)for(Component child:p.getComponents())apply(child,s);
 }
}
