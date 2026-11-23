package timeescape.puzzle;

import java.awt.Point;
import java.util.*;

public final class RepairModels {
    private RepairModels() {}
    public static final class Gears {
        public final int[][] target={{0,2,1,0},{1,0,2,1},{2,1,0,2},{0,2,1,0}};
        public final int[][] placed=new int[4][4];
        public final int[] stock=new int[3];
        public Gears(){for(int r=0;r<4;r++){Arrays.fill(placed[r],-1);for(int t:target[r])stock[t]++;}}
        public boolean put(int row,int col,int size){if(row<0||row>=4||col<0||col>=4||size<0||size>2||stock[size]==0||placed[row][col]>=0)return false;placed[row][col]=size;stock[size]--;return true;}
        public void remove(int row,int col){int s=placed[row][col];if(s>=0){stock[s]++;placed[row][col]=-1;}}
        public boolean faceDone(int row){return Arrays.equals(target[row],placed[row]);}
        public int filled(){int n=0;for(int[] row:placed)for(int s:row)if(s>=0)n++;return n;}
        public boolean solved(){for(int r=0;r<4;r++)if(!faceDone(r))return false;return true;}
    }
    public static final class Wires {
        public static final int N=21;
        public final boolean hard;
        public final int[] rows;
        private final boolean[][] open=new boolean[N][N];
        private int totalCells;
        public final java.util.List<java.util.List<Point>> paths=new ArrayList<>();
        public Wires(boolean hard){
            this.hard=hard;rows=hard?new int[]{4,8,12,16}:new int[]{5,10,15};
            for(int y:rows)paths.add(new ArrayList<>());
            String resource="/assets/puzzles/wires_"+(hard?"hard":"easy")+".txt";
            try(var stream=Wires.class.getResourceAsStream(resource)){
                if(stream==null)throw new IllegalStateException("Missing maze: "+resource);
                var lines=new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8).lines().toList();
                if(lines.size()!=N)throw new IllegalStateException("Maze height");
                for(int y=0;y<N;y++){
                    if(lines.get(y).length()!=N)throw new IllegalStateException("Maze width");
                    for(int x=0;x<N;x++)if(lines.get(y).charAt(x)=='.'){
                        if(!inside(x,y))throw new IllegalStateException("Outside sphere");
                        open[y][x]=true;totalCells++;
                    }
                }
                for(int i=0;i<rows.length;i++)if(blocked(3,rows[i])||blocked(17,rows[i]))throw new IllegalStateException("Blocked terminal");
            }catch(java.io.IOException ex){throw new IllegalStateException("Cannot read maze",ex);}
        }
        public Point start(int i){return new Point(3,rows[i]);}
        public Point end(int i){return new Point(17,rows[i]);}
        public boolean inside(int x,int y){return x>=0&&y>=0&&x<N&&y<N&&(x-10)*(x-10)+(y-10)*(y-10)<=100;}
        public boolean blocked(int x,int y){return !inside(x,y)||!open[y][x];}
        public int totalCells(){return totalCells;}
        public int coveredCells(){return paths.stream().mapToInt(java.util.List::size).sum();}
        public void undo(int i){var p=paths.get(i);if(p.size()>1)p.remove(p.size()-1);}
        /** Interpolate only straight mouse motion; never auto-route around corners. */
        public boolean extend(int i,Point target){
            var path=paths.get(i);if(path.isEmpty())return false;
            Point last=path.get(path.size()-1);
            if(last.x!=target.x&&last.y!=target.y)return false;
            int dx=Integer.compare(target.x,last.x),dy=Integer.compare(target.y,last.y);
            int steps=Math.abs(target.x-last.x)+Math.abs(target.y-last.y);
            boolean moved=false;
            for(int n=0;n<steps;n++){
                if(!step(i,new Point(last.x+dx,last.y+dy)))break;
                moved=true;last=path.get(path.size()-1);
            }
            return moved;
        }
        /** Select an existing circuit without destroying its route. */
        public int selectAt(Point cell){
            for(int i=0;i<rows.length;i++){
                if(paths.get(i).contains(cell))return i;
                if(start(i).equals(cell)){if(paths.get(i).isEmpty())begin(i);return i;}
            }
            return -1;
        }
        public void begin(int i){paths.get(i).clear();paths.get(i).add(start(i));}
        public boolean step(int i,Point p){
            var path=paths.get(i);if(path.isEmpty()||blocked(p.x,p.y))return false;
            Point last=path.get(path.size()-1);if(last.equals(p))return false;
            if(Math.abs(last.x-p.x)+Math.abs(last.y-p.y)!=1)return false;
            if(path.size()>1&&path.get(path.size()-2).equals(p)){path.remove(path.size()-1);return true;}
            if(last.equals(end(i))||path.contains(p))return false;
            for(int j=0;j<rows.length;j++)if(j!=i&&(start(j).equals(p)||end(j).equals(p)||paths.get(j).contains(p)))return false;
            path.add(new Point(p));return true;
        }
        public boolean connected(int i){var p=paths.get(i);return !p.isEmpty()&&p.get(p.size()-1).equals(end(i));}
        public boolean solved(){for(int i=0;i<rows.length;i++)if(!connected(i))return false;return coveredCells()==totalCells;}
    }
}
