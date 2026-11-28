package timeescape.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import timeescape.system.GameAudio;
import timeescape.puzzle.FileRushHourModel;
import timeescape.puzzle.FileRushHourModel.FileBlock;

/**
 * Rush Hour 방식의 컴퓨터 파일 전송 퍼즐.
 * 숫자 스피너 대신 파일 조각을 마우스로 끌거나 화살표 키로 이동해 수출한다.
 * 파일 블록은 가로/세로 한 방향으로만 미끄러진다.
 */
public final class RushHourPanel extends JPanel implements AutoCloseable {
    private static final Color BACK = new Color(11, 22, 31);
    private static final Color PANEL = new Color(19, 39, 51);
    private static final Color BRIGHT = new Color(228, 245, 252);
    private static final Color CYAN = new Color(99, 224, 218);
    private static final Color MUTED = new Color(161, 191, 208);

    private final Runnable onSolved, onClose;
    private final FileRushHourModel model = new FileRushHourModel();
    private final Board board = new Board();
    private final JTextArea status = new JTextArea();
    private String selectedId = "SYS";
    private boolean finished;
    private final timeescape.core.CompletionReservation completion =
        new timeescape.core.CompletionReservation((delay, callback) -> {
            Timer timer = new Timer(delay, e -> callback.run());
            timer.setRepeats(false);
            timer.start();
            return timer::stop;
        });

    public RushHourPanel(Runnable onSolved,Runnable onClose) {
        super(new BorderLayout());this.onSolved=onSolved;this.onClose=onClose;
        initUI();bindKeys();
        refreshStatus("파일 블록을 밀어 SYS.CFG를 오른쪽 전송 포트로 내보내세요.");
    }
    void skip(){if(finished)return;finished=true;completion.cancel();onSolved.run();}

    private static JLabel label(String text, int size, Color color, boolean bold) {
        JLabel out = new JLabel(text);
        out.setForeground(color);
        out.setFont(size>=20?GameFonts.title(size):GameFonts.body(size));
        return out;
    }

    private JButton button(String text, Runnable callback) {
        JButton result = new JButton(text) {
            @Override protected void paintComponent(Graphics raw) {
                Graphics2D g = (Graphics2D) raw.create();
                g.setColor(getModel().isPressed() ? new Color(61, 101, 118)
                    : getModel().isRollover() ? new Color(49, 84, 102) : new Color(37, 62, 78));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
                super.paintComponent(raw);
            }
        };
        result.setContentAreaFilled(false);
        result.setOpaque(false);
        result.setFocusPainted(false);
        result.setForeground(BRIGHT);
        result.setFont(GameFonts.button(15));
        result.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(94, 150, 163)),
            BorderFactory.createEmptyBorder(9, 11, 9, 11)));
        result.addActionListener(e -> callback.run());
        return result;
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(14, 14));
        PuzzleChrome.decorate(root);
        add(root,BorderLayout.CENTER);

        JPanel header=PuzzleChrome.header("파일 옮기기","현재 / 연구실","",onClose);
        // This puzzle keeps its objective in the side panel, not a second header paragraph.
        header.remove(((BorderLayout)header.getLayout()).getLayoutComponent(BorderLayout.CENTER));
        root.add(header,BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(15, 0));
        center.setOpaque(false);
        center.add(board, BorderLayout.CENTER);

        JPanel side=new JPanel(new java.awt.GridBagLayout());side.setBackground(PANEL);
        side.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(48,92,108)),BorderFactory.createEmptyBorder(18,16,15,16)));
        JTextArea info=new JTextArea("황금색 SYS.CFG 파일을\n오른쪽 PORT로 전송합니다.\n\n파일 블록은 정해진 방향으로만 움직이고,\n다른 파일을 통과할 수 없습니다.");
        info.setOpaque(false);info.setEditable(false);info.setFocusable(false);info.setForeground(BRIGHT);info.setFont(GameFonts.body(14));info.setLineWrap(true);info.setWrapStyleWord(true);info.setPreferredSize(new Dimension(250,info.getFontMetrics(info.getFont()).getHeight()*6));
        java.awt.GridBagConstraints row=new java.awt.GridBagConstraints();row.gridx=0;row.gridy=0;row.weightx=1;row.fill=java.awt.GridBagConstraints.HORIZONTAL;row.anchor=java.awt.GridBagConstraints.NORTHWEST;
        side.add(label("목표",15,CYAN,true),row);
        row.gridy++;row.insets=new java.awt.Insets(8,0,0,0);side.add(info,row);
        JPanel navigation=new JPanel(new java.awt.GridLayout(1,2,7,0));navigation.setOpaque(false);navigation.setPreferredSize(new Dimension(250,40));

        navigation.add(button("초기화",this::resetPuzzle));navigation.add(button("힌트",this::showHint));
        JLabel caption=label("조작 안내",15,CYAN,true);
        JTextArea guidance=new JTextArea("• 마우스로 파일을 잡고 끌기\n• 파일 선택 후 방향키 이동\n• 파일은 여러 칸 한 번에 이동 가능\n• R: 초기화, H: 힌트");
        guidance.setEditable(false);guidance.setFocusable(false);guidance.setOpaque(false);guidance.setForeground(MUTED);guidance.setFont(GameFonts.body(13));guidance.setLineWrap(true);guidance.setWrapStyleWord(true);
        JPanel guide=new JPanel(new BorderLayout(0,7));guide.setOpaque(false);guide.add(caption,BorderLayout.NORTH);guide.add(guidance,BorderLayout.CENTER);
        guide.setPreferredSize(new Dimension(250,caption.getPreferredSize().height+7+guidance.getFontMetrics(guidance.getFont()).getHeight()*4+4));
        row.gridy++;row.insets=new java.awt.Insets(16,0,0,0);side.add(guide,row);
        row.gridy++;row.insets=new java.awt.Insets(18,0,0,0);side.add(navigation,row);
        // Keep the card size, centered vertically against the full board region.
        JPanel sidebar=new JPanel(new java.awt.GridBagLayout());sidebar.setOpaque(false);sidebar.setPreferredSize(new Dimension(285,side.getPreferredSize().height));java.awt.GridBagConstraints card=new java.awt.GridBagConstraints();card.gridx=0;card.gridy=0;card.weightx=1;card.weighty=1;card.fill=java.awt.GridBagConstraints.HORIZONTAL;card.anchor=java.awt.GridBagConstraints.CENTER;sidebar.add(side,card);center.add(sidebar,BorderLayout.EAST);
        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(15, 0));
        bottom.setOpaque(false);
        status.setBackground(new Color(18, 41, 52));
        status.setForeground(BRIGHT);
        status.setBorder(BorderFactory.createEmptyBorder(11, 12, 9, 12));
        status.setFont(GameFonts.body(13));
        status.setLineWrap(true);
        status.setWrapStyleWord(true);
        status.setRows(2);
        status.setFocusable(false);
        status.setEditable(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(button("터미널 닫기", onClose), BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
    }

    private void bindKeys() {
        // 키 이벤트가 버튼에 포커스가 있어도 동작하도록 최상위 컴포넌트에 바인딩
        String[] actions = {"left", "up", "right", "down", "reset", "hint"};
        String[] keys = {"LEFT", "UP", "RIGHT", "DOWN", "R", "H"};
        for (int i = 0; i < actions.length; i++) {
            final String action = actions[i];
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keys[i]), action);
            getActionMap().put(action, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    switch (action) {
                        case "left", "up" -> moveSelected(-1);
                        case "right", "down" -> moveSelected(1);
                        case "reset" -> resetPuzzle();
                        case "hint" -> showHint();
                    }
                }
            });
        }
    }

    private void showHint() {
        if (finished) return;
        GameAudio.get().play("file_select");
        List<FileRushHourModel.Move> path = model.solution();
        if (path.isEmpty()) {
            refreshStatus("현재 상태에서 유효한 내보내기 경로를 찾지 못했습니다. 초기화를 사용하세요.");
        } else {
            FileRushHourModel.Move first = path.get(0);
            FileBlock f = model.blocks()[model.findIndex(first.blockId())];
            selectedId = first.blockId();
            String direction = first.to() > first.from() ? (f.horizontal() ? "오른쪽" : "아래쪽")
                : (f.horizontal() ? "왼쪽" : "위쪽");
            refreshStatus("힌트: " + f.name() + " 파일을 " + direction + "으로 " + Math.abs(first.to() - first.from()) + "칸 이동하세요.");
            board.repaint();
        }
    }

    private void moveSelected(int direction) {
        if (finished) return;
        int idx = model.findIndex(selectedId);
        if (idx < 0) return;
        if (model.move(selectedId, model.position(idx) + direction)) {
            onMoved();
        } else {
            GameAudio.get().play("file_blocked");
            refreshStatus("이 방향은 다른 파일로 막혔거나 보드 끝입니다.");
        }
    }

    private void onMoved() {
        GameAudio.get().play("file_move");
        refreshStatus("파일 위치 변경 완료. SYS.CFG의 오른쪽 전송 경로를 확보하세요.");
        board.repaint();
        if (model.solved() && !finished) {
            finished = true;
            GameAudio.get().play("puzzle_success");
            refreshStatus("EXPORT COMPLETE — SYS.CFG 전송 완료. 설계 결함 분석 파일을 복원했습니다!");
            completion.schedule(600, () -> finished && model.solved() && isDisplayable(), () -> {
                onSolved.run();
            });
        }
    }

    private void resetPuzzle() {
        completion.cancel();
        finished = false;
        model.reset();
        selectedId = "SYS";
        board.draggingId = null;
        board.previewTo = -1;
        refreshStatus("파일 배치를 초기 상태로 되돌렸습니다.");
        board.repaint();
    }

    @Override public void close() {completion.close();}

    private void refreshStatus(String message) {
        status.setText(message.isBlank()?"":"›  " + message);
    }

    /** 실제 파일 모양의 둥근 카드. 자유로운 2D 이동이 아닌 지정 방향 슬라이딩만 가능하다. */
    private final class Board extends JPanel {
        private static final int CELL = 73;
        private static final int X = 27;
        private static final int Y = 57;
        private int pressX, pressY;
        private double scale(){return Math.max(.01,Math.min(getWidth()/525.0,getHeight()/540.0));}
        private int offsetX(){return (int)((getWidth()-525*scale())/2);}
        private int offsetY(){return (int)((getHeight()-540*scale())/2);}
        private int localX(MouseEvent e){return (int)((e.getX()-offsetX())/scale());}
        private int localY(MouseEvent e){return (int)((e.getY()-offsetY())/scale());}
        private String draggingId;
        private int previewTo = -1;

        Board() {
            setOpaque(true);
            setBackground(new Color(13, 31, 43));
            setPreferredSize(new Dimension(525, 540));
            setBorder(BorderFactory.createLineBorder(new Color(53, 104, 115)));
            MouseAdapter mouse = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    int col = (localX(e) - X) / CELL, row = (localY(e) - Y) / CELL;
                    if (e.getButton()!=MouseEvent.BUTTON1 || localX(e) < X || localY(e) < Y || col >= 6 || row >= 6 || finished) return;
                    int index = model.findAt(row, col);
                    if (index < 0) return;
                    selectedId = model.blocks()[index].id();
                    GameAudio.get().play("file_select");
                    draggingId = selectedId;
                    pressX = localX(e); pressY = localY(e);
                    previewTo = model.position(index);
                    refreshStatus("선택한 파일을 방향에 맞게 드래그하거나 방향키로 이동하세요.");
                    repaint();
                }
                @Override public void mouseDragged(MouseEvent e) {
                    if (draggingId == null || finished) return;
                    int index = model.findIndex(draggingId);
                    FileBlock f = model.blocks()[index];
                    int pixels = f.horizontal() ? localX(e) - pressX : localY(e) - pressY;
                    int delta = Math.round(pixels / (float) CELL);
                    int proposed = Math.max(0, Math.min(6 - f.length(), model.position(index) + delta));
                    previewTo = proposed;
                    repaint();
                }
                @Override public void mouseReleased(MouseEvent e) {
                    if (draggingId == null || finished) return;
                    String id = draggingId;
                    draggingId = null;
                    int target = previewTo;
                    previewTo = -1;
                    if (model.move(id, target)) onMoved();
                    else { GameAudio.get().play("file_blocked"); repaint(); }
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void text(Graphics2D g, String value, int x, int y, Font font, Color color) {
            g.setFont(font); g.setColor(color); g.drawString(value, x, y);
        }

        private void paintBlock(Graphics2D g, FileBlock f, int pos, boolean selected, float alpha) {
            int x = X + (f.horizontal() ? pos : f.fixed()) * CELL + 3;
            int y = Y + (f.horizontal() ? f.fixed() : pos) * CELL + 3;
            int w = (f.horizontal() ? f.length() * CELL : CELL) - 6;
            int h = (f.horizontal() ? CELL : f.length() * CELL) - 6;
            Graphics2D gg = (Graphics2D) g.create();
            gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            Color accent = new Color(f.rgb());
            gg.setColor(new Color(27, 51, 65));
            gg.fillRoundRect(x, y, w, h, 13, 13);
            gg.setColor(selected ? new Color(242, 227, 162) : accent);
            gg.setStroke(new BasicStroke(selected ? 3f : 2f));
            gg.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 12, 12);
            gg.setColor(accent);
            gg.fillRoundRect(x + 7, y + 8, w - 14, h - 16, 9, 9);
            gg.setColor(new Color(20, 38, 51, 155));
            gg.fillRoundRect(x + 12, y + 14, w - 24, h - 28, 7, 7);
            // 작은 문서 모양 아이콘: 게임 아트와 분리된 컴퓨터 파일의 상징
            int iconX = x + 16, iconY = y + 20;
            gg.setColor(new Color(232, 247, 249));
            gg.fillRoundRect(iconX, iconY, 16, 20, 3, 3);
            gg.setColor(new Color(78, 106, 123));
            gg.drawLine(iconX + 4, iconY + 7, iconX + 12, iconY + 7);
            gg.drawLine(iconX + 4, iconY + 12, iconX + 12, iconY + 12);
            gg.setColor(BRIGHT);
            String name = f.name();
            int fs = f.horizontal() ? 14 : 12;
            Font font = GameFonts.body(fs);
            gg.setFont(font);
            if (f.horizontal()) {
                gg.drawString(name, x + 41, y + h / 2 + 5);
            } else {
                gg.drawString(name, x + (w - gg.getFontMetrics().stringWidth(name))/2, y + h - 15);
                // 2칸 이상의 세로 파일에는 방향 스트라이프 표시
                gg.setColor(new Color(221, 239, 244, 170));
                gg.drawString("↕", x + w / 2 - 6, y + h / 2 + 8);
            }
            gg.dispose();
        }

        @Override protected void paintComponent(Graphics raw) {
            super.paintComponent(raw);
            Graphics2D g = (Graphics2D) raw.create();
            g.translate(offsetX(),offsetY());g.scale(scale(),scale());
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            text(g, "[ MEMORY / BLOCK MAP ]", X, 30, GameFonts.body(16), CYAN);
            int board = CELL * FileRushHourModel.SIZE;
            g.setColor(new Color(17, 42, 56));
            g.fillRoundRect(X - 4, Y - 4, board + 8, board + 8, 11, 11);
            g.setColor(new Color(64, 99, 113));
            for (int n = 0; n <= FileRushHourModel.SIZE; n++) {
                g.drawLine(X + n * CELL, Y, X + n * CELL, Y + board);
                g.drawLine(X, Y + n * CELL, X + board, Y + n * CELL);
            }
            g.setColor(new Color(119, 185, 186, 40));
            g.fillRoundRect(X + board - 3, Y + 2 * CELL + 4, 43, CELL - 8, 8, 8);
            int exitY = Y + 2 * CELL + CELL / 2;
            text(g, "→", X + board + 5, exitY + 6, GameFonts.body(22), new Color(251, 206, 128));
            text(g, "OUT", X + board + 3, exitY + 26, GameFonts.body(10), MUTED);
            FileBlock[] files = model.blocks();
            for (int i = 0; i < files.length; i++) {
                FileBlock f = files[i];
                boolean selected = f.id().equals(selectedId);
                int pos = model.position(i);
                paintBlock(g, f, pos, selected, draggingId != null && draggingId.equals(f.id()) ? .55f : 1f);
            }
            if (draggingId != null && previewTo >= 0) {
                int index = model.findIndex(draggingId);
                if (previewTo != model.position(index)) {
                    paintBlock(g, files[index], previewTo, true, .58f);
                }
            }
            g.dispose();
        }
    }
}
