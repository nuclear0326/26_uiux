package timeescape.ui;
/** Optional frame generation, sharing the current weather regression implementation. */
public final class PreviewLabWeather {
 public static void main(String[] args)throws Exception {
  VerifyLabWeather72.main(new String[]{args.length>0?args[0]:timeescape.testutil.ArtifactPaths.output("lab-weather-preview").toString()});
 }
}
