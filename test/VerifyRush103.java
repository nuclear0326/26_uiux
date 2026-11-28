package timeescape.ui;
import java.awt.*;import java.awt.event.*;import java.nio.file.*;import javax.swing.*;import javax.imageio.ImageIO;import timeescape.puzzle.FileRushHourModel;
public final class VerifyRush103 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static Object field(Object o,String n)throws Exception{var f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static JTextArea guidance(Container c){for(Component x:c.getComponents()){if(x instanceof JTextArea t&&t.getText().contains("• 마우스"))return t;if(x instanceof Container p){var t=guidance(p);if(t!=null)return t;}}return null;}
 static void collectButtons(Container c,java.util.List<JButton> list){for(Component x:c.getComponents()){if(x instanceof JButton b)list.add(b);else if(x instanceof Container nested)collectButtons(nested,list);}}
 static void mouse(JComponent b,int type,int x,int y){b.dispatchEvent(new MouseEvent(b,type,0,type==MouseEvent.MOUSE_DRAGGED?MouseEvent.BUTTON1_DOWN_MASK:0,x,y,1,false,MouseEvent.BUTTON1));}
 static void draft(Container c){for(Component x:c.getComponents()){
  if(x instanceof JTextArea t){String s=t.getText();if(s.startsWith("파일 블록을 밀어"))t.setText("황금색 SYS.CFG를 오른쪽 출구로 보내세요.");else if(s.startsWith("[목표]"))t.setText("파일은 가로 또는 세로 방향으로만 움직입니다.");else if(s.startsWith("• 마우스"))t.setText("파일을 드래그해 이동하세요.\n선택 후 방향키로도 이동할 수 있습니다.");}
  else if(x instanceof Container p)draft(p);
 }}
 public static void main(String[] args)throws Exception{
  java.util.concurrent.CountDownLatch completed=new java.util.concurrent.CountDownLatch(1);RushHourPanel[] normal={null};int[] normalCalls={0};
  Path out=timeescape.testutil.ArtifactPaths.output("v105-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   normal[0]=new RushHourPanel(()->{normalCalls[0]++;completed.countDown();},()->{});normal[0].addNotify();
   FileRushHourModel normalModel=(FileRushHourModel)field(normal[0],"model");for(var move:normalModel.solution())normalModel.move(move.blockId(),move.to());
   var moved=RushHourPanel.class.getDeclaredMethod("onMoved");moved.setAccessible(true);moved.invoke(normal[0]);
   for(int[] dim:new int[][]{{975,728},{905,660}}){
    int[] done={0};RushHourPanel p=new RushHourPanel(()->done[0]++,()->{});
    ImageIO.write(VerifyCipher93.paint(p,dim[0],dim[1]),"png",out.resolve("rush-"+dim[0]+".png").toFile());
    JTextArea t=guidance(p);Rectangle rect=SwingUtilities.convertRectangle(t.getParent(),t.getBounds(),p);
    ok(rect.y>=0&&rect.y+rect.height<=p.getHeight(),"guidance inside window");
    ok(t.getHeight()>=t.getFontMetrics(t.getFont()).getHeight()*4,"all control lines fit");
    JComponent b=(JComponent)field(p,"board");double scale=Math.min(b.getWidth()/525.0,b.getHeight()/540.0);int ox=(int)((b.getWidth()-525*scale)/2),oy=(int)((b.getHeight()-540*scale)/2);
    ok(oy+524*scale<b.getHeight(),"board footer inside scaled bounds");
    Container card=t.getParent().getParent();Rectangle cardRect=SwingUtilities.convertRectangle(card.getParent(),card.getBounds(),p),boardRect=SwingUtilities.convertRectangle(b.getParent(),b.getBounds(),p);
    ok(Math.abs(cardRect.getCenterY()-boardRect.getCenterY())<=1,"description vertically centered with board");
    ok(card.getHeight()==card.getPreferredSize().height,"description height does not stretch");
    ok(((JTextArea)field(p,"status")).getText().contains("SYS.CFG"),"terminal objective retained");
    java.util.List<JButton> buttons=new java.util.ArrayList<>();collectButtons(card,buttons);
    ok(buttons.size()==2&&buttons.get(0).getText().equals("초기화")&&buttons.get(1).getText().equals("힌트"),"only reset and hint in card");
    for(JButton button:buttons){Rectangle br=SwingUtilities.convertRectangle(button.getParent(),button.getBounds(),p);ok(br.y>=rect.y+rect.height,"buttons below controls guide");}
    FileRushHourModel model=(FileRushHourModel)field(p,"model");
    for(var move:model.solution()){
     var f=model.blocks()[model.findIndex(move.blockId())];int pos=model.position(model.findIndex(move.blockId()));
     int x=ox+(int)((27+(f.horizontal()?pos:f.fixed())*73+36)*scale),y=oy+(int)((57+(f.horizontal()?f.fixed():pos)*73+36)*scale);
     int dx=f.horizontal()?(int)Math.round((move.to()-pos)*73*scale):0,dy=f.horizontal()?0:(int)Math.round((move.to()-pos)*73*scale);
     mouse(b,MouseEvent.MOUSE_PRESSED,x,y);mouse(b,MouseEvent.MOUSE_DRAGGED,x+dx,y+dy);mouse(b,MouseEvent.MOUSE_RELEASED,x+dx,y+dy);
     ok(model.position(model.findIndex(move.blockId()))==move.to(),"scaled drag matches original model");
    }
    ok(model.solved(),"whole solution works at both sizes");p.close();ok(done[0]==0,"closing cancels pending completion");
   }
   int[] skipped={0};RushHourPanel skip=new RushHourPanel(()->skipped[0]++,()->{});skip.skip();skip.skip();ok(skipped[0]==1,"developer completion once");skip.close();
   RushHourPanel sample=new RushHourPanel(()->{},()->{});draft(sample);ImageIO.write(VerifyCipher93.paint(sample,975,728),"png",out.resolve("rush-copy-draft.png").toFile());sample.close();
   ImageIO.write(VerifyCipher93.paint(new ArchiveLoginPanel(()->{},()->{},()->{}),900,680),"png",out.resolve("pc-approved-copy.png").toFile());
  }catch(Exception e){throw new RuntimeException(e);}});
  ok(completed.await(3,java.util.concurrent.TimeUnit.SECONDS),"normal completion timer fires without a window");SwingUtilities.invokeAndWait(()->{ok(normalCalls[0]==1,"normal completion exactly once");normal[0].close();normal[0].removeNotify();});
  System.out.println("PASS "+checks+" control visibility, scaled drag solution, cancellation and skip checks; no game window.");
 }
}
