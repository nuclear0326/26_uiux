package timeescape.ui;
import java.awt.*;import java.util.function.Consumer;import javax.swing.*;
import timeescape.core.GameState;
/** A single scrolling column of actual owned items; the whole row is actionable. */
public final class InventoryPanel extends JPanel implements Scrollable {
 private final Consumer<String> onClick;
 private GameState observedState;
 private java.util.Set<String> observed=java.util.Set.of();
 private Runnable readChanged=()->{};
 void setReadListener(Runnable callback){readChanged=callback;}
 private java.util.function.BiConsumer<String,String> acquired=(id,name)->{};
 private Runnable resetNotices=()->{};
 void setAcquisitionListener(java.util.function.BiConsumer<String,String> callback,Runnable reset){acquired=callback;resetNotices=reset;}
 boolean isUnread(String id){return observedState!=null&&observedState.isItemUnread(id);}
 void acknowledge(String id){if(interactionAllowed.getAsBoolean()&&observedState!=null&&observedState.markItemRead(id)){repaint();readChanged.run();}}
 private void syncItems(GameState s){
  java.util.Set<String> current=new java.util.LinkedHashSet<>();for(String id:s.catalog().items().keySet())if(s.has(id))current.add(id);
  if(observedState!=s){observedState=s;observed=current;resetNotices.run();return;}
  for(String id:current)if(!observed.contains(id)){acquired.accept(id,itemName(s,id));}
  observed=current;
 }
 static String itemName(GameState s,String id){return switch(id){case "USB"->s.usbComplete()?"연구 데이터 USB":"빈 USB";case "CASE"->"장치 케이스";case "CELL"->"전원 셀";case "MODULE"->"제어 모듈";case "DEVICE"->"손목 시간여행 장치";case "SPARE"->"대체 부품";case "CORE"->"미래 핵심 부품";default->id;};}

 private java.util.function.BooleanSupplier interactionAllowed=()->true;
 void setInteractionAllowed(java.util.function.BooleanSupplier allowed){interactionAllowed=allowed;}
 public InventoryPanel(Consumer<String> action){onClick=action;setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));}
 private void item(String title,String detail,String id,boolean selected){
  JButton row=new JButton(){protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();LabTheme.quality(g);int w=getWidth(),h=getHeight();if(getModel().isRollover()||selected||isFocusOwner()){g.setColor(new Color(151,111,53,40));g.fillRoundRect(0,0,w,h,8,8);}g.setColor(NotebookStyle.LINE);g.drawLine(0,h-1,w,h-1);g.drawLine(90,15,90,h-18);paintItem(g,id,12,h/2-28);if(isUnread(id)){g.setColor(new Color(250,230,206));g.fillOval(74,h/2-50,12,12);g.setColor(new Color(198,45,48));g.fillOval(76,h/2-48,8,8);}g.setColor(NotebookStyle.INK);g.setFont(GameFonts.button(16));int y=wrap(g,title,103,27,w-112,22);g.setFont(GameFonts.body(14));wrap(g,detail,103,y+7,w-112,20);if(selected){g.setColor(new Color(117,77,28));g.drawRoundRect(1,1,w-3,h-3,7,7);}g.dispose();}};
  java.awt.event.MouseAdapter read=new java.awt.event.MouseAdapter(){public void mouseEntered(java.awt.event.MouseEvent e){acknowledge(id);}public void mouseMoved(java.awt.event.MouseEvent e){acknowledge(id);}};row.addMouseListener(read);row.addMouseMotionListener(read);
  row.setName(id);row.setToolTipText(title+" — "+detail);row.getAccessibleContext().setAccessibleName(title);row.getAccessibleContext().setAccessibleDescription(detail);row.setPreferredSize(new Dimension(310,146));row.setMinimumSize(new Dimension(0,146));row.setMaximumSize(new Dimension(Integer.MAX_VALUE,146));row.setAlignmentX(LEFT_ALIGNMENT);row.setOpaque(false);row.setContentAreaFilled(false);row.setBorderPainted(false);row.setFocusPainted(false);row.setCursor(GameCursors.interact());row.addActionListener(e->{if(interactionAllowed.getAsBoolean())onClick.accept(id);});add(row);
 }
 static int wrap(Graphics2D g,String text,int x,int y,int width,int line){StringBuilder current=new StringBuilder();for(char ch:text.toCharArray()){if(g.getFontMetrics().stringWidth(current.toString()+ch)>width&&current.length()>0){g.drawString(current.toString(),x,y);current.setLength(0);y+=line;}current.append(ch);}if(current.length()>0){g.drawString(current.toString(),x,y);y+=line;}return y;}
 static void prepareImages(){if(ATLAS==null)throw new IllegalStateException("Missing item atlas");}
 private static final java.awt.image.BufferedImage ATLAS=NotebookStyle.load("items.png");
 private static final java.util.Map<String,java.awt.image.BufferedImage> ICONS=new java.util.HashMap<>();
 static void paintItem(Graphics2D g,String id,int x,int y){
  java.awt.image.BufferedImage icon=ICONS.computeIfAbsent(id,key->{int cell=switch(key){case "USB"->0;case "CASE"->1;case "CELL"->2;case "MODULE"->3;case "DEVICE"->4;case "SPARE"->5;case "CORE"->6;default->7;};int cw=ATLAS.getWidth()/4,ch=ATLAS.getHeight()/2;return ATLAS.getSubimage(cell%4*cw,cell/4*ch,cw,ch);});
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  g.drawImage(icon,x-12,y-22,90,90,null);
 }
 public void refresh(GameState s,boolean selected){syncItems(s);removeAll();if(s.has("DEVICE"))item("손목 시간여행 장치",s.introPresent()?"상자에서 꺼낸 시제품. 클릭해 사용 화면을 엽니다.":s.flag("device_broken")?"이동 중 고장난 장치. 클릭해서 상태를 확인합니다.":"시간 이동 및 장치 수리. 클릭해서 엽니다.","DEVICE",false);
  if(s.hasSparePart())item("대체 부품",selected?"선택됨. 손목 장치를 열어 수리하세요.":"과거에서 얻은 부품. 클릭해 수리에 사용할 부품을 선택합니다.","SPARE",selected);
  if(s.hasCorePart())item("미래 핵심 부품","손목 장치의 최종 수리에 사용하는 핵심 부품입니다.","CORE",false);
  if(s.has("USB"))item(s.usbComplete()?"연구 데이터 USB":"빈 USB",s.usbComplete()?"연구 기록이 저장된 장치. 미래에서 사용할 수 있습니다.":"컴퓨터에서 복구한 연구 데이터를 복사할 저장 매체입니다.","USB",false);
  for(String id:new String[]{"CASE","CELL","MODULE"})if(s.has(id))item(switch(id){case "CASE"->"장치 케이스";case "CELL"->"전원 셀";default->"제어 모듈";},switch(id){case "CASE"->"장치의 내부 부품을 보호하는 외장입니다.";case "CELL"->"시간여행 장치에 전력을 공급하는 제작 부품입니다.";default->"시간 좌표를 제어하는 제작 부품입니다.";},id,false);
  if(getComponentCount()==0){JTextArea empty=NotebookStyle.text("아직 보관 중인 아이템이 없습니다.\n\n연구실의 오브젝트를 조사해 아이템을 찾아보세요.",16);empty.setAlignmentX(LEFT_ALIGNMENT);add(empty);}revalidate();repaint();
 }
 public Dimension getPreferredScrollableViewportSize(){return new Dimension(320,450);}public int getScrollableUnitIncrement(Rectangle r,int o,int d){return 24;}public int getScrollableBlockIncrement(Rectangle r,int o,int d){return r.height-24;}public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
}
