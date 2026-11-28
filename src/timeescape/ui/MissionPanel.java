package timeescape.ui;
import java.awt.*;import javax.swing.*;import timeescape.core.GameState;import timeescape.model.Era;
public final class MissionPanel extends JPanel {
 private String lastMessage="";private GameState state;
 private boolean messageRefreshPending;
 public MissionPanel(){setOpaque(false);setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));}
 // Coalesce message-only updates; a synchronous full refresh consumes this work.
 public void setLastMessage(String text){
  lastMessage=text;
  if(state==null||messageRefreshPending)return;
  messageRefreshPending=true;
  SwingUtilities.invokeLater(()->{if(messageRefreshPending){messageRefreshPending=false;refresh(state);}});
 }
 private void text(String value,int size){JTextArea t=NotebookStyle.text(value,size);t.setAlignmentX(LEFT_ALIGNMENT);t.setMaximumSize(new Dimension(Integer.MAX_VALUE,100));add(t);add(Box.createVerticalStrut(10));}
 private void line(){JSeparator s=new JSeparator();s.setForeground(NotebookStyle.LINE);s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));add(s);add(Box.createVerticalStrut(17));}
 private void card(String title,String next,int count,int total,boolean locked){text(title,18);text(next,15);String dots="";for(int i=0;i<total;i++)dots+=(i<count?"■":"□")+"  ";text(dots+"  "+count+" / "+total,15);line();}
 public void refresh(GameState state){messageRefreshPending=false;this.state=state;removeAll();text("현재 목표",18);
  if(state.introPresent()||state.firstTravelComplete()){text(state.introGoal(),16);}
  else if(state.era()==Era.PRESENT){text(!state.powered()?"벽면 배전함에서 전력을 복구하세요.":state.canDepart()?"보관함의 손목 장치로 과거에 이동하세요.":state.done("PZ021")?(state.done("PZ008")?state.deviceNext():"작업대에서 장치를 수리하세요."):state.usbComplete()?"PC 또는 보관함의 USB를 눌러 사고 로그를 분석하세요.":state.usbNext(),16);line();
   for(var mission:state.catalog().missions()){card(String.format("%02d  %s",mission.order(),(state.prototypeFailed()?mission.id().equals("M_USB")?"사고 로그 확보 및 분석":"시간이동장치 수리":mission.title())),state.missionNext(mission.id()),state.missionCompletedCount(mission.id()),state.catalog().missionSteps(mission.id()).size(),state.missionLocked(mission.id()));}text(state.canDepart()?"과거 좌표 개방":"분석과 장치 수리 완료 후 과거로 이동 가능",15);
  }else{text(state.isEscaped()?"탈출 완료":state.era()==Era.PAST?state.isWatchRepaired()?"손목 장치로 미래로 이동하세요.":state.hasSparePart()?"대체 부품을 선택하고 손목 장치를 수리하세요.":"황동 장치의 톱니바퀴를 수리하세요.":state.isCoreInstalled()?"손목 장치에서 최종 좌표를 입력하세요.":state.hasCorePart()?"손목 장치에 핵심 부품을 장착하세요.":"시간 실험 장치에서 전원 켜기 (임시)를 완료하세요.",16);}
  if(!lastMessage.isBlank()){line();text("최근 기록",16);text(lastMessage,14);}add(Box.createVerticalGlue());
  // BoxLayout shares an X alignment axis across text, separators and spacers.
  // Center-aligned filler used to push left-aligned text halfway across the page.
  for(Component child:getComponents())if(child instanceof JComponent component)component.setAlignmentX(LEFT_ALIGNMENT);
  NotebookScale.apply(this,NotebookScale.of(this));revalidate();repaint();}
}
