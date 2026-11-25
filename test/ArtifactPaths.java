package timeescape.testutil;
import java.nio.file.*;
/** Test-only output routing; never included in the game JAR. */
public final class ArtifactPaths {
 private ArtifactPaths() {}
 public static Path root() {
  String override=System.getProperty("timeescape.artifactsRoot");
  if(override!=null&&!override.isBlank())return Path.of(override).toAbsolutePath().normalize();
  Path cwd=Path.of("").toAbsolutePath().normalize();
  for(Path p=cwd;p!=null;p=p.getParent()) {
   if(Files.isDirectory(p.resolve("src"))&&Files.isDirectory(p.resolve("tools")))return p.resolveSibling(p.getFileName()+"_artifacts");
   if(p.getFileName()!=null&&p.getFileName().toString().endsWith("_artifacts"))return p;
  }
  throw new IllegalStateException("Run from the repository or set -Dtimeescape.artifactsRoot=<directory>");
 }
 public static Path output(String first,String... more) {
  Path folder=root().resolve("outputs");
  Path result=folder.resolve(Path.of(first,more)).normalize();
  if(!result.startsWith(folder))throw new IllegalArgumentException("Output must remain under artifacts/outputs");
  try{Files.createDirectories(result.getParent());}catch(java.io.IOException e){throw new java.io.UncheckedIOException(e);}
  return result;
 }
 public static java.io.File file(String first){return output(first).toFile();}
}
