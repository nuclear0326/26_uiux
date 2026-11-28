package timeescape.ui;
import java.awt.*;
import java.util.IdentityHashMap;
import timeescape.scene.Hotspot;
import timeescape.system.ShiftGlint;

/** Approved warm reflection overlay. Small geometry caches only; no frame images allocated. */
final class ObjectHighlightEffect {
 private ObjectHighlightEffect(){}
 private static final IdentityHashMap<Hotspot,Geometry> CACHE=new IdentityHashMap<>();
 private static final BasicStroke[] HALO=strokes(30,24,18,12,6),EDGE=strokes(14,8,3);
 private static BasicStroke[] strokes(int... widths){BasicStroke[] out=new BasicStroke[widths.length];for(int i=0;i<out.length;i++)out[i]=new BasicStroke(widths[i],BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);return out;}
 private static final class Geometry {
  final Rectangle bounds;final double seed;final java.util.List<Point> spots=new java.util.ArrayList<>();
  Geometry(Hotspot h){bounds=h.bounds();seed=(h.id().hashCode()&0xffff)*.01;
   for(int i=0;i<24&&spots.size()<3;i++){
    double u=.15+((h.id().hashCode()&255)+i*43)%83/82.0*.7,v=.14+((h.id().hashCode()&127)+i*29)%79/78.0*.7;
    int x=(int)(bounds.x+bounds.width*u),y=(int)(bounds.y+bounds.height*v);if(h.shape().contains(x,y))spots.add(new Point(x,y));
   }
  }
 }
 static void paint(Graphics2D target,Hotspot h,double t,float strength){
  if(strength<=0)return;
  Geometry geometry=CACHE.computeIfAbsent(h,Geometry::new);
  Rectangle b=geometry.bounds;Shape shape=h.shape();double seed=geometry.seed;
  float breath=(float)(.75+.25*Math.sin(t*Math.PI*2/3.84+seed));Graphics2D g=(Graphics2D)target.create();g.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f,strength)));
  // Soft local light has no label box or opaque fill and only belongs to READY objects.
  for(BasicStroke stroke:HALO){g.setStroke(stroke);g.setColor(new Color(245,203,127,(int)(2+breath*2)));g.draw(shape);}
  Graphics2D inside=(Graphics2D)g.create();inside.clip(shape);
  float cx=(float)b.getCenterX(),cy=(float)b.getCenterY(),radius=(float)Math.max(b.width,b.height)*.70f;
  inside.setPaint(new RadialGradientPaint(cx,cy,radius,new float[]{0,.65f,1},new Color[]{new Color(255,225,166,(int)(44*breath)),new Color(246,207,143,16),new Color(246,207,143,0)}));inside.fill(shape);
  // Larger original Effekseer glints, fixed in place rather than drifting circles.
  for(int placed=0;placed<geometry.spots.size();placed++){
   Point spot=geometry.spots.get(placed);int f=Math.floorMod((int)Math.floor(t*25+seed*9+placed*24),96),size=82+placed%2*12;
   if(f<90)inside.drawImage(ShiftGlint.frame(f),spot.x-size/2,spot.y-size/2,size,size,null);
  }
  inside.dispose();
  {
   // A soft broken edge reflection, not a rectangular UI border.
   for(BasicStroke stroke:EDGE){
    int width=(int)stroke.getLineWidth();g.setStroke(stroke);
    g.setPaint(new LinearGradientPaint(b.x,b.y,b.x+b.width,b.y+b.height,new float[]{0,.20f,.47f,.75f,1},new Color[]{new Color(255,219,148,0),new Color(255,219,148,(int)((width==3?135:width==8?22:8)*breath)),new Color(255,219,148,0),new Color(255,219,148,(int)((width==3?105:width==8?18:6)*breath)),new Color(255,219,148,0)}));g.draw(shape);
   }
  }
  g.dispose();
 }
}
