package timeescape.ui;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
/** Same responsive image viewer for room inspection and inventory rereading. */
public final class CipherMemoOverlay extends JPanel {
 private static class Asset { static final BufferedImage IMAGE=load(); }
 static BufferedImage image(){return Asset.IMAGE;}
 private static BufferedImage load(){try(var in=CipherMemoOverlay.class.getResourceAsStream("/assets/images/notebook/cipher_table.png")){if(in==null)throw new IllegalStateException("Missing cipher table");return ImageIO.read(in);}catch(java.io.IOException e){throw new IllegalStateException(e);}}
 private final JButton close=NotebookStyle.closeButton();
 public CipherMemoOverlay(Runnable dismiss){setOpaque(false);setLayout(null);setVisible(false);addMouseListener(new MouseAdapter(){});addMouseMotionListener(new MouseMotionAdapter(){});close.setToolTipText("해독표 닫기 (Esc)");close.getAccessibleContext().setAccessibleName("해독표 닫기");close.addActionListener(e->dismiss.run());add(close);getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"),"dismiss");getActionMap().put("dismiss",new AbstractAction(){public void actionPerformed(ActionEvent e){dismiss.run();}});}
 Rectangle paperBounds(){var img=image();double scale=Math.min(Math.max(1,getWidth()-64)/(double)img.getWidth(),Math.max(1,getHeight()-70)/(double)img.getHeight());int w=(int)(img.getWidth()*scale),h=(int)(img.getHeight()*scale);return new Rectangle((getWidth()-w)/2,(getHeight()-h)/2,w,h);}
 public void doLayout(){Rectangle r=paperBounds();close.setBounds(r.x+r.width-43,r.y+8,34,34);}
 public void open(){setVisible(true);doLayout();close.requestFocusInWindow();repaint();}
 protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);g.setColor(new Color(0,0,0,178));g.fillRect(0,0,getWidth(),getHeight());Rectangle r=paperBounds();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);g.drawImage(image(),r.x,r.y,r.width,r.height,null);g.dispose();}
 static void showReference(Window owner){JDialog d=new JDialog(owner,"메모 해독표",Dialog.ModalityType.APPLICATION_MODAL);d.setUndecorated(true);d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);var panel=new CipherMemoOverlay(d::dispose);d.setContentPane(panel);d.setSize(Math.min(850,owner.getWidth()),Math.min(780,owner.getHeight()));d.setLocationRelativeTo(owner);PuzzleChrome.bindClose(d.getRootPane(),d::dispose);panel.open();d.setVisible(true);}
}
