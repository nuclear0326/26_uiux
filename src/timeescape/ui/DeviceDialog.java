package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import timeescape.core.GameState;

/** Modal device host. No native title bar; existing game callbacks own effects and autosaves. */
public final class DeviceDialog extends JDialog {
    private DeviceActions actions;
    public DeviceDialog(Frame owner,GameState state,boolean selected,Runnable changed){this(owner,state,selected,changed,null);}
    public DeviceDialog(Frame owner,GameState state,boolean selected,Runnable changed,Runnable intro){
        super(owner,"손목 시간여행 장치",true);
        setUndecorated(true);setResizable(false);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JComponent root=owner instanceof RootPaneContainer rc?(JComponent)rc.getContentPane():null;
        BufferedImage background;int header=0;
        if(root!=null&&root.isShowing()){
            background=DevicePanel.capture(root);setBounds(new Rectangle(root.getLocationOnScreen(),root.getSize()));
            GameScenePanel scene=DevicePanel.findScene(root);if(scene!=null)header=SwingUtilities.convertPoint(scene,0,0,root).y;
        }else{background=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);setSize(900,650);setLocationRelativeTo(owner);}
        actions=new DeviceActions(state,selected,this::dispose,changed,intro);
        DevicePanel content=new DevicePanel(background,header,actions,this::dispose);setContentPane(content);setCursor(GameCursors.normal());
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){content.close.requestFocusInWindow();}});
    }
    @Override public void dispose(){if(actions!=null)actions.cancel();super.dispose();}
}
