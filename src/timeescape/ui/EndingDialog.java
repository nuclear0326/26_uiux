package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Acknowledging the ending only disposes this modal; it never saves or exits. */
final class EndingDialog extends JDialog {
    private EndingPanel content;
    static void show(Frame owner){EndingDialog dialog=new EndingDialog(owner);dialog.setVisible(true);}
    private EndingDialog(Frame owner){
        super(owner,"탈출 성공",true);setUndecorated(true);setResizable(false);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JComponent root=owner instanceof RootPaneContainer rc?(JComponent)rc.getContentPane():null;BufferedImage background;int header=0;
        if(root!=null&&root.isShowing()){
            background=DevicePanel.capture(root);setBounds(new Rectangle(root.getLocationOnScreen(),root.getSize()));
            GameScenePanel scene=DevicePanel.findScene(root);if(scene!=null)header=SwingUtilities.convertPoint(scene,0,0,root).y;
        }else{background=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);setSize(900,650);setLocationRelativeTo(owner);}
        content=new EndingPanel(background,header,this::dispose);setContentPane(content);setCursor(GameCursors.normal());
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){content.confirm.requestFocusInWindow();}});
    }
    @Override public void dispose(){if(content!=null)content.cancelSession();super.dispose();}
}
