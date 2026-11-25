package timeescape.ui;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Build-only generator for the two active laboratory lightning masks. */
public final class GenerateLabWeather {
    static final int W=1672,H=941;
    static BufferedImage photo(Path path)throws IOException {
        BufferedImage src=ImageIO.read(path.toFile()),im=new BufferedImage(W,H,BufferedImage.TYPE_INT_RGB);
        var g=im.createGraphics();g.drawImage(src,0,0,W,H,null);g.dispose();return im;
    }
    public static void main(String[] args)throws Exception {
        Path assets=Path.of(args[0]),out=assets.resolve("effects");Files.createDirectories(out);
        BufferedImage on=photo(assets.resolve("images/present.png")),off=photo(assets.resolve("images/present_off.png"));
        LabWindowMask mask=new LabWindowMask();
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
        System.out.println("Generated powered/unpowered laboratory lightning masks.");
    }
}
