package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import timeescape.puzzle.AccidentLogPuzzle;
import timeescape.puzzle.AccidentLogEvidence;

/** Reader is session-only: no moves, selection, completion or closing the host through it. */
public final class VerifyLogEvidence109 {
 static int checks;
 static void ok(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
 static void fit(Container c){for(Component child:c.getComponents()){if(!child.isVisible())continue;
  if(child instanceof JLabel l&&!l.getText().isBlank())ok(l.getFontMetrics(l.getFont()).stringWidth(l.getText())<=l.getWidth(),"label fits: "+l.getText());
  if(child instanceof JTextArea t&&!t.getText().isBlank())try{
   var last=t.modelToView2D(t.getDocument().getLength());ok(last!=null&&last.getMaxY()<=t.getHeight(),"procedure body fits: "+t.getText());
  }catch(javax.swing.text.BadLocationException e){throw new AssertionError(e);}
  if(child instanceof Container nested)fit(nested);
 }}
 static void image(AccidentLogPanel p,int w,int h,Path file)throws Exception{ImageIO.write(VerifyAccident94.paint(p,w,h),"png",file.toFile());VerifyLogin102.bounds(p);VerifyPuzzleLayout106.textBounds(p);fit(p);}
 static void rejectInput(AccidentLogPanel p){
  var order=p.model.order();int stage=p.model.stage(),selected=p.model.selected();String message=p.status.getText();
  p.reset.doClick(0);p.action.doClick(0);p.verify();p.developerSkip();
  VerifyAccidentDrag108.move(p,0,4);VerifyAccidentDrag108.click(p,2);
  ok(p.model.order().equals(order)&&p.model.stage()==stage&&p.model.selected()==selected&&p.status.getText().equals(message),"reader blocks all background input and cheat");
 }
 public static void main(String[] args)throws Exception{Path out=timeescape.testutil.ArtifactPaths.output("v109-preview");Files.createDirectories(out);
  SwingUtilities.invokeAndWait(()->{try{
   GameFonts.installDefaults();
   ok(AccidentLogEvidence.PROCEDURE.size()==6,"six ordered normal procedure steps");
   String[] titles={"장치 가동","목표 좌표 입력","좌표 보정 검사","출력 상승","보호 회로 확인","최종 작동 확인"};
   for(int i=0;i<6;i++)ok(AccidentLogEvidence.PROCEDURE.get(i).title().equals(String.format("%02d. %s",i+1,titles[i])),"procedure number and order");
   for(var record:AccidentLogPuzzle.RECORDS)for(String phrase:new String[]{"첫 기록","직후","이후","이전","다음에","앞서","열린 뒤"})ok(!record.clue().contains(phrase),"observation instead of order hint");
   ok(AccidentLogPuzzle.RECORDS.get(2).clue().contains("+0.18")&&AccidentLogPuzzle.RECORDS.get(2).failed().contains("+0.18"),"measurement consistent with comparison");
   for(int[] size:new int[][]{{1000,820},{900,760}}){
    int[] completed={0},closed={0};AccidentLogPanel p=new AccidentLogPanel(()->completed[0]++,()->closed[0]++);
    image(p,size[0],size[1],out.resolve("01-order-"+size[0]+".png"));
    VerifyAccidentDrag108.move(p,3,0);p.action.doClick(0);var order=p.model.order();String feedback=p.status.getText();
    VerifyAccidentDrag108.press(p,2);p.openEvidence();ok(p.source<0,"opening cancels floating card without moving it");
    ok(p.evidence.isVisible()&&!p.reset.isEnabled()&&!p.action.isEnabled()&&!p.board.isEnabled(),"reader disables background controls");
    p.evidence.showPage(false);image(p,size[0],size[1],out.resolve("02-procedure-"+size[0]+".png"));
    p.evidence.incident.doClick(0);image(p,size[0],size[1],out.resolve("03-incident-"+size[0]+".png"));rejectInput(p);
    p.evidence.getActionMap().get("dismiss").actionPerformed(null);
    ok(!p.evidence.isVisible()&&p.reset.isEnabled()&&p.action.isEnabled()&&p.board.isEnabled(),"close restores controls");
    ok(p.model.order().equals(order)&&p.status.getText().equals(feedback)&&p.model.stage()==1&&closed[0]==0&&completed[0]==0,"Esc reader keeps order feedback and host");
    // Preserve pre-existing disabled states instead of blindly enabling everything.
    p.action.setEnabled(false);p.openEvidence();p.evidence.dismiss.doClick(0);ok(!p.action.isEnabled(),"disabled state restored exactly");p.action.setEnabled(true);
    VerifyAccidentDrag108.order(p);p.action.doClick(0);ok(p.model.stage()==2&&completed[0]==0,"order alone not rewarded");
    image(p,size[0],size[1],out.resolve("04-compare-"+size[0]+".png"));
    VerifyAccidentDrag108.click(p,3);p.action.doClick(0);int selection=p.model.selected();feedback=p.status.getText();
    p.recordButton.doClick(0);rejectInput(p);p.evidence.dismiss.doClick(0);ok(p.model.stage()==2&&p.model.selected()==selection&&p.status.getText().equals(feedback),"comparison selection and error survive X close");
    // Parent Esc prioritizes reader too, even when focus is not on a tab.
    p.openEvidence();p.getActionMap().get("cancel-drag").actionPerformed(null);ok(!p.evidence.isVisible()&&closed[0]==0,"parent Esc closes only reader");
    VerifyAccidentDrag108.click(p,2);p.openEvidence();p.evidence.normal.doClick(0);p.evidence.incident.doClick(0);p.evidence.dismiss.doClick(0);ok(p.model.selected()==2,"correct selection retained without auto-completing");
    ok(completed[0]==0,"reading never completes puzzle");p.action.doClick(0);p.action.doClick(0);ok(completed[0]==1,"only final verify completes once");p.openEvidence();ok(!p.evidence.isVisible(),"completed panel cannot reopen reader");
    AccidentLogPanel closedPanel=new AccidentLogPanel(()->completed[0]++,()->closed[0]++);VerifyAccident94.paint(closedPanel,size[0],size[1]);closedPanel.openEvidence();closedPanel.cancelSession();closedPanel.closeEvidence();closedPanel.openEvidence();closedPanel.verify();closedPanel.developerSkip();ok(!closedPanel.evidence.isVisible()&&completed[0]==1,"closing host prevents later reader and completion");
   }
   System.out.println("PASS "+checks+" evidence tabs, input blocking, state retention, lifecycle, copy and layout checks; no windows.");
  }catch(Exception e){throw new RuntimeException(e);}});
 }
}
