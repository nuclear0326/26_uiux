package timeescape.scene;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import timeescape.model.Era;
/** Background silhouettes in original image coordinates. */
public final class HotspotCatalog {
    private HotspotCatalog() { }
    private static final List<Hotspot> PAST = List.of(
        new Hotspot("PAST_PLANTER", "연구 화분 · 씨앗 메모", new java.awt.Rectangle(210,700,560,340)),
        new Hotspot("PAST_SPARE", "황동 부품 장치",
            polygon(1088,714, 1127,696, 1238,713, 1236,844, 1211,871, 1086,849)));
    private static final List<Hotspot> PRESENT = List.of(new Hotspot("PRESENT_PC", "진단 컴퓨터",
        polygon(873,489, 999,487, 1007,500, 1006,581, 995,591, 878,589, 872,580)),
        new Hotspot("POWER", "벽면 철제 배전함", polygon(773,325, 830,320, 844,326, 844,410, 773,413)),
        new Hotspot("MEMO", "연구 메모", new java.awt.Rectangle(294,340,62,113)),
        new Hotspot("WORKBENCH", "설계도 · 조립 작업대", new java.awt.Rectangle(960,751,225,62)),
        new Hotspot("USB", "서랍 · USB", new java.awt.Rectangle(1117,635,118,100)),
        new Hotspot("CASE", "보관 상자", new java.awt.Rectangle(5,508,74,91)),
        new Hotspot("CELL", "선반 · 전원 셀", new java.awt.Rectangle(480,218,66,66)),
        new Hotspot("MODULE", "서랍 · 제어 모듈", new java.awt.Rectangle(1565,610,143,64)));
    private static final List<Hotspot> FUTURE = List.of(new Hotspot("FUTURE_CORE", "시간 실험 장치", futureShape()));
    private static Shape polygon(int... xy) {
        Polygon p = new Polygon();
        for (int i=0; i<xy.length; i+=2) p.addPoint(xy[i], xy[i+1]);
        return p;
    }
    private static Shape futureShape() {
        Area ring = new Area(new Ellipse2D.Double(895,280,375,380));
        ring.subtract(new Area(new Ellipse2D.Double(942,325,281,290)));
        ring.add(new Area(new Ellipse2D.Double(1007,391,151,158)));
        return ring;
    }
    public static List<Hotspot> of(Era era) {
        return switch(era) { case PAST -> PAST; case PRESENT -> PRESENT; case FUTURE -> FUTURE; };
    }
}
