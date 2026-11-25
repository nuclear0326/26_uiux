# Build and release artifacts

The default artifact directory is a sibling of the repository:

```
26_uiux/                 source, tools, tests, documentation
26_uiux_artifacts/
  build/                 latest successful build only
  outputs/               Windows releases and previews, retained
```

Build: `powershell -File tools/build.ps1 -Version 93 -JavaHome <JDK21>`
Package: `python tools/package_windows.py --version 93 --base 92 --zip`

Override the location with `-ArtifactsRoot` (PowerShell) or
`--artifacts-root` (Python); use the same directory for both commands.
Cleanup never targets outputs. Failed builds preserve the last successful
build. Unknown files in build are preserved. build/ and outputs/ remain
ignored in Git as a fallback for manually generated artifacts.

Java preview tests use `timeescape.testutil.ArtifactPaths` (test/ArtifactPaths.java).
Compile that helper together with the selected tests. Run from the repository
or artifact directory; previews always go to the external outputs directory.
Override with `-Dtimeescape.artifactsRoot=<directory>` when needed.
The helper is test-only and is not packaged into the game.

Moving these directories changes old local download links. EXEs still
require their adjacent app and runtime directories; move complete packages.

Manual audio authoring: [authoring/README.md](authoring/README.md). Drafts always use fresh external directories and never replace game audio. Effect source files: [design](../design/README.md). The normal build does not run audio authoring or Effekseer.
