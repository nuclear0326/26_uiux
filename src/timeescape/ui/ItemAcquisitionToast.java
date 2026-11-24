package timeescape.ui;
import java.awt.*;
import java.util.ArrayDeque;
import javax.swing.*;
/** Short non-intercepting pickup notices, queued rather than overwritten. EDT only. */
final class ItemAcquisitionToast extends JComponent {
 static final double DURATION=1.8;
 record Notice(String id,String title){}
 private final ArrayDeque<Notice> queue=new ArrayDeque<>();
 private Notice current;
 private long started;
 private double elapsed;
 private final Timer timer=new Timer(16,e->advance((System.nanoTime()-started)/1_000_000_000.0));
 ItemAcquisitionToast(){setOpaque(false);setVisible(false);setFocusable(false);}
 @Override public boolean contains(int x,int y){return false;}
 void enqueue(String id,String name){queue.addLast(new Notice(id,name));if(current==null)next();}
 private void next(){current=queue.pollFirst();elapsed=0;started=System.nanoTime();setVisible(current!=null);if(current==null)timer.stop();else timer.start();repaint();}
 void advance(double seconds){elapsed=seconds;if(elapsed>=DURATION)next();else repaint();}
 void clear(){timer.stop();queue.clear();current=null;setVisible(false);}
 int pendingCount(){return queue.size()+(current==null?0:1);}
 @Override public void removeNotify(){clear();super.removeNotify();}
 @Override protected void paintComponent(Graphics raw){if(current==null)return;Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
  float alpha=(float)(elapsed<.15?elapsed/.15:elapsed<1.1?1:Math.max(0,(DURATION-elapsed)/.7));g.setComposite(AlphaComposite.SrcOver.derive(alpha));
  g.translate(0,(int)(18*Math.min(1,elapsed/DURATION)));int w=getWidth();g.setColor(new Color(7,12,15,237));g.fillRoundRect(0,0,w-1,74,12,12);g.setColor(new Color(113,104,84));g.drawRoundRect(0,0,w-2,73,12,12);
  Graphics2D icon=(Graphics2D)g.create();icon.translate(8,4);icon.scale(.72,.72);InventoryPanel.paintItem(icon,current.id(),12,22);icon.dispose();
  g.setFont(GameFonts.body(14));g.setColor(new Color(173,179,173));g.drawString("아이템 획득",86,26);g.setFont(GameFonts.button(18));g.setColor(new Color(239,228,207));
  String name=current.title();while(g.getFontMetrics().stringWidth(name)>w-104&&name.length()>1)name=name.substring(0,name.length()-2)+"…";
  g.drawString(name,86,52);g.dispose();
 }
}
