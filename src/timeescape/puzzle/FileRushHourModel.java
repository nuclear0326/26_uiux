package timeescape.puzzle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 데이터 파일 수출 퍼즐의 규칙 모델 (Swing UI와 분리됨).
 * 차와 달리, 각 조각은 파일 묶음이며 정해진 가로/세로 축으로만 이동한다.
 */
public final class FileRushHourModel {
    public static final int SIZE = 6;

    public record FileBlock(String id, String name, boolean horizontal,
                            int fixed, int length, int rgb) { }
    public record Move(String blockId, int from, int to) { }

    // fixed: 가로 파일은 행, 세로 파일은 열. position: 가로 파일은 시작 열, 세로 파일은 시작 행.
    private static final FileBlock[] FILES = {
        new FileBlock("SYS", "SYS.CFG", true,  2, 2, 0xDFAA65), // 목표 파일: 3행 우측 포트
        new FileBlock("LOG", "LOG",     false, 2, 3, 0x4EABB8),
        new FileBlock("CFG", "CFG",     false, 3, 2, 0x789ACB),
        new FileBlock("ENC", "ENC",     false, 4, 3, 0xB084BE),
        new FileBlock("KEY", "KEY",     false, 5, 2, 0x65BCA7),
        new FileBlock("MAP", "MAP",     true,  0, 2, 0x8399AF),
        new FileBlock("LIB", "LIB",     true,  5, 2, 0x9AACC1),
        new FileBlock("TMP", "TMP",     true,  0, 2, 0x7E98B0),
        new FileBlock("IDX", "IDX",     true,  3, 2, 0x77A8AA)
    };
    private static final int[] START = {0, 0, 1, 2, 1, 0, 2, 4, 2};

    private int[] positions = START.clone();
    private int moveCount;

    public void reset() { positions = START.clone(); moveCount = 0; }
    public FileBlock[] blocks() { return FILES.clone(); }
    public int position(int index) { return positions[index]; }
    public int moveCount() { return moveCount; }
    public boolean solved() { return positions[0] == SIZE - FILES[0].length(); }

    public int findIndex(String id) {
        for (int i = 0; i < FILES.length; i++) if (FILES[i].id().equals(id)) return i;
        return -1;
    }

    /** 한 번의 조작으로 여러 칸 이동 가능, 중간 칸 모두 비어야 이동 가능. */
    public boolean move(String id, int to) {
        int index = findIndex(id);
        if (index < 0 || to == positions[index] || !canMove(positions, index, to)) return false;
        positions[index] = to;
        moveCount++;
        return true;
    }

    private static boolean canMove(int[] state, int index, int to) {
        FileBlock file = FILES[index];
        if (to < 0 || to + file.length() > SIZE) return false;
        int from = state[index];
        if (to == from) return false;
        boolean[][] occupied = occupiedExcept(state, index);
        int lo = Math.min(from, to), hi = Math.max(from, to) + file.length();
        for (int pos = lo; pos < hi; pos++) {
            int row = file.horizontal() ? file.fixed() : pos;
            int col = file.horizontal() ? pos : file.fixed();
            if (occupied[row][col]) return false;
        }
        return true;
    }

    private static boolean[][] occupiedExcept(int[] state, int except) {
        boolean[][] occ = new boolean[SIZE][SIZE];
        for (int i = 0; i < FILES.length; i++) {
            if (i == except) continue;
            FileBlock f = FILES[i];
            for (int j = 0; j < f.length(); j++) {
                int r = f.horizontal() ? f.fixed() : state[i] + j;
                int c = f.horizontal() ? state[i] + j : f.fixed();
                occ[r][c] = true;
            }
        }
        return occ;
    }

    public int findAt(int row, int col) {
        if (row < 0 || col < 0 || row >= SIZE || col >= SIZE) return -1;
        for (int i = 0; i < FILES.length; i++) {
            FileBlock f = FILES[i];
            for (int j = 0; j < f.length(); j++) {
                int r = f.horizontal() ? f.fixed() : positions[i] + j;
                int c = f.horizontal() ? positions[i] + j : f.fixed();
                if (r == row && c == col) return i;
            }
        }
        return -1;
    }

    /** 현재 상태에서 목표 파일을 내보내기까지의 최단 경로를 BFS로 계산. */
    public List<Move> solution() {
        record Node(int[] positions, int parent, Move move) { }
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        Map<String, Integer> seen = new HashMap<>();
        nodes.add(new Node(positions.clone(), -1, null));
        queue.add(0);
        seen.put(Arrays.toString(positions), 0);
        while (!queue.isEmpty() && nodes.size() < 200000) {
            int at = queue.poll();
            int[] state = nodes.get(at).positions();
            if (state[0] == SIZE - FILES[0].length()) {
                ArrayList<Move> answer = new ArrayList<>();
                int cur = at;
                while (nodes.get(cur).parent() >= 0) {
                    answer.add(0, nodes.get(cur).move());
                    cur = nodes.get(cur).parent();
                }
                return answer;
            }
            for (int i = 0; i < FILES.length; i++) {
                for (int to = 0; to <= SIZE - FILES[i].length(); to++) {
                    if (!canMove(state, i, to)) continue;
                    int[] next = state.clone();
                    next[i] = to;
                    String key = Arrays.toString(next);
                    if (seen.containsKey(key)) continue;
                    int index = nodes.size();
                    nodes.add(new Node(next, at, new Move(FILES[i].id(), state[i], to)));
                    seen.put(key, index);
                    queue.add(index);
                }
            }
        }
        return List.of();
    }
}
