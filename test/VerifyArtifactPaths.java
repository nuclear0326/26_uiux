package timeescape.testutil;
import java.nio.file.*;
public final class VerifyArtifactPaths {
 public static void main(String[] args)throws Exception {
  Path repo=Path.of("").toAbsolutePath().normalize();
  String previous=System.getProperty("timeescape.artifactsRoot");
  Path expected=previous==null?repo.resolveSibling(repo.getFileName()+"_artifacts"):Path.of(previous).toAbsolutePath().normalize();
  if(!ArtifactPaths.root().equals(expected))throw new AssertionError("default path");
  if(!ArtifactPaths.output("v92-preview","path-check.txt").equals(expected.resolve("outputs/v92-preview/path-check.txt")))throw new AssertionError("varargs path");
  if(!ArtifactPaths.file("v92-preview/check.txt").toPath().equals(expected.resolve("outputs/v92-preview/check.txt")))throw new AssertionError("file path");
  try{ArtifactPaths.output("../../escape");throw new AssertionError("traversal allowed");}catch(IllegalArgumentException expectedError){}
  Path temp=Files.createTempDirectory("artifact-path-test-");
  System.setProperty("timeescape.artifactsRoot",temp.toString());
  if(!ArtifactPaths.output("preview/check.txt").equals(temp.resolve("outputs/preview/check.txt")))throw new AssertionError("override");
  if(previous==null)System.clearProperty("timeescape.artifactsRoot");else System.setProperty("timeescape.artifactsRoot",previous);
  System.out.println("PASS 5 artifact path routing checks");
 }
}
