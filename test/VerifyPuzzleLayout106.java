package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.nio.file.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.puzzle.RepairModels.Wires;

public final class VerifyPuzzleLayout106 {
 static int checks;
 static void ok(boolean value,String detail){if(!value)throw new AssertionError(detail);checks++;}
 static void textBounds(Container c){for(Component item:c.getComponents()){
  if(!item.isVisible())continue;
  if(item instanceof JTextArea t){ok(t.getHeight()>=t.getFontMetrics(t.getFont()).getHeight()*t.getRows(),"all instruction rows visible");for(String line:t.getText().split("\n"))ok(t.getFontMetrics(t.getFont()).stringWidth(line)<=t.getWidth(),"line fits: "+line);}
  if(item instanceof JButton b){Insets in=b.getInsets();ok(b.getWidth()>=b.getFontMetrics(b.getFont()).stringWidth(b.getText())+in.left+in.right,"button text fits: "+b.getText());}
  if(item instanceof Container nested)textBounds(nested);
 }}
 static void save(JPanel p,int w,int h,Path path)throws Exception{BufferedImage im=VerifyCipher93.paint(p,w,h);VerifyLogin102.bounds(p);textBounds(p);ImageIO.write(im,"png",path.toFile());}
 static void verifyTerminalReset(WiresPuzzlePanel p){
  Wires model=(Wires)VerifyRepairPanels.field(p,"wires");var original=new java.util.ArrayList<java.util.List<Point>>();for(var path:model.paths)original.add(new java.util.ArrayList<>(path));
  Point start=model.start(0),end=model.end(0);
  VerifyRepairPanels.click(p,190+start.x*24,30+start.y*24);ok(model.paths.equals(original),"single click keeps circuit");
  VerifyRepairPanels.doubleClick(p,190+end.x*24,30+end.y*24);ok(model.paths.equals(original),"right endpoint cannot reset");
  Point middle=model.paths.get(0).get(2);VerifyRepairPanels.doubleClick(p,190+middle.x*24,30+middle.y*24);ok(model.paths.equals(original),"path double click cannot reset");
  // A square cell corner lies outside the visible circular terminal.
  VerifyRepairPanels.doubleClick(p,190+start.x*24+10,30+start.y*24+10);ok(model.paths.equals(original),"outside terminal disc cannot reset");
  JPanel board=(JPanel)VerifyRepairPanels.field(p,"board");double s=((Number)VerifyRepairPanels.field(board,"scale")).doubleValue(),ox=((Number)VerifyRepairPanels.field(board,"ox")).doubleValue(),oy=((Number)VerifyRepairPanels.field(board,"oy")).doubleValue();
  board.dispatchEvent(new MouseEvent(board,MouseEvent.MOUSE_PRESSED,0,0,(int)Math.round(ox+(190+start.x*24)*s),(int)Math.round(oy+(30+start.y*24)*s),2,false,MouseEvent.BUTTON3));ok(model.paths.equals(original),"right mouse double click ignored");
  VerifyRepairPanels.doubleClick(p,190+start.x*24,30+start.y*24);ok(model.paths.get(0).size()==1&&!model.connected(0)&&!p.action.isEnabled(),"double click resets target and updates completion");
  for(int i=1;i<model.rows.length;i++)ok(model.paths.get(i).equals(original.get(i)),"other colors kept");
  VerifyRepairPanels.event(p,MouseEvent.MOUSE_DRAGGED,190+original.get(0).get(1).x*24,30+original.get(0).get(1).y*24);ok(model.paths.get(0).size()==2,"can redraw immediately after reset");
 }
 public static void main(String[] args)throws Exception{
  Path out=timeescape.testutil.ArtifactPaths.output("v107-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   for(int[] size:new int[][]{{1000,820},{900,760}}){int[] completed={0};WiresPuzzlePanel p=new WiresPuzzlePanel(()->completed[0]++,()->{});
    save(p,size[0],size[1],out.resolve("wires-easy-"+size[0]+".png"));
    ok(VerifyRepairPanels.button(p,"선 지우기")==null,"clear button replaced by terminal double click");
    ok(VerifyRepairPanels.button(p,"한 칸 취소")==null&&VerifyRepairPanels.button(p,"조사 종료")==null,"duplicate buttons removed");
    VerifyRepairPanels.fillWires(p,false);ok(p.action.isEnabled(),"easy mouse solution at size "+size[0]);
    save(p,size[0],size[1],out.resolve("wires-easy-complete-"+size[0]+".png"));verifyTerminalReset(p);save(p,size[0],size[1],out.resolve("wires-after-reset-"+size[0]+".png"));VerifyRepairPanels.fillWires(p,false);p.action.doClick(0);ok(completed[0]==0,"easy not final completion");
    save(p,size[0],size[1],out.resolve("wires-hard-"+size[0]+".png"));
    VerifyRepairPanels.fillWires(p,true);ok(p.action.isEnabled(),"hard mouse solution at size "+size[0]);verifyTerminalReset(p);VerifyRepairPanels.fillWires(p,true);p.action.doClick(0);p.action.doClick(0);ok(completed[0]==1,"hard completes exactly once");
   }
   for(int[] size:new int[][]{{900,680},{820,620}}){ArchiveLoginPanel p=new ArchiveLoginPanel(()->{},()->{},()->{});save(p,size[0],size[1],out.resolve("pc-login-"+size[0]+".png"));
    p.password.setText("0000");p.submit.doClick(0);save(p,size[0],size[1],out.resolve("pc-error-"+size[0]+".png"));
    p.openReference();ImageIO.write(VerifyCipher93.paint(p,size[0],size[1]),"png",out.resolve("pc-reference-"+size[0]+".png").toFile());p.closeReference();ok(p.password.getText().equals("0000"),"reference keeps password");
   }
   System.out.println("PASS "+checks+" layout, text fit, scaled input and stage checks; actual panels rendered without game windows.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
