package timeescape.ui;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import timeescape.puzzle.PuzzleResult;
/** USB-specific labels and the unchanged puzzle result contract. */
final class UsbCopyPanel extends MenuConfirmPanel {
    final JButton copy;
    UsbCopyPanel(BufferedImage background,int header,Runnable close){
        super(background,header,close,"USB 데이터 복사","USB에 연구 데이터를 복사할까요?","복사");copy=confirm;
    }
    PuzzleResult result(){return PuzzleResult.from(isConfirmed());}
}
