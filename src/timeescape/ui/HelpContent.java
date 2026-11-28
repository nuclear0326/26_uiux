package timeescape.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/** Text-only help content within the same modal shell as the game menu. */
final class HelpContent extends JPanel {
    private static final String[][] ROWS = {
        {"마우스 클릭","배경의 오브젝트를 조사합니다."},
        {"Shift / 휠 버튼","누르는 동안 상호작용 가능한 위치를 표시합니다."},
        {"연구 노트","현재 목표와 보유 아이템을 확인합니다."},
        {"아이템 클릭","보관함의 아이템을 사용합니다."},
        {"Esc","메뉴를 열거나 열린 창을 닫습니다."}
    };
    private static final String[] GUIDE = {
        "연구 노트의 현재 목표를 확인하며 연구실을 조사하세요.",
        "획득한 장치는 보관함에서 클릭해 사용할 수 있습니다."
    };
    final List<Rectangle> guideLines = new ArrayList<>();
    int contentBottom;
    HelpContent(){setOpaque(false);getAccessibleContext().setAccessibleName("조작 도움말");
        getAccessibleContext().setAccessibleDescription(java.util.Arrays.deepToString(ROWS)+String.join(" ",GUIDE));}
    private List<String> wrap(FontMetrics fm,String value,int width){
        List<String> lines=new ArrayList<>();String line="";
        for(char c:value.toCharArray()){
            if(!line.isEmpty()&&fm.stringWidth(line+c)>width){lines.add(line);line="";}
            line+=c;
        }
        if(!line.isEmpty())lines.add(line);return lines;
    }
    @Override protected void paintComponent(Graphics raw){
        Graphics2D g=(Graphics2D)raw.create();LabTheme.quality(g);
        Color bright=new Color(239,227,203),muted=new Color(188,184,173);
        boolean compact=getHeight()<360;
        int row=compact?40:46,desc=getWidth()<400?142:158;
        for(int i=0;i<ROWS.length;i++){
            int y=22+i*row;g.setFont(GameFonts.button(17));g.setColor(bright);g.drawString(ROWS[i][0],0,y);
            g.setFont(GameFonts.body(compact?14:16));g.setColor(muted);
            for(String line:wrap(g.getFontMetrics(),ROWS[i][1],getWidth()-desc)){g.drawString(line,desc,y);y+=19;}
        }
        int separator=row*5+12;
        g.setColor(new Color(151,151,145,90));g.drawLine(0,separator,getWidth(),separator);
        g.setFont(GameFonts.button(17));g.setColor(bright);g.drawString("진행 안내",0,separator+29);
        g.setFont(GameFonts.body(compact?14:16));g.setColor(muted);FontMetrics fm=g.getFontMetrics();int y=separator+(compact?50:59);
        guideLines.clear();
        for(String paragraph:GUIDE){
            for(String line:wrap(fm,paragraph,getWidth())){
                int width=fm.stringWidth(line),x=(getWidth()-width)/2;
                g.drawString(line,x,y);guideLines.add(new Rectangle(x,y-fm.getAscent(),width,fm.getHeight()));
                y+=compact?18:21;
            }
            y+=compact?4:6;
        }
        contentBottom=y-6;g.dispose();
    }
}
