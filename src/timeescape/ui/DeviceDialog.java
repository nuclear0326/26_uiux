package timeescape.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import timeescape.core.GameState;
import timeescape.core.TimeTravelController;
import timeescape.model.Era;

/** 손목 장치를 인벤토리에서 클릭할 때만 여는 사용/시간이동 창. */
public final class DeviceDialog extends JDialog {
    private final GameState state;
    private final boolean spareSelected;
    private final Runnable onChange;

    public DeviceDialog(Frame owner, GameState state, boolean spareSelected, Runnable onChange) {
        super(owner, "손목 시간여행 장치", true);
        this.state = state;
        this.spareSelected = spareSelected;
        this.onChange = onChange;
        setSize(500, 340);
        setMinimumSize(new Dimension(460, 320));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JPanel root = new JPanel(new BorderLayout(12, 15));
        root.setBackground(new Color(19, 27, 36));
        root.setBorder(BorderFactory.createEmptyBorder(24, 24, 20, 24));
        setContentPane(root);

        JLabel title = new JLabel("TIME DEVICE  /  STATUS");
        title.setFont(GameFonts.title(20));
        title.setForeground(new Color(106, 230, 224));
        root.add(title, BorderLayout.NORTH);

        JTextArea description = new JTextArea(describe());
        description.setEditable(false);
        description.setFocusable(false);
        description.setForeground(new Color(226, 236, 242));
        description.setBackground(new Color(31, 43, 55));
        description.setFont(GameFonts.body(15));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setBorder(BorderFactory.createEmptyBorder(20, 18, 15, 18));
        root.add(description, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton close = new JButton("닫기");
        close.addActionListener(e -> dispose());
        JButton action = new JButton(actionName());
        action.setEnabled(isActionEnabled());
        action.addActionListener(e -> {
            execute();
            dispose();
            onChange.run();
        });
        buttons.add(close);
        buttons.add(action);
        root.add(buttons, BorderLayout.SOUTH);
    }

    private String describe() {
        if (state.era() == Era.PRESENT) {
            return "USB 확보: " + (state.usbComplete() ? "완료" : "미완료")
                + "\n장치 제작: " + (state.deviceComplete() ? "완료" : "미완료")
                + "\n\n" + (state.canDepart() ? "두 미션 완료. 과거로 이동할 수 있습니다." : "두 미션을 모두 완료해야 과거로 이동할 수 있습니다.");
        }
        if (state.era() == Era.PAST && !state.isWatchRepaired()) {
            return "장치 상태: 심각하게 파손됨\n과거 좌표 이동: 완료\n\n과거 작업대의 황동 장치에서 대체 부품을 찾고, 인벤토리에서 부품을 선택한 뒤 이 장치를 다시 클릭하세요.";
        }
        if (state.era() == Era.PAST) {
            return "장치 상태: 임시 복구 완료\n사용 가능한 기능: 수정된 미래 좌표로 이동\n\n이제 미래의 연구실로 이동할 수 있습니다.";
        }
        return state.isCoreInstalled()
            ? "핵심 부품: 장착 완료\n장치 상태: 최종 복구\n\n최종 시간 좌표를 입력하여 연구실을 탈출하세요."
            : state.hasCorePart()
                ? "핵심 부품: 확보됨\n장치 상태: 추가 수리 필요\n\n확보한 부품을 손목 장치에 장착할 수 있습니다."
                : "장치 상태: 미래 핵심 부품 필요\n연구실 상태: 사고 직후 봉쇄\n\n중앙 시간 실험 장치에서 필요한 핵심 부품을 확보하세요.";
    }

    private String actionName() {
        if (state.era() == Era.PRESENT) return "과거로 시간 이동";
        if (state.era() == Era.PAST && !state.isWatchRepaired()) return "대체 부품 장착";
        if (state.era() == Era.PAST) return "미래로 시간 이동";
        if (state.isCoreInstalled()) return "최종 좌표 입력 · 탈출";
        return "미래 핵심 부품 장착";
    }

    private boolean isActionEnabled() {
        if (state.era() == Era.PRESENT) return state.canDepart();
        if (state.era() == Era.PAST && !state.isWatchRepaired()) return state.hasSparePart() && spareSelected;
        if (state.era() == Era.PAST) return true;
        return state.hasCorePart() || state.isCoreInstalled();
    }

    private void execute() {
        if (state.era() == Era.PRESENT) new TimeTravelController(state).travel();
        else if (state.era() == Era.PAST && !state.isWatchRepaired()) state.repairWatch();
        else if (state.era() == Era.PAST) new TimeTravelController(state).travel();
        else if (!state.isCoreInstalled()) state.installCorePart();
        else state.escape();
    }
}
