package timeescape.ui;
import java.awt.geom.*;
/** Shared build/runtime window geometry in photograph coordinates. */
final class LabWindowMask {
    final Area glass = new Area();
    LabWindowMask() {
        bay(781,967,181,164,251,241,333,324,413,410,496,496,835,899);
        bay(996,1213,161,144,237,223,322,313,410,405,498,498,1061,1136);
        bay(1248,1464,140,120,220,205,310,302,404,398,498,493,1298,1358);
        // Foreground monitors, binders, plant, boxes and the hanging fluorescent lamp.
        glass.subtract(new Area(new Rectangle2D.Double(760,417,220,100)));
        glass.subtract(new Area(new Rectangle2D.Double(994,460,113,50)));
        glass.subtract(new Area(new Rectangle2D.Double(1382,365,95,145)));
        glass.subtract(new Area(new Rectangle2D.Double(1066,90,129,64)));
    }
    private void bay(double left,double right,double tl,double tr,double a,double b,
                     double c,double d,double e,double f,double bl,double br,double v1,double v2) {
        double[][] rows={{tl,tr,a-4,b-4},{a+5,b+5,c-4,d-4},{c+4,d+4,e-4,f-4},{e+5,f+5,bl,br}};
        for(double[] row:rows){
            Path2D p=new Path2D.Double();p.moveTo(left,row[0]);p.lineTo(right,row[1]);
            p.lineTo(right,row[3]);p.lineTo(left,row[2]);p.closePath();glass.add(new Area(p));
        }
        glass.subtract(new Area(new Rectangle2D.Double(v1-3,100,6,410)));
        glass.subtract(new Area(new Rectangle2D.Double(v2-3,100,6,410)));
    }
    boolean contains(double x,double y) { return glass.contains(x,y); }
}
