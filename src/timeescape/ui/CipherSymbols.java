package timeescape.ui;
import java.awt.*;
import javax.swing.*;

/** Draws the original paper glyph cells without duplicating or regenerating the artwork. */
final class CipherSymbols extends JPanel {
 // Row coordinates in the approved 1312 x 1199 cipher table. Digits never enter the crop.
 private static final int[] TOP={299,477,648,815,985};
 static Rectangle sourceForDigit(int digit){if(digit<0||digit>9)throw new IllegalArgumentException();return new Rectangle(digit<5?151:733,TOP[digit%5],174,157);}
 private static final int[] CODE={7,1,3,5};
 CipherSymbols(){setOpaque(false);setPreferredSize(new Dimension(330,83));getAccessibleContext().setAccessibleName("암호 문양: 네모, 삼각형, 별, 원");}
 protected void paintComponent(Graphics raw){Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  var image=CipherMemoOverlay.image();int w=70,h=64,gap=12,x=(getWidth()-(w*4+gap*3))/2,y=(getHeight()-h)/2;
  for(int digit:CODE){Rectangle r=sourceForDigit(digit);g.drawImage(image,x,y,x+w,y+h,r.x,r.y,r.x+r.width,r.y+r.height,null);g.setColor(LabTheme.LINE);g.drawRect(x,y,w,h);x+=w+gap;}g.dispose();
 }
}
