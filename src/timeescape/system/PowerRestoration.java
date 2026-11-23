package timeescape.system;
/** One-shot fluorescent startup curve, independent of render frame rate. */
public final class PowerRestoration {
    public static final double DURATION = 4.2;
    private static final double[] TIMES = {0,.18,.30,.46,.68,.84,1.02,1.28,1.55,4.2};
    private static final double[] LEVELS = {0,0,.24,.025,.04,.42,.08,.16,.22,1};
    private PowerRestoration() {}
    public static float level(double seconds) {
        if(seconds<=0)return 0;
        for(int i=1;i<TIMES.length;i++) if(seconds<TIMES[i]) {
            double t=(seconds-TIMES[i-1])/(TIMES[i]-TIMES[i-1]);
            t=t*t*(3-2*t);
            return (float)(LEVELS[i-1]+(LEVELS[i]-LEVELS[i-1])*t);
        }
        return 1;
    }
}
