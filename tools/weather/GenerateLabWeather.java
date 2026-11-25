package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import javax.imageio.ImageIO;
/** Build-only extraction. Runtime receives small texture sprites and valid single-pane paths. */
public final class GenerateLabWeather {
    static final int W=1672,H=941;
    static BufferedImage photo(Path path)throws IOException {
        BufferedImage src=ImageIO.read(path.toFile()),im=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        var g=im.createGraphics();g.drawImage(src,0,0,W,H,null);g.dispose();return im;
    }
    static double lum(int c){return ((c>>16&255)+(c>>8&255)+(c&255))/3.;}
    static boolean safe(LabWindowMask mask,int x,int y) {
        return mask.contains(x-5,y-7)&&mask.contains(x+5,y-7)&&mask.contains(x-5,y+7)&&mask.contains(x+5,y+7);
    }
    public static void main(String[] args)throws Exception {
        Path assets=Path.of(args[0]),out=assets.resolve("effects");Files.createDirectories(out);
        BufferedImage on=photo(assets.resolve("images/present.png")),off=photo(assets.resolve("images/present_off.png"));
        LabWindowMask mask=new LabWindowMask();float[] detail=new float[W*H];
        for(int y=120;y<500;y++)for(int x=775;x<1470;x++) {
            if(!safe(mask,x,y))continue;
            int c=on.getRGB(x,y);double average=0;
            for(int dx=-8;dx<=8;dx++)average+=lum(on.getRGB(x+dx,y));
            // Fine cool highlights only: reject broad lights, warm reflections and frame edges.
            double cool=Math.min(1,Math.max(0,((c&255)-(c>>16&255)+4)/20.));
            detail[y*W+x]=(float)(Math.max(0,lum(c)-average/17-3)*cool);
        }
        var seeds=new ArrayList<int[]>();
        for(int gy=125;gy<470;gy+=14)for(int gx=785;gx<1450;gx+=14) {
            int bx=-1,by=-1;float best=7;
            for(int y=gy;y<gy+10;y++)for(int x=gx;x<gx+10;x++)if(detail[y*W+x]>best){best=detail[y*W+x];bx=x;by=y;}
            if(bx<0)continue;int distance=0;
            for(int d=1;d<=95;d++){if(!safe(mask,bx,by+d))break;distance=d;}
            if(distance>=24)seeds.add(new int[]{bx,by,distance});
        }
        if(seeds.size()<20)throw new IOException("Insufficient water paths: "+seeds.size());
        Random rand=new Random(71031);
        try(var data=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(out.resolve("lab_rain.bin.gz")))))) {
            data.writeInt(0x54454C31);data.writeInt(seeds.size());
            for(int[] seed:seeds){
                int x=seed[0],y=seed[1],distance=Math.min(seed[2],35+rand.nextInt(61));
                double duration=.65+distance/(100.+rand.nextDouble()*50);
                data.writeInt(x);data.writeInt(y);data.writeInt(distance);
                data.writeDouble(rand.nextDouble()*8);data.writeDouble(duration);data.writeDouble(3.5+rand.nextDouble()*5);
                for(int type=0;type<2;type++)for(int dy=-6;dy<=6;dy++)for(int dx=-4;dx<=4;dx++){
                    double taper=Math.max(0,1-dx*dx/25.)*Math.max(0,1-dy*dy/49.);
                    double value=detail[(y+dy)*W+x+dx]*taper;
                    double base=lum(on.getRGB(x+dx,y+dy));
                    int alpha=(int)Math.min(150,255*value/Math.max(40,type==0?base:255-base));
                    data.writeInt((alpha<<24)|(type==0?0x07121b:0xc9e4ef));
                }
            }
        }
        for(int variant=0;variant<2;variant++){
            BufferedImage base=variant==0?off:on,glow=new BufferedImage(720,400,BufferedImage.TYPE_INT_ARGB);
            for(int y=110;y<510;y++)for(int x=760;x<1480;x++){
                if(!mask.contains(x,y))continue;
                double edge=1;for(int d=1;d<=9;d++)if(!mask.contains(x-d,y)||!mask.contains(x+d,y)||!mask.contains(x,y-d)||!mask.contains(x,y+d)){edge=(d-1)/9.;break;}
                double falloff=Math.exp(-Math.pow((x-1260)/650.,2)-Math.pow((y-180)/440.,2));
                int c=base.getRGB(x,y),red=Math.min(255,(int)((c>>16&255)*1.28+12)),green=Math.min(255,(int)((c>>8&255)*1.35+17)),blue=Math.min(255,(int)((c&255)*1.40+21));
                glow.setRGB(x-760,y-110,((int)(220*edge*falloff)<<24)|(red<<16)|(green<<8)|blue);
            }
            ImageIO.write(glow,"png",out.resolve(variant==0?"lab_storm_off.png":"lab_storm_on.png").toFile());
        }
        System.out.println("Lab weather: "+seeds.size()+" original-texture paths, each contained in one pane; build-time extraction complete.");
    }
}
