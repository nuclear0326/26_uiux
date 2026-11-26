package timeescape.ui;
import timeescape.core.*;import java.util.*;
public class VerifyPuzzleRegistry {
 public static void main(String[] args){var registry=PuzzleDialogs.launchers(null);Set<String> expected=Set.of("present","rush_hour","gears","seed_planting","wires","temporary_power","log_analysis","confirm","action");if(!registry.keySet().equals(expected))throw new AssertionError("registry kinds");for(var step:ProgressCatalog.standard().steps().values())if(!step.automatic()&&!registry.containsKey(step.kind()))throw new AssertionError("missing "+step.kind());System.out.println("PASS production registry covers all nonautomatic step kinds; no launcher invoked");}
}
