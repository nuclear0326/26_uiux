package timeescape.ui;
import java.awt.*;import java.awt.event.*;import javax.swing.*;
/** Production HUD shared with offscreen verification. */
public final class GameHud extends JPanel {
 final JLayeredPane layers=new JLayeredPane();final TurningNotebook notebook=new TurningNotebook();final JPanel pages=new JPanel(new CardLayout());
 final JButton noteButton=NotebookStyle.button("연구 노트",true),recordTab=NotebookStyle.button("연구 기록",false),inventoryTab=NotebookStyle.button("보관함",false);
 final GameMenuOverlay gameMenu;
 final ItemAcquisitionToast itemNotice=new ItemAcquisitionToast();
 final FirstTravelOverlay travel=new FirstTravelOverlay();
 final DialogueOverlay dialogue=new DialogueOverlay();
 private final GameScenePanel gameScene;
 private JButton helpControl,menuControl;
 final JLabel timeValue=new JLabel("현재"),placeValue=new JLabel("연구실");
 final JLabel location=new JLabel("TIME ESCAPE");final JTextArea toast=NotebookStyle.text("",15);
 private final Timer toastTimer=new Timer(6500,e->toast.setVisible(false));private final MissionPanel missions;private String selectedPage="records";
 public GameHud(GameScenePanel scene,MissionPanel missions,InventoryPanel inventory,Runnable help,Runnable reset,Runnable menu){
  this.missions=missions;this.gameScene=scene;inventory.setAcquisitionListener(itemNotice::enqueue,itemNotice::clear);gameMenu=new GameMenuOverlay(scene,this::closeGameMenu,menu,reset);inventory.setInteractionAllowed(()->!notebook.isTurning()&&!gameMenu.isVisible()&&!dialogue.isVisible()&&!travel.isVisible());setLayout(new BorderLayout());setBackground(new Color(20,19,17));
  JPanel header=new JPanel(new BorderLayout(18,0));header.setBackground(new Color(27,25,22));header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,NotebookStyle.LINE),BorderFactory.createEmptyBorder(9,20,9,18)));
  location.setFont(GameFonts.title(19));location.setForeground(new Color(236,222,194));
  JPanel identity=new JPanel(new FlowLayout(FlowLayout.LEFT,22,0));identity.setOpaque(false);identity.add(location);
  JSeparator divider=new JSeparator(SwingConstants.VERTICAL);divider.setForeground(new Color(112,98,75));divider.setPreferredSize(new Dimension(1,34));identity.add(divider);
  JPanel timeField=locationField("시간",timeValue);timeField.setBorder(BorderFactory.createEmptyBorder(0,0,0,28));identity.add(timeField);identity.add(locationField("장소",placeValue));header.add(identity);
  JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,9,0));buttons.setOpaque(false);JButton helpButton=NotebookStyle.button("?",true),menuButton=NotebookStyle.button("",true);menuButton.setName("hamburger");helpControl=helpButton;menuControl=menuButton;helpButton.setPreferredSize(new Dimension(38,36));menuButton.setPreferredSize(new Dimension(38,36));
  helpButton.setToolTipText("조작 도움말");helpButton.getAccessibleContext().setAccessibleName("조작 도움말");menuButton.setToolTipText("메뉴");menuButton.getAccessibleContext().setAccessibleName("메뉴");
  noteButton.addActionListener(e->{if(notebook.isVisible())closeNotebook();else openNotebook(selectedPage);});helpButton.addActionListener(e->help.run());
  menuButton.addActionListener(e->{if(gameMenu.isVisible())closeGameMenu();else openGameMenu();});buttons.add(noteButton);buttons.add(helpButton);buttons.add(menuButton);header.add(buttons,BorderLayout.EAST);add(header,BorderLayout.NORTH);
  layers.setLayout(new LayoutManager(){public void addLayoutComponent(String n,Component c){}public void removeLayoutComponent(Component c){}public Dimension preferredLayoutSize(Container c){return new Dimension(1440,755);}public Dimension minimumLayoutSize(Container c){return new Dimension(800,500);}public void layoutContainer(Container c){int w=c.getWidth(),h=c.getHeight();scene.setBounds(0,0,w,h);travel.setBounds(0,0,w,h);dialogue.setBounds(0,0,w,h);itemNotice.setBounds(Math.max(12,w-378),14,Math.min(360,w-24),100);gameMenu.setBounds(0,0,w,h);int nw=Math.min(460,Math.max(370,(int)(w*.32)));notebook.setBounds(w-nw-18,18,nw,Math.min(660,h-36));int tw=Math.min(680,w-nw-54);toast.setBounds(24,h-94,Math.max(280,tw),70);}});
  layers.add(travel,Integer.valueOf(500));
  layers.add(dialogue,Integer.valueOf(400));layers.add(itemNotice,Integer.valueOf(250));layers.add(gameMenu,Integer.valueOf(300));layers.add(scene,Integer.valueOf(0));layers.add(notebook,Integer.valueOf(100));layers.add(toast,Integer.valueOf(200));add(layers);
  notebook.setLayout(new BorderLayout(0,16));notebook.setBorder(BorderFactory.createEmptyBorder(23,24,26,27));notebook.addMouseListener(new MouseAdapter(){});notebook.addMouseMotionListener(new MouseMotionAdapter(){});
  JPanel top=new JPanel(new BorderLayout(0,16));top.setOpaque(false);JPanel titleRow=new JPanel(new BorderLayout());titleRow.setOpaque(false);JLabel title=new JLabel("연구 노트");title.setFont(GameFonts.title(28));title.setForeground(NotebookStyle.INK);titleRow.add(title);JButton close=NotebookStyle.button("×",false);close.setPreferredSize(new Dimension(32,32));close.setToolTipText("연구 노트 닫기 (Esc)");close.addActionListener(e->closeNotebook());titleRow.add(close,BorderLayout.EAST);top.add(titleRow,BorderLayout.NORTH);
  JPanel tabs=new JPanel(new GridLayout(1,2,4,0));tabs.setOpaque(false);tabs.add(recordTab);tabs.add(inventoryTab);top.add(tabs);notebook.add(top,BorderLayout.NORTH);recordTab.addActionListener(e->openNotebook("records"));inventoryTab.addActionListener(e->openNotebook("inventory"));
  pages.setOpaque(false);pages.add(NotebookStyle.scroll(missions),"records");pages.add(NotebookStyle.scroll(inventory),"inventory");notebook.add(pages);notebook.setVisible(false);
  toast.setForeground(new Color(240,226,201));toast.setBackground(new Color(31,29,25));toast.setOpaque(true);toast.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(NotebookStyle.LINE),BorderFactory.createEmptyBorder(9,12,9,12)));toast.setVisible(false);toastTimer.setRepeats(false);
  getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"closeNotebook");getActionMap().put("closeNotebook",new AbstractAction(){public void actionPerformed(ActionEvent e){if(dialogue.isVisible()||travel.isVisible())return;if(gameMenu.isVisible())gameMenu.back();else closeNotebook();}});
 }
 public void setSaveSlots(java.util.function.Supplier<java.util.List<timeescape.system.SaveSlots.Summary>> slots,java.util.function.Predicate<timeescape.system.SaveSlots.Slot> save){gameMenu.setSaveSlots(slots,save,slot->false);}
 public void setSaveSlots(java.util.function.Supplier<java.util.List<timeescape.system.SaveSlots.Summary>> slots,java.util.function.Predicate<timeescape.system.SaveSlots.Slot> save,java.util.function.Predicate<timeescape.system.SaveSlots.Slot> load){gameMenu.setSaveSlots(slots,save,load);}
 public void setAsyncSaveSlots(java.util.function.Supplier<java.util.concurrent.CompletableFuture<java.util.List<timeescape.system.SaveSlots.Summary>>> slots,java.util.function.Function<timeescape.system.SaveSlots.Slot,java.util.concurrent.CompletableFuture<Boolean>> save,java.util.function.Function<timeescape.system.SaveSlots.Slot,java.util.concurrent.CompletableFuture<Boolean>> load){gameMenu.setAsyncSaveSlots(slots,save,load);}
 public void setTransitionSettings(java.util.function.BooleanSupplier get,java.util.function.Consumer<Boolean> set){gameMenu.setTransitionSettings(get,set);}
 public void openHelp(){if(dialogue.isVisible()||travel.isVisible())return;openGameMenu();gameMenu.showPage("help");}
 private JPanel locationField(String label,JLabel value){
  JPanel field=new JPanel();field.setOpaque(false);field.setLayout(new BoxLayout(field,BoxLayout.Y_AXIS));
  JLabel caption=new JLabel(label);caption.setFont(GameFonts.body(16));caption.setForeground(new Color(164,157,143));caption.setAlignmentX(LEFT_ALIGNMENT);
  value.setFont(GameFonts.button(22));value.setForeground(new Color(236,222,194));value.setAlignmentX(LEFT_ALIGNMENT);value.getAccessibleContext().setAccessibleName(label);
  field.add(caption);field.add(value);return field;
 }
 public void setLocation(String time,String place){timeValue.setText(time);placeValue.setText(place);location.setToolTipText("시간: "+time+" | 장소: "+place);revalidate();repaint();}
 public void openGameMenu(){if(dialogue.isVisible()||travel.isVisible())return;closeNotebook();toastTimer.stop();toast.setVisible(false);gameScene.setInputBlocked(true);noteButton.setEnabled(false);helpControl.setEnabled(false);menuControl.setSelected(true);gameMenu.open();}
 public void closeGameMenu(){if(gameMenu.isBusy())return;gameMenu.release();gameScene.setInputBlocked(false);noteButton.setEnabled(true);helpControl.setEnabled(true);menuControl.setSelected(false);menuControl.requestFocusInWindow();}
 public void setLocationTitle(String text){String[] parts=text.split(" · ",2);setLocation(parts[0],parts.length>1?parts[1]:"연구실");}
 public void openNotebook(String page){
  if(gameMenu.isVisible()||dialogue.isVisible()||travel.isVisible())return;
  boolean animate=notebook.isVisible()&&!selectedPage.equals(page);
  notebook.turnTo(()->{selectedPage=page;((CardLayout)pages.getLayout()).show(pages,page);recordTab.setSelected(page.equals("records"));inventoryTab.setSelected(page.equals("inventory"));},page.equals("records"),animate);
  notebook.setVisible(true);noteButton.setSelected(true);revalidate();repaint();recordTab.requestFocusInWindow();
 }
 public void closeNotebook(){notebook.finishTurn();notebook.setVisible(false);noteButton.setSelected(false);noteButton.requestFocusInWindow();repaint();}
 public void showDialogue(java.util.List<String> pages){showDialogue(pages,()->{});}
 public void showOpeningTutorial(java.util.List<String> pages){showDialogue(pages,this::openHelp);}
 private void showDialogue(java.util.List<String> pages,Runnable completed){
  if(gameMenu.isVisible())closeGameMenu();closeNotebook();toastTimer.stop();toast.setVisible(false);
  gameScene.setInputBlocked(true);noteButton.setEnabled(false);helpControl.setEnabled(false);menuControl.setEnabled(false);
  dialogue.open(pages,()->{finishDialogue();completed.run();});revalidate();repaint();
 }
 private void finishDialogue(){
  gameScene.setInputBlocked(false);noteButton.setEnabled(true);helpControl.setEnabled(true);menuControl.setEnabled(true);gameScene.requestFocusInWindow();
 }
 public void dismissDialogue(){dialogue.dismiss();finishDialogue();}
 public void showMessage(String value){missions.setLastMessage(value);if(dialogue.isVisible()||travel.isVisible())return;toast.setText(value);toast.setVisible(true);toastTimer.restart();}
 public boolean isTravelling(){return travel.isVisible();}
 public void startFirstTravel(Runnable completed){
  if(isTravelling())return;
  closeNotebook();dismissDialogue();gameMenu.release();itemNotice.clear();toastTimer.stop();toast.setVisible(false);
  gameScene.setInputBlocked(true);noteButton.setEnabled(false);helpControl.setEnabled(false);menuControl.setEnabled(false);
  travel.start(()->setLocation("현재","연구실"),()->{finishDialogue();completed.run();},()->{finishDialogue();showMessage("이동 연출을 준비하지 못했습니다. 장치를 다시 사용해 주세요.");});
 }
 public void cancelFirstTravel(){travel.cancel();finishDialogue();}
 public void removeNotify(){cancelFirstTravel();dismissDialogue();itemNotice.clear();gameMenu.release();gameScene.setInputBlocked(false);notebook.finishTurn();toastTimer.stop();toast.setVisible(false);super.removeNotify();}
}
