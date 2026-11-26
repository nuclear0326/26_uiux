import timeescape.model.Era;
import timeescape.scene.HotspotCatalog;

public final class VerifySeedPlanterHotspot {
    public static void main(String[] args) {
        var planter=HotspotCatalog.of(Era.PAST).stream()
                .filter(h -> h.id().equals("PAST_PLANTER"))
                .findFirst().orElseThrow(() -> new AssertionError("Past planter hotspot missing"));
        if (!planter.contains(400,850)) throw new AssertionError("Planter click area misses its illustration");
        if (planter.contains(1145,780)) throw new AssertionError("Planter overlaps the brass device");
        System.out.println("PASS past planter hotspot bounds");
    }
}
