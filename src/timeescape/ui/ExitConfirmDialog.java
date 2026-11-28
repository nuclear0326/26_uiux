package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/** Only asks for a result; GameController retains its save-before-exit lifecycle. */
final class ExitConfirmDialog extends JDialog {
    private MenuConfirmPanel content;
    static boolean confirm(Frame owner){ExitConfirmDialog dialog=new ExitConfirmDialog(owner);dialog.setVisible(true);return dialog.content.isConfirmed();}
    private ExitConfirmDialog(Frame owner){
        super(owner,"게임 종료",true);setUndecorated(true);setResizable(false);setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JComponent root=owner instanceof RootPaneContainer rc?(JComponent)rc.getContentPane():null;
        BufferedImage background;int header=0;
        if(root!=null&&root.isShowing()){
            background=captureVisible(root);setBounds(new Rectangle(root.getLocationOnScreen(),root.getSize()));
            GameScenePanel scene=DevicePanel.findScene(root);if(scene!=null&&scene.isShowing())header=SwingUtilities.convertPoint(scene,0,0,root).y;
        }else{background=new BufferedImage(900,650,BufferedImage.TYPE_INT_RGB);setSize(900,650);setLocationRelativeTo(owner);}
        content=new MenuConfirmPanel(background,header,this::dispose,"게임 종료","게임을 종료하시겠습니까?","종료");setContentPane(content);setCursor(GameCursors.normal());
        addWindowListener(new WindowAdapter(){@Override public void windowOpened(WindowEvent e){content.cancel.requestFocusInWindow();}});
    }
    static BufferedImage captureVisible(JComponent root){
        BufferedImage image=new BufferedImage(Math.max(1,root.getWidth()),Math.max(1,root.getHeight()),BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();root.printAll(g);g.dispose();return image;
    }
    @Override public void dispose(){if(content!=null)content.cancelSession();super.dispose();}
}
