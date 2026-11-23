package timeescape.ui;
import java.awt.*;
import java.awt.geom.*;
import timeescape.puzzle.RepairModels.Gears;
/** Procedural brass artwork; slot and tray positions match the existing input grid. */
public final class GearBoardRenderer {
 private GearBoardRenderer(){}
 static int radius(int size){return new int[]{25,37,49}[size];}
 public static void paint(Graphics2D g,Gears gears,int selected,int dragSize,Point drag){
  g.setPaint(new GradientPaint(80,10,new Color(167,139,91),620,550,new Color(68,55,41)));
  g.fillRoundRect(80,10,540,540,24,24);g.setStroke(new BasicStroke(1));g.setColor(new Color(205,177,126));g.drawRoundRect(81,11,538,538,24,24);
  for(int y=15;y<550;y+=4){g.setColor(new Color(255,235,190,9));g.drawLine(94,y,607,y);}
  for(int row=0;row<4;row++)for(int col=0;col<4;col++){
   int x=90+col*130,y=20+row*130,cx=x+65,cy=y+65,size=gears.placed[row][col];
   boolean match=size>=0&&size==gears.target[row][col];
   g.setPaint(new GradientPaint(x,y,new Color(49,47,43),x+124,y+124,new Color(26,29,30)));g.fillRoundRect(x+4,y+4,122,122,16,16);
   g.setColor(new Color(175,151,104,100));g.setStroke(new BasicStroke(1));g.drawRoundRect(x+4,y+4,122,122,16,16);
   int hole=radius(gears.target[row][col])+5;
   g.setColor(new Color(12,18,21));g.fillOval(cx-hole,cy-hole,hole*2,hole*2);
   g.setColor(new Color(125,119,99));g.setStroke(new BasicStroke(1.4f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,10,new float[]{3,5},0));g.drawOval(cx-hole,cy-hole,hole*2,hole*2);
   g.setStroke(new BasicStroke(1));g.setColor(new Color(71,77,75));g.drawOval(cx-hole+4,cy-hole+4,hole*2-8,hole*2-8);
   if(size>=0)gear(g,cx,cy,size);
   else{g.setPaint(new GradientPaint(cx-5,cy-5,new Color(201,194,169),cx+5,cy+5,new Color(72,76,74)));g.fillOval(cx-5,cy-5,10,10);}
   g.setFont(GameFonts.body(11));g.setColor(new Color(159,160,147));g.drawString(String.format("%02d",row*4+col+1),x+12,y+20);
   g.setColor(match?new Color(121,212,172):new Color(65,70,64));g.fillOval(x+107,y+12,7,7);
  }
  for(int[] p:new int[][]{{87,17},{613,17},{87,543},{613,543}}){g.setColor(new Color(59,52,40));g.fillOval(p[0]-3,p[1]-3,6,6);g.setColor(new Color(188,165,115));g.drawLine(p[0]-2,p[1],p[0]+2,p[1]);}
  g.setFont(GameFonts.title(20));g.setColor(new Color(221,205,170));g.drawString("CLOCKWORK PARTS",650,48);
  g.setFont(GameFonts.body(14));g.setColor(new Color(170,184,189));g.drawString("홈 크기를 맞춰 기어를 복원하세요",650,78);
  for(int s=0;s<3;s++){
   int y=120+s*135;g.setPaint(new GradientPaint(670,y,new Color(46,55,61),840,y+120,new Color(23,32,39)));g.fillRoundRect(670,y,170,120,18,18);
   g.setColor(selected==s?new Color(223,188,120):new Color(77,89,96));g.setStroke(new BasicStroke(selected==s?2:1));g.drawRoundRect(670,y,170,120,18,18);
   gear(g,730,y+60,s);g.setFont(GameFonts.body(15));g.setColor(new Color(236,225,203));g.drawString(new String[]{"소형","중형","대형"}[s],791,y+45);
   g.setFont(GameFonts.body(17));g.drawString("× "+gears.stock[s],791,y+75);
  }
  g.setFont(GameFonts.body(13));g.setColor(new Color(151,199,177));g.drawString("초록 표시등: 알맞게 장착됨",665,550);
  if(dragSize>=0&&drag!=null)gear(g,drag.x,drag.y,dragSize);
 }
 static void gear(Graphics2D g,int x,int y,int size){
  double r=radius(size);Path2D p=new Path2D.Double();int teeth=12+size*4;
  for(int i=0;i<teeth*4;i++){double a=i*Math.PI*2/(teeth*4),rr=r+((i%4==1||i%4==2)?4:-4);double px=x+Math.cos(a)*rr,py=y+Math.sin(a)*rr;if(i==0)p.moveTo(px,py);else p.lineTo(px,py);}p.closePath();
  g.translate(2,3);g.setColor(new Color(0,0,0,120));g.fill(p);g.translate(-2,-3);
  g.setPaint(new GradientPaint(x-(float)r,y-(float)r,new Color(237,211,151),x+(float)r,y+(float)r,new Color(115,80,37)));g.fill(p);
  g.setStroke(new BasicStroke(1.2f));g.setColor(new Color(239,215,161));g.draw(p);
  g.setColor(new Color(114,84,43));g.drawOval(x-(int)r+9,y-(int)r+9,(int)r*2-18,(int)r*2-18);
  for(int i=0;i<5;i++){double a=i*Math.PI*2/5;int px=x+(int)(Math.cos(a)*r*.52),py=y+(int)(Math.sin(a)*r*.52);int hole=size+3;g.setColor(new Color(48,48,41));g.fillOval(px-hole,py-hole,hole*2,hole*2);g.setColor(new Color(233,205,142,150));g.drawArc(px-hole,py-hole,hole*2,hole*2,180,180);}
  g.setPaint(new GradientPaint(x-9,y-9,new Color(245,223,174),x+9,y+9,new Color(120,93,53)));g.fillOval(x-10,y-10,20,20);g.setColor(new Color(41,46,45));g.fillOval(x-4,y-4,8,8);
 }
}
