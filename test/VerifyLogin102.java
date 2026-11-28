package timeescape.ui;
import java.awt.*;import java.awt.event.*;import javax.swing.*;import javax.imageio.ImageIO;import java.nio.file.*;
public final class VerifyLogin102 {
 static int checks;static void ok(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static JButton find(Container root,String key){for(Component c:root.getComponents()){if(c instanceof JButton b&&(key.equals(b.getName())||key.equals(b.getText())))return b;if(c instanceof Container nested){JButton b=find(nested,key);if(b!=null)return b;}}return null;}
 static void tap(ArchiveLoginPanel p,String key){find(p,key).doClick(0);}
 static void bounds(Container c){for(Component v:c.getComponents()){if(!v.isVisible())continue;ok(v.getX()>=0&&v.getY()>=0&&v.getX()+v.getWidth()<=c.getWidth()+1&&v.getY()+v.getHeight()<=c.getHeight()+1,"visible child within parent: "+v.getClass());if(v instanceof Container nested)bounds(nested);}}
 public static void main(String[] args)throws Exception{
  Path out=timeescape.testutil.ArtifactPaths.output("v102-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();int[] solved={0},closed={0},views={0};ArchiveLoginPanel p=new ArchiveLoginPanel(()->solved[0]++,()->closed[0]++,()->views[0]++);
   for(int[] size:new int[][]{{900,680},{820,620}}){ImageIO.write(VerifyCipher93.paint(p,size[0],size[1]),"png",out.resolve("login-"+size[0]+".png").toFile());bounds(p);}
   for(String k:new String[]{"7","1","3","5","9"})tap(p,"key-"+k);ok(p.password.getText().equals("7135"),"mouse digits limited to four");
   tap(p,"key-지우기");ok(p.password.getText().equals("713"),"backspace removes last digit");p.password.select(1,3);tap(p,"key-0");ok(p.password.getText().equals("70"),"selected digits replaced");
   p.password.setText("713");p.password.select(1,2);tap(p,"해독표 보기");ok(p.memo.isVisible()&&views[0]==1&&!p.password.isEnabled(),"reference overlay blocks background input");
   tap(p,"key-9");p.password.postActionEvent();ok(p.password.getText().equals("713")&&solved[0]==0,"overlay prevents input and completion");
   ImageIO.write(VerifyCipher93.paint(p,900,680),"png",out.resolve("reference-open.png").toFile());
   p.memo.getActionMap().get("dismiss").actionPerformed(null);ok(!p.memo.isVisible()&&p.password.isEnabled()&&closed[0]==0&&p.password.getText().equals("713")&&p.password.getSelectionStart()==1&&p.password.getSelectionEnd()==2,"Esc returns to same input and selection");
   tap(p,"해독표 보기");find(p.memo,"notebook-close").doClick(0);ok(closed[0]==0&&p.password.getText().equals("713"),"reopen and close preserves puzzle");
   tap(p,"key-전체 삭제");ok(p.password.getText().isEmpty(),"clear all");p.password.setText("abc");ok(p.password.getText().isEmpty(),"keyboard rejects letters");
   for(String k:new String[]{"0","0","0","0"})tap(p,"key-"+k);tap(p,"로그인");ok(solved[0]==0&&p.password.getSelectedText().equals("0000"),"wrong input stays open and selects all");
   for(String k:new String[]{"7","1","3","5"})tap(p,"key-"+k);tap(p,"로그인");p.password.postActionEvent();ok(solved[0]==1,"mouse login completes once");
   var img=CipherMemoOverlay.image();for(int d=0;d<10;d++){Rectangle r=CipherSymbols.sourceForDigit(d);ok(r.x>=0&&r.y>=0&&r.x+r.width<img.getWidth()&&r.y+r.height<img.getHeight(),"glyph uses source image region");}
   ArchiveLoginPanel keyboard=new ArchiveLoginPanel(()->solved[0]++,()->closed[0]++,()->{});keyboard.password.setText("7135");keyboard.password.postActionEvent();ok(solved[0]==2,"keyboard Enter preserved");
   ArchiveLoginPanel draft=new ArchiveLoginPanel(()->{},()->{},()->{});replaceHelp(draft.content,"해독표를 보고 네 자리 암호를 입력하세요.");draft.status.setText("숫자 버튼 또는 키보드로 입력할 수 있습니다.");ImageIO.write(VerifyCipher93.paint(draft,900,680),"png",out.resolve("copy-draft-A.png").toFile());
  }catch(Exception e){throw new RuntimeException(e);}});
  System.out.println("PASS "+checks+" keypad, reference overlay, selection, completion and size checks; no game window.");
 }
 static void replaceHelp(Container c,String text){for(Component v:c.getComponents()){if(v instanceof JTextArea area)area.setText(text);else if(v instanceof Container nested)replaceHelp(nested,text);}}
}
