package timeescape.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.JComponent;
import timeescape.model.Era;

/** Restrained, aged scene illustration for the floor planter and its field note. */
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
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        paintSoil(g);
        paintPot(g);
        paintNote(g);
        paintSeeds(g);
        if(planted) paintSprout(g);
        g.dispose();
    }

    private void paintSoil(Graphics2D g) {
        // Uneven spill of dark potting earth, softened into the photographed floor.
        g.setColor(new Color(9,7,5,118));
        g.fillOval(270,930,390,44);
        Path2D earth=new Path2D.Double();
        earth.moveTo(293,927); earth.curveTo(305,906,343,916,359,911);
        earth.curveTo(382,899,409,915,428,908); earth.curveTo(451,900,480,914,501,906);
        earth.curveTo(529,897,554,916,579,909); earth.curveTo(608,903,630,920,647,930);
        earth.curveTo(620,950,598,960,562,958); earth.curveTo(532,972,497,955,468,963);
        earth.curveTo(438,970,410,956,378,963); earth.curveTo(346,970,315,954,293,947); earth.closePath();
        g.setPaint(new GradientPaint(0,904,new Color(77,51,33,232),0,962,new Color(35,25,19,238)));
        g.fill(earth);
        g.setColor(new Color(139,99,60,115));
        g.setStroke(new BasicStroke(2));
        g.drawArc(312,911,305,43,8,158);
        g.setColor(new Color(25,18,13,210));
        for(int i=0;i<27;i++) {
            int x=307+(i*43%321), y=920+(i*31%32), r=2+i%3;
            g.fillOval(x,y,r*2,r);
        }
        g.setColor(new Color(172,133,84,155));
        for(int i=0;i<8;i++) {
            int x=322+(i*71%286), y=916+(i*19%28);
            g.fillOval(x,y,3,2);
        }
    }

    private void paintPot(Graphics2D g) {
        // Medium hand-thrown terracotta pot, deliberately muted to match the room.
        g.setColor(new Color(7,5,4,140));
        g.fillOval(414,919,172,28);
        Polygon body=new Polygon(
                new int[]{425,567,550,443},
                new int[]{806,806,922,922},4);
        g.setPaint(new GradientPaint(421,0,new Color(119,68,45),568,0,new Color(64,39,30)));
        g.fillPolygon(body);
        g.setColor(new Color(185,119,76,130));
        g.setStroke(new BasicStroke(2));
        g.drawLine(430,826,560,826);
        g.drawLine(439,884,552,884);
        g.setColor(new Color(43,29,23,230));
        g.fillOval(415,788,158,40);
        g.setPaint(new GradientPaint(0,790,new Color(79,54,38),0,824,new Color(33,26,22)));
        g.fillOval(426,793,136,29);
        g.setColor(new Color(139,84,54));
        g.fillRoundRect(411,781,166,23,7,7);
        g.setColor(new Color(194,133,88,180));
        g.setStroke(new BasicStroke(1.6f));
        g.drawRoundRect(411,781,166,23,7,7);
        g.setColor(new Color(49,35,27,240));
        g.fillOval(423,785,142,20);
        // Small scuffs and kiln marks keep the silhouette from reading as a flat icon.
        g.setColor(new Color(219,161,109,90));
        g.drawArc(431,835,34,27,195,118);
        g.drawArc(516,857,22,33,35,112);
        g.setColor(new Color(36,25,20,155));
        g.drawLine(450,872,455,878);
        g.drawLine(530,839,534,844);
    }

    private void paintNote(Graphics2D g) {
        AffineTransform old=g.getTransform();
        g.rotate(-0.10,328,862);
        // Shadow, torn lower edge, yellowed paper and faint ruled writing.
        g.setColor(new Color(5,4,3,145));
        g.fillRoundRect(250,815,145,99,3,3);
        Path2D paper=new Path2D.Double();
        paper.moveTo(244,805); paper.lineTo(387,805); paper.lineTo(390,894);
        paper.lineTo(376,891); paper.lineTo(361,896); paper.lineTo(344,891);
        paper.lineTo(327,896); paper.lineTo(310,891); paper.lineTo(293,896);
        paper.lineTo(277,891); paper.lineTo(258,895); paper.lineTo(246,889); paper.closePath();
        g.setPaint(new GradientPaint(244,805,new Color(195,174,126),390,895,new Color(143,119,80)));
        g.fill(paper);
        g.setColor(new Color(76,59,39,190));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(paper);
        g.setColor(new Color(91,74,49,135));
        g.drawLine(258,842,375,842);
        g.drawLine(258,860,370,860);
        g.drawLine(258,878,370,878);
        g.setColor(new Color(70,54,37,225));
        g.setFont(GameFonts.button(11));
        g.drawString("실험 기록  17",258,827);
        g.setFont(GameFonts.body(10));
        g.drawString("청색광에서 발아가 빠름",258,839);
        g.drawString("물은 한 차례만 공급할 것",258,857);
        g.drawString("잎의 순서를 기록할 것",258,875);
        // Tarnished brass clip, visually fastening the note to the floor.
        g.setColor(new Color(122,91,49,220));
        g.fillRoundRect(374,809,10,15,2,2);
        g.setColor(new Color(194,155,91,175));
        g.drawRoundRect(374,809,10,15,2,2);
        g.setTransform(old);
    }

    private void paintSeeds(Graphics2D g) {
        int[][] seeds={{358,938},{377,946},{395,934},{414,947}};
        for(int i=0;i<seeds.length;i++) {
            int x=seeds[i][0], y=seeds[i][1];
            g.setColor(new Color(8,6,4,160)); g.fillOval(x+1,y+2,10,6);
            g.setPaint(new GradientPaint(x,y,new Color(175,139,82),x+10,y+6,new Color(69,49,31)));
            g.fillOval(x,y,10,6);
            g.setColor(new Color(221,190,126,145));
            g.drawArc(x+2,y+1,6,3,10,125);
        }
    }

    private void paintSprout(Graphics2D g) {
        // Restrained, delicate shoot: muted greens and a tiny pale flower bud.
        g.setStroke(new BasicStroke(3.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.setColor(new Color(54,87,47,235));
        g.drawLine(493,796,493,737);
        g.setStroke(new BasicStroke(2.2f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.drawLine(493,765,477,752);
        g.drawLine(493,755,509,744);
        g.setColor(new Color(103,133,71,225));
        g.fillOval(464,744,23,10);
        g.fillOval(503,736,22,10);
        g.setColor(new Color(173,150,91,235));
        g.fillOval(489,728,8,8);
        g.setColor(new Color(210,190,137,160));
        g.drawArc(490,729,5,5,30,100);
    }
}
