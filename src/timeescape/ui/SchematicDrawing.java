package timeescape.ui;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;
/** Approved drafting artwork, rendered once per puzzle session. */
final class SchematicDrawing {
 private SchematicDrawing(){}
 static final Color INK=new Color(179,206,204);
    static BufferedImage create(){
        BufferedImage b=new BufferedImage(420,420,BufferedImage.TYPE_INT_RGB);Graphics2D g=b.createGraphics();LabTheme.quality(g);
        g.setColor(new Color(34,60,68));g.fillRect(0,0,420,420);
        Random r=new Random(81);for(int i=0;i<18000;i++){int n=r.nextInt(19);g.setColor(new Color(160,176,162,n));g.fillRect(r.nextInt(420),r.nextInt(420),1,1);}
        g.setColor(new Color(126,163,164,24));for(int x=0;x<420;x+=14)g.drawLine(x,0,x,420);for(int y=0;y<420;y+=14)g.drawLine(0,y,420,y);
        g.setColor(new Color(158,190,189,110));g.setStroke(new BasicStroke(.8f));g.drawRect(14,14,391,391);
        // Device body and concentric alignment assembly cross several fragment edges.
        g.setColor(INK);g.setStroke(new BasicStroke(1.7f));g.drawRoundRect(100,87,216,249,13,13);
        g.drawRoundRect(108,95,200,233,8,8);
        g.drawOval(121,125,174,174);g.drawOval(132,136,152,152);g.drawOval(156,160,104,104);g.drawOval(165,169,86,86);
        for(int a=0;a<360;a+=15){double t=Math.toRadians(a);int R=a%45==0?82:79;g.drawLine(208+(int)(73*Math.cos(t)),212+(int)(73*Math.sin(t)),208+(int)(R*Math.cos(t)),212+(int)(R*Math.sin(t)));}
        g.setStroke(new BasicStroke(.8f));g.setColor(new Color(179,206,204,110));g.drawLine(208,112,208,313);g.drawLine(109,212,307,212);
        g.setColor(INK);g.setStroke(new BasicStroke(1.5f));g.drawRoundRect(190,190,36,44,4,4);g.drawOval(202,204,12,12);
        g.drawRect(167,48,83,26);g.drawLine(188,74,188,87);g.drawLine(229,74,229,87);g.drawLine(201,34,215,34);g.drawLine(208,34,208,48);
        // Different components, conductor routes, and terminal patterns are the matching clues.
        g.drawRect(30,61,48,69);g.drawLine(41,74,66,74);g.drawLine(41,83,66,83);g.drawLine(41,92,66,92);g.drawLine(41,101,66,101);
        route(g,78,108,91,108,91,160,121,160);
        g.drawRect(337,105,49,51);for(int y=114;y<150;y+=9){g.drawLine(344,y,379,y);}
        route(g,316,140,326,140,326,130,337,130);route(g,316,151,321,151,321,191,370,191,370,156);
        g.drawRoundRect(26,217,52,74,5,5);for(int i=0;i<4;i++){g.drawArc(34,229+i*12,35,13,0,180);}route(g,78,250,91,250,91,278,132,278);
        g.drawOval(340,246,42,42);g.drawOval(350,256,22,22);route(g,295,255,325,255,325,267,340,267);
        route(g,78,122,84,122,84,349,171,349,171,336);
        route(g,251,336,251,357,363,357,363,288);
        for(int x=121;x<300;x+=22){g.drawRect(x,307,12,9);g.drawLine(x+6,316,x+6,324);}
        g.drawRect(114,369,188,24);g.drawLine(245,369,245,393);
        g.setFont(GameFonts.body(14));g.drawString("시간 장치 / 제어 회로",122,386);g.drawString("R-07",252,386);
        g.setFont(GameFonts.body(14));g.drawString("전원",30,51);g.drawString("제어",339,96);g.drawString("출력",339,309);
        // Drafting dimensions and small locating marks, with no placement numbers.
        g.setStroke(new BasicStroke(.8f));g.setColor(new Color(179,206,204,155));g.drawLine(100,81,100,30);g.drawLine(316,81,316,30);g.drawLine(100,39,185,39);g.drawLine(236,39,316,39);
        for(int x:new int[]{100,316})g.drawLine(x-4,43,x+4,35);
        for(int[] p:new int[][]{{116,105},{300,105},{116,321},{300,321}}){g.drawOval(p[0]-3,p[1]-3,6,6);}
        g.dispose();return b;
    }
    static void route(Graphics2D g,int... points){Path2D p=new Path2D.Double();p.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)p.lineTo(points[i],points[i+1]);g.draw(p);g.drawOval(points[0]-2,points[1]-2,4,4);}
}
