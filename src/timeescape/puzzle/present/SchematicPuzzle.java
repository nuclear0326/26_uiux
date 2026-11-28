package timeescape.puzzle.present;

import java.util.Arrays;

/** Nine drawing fragments, nine board cells and nine reusable tray cells. No save state. */
public final class SchematicPuzzle {
    public static final int PIECES=9, CELLS=18;
    private static final int[] SHUFFLE={7,3,8,1,5,0,4,2,6};
    private final int[] cells=new int[CELLS];
    public SchematicPuzzle(){reset();}
    public String id(){return "PZ008";}
    public String title(){return "설계도 복원";}
    public int pieceAt(int cell){check(cell);return cells[cell];}
    public int[] snapshot(){return cells.clone();}
    public void reset(){Arrays.fill(cells,-1);System.arraycopy(SHUFFLE,0,cells,PIECES,PIECES);}
    /** Swap with an occupied cell, or move into an empty cell. Invalid drops do nothing. */
    public boolean move(int from,int to){
        if(from<0||from>=CELLS||to<0||to>=CELLS||from==to||cells[from]<0)return false;
        int replaced=cells[to];cells[to]=cells[from];cells[from]=replaced;return true;
    }
    public int placed(){int n=0;for(int i=0;i<PIECES;i++)if(cells[i]>=0)n++;return n;}
    public boolean solved(){for(int i=0;i<PIECES;i++)if(cells[i]!=i)return false;return true;}
    private static void check(int cell){if(cell<0||cell>=CELLS)throw new IllegalArgumentException("Invalid schematic cell: "+cell);}
}
