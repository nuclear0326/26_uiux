package timeescape.ui;
import java.awt.*;import java.awt.image.BufferedImage;
/** Advection of the original glass highlights, not new particle strokes. */
final class TextureRain {
 final int W=1600,H=400;final int[] source=new int[W*H];final float[] detail=new float[W*H];
 final float[] weight=new float[W*H];final BufferedImage layer=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
 final java.util.List<double[]> events=new java.util.ArrayList<>();
 private double cycleSeconds;
 final float[] delta=new float[W*H];
 TextureRain(){
  try(var raw=TextureRain.class.getResourceAsStream("/assets/effects/menu_rain.bin.gz")){
   if(raw==null)throw new java.io.IOException("Missing precomputed rain data");
   try(var in=new java.io.DataInputStream(new java.io.BufferedInputStream(new java.util.zip.GZIPInputStream(raw)))){
    if(in.readInt()!=0x54455231 || in.readInt()!=1 || in.readInt()!=W || in.readInt()!=H)
     throw new java.io.IOException("Unsupported rain data");
    for(int i=0;i<source.length;i++)source[i]=in.readInt();
    for(int i=0;i<detail.length;i++)detail[i]=in.readFloat();
    for(int i=0;i<weight.length;i++)weight[i]=in.readFloat();
    cycleSeconds=in.readDouble();int count=in.readInt();
    if(!Double.isFinite(cycleSeconds)||cycleSeconds<=0||count<1||count>10000)throw new java.io.IOException("Invalid rain events");
    for(int i=0;i<count;i++){double[] event=new double[5];for(int k=0;k<5;k++)event[k]=in.readDouble();events.add(event);}
    if(in.read()!=-1)throw new java.io.IOException("Unexpected rain data");
   }
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot load precomputed menu rain",e);}
 }
 void updateDelta(double t){
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

 }
 /** Distant glass: subdued small heads and longer narrow wet trails.
  * Some drops gather before sliding; density is five staggered streams. */
 void updateDistantDelta(double t){
  java.util.Arrays.fill(delta,0);
  double size=.55;
  for(int stream=0;stream<5;stream++)for(double[] event:events){
   int x0=(int)event[1],y0=(int)event[2];
   double hold=(Math.floorMod(x0+y0+stream,3)==0)?.28:0;
   double duration=event[4]+hold;
   double age=((t+stream*17.3-event[0])%cycleSeconds+cycleSeconds)%cycleSeconds;
   if(age>duration)continue;
   double envelope=Math.min(1,age/.20)*Math.min(1,(duration-age)/.28);
   double moving=Math.max(0,age-hold);
   double startY=10+Math.floorMod(y0+stream*73,285);
   double travel=event[3]*1.5*(moving-.12*(1-Math.exp(-moving/.12)));
   double trailLength=Math.min(travel,38+Math.floorMod(x0+y0,19));
   for(int dy=-6;dy<=6;dy++)for(int dx=-4;dx<=4;dx++){
    double taper=Math.pow(Math.max(0,1-dx*dx/25.),.7)*Math.max(0,1-dy*dy/49.);
    double value=detail[(y0+dy)*W+x0+dx]*taper*envelope*size*size;
    double y=startY+dy*size*1.35;
    double pointWidth=.65*Math.max(.20,1-Math.abs(dy)/8.0);
    depositXY(x0+dx*size*pointWidth,y+travel,value*.70);
    // Tail length is independent of head scale; it narrows and fades toward its origin.
    for(int tail=1;tail<=trailLength;tail++){
     double fade=1-tail/trailLength;
     depositXY(x0+dx*size*.32*(.20+.80*fade),y+travel-tail,value*.10*fade);
    }
   }
  }
 }
 private void depositXY(double x,double y,double value){
  int ix=(int)Math.floor(x);if(ix<0||ix+1>=W)return;double fraction=x-ix;
  deposit(ix,y,value*(1-fraction));deposit(ix+1,y,value*fraction);
 }
 void draw(Graphics2D g,double t){
  updateDelta(t);
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
