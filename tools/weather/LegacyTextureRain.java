package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;
/** Advection of the original glass highlights, not new particle strokes. */
final class LegacyTextureRain {
 final int W=1600,H=400;final int[] source=new int[W*H];final float[] detail=new float[W*H];
 final float[] weight=new float[W*H];final BufferedImage layer=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
 final java.util.List<double[]> events=new java.util.ArrayList<>();
 private double cycleSeconds;
 final float[] delta=new float[W*H];
 LegacyTextureRain(BufferedImage image,Shape glass){
  var scaled=new BufferedImage(1600,900,BufferedImage.TYPE_INT_RGB);var g=scaled.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g.drawImage(image,0,0,1600,900,null);g.dispose();
  scaled.getRGB(0,0,W,H,source,0,W);
  for(int y=0;y<H;y++)for(int x=770;x<W-10;x++){
   int idx=y*W+x;if(!glass.contains(x,y))continue;
   double edge=1;
   for(int d=1;d<=14;d++)if(!glass.contains(x,y-d)||!glass.contains(x,y+d)||!glass.contains(x-3,y)||!glass.contains(x+3,y)){edge=Math.min(edge,(d-1)/14.);break;}
   weight[idx]=(float)edge;
   int c=source[idx];double lum=((c>>16&255)+(c>>8&255)+(c&255))/3.;double avg=0;
   for(int dx=-9;dx<=9;dx++){int q=source[y*W+x+dx];avg+=((q>>16&255)+(q>>8&255)+(q&255))/3.;}
   // Fine, cool highlights belong to water; broad city lights remain in the base.
   detail[idx]=(float)(Math.max(0,lum-avg/19-2)*Math.min(1,Math.max(0,((c&255)-(c>>16&255)+8)/22.)));
  }
  java.util.List<int[]> seeds=new java.util.ArrayList<>();
  for(int y=25;y<220;y+=17)for(int x=790;x<1580;x+=11){
   int best=-1;float strength=4;
   for(int yy=y;yy<y+12;yy++)for(int xx=x;xx<x+8;xx++){
    int idx=yy*W+xx;
    if(weight[idx]>.95 && detail[idx]>strength){best=idx;strength=detail[idx];}
   }
   if(best<0)continue;int bx=best%W,by=best/W;int distance=0;
   for(int d=0;d<155&&by+d<H;d++){if(weight[(by+d)*W+bx]<.8)break;distance=d;}
   if(distance>=70)seeds.add(new int[]{bx,by,distance});
  }
  if(seeds.isEmpty())throw new IllegalStateException("No water paths");
  var random=new java.util.Random(42173);var extra=new java.util.Random(91287);double start=-1.2;
  for(int k=0;k<160;k++){
   int[] seed=seeds.get(random.nextInt(seeds.size()));double speed=94.875+random.nextDouble()*51.75;
   double distance=Math.min(seed[2]-8,95+random.nextDouble()*45);
   events.add(new double[]{start,seed[0],seed[1],speed,distance/speed});
   // Keep every original start time and speed. Add companions on four of every five starts.
   if(k%5!=4){
    int[] companion;
    do{companion=seeds.get(extra.nextInt(seeds.size()));}while(Math.abs(companion[0]-seed[0])<18);
    double extraSpeed=94.875+extra.nextDouble()*51.75;
    double extraDistance=Math.min(companion[2]-8,95+extra.nextDouble()*45);
    events.add(new double[]{start,companion[0],companion[1],extraSpeed,extraDistance/extraSpeed});
   }
   start+=.33+random.nextDouble()*.53;
  }
  cycleSeconds=start+1.2;

 }
 void draw(Graphics2D g,double t){
  java.util.Arrays.fill(delta,0);int active=0;
  for(double[] event:events){
   double age=((t-event[0])%cycleSeconds+cycleSeconds)%cycleSeconds,duration=event[4];if(age<0||age>duration)continue;active++;
   double envelope=Math.min(1,age/.12)*Math.min(1,(duration-age)/.24);
   int x0=(int)event[1],y0=(int)event[2];double travel=age*event[3];
   for(int dy=-6;dy<=6;dy++)for(int dx=-4;dx<=4;dx++){
    int x=x0+dx,y=y0+dy;float patch=detail[y*W+x];
    double taper=Math.pow(Math.max(0,1-dx*dx/25.),.7)*Math.max(0,1-dy*dy/49.);
    double value=patch*taper*envelope;
    delta[y*W+x]-=value*.9;
    deposit(x,y+travel,value);
    for(int tail=1;tail<=14;tail++)deposit(x,y+travel-tail,value*.04*(15-tail)/14.);
   }
  }

  for(int y=0;y<H;y++)for(int x=770;x<W-10;x++){
   int i=y*W+x;float w=weight[i];if(w<=0)continue;
   double change=delta[i]*w;int c=source[i];
   int rr=clamp((c>>16&255)+change*.80),gg=clamp((c>>8&255)+change*.97),bb=clamp((c&255)+change*1.08);
   layer.setRGB(x,y,((int)(255*w)<<24)|(rr<<16)|(gg<<8)|bb);
  }
  g.drawImage(layer,0,0,null);
 }
 void deposit(int x,double y,double value){
  int iy=(int)Math.floor(y);if(iy<0||iy+1>=H)return;double f=y-iy;
  delta[iy*W+x]+=value*(1-f);delta[(iy+1)*W+x]+=value*f;
 }
 double sample(int x,double y){
  int ya=(int)Math.floor(y);if(ya<0||ya+1>=H)return 0;
  double f=y-ya;return detail[ya*W+x]*(1-f)+detail[(ya+1)*W+x]*f;
 }
 static int clamp(double v){return (int)Math.max(0,Math.min(255,v));}
}
