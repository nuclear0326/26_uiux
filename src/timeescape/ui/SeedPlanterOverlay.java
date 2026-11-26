package timeescape.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;
import timeescape.model.Era;

/** Java2D scene illustration for the floor planter, note and loose seeds. */
final class SeedPlanterOverlay extends JComponent {
    private static final int IMAGE_W=1920, IMAGE_H=1080;
    private Era era=Era.PAST;
    private boolean planted;

    SeedPlanterOverlay() {
        setOpaque(false);
        setFocusable(false);
        setEnabled(false);
    }

    void setSceneState(Era era, boolean planted) {
        this.era=era;
        this.planted=planted;
        repaint();
    }

    /** Let the parent scene receive mouse clicks for its registered hotspot. */
    @Override public boolean contains(int x,int y) { return false; }

    @Override protected void paintComponent(Graphics raw) {
        if (era!=Era.PAST) return;
        Graphics2D g=(Graphics2D)raw.create();
        Rectangle viewport=SceneViewport.cover(getWidth(),getHeight());
        g.translate(viewport.x,viewport.y);
        g.scale(viewport.width/(double)IMAGE_W,viewport.height/(double)IMAGE_H);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        paintFloorDirt(g);
        paintPot(g);
        paintNote(g);
        paintSeeds(g);
        if(planted) paintSprout(g);
        g.dispose();
    }

    private void paintFloorDirt(Graphics2D g) {
        g.setColor(new Color(15,11,8,105));
        g.fillOval(245,930,455,62);
        g.setColor(new Color(75,48,29,205));
        g.fillOval(260,910,410,65);
        g.setColor(new Color(112,76,47,210));
        g.fillOval(280,919,370,44);
        g.setColor(new Color(45,31,22,220));
        for(int i=0;i<18;i++) {
            int x=282+(i*47%354), y=925+(i*29%30);
            g.fillOval(x,y,5+(i%3),3+(i%2));
        }
    }

    private void paintPot(Graphics2D g) {
        // Soft cast shadow and a medium-sized aged terracotta planter.
        g.setColor(new Color(10,8,6,100));
        g.fillOval(396,914,194,35);
        Polygon body=new Polygon(
                new int[]{414,566,548,435},
                new int[]{803,803,925,925},4);
        g.setPaint(new java.awt.GradientPaint(410,0,new Color(159,91,55),570,0,new Color(89,48,34)));
        g.fillPolygon(body);
        g.setColor(new Color(205,132,83,210));
        g.setStroke(new BasicStroke(3));
        g.drawLine(421,823,559,823);
        g.drawLine(432,884,551,884);
        g.setColor(new Color(64,39,29,190));
        g.fillOval(413,785,155,42);
        g.setPaint(new java.awt.GradientPaint(0,786,new Color(113,73,45),0,819,new Color(45,34,26)));
        g.fillOval(422,791,137,30);
        g.setColor(new Color(198,127,79));
        g.fillRoundRect(405,778,171,25,9,9);
        g.setColor(new Color(229,160,105,210));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(405,778,171,25,9,9);
        g.setColor(new Color(72,47,33,220));
        g.fillOval(418,783,145,22);
        g.setColor(new Color(120,82,50,220));
        for(int i=0;i<13;i++) {
            int x=429+(i*31%112), y=787+(i*11%13);
            g.fillOval(x,y,5,3);
        }
    }

    private void paintNote(Graphics2D g) {
        AffineTransform old=g.getTransform();
        g.rotate(-0.12,330,862);
        g.setColor(new Color(8,7,6,100));
        g.fillRoundRect(252,814,150,103,5,5);
        g.setColor(new Color(202,184,139));
        g.fillRoundRect(244,804,150,103,5,5);
        g.setColor(new Color(104,83,55));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(244,804,150,103,5,5);
        g.setFont(GameFonts.button(13));
        g.drawString("재배 기록",260,829);
        g.setFont(GameFonts.body(12));
        g.drawString("푸른 빛 아래",260,851);
        g.drawString("물은 한 번만",260,869);
        g.setColor(new Color(133,74,48,170));
        g.drawLine(260,878,374,878);
        g.setTransform(old);
    }

    private void paintSeeds(Graphics2D g) {
        int[][] seeds={{353,932},{371,944},{386,930},{400,948}};
        for(int i=0;i<seeds.length;i++) {
            int x=seeds[i][0],y=seeds[i][1];
            g.setColor(new Color(30,21,15,190));
            g.fillOval(x+1,y+2,13,8);
            g.setPaint(new java.awt.GradientPaint(x,y,new Color(212,174,105),x+12,y+8,new Color(105,69,39)));
            g.fillOval(x,y,13,8);
            g.setColor(new Color(235,207,143,190));
            g.drawArc(x+2,y+1,8,5,15,120);
        }
    }

    private void paintSprout(Graphics2D g) {
        g.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.setColor(new Color(94,139,81));
        g.drawLine(490,794,490,724);
        g.setColor(new Color(132,183,103));
        g.fillOval(458,728,39,19);
        g.fillOval(488,744,39,19);
        g.setColor(new Color(202,183,91));
        g.fillOval(481,714,18,18);
        g.setColor(new Color(237,220,149,210));
        g.drawArc(484,716,10,10,35,110);
    }
}
