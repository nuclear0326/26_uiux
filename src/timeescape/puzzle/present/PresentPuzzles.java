package timeescape.puzzle.present;
import java.util.Map;import java.util.function.Supplier;
/** Stable progress IDs select fresh, independent puzzle sessions. */
public final class PresentPuzzles {
 private PresentPuzzles(){}
 private static final Map<String,Supplier<PresentPuzzle>> FACTORIES=Map.of(
  "PZ004", PowerDistributionPuzzle::new,
  "PZ006", ArchiveCodePuzzle::new,
  "PZ008", SchematicPuzzle::new,
  "PZ010", ModuleAssemblyPuzzle::new,
  "PZ011", TimeCoordinatesPuzzle::new);
 public static PresentPuzzle create(String id){var factory=FACTORIES.get(id);if(factory==null)throw new IllegalArgumentException(id);return factory.get();}
}
