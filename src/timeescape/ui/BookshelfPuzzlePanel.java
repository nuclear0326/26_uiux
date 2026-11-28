package timeescape.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import timeescape.puzzle.BookshelfPuzzle;

/** Interactive past-era bookshelf puzzle. Puzzle progress is committed by PuzzleRunner only. */
public final class BookshelfPuzzlePanel extends RepairPuzzlePanel {
    private final BookshelfPuzzle puzzle = new BookshelfPuzzle();
    private final JPanel board = new JPanel(new BorderLayout(10, 14));
    private final JPanel slots = new JPanel(new GridLayout(1, 4, 9, 0));
    private final JPanel books = new JPanel(new GridLayout(1, 5, 8, 0));
    private final JButton[] slotButtons = new JButton[4];
    private final java.util.Map<String, JButton> bookButtons = new java.util.LinkedHashMap<>();
    private final JTextArea detail = new JTextArea("책을 선택한 뒤 책장 칸을 누르세요. 책 버튼을 다시 누르면 책 설명을 볼 수 있습니다.");
    private String selected;
    private int hintLevel;

    public BookshelfPuzzlePanel(Runnable onSolved, Runnable onClose) {
        super(onSolved, onClose);
        board.setOpaque(false);
        JPanel shelf = new JPanel(new BorderLayout(0, 9)); shelf.setOpaque(false);
        shelf.add(section("01  복원할 책장 순서"), BorderLayout.NORTH);
        slots.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            JButton button = cardButton("<html><center>칸 " + (i + 1) + "<br><br>비어 있음</center></html>", true);
            button.setName("bookshelf-slot-" + (i + 1));
            button.setPreferredSize(new Dimension(170, 112));
            button.getAccessibleContext().setAccessibleName("책장 " + (i + 1) + "번째 칸");
            button.addActionListener(e -> clickSlot(index));
            slotButtons[i] = button; slots.add(button);
        }
        shelf.add(slots, BorderLayout.CENTER); board.add(shelf, BorderLayout.NORTH);

        JPanel lower = new JPanel(new BorderLayout(0, 8)); lower.setOpaque(false);
        lower.add(section("02  책장에 섞인 책"), BorderLayout.NORTH);
        books.setOpaque(false);
        for (String id : BookshelfPuzzle.books()) {
            JButton button = cardButton(bookMarkup(id, "선택"), false);
            button.setName("book-" + id); button.setPreferredSize(new Dimension(140, 88));
            button.setToolTipText(BookshelfPuzzle.detail(id));
            button.getAccessibleContext().setAccessibleName(BookshelfPuzzle.title(id));
            button.addActionListener(e -> clickBook(id));
            bookButtons.put(id, button); books.add(button);
        }
        lower.add(books, BorderLayout.CENTER);
        detail.setName("bookshelf-detail"); detail.setEditable(false); detail.setFocusable(false);
        detail.setLineWrap(true); detail.setWrapStyleWord(true); detail.setRows(2); detail.setFont(GameFonts.body(14));
        detail.setBackground(LabTheme.PANEL); detail.setForeground(LabTheme.TEXT);
        detail.setBorder(BorderFactory.createCompoundBorder(new LineBorder(LabTheme.LINE), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        lower.add(detail, BorderLayout.SOUTH); board.add(lower, BorderLayout.CENTER);

        JButton memo = new JButton("메모 다시 보기");
        JButton hint = new JButton("단서 보기");
        JButton reset = new JButton("초기화");
        memo.addActionListener(e -> showMemo()); hint.addActionListener(e -> showHint()); reset.addActionListener(e -> resetPuzzle());
        build("뒤바뀐 연구 서가", "과거 / 연구실 자료실",
            "책상 위 메모를 읽고 연구서 네 권을 순서대로 꽂으세요. 책을 선택한 뒤 칸을 누르면 배치됩니다. 틀린 배치는 다시 옮길 수 있습니다.",
            board, memo, hint, reset);
        action.setText("복원 확인"); action.setName("bookshelf-confirm"); action.addActionListener(e -> verify());
        refreshBoard();
    }

    private JLabel section(String text) {
        JLabel label = new JLabel(text); label.setFont(GameFonts.button(15)); label.setForeground(LabTheme.GOLD); return label;
    }
    private static String bookMarkup(String id, String suffix) {
        return "<html><center><font size='+2'>" + BookshelfPuzzle.symbol(id) + "</font><br>" +
            BookshelfPuzzle.title(id) + "<br><font size='-1'>" + suffix + "</font></center></html>";
    }
    /** Explicit custom paint avoids the white native button fill on Windows look-and-feels. */
    private JButton cardButton(String text, boolean slot) {
        JButton button = new JButton(text);
        button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
        button.setForeground(LabTheme.TEXT); button.setBackground(slot ? new Color(38, 43, 40) : new Color(51, 43, 33));
        button.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7)); button.setFont(GameFonts.button(15));
        button.setCursor(GameCursors.interact());
        button.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics raw, JComponent component) {
                JButton b = (JButton) component; Graphics2D g = (Graphics2D) raw.create(); LabTheme.quality(g);
                Color fill = b.getBackground();
                if (!b.isEnabled()) fill = new Color(25, 31, 32);
                else if (b.getModel().isPressed()) fill = new Color(57, 55, 43);
                else if (b.getModel().isRollover()) fill = new Color(67, 59, 42);
                g.setColor(fill); g.fillRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1, 8, 8);
                g.setColor(b.hasFocus() ? LabTheme.TEXT : LabTheme.GOLD);
                g.setStroke(new BasicStroke(b.hasFocus() ? 2f : 1f));
                g.drawRoundRect(1, 1, b.getWidth() - 3, b.getHeight() - 3, 8, 8); g.dispose();
                super.paint(raw, component);
            }
        });
        return button;
    }
    private void clickBook(String id) {
        if (isFinished()) return;
        if (id.equals(selected)) { detail.setText(BookshelfPuzzle.title(id) + "\n" + BookshelfPuzzle.detail(id)); return; }
        puzzle.take(id); selected = id;
        detail.setText("선택: " + BookshelfPuzzle.symbol(id) + " " + BookshelfPuzzle.title(id) + " — 놓을 칸을 선택하세요.");
        status.setText(BookshelfPuzzle.title(id) + "을(를) 선택했습니다."); refreshBoard();
    }
    private void clickSlot(int index) {
        if (isFinished()) return;
        String placed = puzzle.at(index);
        if (selected == null) {
            if (placed == null) { status.setText("먼저 아래에서 책을 선택하세요."); return; }
            selected = placed; puzzle.take(placed);
            status.setText(BookshelfPuzzle.title(placed) + "을(를) 꺼냈습니다. 새 칸을 선택하세요.");
        } else {
            puzzle.place(index, selected);
            status.setText(BookshelfPuzzle.title(selected) + "을(를) " + (index + 1) + "번째 칸에 놓았습니다.");
            selected = null;
        }
        refreshBoard();
    }
    private void showMemo() {
        detail.setText("연구 메모\n관측 기록은 왼쪽 끝에서 두 번째 칸. 시간 실험서는 사고 보고서 바로 왼쪽. 장치 도면은 남은 자리. 식물 도감은 연구 자료가 아닙니다.");
        status.setText("메모 단서: 관측 기록은 두 번째 칸입니다.");
    }
    private void showHint() {
        detail.setText("단서 " + Math.min(hintLevel + 1, 3) + " / 3\n" + BookshelfPuzzle.hint(Math.min(hintLevel, 2)));
        hintLevel = Math.min(hintLevel + 1, 3);
    }
    private void resetPuzzle() {
        if (isFinished()) return;
        puzzle.reset(); selected = null; hintLevel = 0;
        detail.setText("책을 선택한 뒤 책장 칸을 누르세요. 책 버튼을 다시 누르면 책 설명을 볼 수 있습니다.");
        status.setText("책 배치를 초기화했습니다."); refreshBoard();
    }
    private void verify() {
        if (isFinished()) return;
        if (!puzzle.full()) { status.setText("연구서 네 권을 모두 책장에 배치하세요."); return; }
        if (!puzzle.solved()) { status.setText("순서가 맞지 않습니다. 메모와 책의 표식을 다시 확인하세요."); return; }
        status.setText("정답입니다. 책등의 표식이 이어지며 숨겨진 연구 기록이 열립니다.");
        detail.setText("원본 연구 기록 복원 완료\n기록의 내용을 확인했습니다. 이 연구 자료는 진행 상태에 반영됩니다.");
        solve();
    }
    private void refreshBoard() {
        for (int i = 0; i < slotButtons.length; i++) {
            String id = puzzle.at(i); JButton b = slotButtons[i];
            b.setText(id == null ? "<html><center>칸 " + (i + 1) + "<br><br>비어 있음</center></html>" : bookMarkup(id, "배치됨"));
            b.setToolTipText(id == null ? "선택한 책을 놓을 칸" : BookshelfPuzzle.detail(id));
            b.getAccessibleContext().setAccessibleName("책장 " + (i + 1) + "번째 칸 " + (id == null ? "비어 있음" : BookshelfPuzzle.title(id)));
            b.setEnabled(!isFinished());
        }
        for (String id : BookshelfPuzzle.books()) {
            JButton b = bookButtons.get(id); boolean placed = puzzle.contains(id);
            b.setText(bookMarkup(id, placed ? "배치됨" : id.equals(selected) ? "선택 중" : "선택"));
            b.getAccessibleContext().setAccessibleName(BookshelfPuzzle.title(id) + (placed ? " 배치됨" : " 선택"));
            b.setEnabled(!isFinished());
        }
        action.setEnabled(puzzle.full()); board.revalidate(); board.repaint();
    }
    @Override protected void skipStage() {
        if (isFinished()) return;
        puzzle.reset();
        for (int i = 0; i < BookshelfPuzzle.solution().size(); i++) puzzle.place(i, BookshelfPuzzle.solution().get(i));
        selected = null; refreshBoard(); solve();
    }
}
