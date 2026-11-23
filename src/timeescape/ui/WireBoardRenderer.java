package timeescape.ui;
import java.awt.*;
import timeescape.puzzle.RepairModels.Wires;
public final class WireBoardRenderer {
    private WireBoardRenderer() {}
    private static final Color[] COLORS={new Color(255,151,90),new Color(88,209,229),new Color(194,148,255),new Color(244,211,91)};
    public static void paint(Graphics2D g, Wires wires){paint(g,wires,-1);}
    public static void paint(Graphics2D g, Wires wires,int active){
            g.setColor(new Color(30,48,61));g.fillOval(178,18,504,504);g.setColor(new Color(104,147,166));g.setStroke(new BasicStroke(4));g.drawOval(178,18,504,504);
            for(int y=0;y<21;y++)for(int x=0;x<21;x++)if(wires.inside(x,y)){int px=190+x*24,py=30+y*24;if(wires.blocked(x,y)){g.setColor(new Color(103,118,128));g.fillRoundRect(px-10,py-10,20,20,4,4);}else{g.setColor(new Color(58,80,94));g.fillRoundRect(px-9,py-9,18,18,4,4);g.setColor(new Color(134,160,174));g.fillOval(px-2,py-2,4,4);}}
            for(int i=0;i<wires.rows.length;i++){g.setColor(COLORS[i]);g.setStroke(new BasicStroke(7,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));var path=wires.paths.get(i);for(int j=1;j<path.size();j++){Point a=path.get(j-1),b=path.get(j);g.drawLine(190+a.x*24,30+a.y*24,190+b.x*24,30+b.y*24);}for(Point p:new Point[]{wires.start(i),wires.end(i)}){int x=190+p.x*24,y=30+p.y*24;g.setColor(COLORS[i]);g.fillOval(x-11,y-11,22,22);g.setColor(new Color(17,24,32));g.setFont(GameFonts.body(14));g.drawString(""+(i+1),x-4,y+5);}}
            if(active>=0&&!wires.paths.get(active).isEmpty()){
                var path=wires.paths.get(active);Point tip=path.get(path.size()-1);
                g.setColor(Color.WHITE);g.setStroke(new BasicStroke(2));g.drawOval(190+tip.x*24-10,30+tip.y*24-10,20,20);
                g.setColor(COLORS[active]);g.setFont(GameFonts.body(16));g.drawString("선택: "+(active+1)+"번 · 흰 원에서 이어 그리기",690,355);
            }
            g.setColor(Color.WHITE);g.setFont(GameFonts.title(18));g.drawString(wires.hard?"BACK / 02":"FRONT / 01",710,115);g.setFont(GameFonts.body(14));g.drawString("회색 블록 = 장애물",710,155);g.drawString("기존 선 클릭: 선택 / 유지",710,185);g.drawString("직전 칸으로 이동: 되돌리기",710,215);g.drawString("모든 통로 칸을 채워야 완료",710,245);g.drawString("통로 "+wires.coveredCells()+" / "+wires.totalCells(),710,280);g.drawString("회로별로 한 줄 · 교차 금지",710,315);
        }
}
