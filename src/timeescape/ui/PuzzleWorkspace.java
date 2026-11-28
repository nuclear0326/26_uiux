package timeescape.ui;

import java.awt.*;
import javax.swing.*;

/** Shared board above a compact goal / controls / actions strip. */
final class PuzzleWorkspace {
    static final Color BACK = new Color(15,30,38);
    static final Color LINE = new Color(52,76,85);
    static final Color ACCENT = new Color(96,209,220);
    private PuzzleWorkspace() {}

    static JPanel header(String title,String context,Runnable close){
        JPanel header=PuzzleChrome.header(title,context,"",close);
        header.remove(((BorderLayout)header.getLayout()).getLayoutComponent(BorderLayout.CENTER));
        return header;
    }
    static JPanel surface(JComponent board,String goal,String guide,JComponent actions){return surface(board,goal,guide,actions,null);}
    static JPanel surface(JComponent board,String goal,String guide,JComponent actions,JComponent reference){
        JPanel surface=new JPanel(new BorderLayout());
        surface.setBackground(BACK);surface.setBorder(BorderFactory.createLineBorder(LINE));
        board.setBackground(BACK);surface.add(board,BorderLayout.CENTER);
        JPanel strip=new JPanel(new GridBagLayout());strip.setOpaque(false);strip.setName("puzzle-bottom-strip");
        strip.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,0,0,0,LINE),BorderFactory.createEmptyBorder(18,20,18,20)));
        JPanel goalSection=section("목표",goal);
        if(reference!=null){
            JPanel row=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));row.setOpaque(false);row.add(reference);goalSection.add(row,BorderLayout.SOUTH);
            goalSection.setPreferredSize(new Dimension(220,Math.max(106,goalSection.getPreferredSize().height+reference.getPreferredSize().height+9)));
        }
        JComponent[] columns={goalSection,section("조작 안내",guide),actions};
        for(int i=0;i<columns.length;i++){
            columns[i].setMinimumSize(new Dimension(0,0));
            GridBagConstraints c=new GridBagConstraints();c.gridx=i;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTH;c.insets=new Insets(0,i==0?0:18,0,0);
            strip.add(columns[i],c);
        }
        surface.add(strip,BorderLayout.SOUTH);return surface;
    }
    private static JPanel section(String heading,String text){
        JPanel section=new JPanel(new BorderLayout(0,9));section.setOpaque(false);
        JLabel title=new JLabel(heading);title.setFont(GameFonts.body(15));title.setForeground(ACCENT);section.add(title,BorderLayout.NORTH);
        JTextArea copy=new JTextArea(text);copy.setFont(GameFonts.body(15));copy.setForeground(LabTheme.TEXT);copy.setEditable(false);copy.setFocusable(false);copy.setOpaque(false);copy.setLineWrap(true);copy.setWrapStyleWord(true);copy.setRows(text.split("\n").length);copy.setBorder(BorderFactory.createEmptyBorder());section.add(copy,BorderLayout.CENTER);
        section.setPreferredSize(new Dimension(220,section.getPreferredSize().height));return section;
    }
    static JPanel buttons(JButton... buttons){
        JPanel panel=new JPanel(new GridLayout(1,buttons.length,8,0));panel.setOpaque(false);
        for(JButton button:buttons){PuzzleChrome.styleButton(button);panel.add(button);}
        panel.setPreferredSize(new Dimension(220,42));panel.setMinimumSize(new Dimension(0,42));return panel;
    }
}
