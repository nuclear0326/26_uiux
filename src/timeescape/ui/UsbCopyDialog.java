package timeescape.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import timeescape.puzzle.PuzzleResult;

/** Borderless modal host. Returns the same result contract as the former yes/no dialog. */
final class UsbCopyDialog extends JDialog {
    private UsbCopyPanel content;
    static PuzzleResult play(Frame owner){UsbCopyDialog dialog=new UsbCopyDialog(owner);dialog.setVisible(true);return dialog.content.result();}
    private UsbCopyDialog(Frame owner){
        super(owner,"USB 데이터 복사",true);setUndecorated(true);setResizable(false);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JComponent root=owner instanceof RootPaneContainer rc?(JComponent)rc.getContentPane():null;BufferedImage background;int header=0;
        if(root!=null&&root.isShowing()){
            background=DevicePanel.capture(root);setBounds(new Rectangle(root.getLocationOnScreen(),root.getSize()));
            GameScenePanel scene=DevicePanel.findScene(root);if(scene!=null)header=SwingUtilities.convertPoint(scene,0,0,root).y;
        }else{background=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);setSize(900,650);setLocationRelativeTo(owner);}
        content=new UsbCopyPanel(background,header,this::dispose);setContentPane(content);setCursor(GameCursors.normal());
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){content.cancel.requestFocusInWindow();}});
    }
    @Override public void dispose(){if(content!=null)content.cancelSession();super.dispose();}
}
