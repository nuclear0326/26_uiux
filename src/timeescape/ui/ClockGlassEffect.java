package timeescape.ui;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
/** Fixed irregular fracture ribbons and intermittent edge reflections, no particles. */
final class ClockGlassEffect {
 private final BufferedImage main=new BufferedImage(768,876,BufferedImage.TYPE_INT_ARGB);
 private final BufferedImage fine=new BufferedImage(768,876,BufferedImage.TYPE_INT_ARGB);
 private final Shape face=new Ellipse2D.Double(MenuAtmosphere.CX-MenuAtmosphere.RX,MenuAtmosphere.CY-MenuAtmosphere.RY,MenuAtmosphere.RX*2,MenuAtmosphere.RY*2);
 ClockGlassEffect(){
  // Three separated junctions, unequal paths and interior tips: no common radial hub.
  fracture(main,new double[][]{{.30,-.02},{.35,.15},{.41,.30},{.46,.43},{.38,.64},{.30,.77},{.20,.94}},1.10,31);
  fracture(main,new double[][]{{.41,.30},{.53,.32},{.58,.40},{.65,.44},{.81,.40},{1.02,.32}},.85,45);
  fracture(main,new double[][]{{.65,.44},{.69,.58},{.64,.71},{.72,.92}},1.00,61);
  fracture(main,new double[][]{{.41,.30},{.26,.34},{.17,.28},{-.01,.24}},.80,62);
  fracture(main,new double[][]{{.38,.64},{.25,.61},{.12,.66}},.60,63);
  fracture(main,new double[][]{{.53,.32},{.57,.20},{.55,.10}},.55,64);
  fracture(fine,new double[][]{{.81,.40},{.84,.28},{.91,.22}},.38,72);
  fracture(fine,new double[][]{{.30,.77},{.40,.80},{.43,.86}},.35,73);
  fracture(fine,new double[][]{{.69,.58},{.79,.63},{.83,.72}},.42,74);
  fracture(fine,new double[][]{{.35,.15},{.39,.12},{.41,.07}},.26,75);
  fracture(fine,new double[][]{{.38,.225},{.416,.218},{.429,.190}},.32,91);
  fracture(fine,new double[][]{{.44,.378},{.408,.394},{.397,.413}},.28,92);
  fracture(fine,new double[][]{{.34,.705},{.305,.691},{.287,.704}},.35,93);
  fracture(fine,new double[][]{{.665,.645},{.700,.657},{.710,.681}},.29,94);
  fracture(fine,new double[][]{{.89,.37},{.902,.401},{.925,.413}},.25,95);
  opening(new double[][]{{.30,-.02,.05},{.35,.15,.60},{.41,.30,2.9},{.46,.43,.20},{.42,.52,1.1},{.38,.64,3.8},{.30,.77,.50},{.20,.94,.02}},2.3);
  opening(new double[][]{{.58,.40,.08},{.65,.44,1.40},{.69,.58,2.6},{.64,.71,.35},{.72,.92,.02}},1.8);
  opening(new double[][]{{.41,.30,.12},{.30,.33,1.30},{.26,.34,.20},{.17,.28,.05}},1.2);
  opening(new double[][]{{.65,.44,.08},{.74,.42,.45},{.81,.40,1.15},{.90,.36,.12},{1.02,.32,.02}},.80);
  // Localized central damage; branches leave different points of the impact area.
  fracture(main,new double[][]{{.51,.44},{.46,.43},{.34,.47},{.24,.45},{.09,.49}},.90,111);
  fracture(main,new double[][]{{.56,.44},{.58,.40},{.63,.29},{.70,.21},{.76,.06}},.80,112);
  fracture(main,new double[][]{{.57,.49},{.65,.52},{.74,.59},{.80,.61},{.91,.69}},.95,113);
  fracture(main,new double[][]{{.52,.51},{.49,.61},{.53,.72},{.48,.81},{.47,.95}},.80,114);
  fracture(main,new double[][]{{.49,.48},{.45,.56},{.38,.64}},1.05,115);
  fracture(main,new double[][]{{.51,.44},{.54,.415},{.575,.45},{.57,.49},{.535,.525},{.49,.48},{.51,.44}},1.0,116);
  fracture(fine,new double[][]{{.54,.415},{.535,.465},{.49,.48}},.70,117);
  fracture(fine,new double[][]{{.535,.465},{.57,.49}},.65,118);
  fracture(fine,new double[][]{{.535,.465},{.535,.525}},.75,119);
  fracture(fine,new double[][]{{.51,.44},{.47,.395},{.44,.40}},.42,120);
  fracture(fine,new double[][]{{.575,.45},{.615,.43},{.65,.44}},.60,121);
  opening(new double[][]{{.535,.465,.4},{.515,.453,2.7},{.49,.45,1.9},{.46,.43,.25},{.34,.47,.02}},1.1);
  opening(new double[][]{{.535,.465,.3},{.553,.475,2.4},{.57,.49,1.6},{.65,.52,.45},{.74,.59,.02}},.9);
  opening(new double[][]{{.535,.465,.3},{.531,.50,2.8},{.52,.54,1.1},{.49,.61,.12}},1.0);
  opening(new double[][]{{.54,.415,.05},{.558,.435,1.4},{.575,.45,.25}},.8);

  // Extra short interconnected fractures confined to the existing central impact region.
  fracture(fine,new double[][]{{.51,.44},{.497,.424},{.505,.407},{.528,.416},{.54,.415}},.55,141);
  fracture(fine,new double[][]{{.54,.415},{.558,.403},{.578,.421},{.575,.45}},.50,142);
  fracture(fine,new double[][]{{.575,.45},{.592,.459},{.585,.481},{.57,.49}},.62,143);
  fracture(fine,new double[][]{{.57,.49},{.572,.515},{.551,.535},{.535,.525}},.52,144);
  fracture(fine,new double[][]{{.535,.525},{.511,.531},{.496,.508},{.49,.48}},.55,145);
  fracture(fine,new double[][]{{.49,.48},{.475,.465},{.487,.446},{.51,.44}},.48,146);
  fracture(fine,new double[][]{{.51,.44},{.524,.431},{.539,.441},{.535,.465}},.60,147);
  fracture(fine,new double[][]{{.539,.441},{.557,.447},{.575,.45}},.52,148);
  fracture(fine,new double[][]{{.535,.465},{.519,.477},{.522,.495},{.535,.525}},.58,149);
  fracture(fine,new double[][]{{.522,.495},{.543,.501},{.57,.49}},.51,150);
  fracture(fine,new double[][]{{.487,.446},{.480,.429}},.35,151);
  fracture(fine,new double[][]{{.585,.481},{.603,.493}},.33,152);

 }
 // Shared spatial deformation keeps every branch and wide gap registered.
 private double[][] irregular(double[][] source){
  ArrayList<double[]> samples=new ArrayList<>();
  for(int i=0;i<source.length-1;i++){
   double[] a=source[i],b=source[i+1];
   int steps=Math.max(1,(int)Math.ceil(Math.hypot((b[0]-a[0])*256,(b[1]-a[1])*292)/5));
   for(int j=0;j<steps;j++){
    double t=(double)j/steps;double[] point=new double[a.length];
    for(int k=0;k<a.length;k++)point[k]=a[k]+(b[k]-a[k])*t;
    samples.add(warp(point));
   }
  }
  samples.add(warp(source[source.length-1].clone()));
  return samples.toArray(new double[0][]);
 }
 private double[] warp(double[] p){
  double x=p[0]*256,y=p[1]*292;
  double dx=1.65*Math.sin(y*.115+x*.039)+.65*Math.sin(y*.39-x*.083)+.75*Math.sin(x*.071+y*.029);
  double dy=1.25*Math.sin(x*.132-y*.044)+.55*Math.sin(x*.33+y*.19);
  p[0]=(x+dx)/256;p[1]=(y+dy)/292;return p;
 }
 private void opening(double[][] nodes,double bevelWidth){
  nodes=irregular(nodes);
  Graphics2D g=main.createGraphics();g.scale(3,3);
  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  int n=nodes.length;double[][] left=new double[n][2],right=new double[n][2],outer=new double[n][2];
  for(int i=0;i<n;i++){
   int prev=Math.max(0,i-1),next=Math.min(n-1,i+1);
   double dx=(nodes[next][0]-nodes[prev][0])*256,dy=(nodes[next][1]-nodes[prev][1])*292,len=Math.hypot(dx,dy),nx=-dy/len,ny=dx/len;
   double x=nodes[i][0]*256,y=nodes[i][1]*292,w=nodes[i][2];
   left[i]=new double[]{x+nx*w,y+ny*w};right[i]=new double[]{x-nx*w*.65,y-ny*w*.65};
   double edge=(i==0||i==n-1)?.08:bevelWidth*(i%2==0?1.0:.35);
   outer[i]=new double[]{x+nx*(w+edge),y+ny*(w+edge)};
  }
  Path2D gap=new Path2D.Double();gap.moveTo(left[0][0],left[0][1]);
  for(int i=1;i<n;i++)gap.lineTo(left[i][0],left[i][1]);
  for(int i=n-1;i>=0;i--)gap.lineTo(right[i][0],right[i][1]);gap.closePath();
  g.setColor(new Color(4,13,18,195));g.fill(gap);
  for(int i=1;i<n;i++){
   boolean glint=Math.sin(left[i][0]*.071+left[i][1]*.043)+.5*Math.sin(left[i][1]*.139-left[i][0]*.053)>1.03;
   Path2D facet=new Path2D.Double();facet.moveTo(left[i-1][0],left[i-1][1]);facet.lineTo(left[i][0],left[i][1]);
   facet.lineTo(outer[i][0],outer[i][1]);facet.lineTo(outer[i-1][0],outer[i-1][1]);facet.closePath();
   g.setPaint(new GradientPaint((float)left[i][0],(float)left[i][1],new Color(168,187,193,glint?100:0),(float)outer[i][0],(float)outer[i][1],new Color(94,119,128,glint?16:0)));
   g.fill(facet);
   g.setColor(new Color(188,205,210,glint?110:0));
   g.setStroke(new BasicStroke(i%2==0?.55f:.35f));g.draw(new Line2D.Double(left[i-1][0],left[i-1][1],left[i][0],left[i][1]));
   g.setColor(new Color(90,113,122,glint?40:0));g.setStroke(new BasicStroke(.5f));
   g.draw(new Line2D.Double(right[i-1][0],right[i-1][1],right[i][0],right[i][1]));
  }
  g.dispose();
 }
 private void bevel(double[][] nodes,double width,int alpha){
  Graphics2D g=main.createGraphics();g.scale(3,3);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  for(int i=1;i<nodes.length;i++){
   double ax=nodes[i-1][0]*256,ay=nodes[i-1][1]*292,bx=nodes[i][0]*256,by=nodes[i][1]*292;
   double len=Math.hypot(bx-ax,by-ay),nx=-(by-ay)/len,ny=(bx-ax)/len;
   Path2D p=new Path2D.Double();p.moveTo(ax,ay);p.lineTo(bx,by);p.lineTo(ax+(bx-ax)*.70+nx*width,ay+(by-ay)*.70+ny*width);p.closePath();
   g.setColor(new Color(210,225,230,alpha));g.fill(p);
  }
  g.dispose();
 }
 private void fracture(BufferedImage target,double[][] nodes,double weight,long seed){
  nodes=irregular(nodes);
  Random rng=new Random(seed);ArrayList<Point2D.Double> points=new ArrayList<>();
  points.add(new Point2D.Double(nodes[0][0]*256,nodes[0][1]*292));
  for(int i=1;i<nodes.length;i++){
   double ax=nodes[i-1][0]*256,ay=nodes[i-1][1]*292,bx=nodes[i][0]*256,by=nodes[i][1]*292;
   double len=Math.hypot(bx-ax,by-ay);int count=Math.max(2,(int)(len/9));
   for(int j=1;j<=count;j++){
    double t=(double)j/count,bend=j==count?0:(rng.nextDouble()-.5)*.65;
    points.add(new Point2D.Double(ax+(bx-ax)*t-(by-ay)/len*bend,ay+(by-ay)*t+(bx-ax)/len*bend));
   }
  }
  Graphics2D g=target.createGraphics();g.scale(3,3);
  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
  double width=weight*.35;
  for(int i=1;i<points.size();i++){
   var a=points.get(i-1);var b=points.get(i);double len=a.distance(b),nx=-(b.y-a.y)/len,ny=(b.x-a.x)/len;
   double next=weight*(.40+Math.pow(rng.nextDouble(),2)*1.45);
   // Jagged, tapered fissure faces rather than a uniform-width drawn stroke.
   Path2D slit=new Path2D.Double();slit.moveTo(a.x+nx*width*.5,a.y+ny*width*.5);
   slit.lineTo(b.x+nx*next*.5,b.y+ny*next*.5);slit.lineTo(b.x-nx*next*.5,b.y-ny*next*.5);
   slit.lineTo(a.x-nx*width*.5,a.y-ny*width*.5);slit.closePath();
   g.setColor(new Color(7,17,21,105+rng.nextInt(65)));g.fill(slit);
   // Reflections vary with facet direction and are deliberately interrupted.
   if(rng.nextDouble()<.87){
    double end=.40+rng.nextDouble()*.60,offset=.25+next*.45;
    g.setStroke(new BasicStroke((float)(.20+rng.nextDouble()*.55)));
    int reflectionAlpha=35+rng.nextInt(55);
    boolean glint=Math.sin(a.x*.071+a.y*.043)+.5*Math.sin(a.y*.139-a.x*.053)>1.03;
    g.setColor(new Color(163,185,192,glint?reflectionAlpha:0));
    g.draw(new Line2D.Double(a.x+nx*offset,a.y+ny*offset,a.x+(b.x-a.x)*end+nx*offset,a.y+(b.y-a.y)*end+ny*offset));
   }
   if(rng.nextDouble()<.13){
    Path2D facet=new Path2D.Double();facet.moveTo(a.x,a.y);facet.lineTo(b.x,b.y);
    facet.lineTo(b.x+nx*(1.2+next),b.y+ny*(1.2+next));facet.closePath();
    g.setColor(new Color(132,157,168,8));g.fill(facet);
   }
   width=next;
  }
  g.dispose();
 }
 void draw(Graphics2D canvas,double age){
  if(age<0)return;Graphics2D g=(Graphics2D)canvas.create();g.clip(face);
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
  int x=(int)(MenuAtmosphere.CX-MenuAtmosphere.RX),y=(int)(MenuAtmosphere.CY-MenuAtmosphere.RY);
  g.drawImage(main,x,y,256,292,null);
  if(age>=.06)g.drawImage(fine,x,y,256,292,null);
  g.dispose();
 }
}
