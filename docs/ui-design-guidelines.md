# UI design guidelines

- Do not use middle dots as text separators. Prefer natural sentences, line breaks or slashes.
- Titles: Noto Serif KR SemiBold. Body and small labels: Pretendard Regular. Buttons: Pretendard SemiBold.
- Puzzle headers use a centered title between fine muted brass rules, without a raised brass nameplate.
- PuzzleChrome owns the common frame, header, close button and instruction styling.
- Current puzzle hosts remain modal JDialogs, now undecorated. This preserves existing runner completion flow. They are not yet in-scene overlays.
- Do not launch the game for previews. Render panels headlessly. OS window, focus and DPI behavior needs a user-run visual check.

## Current interface conventions (2026-11-26)

- Main HUD: TIME ESCAPE, separate time/place labels, research notebook, ?, hamburger menu.
- The notebook switches between research records and inventory. Close uses a large plain X; the whole page turns, and the layout scales with the viewport.
- Help, settings and menus use the shared dark translucent GameMenuOverlay shell.
- Settings use independent BGM, ambience and SFX sliders; AudioSettings persists the levels outside version-specific progress saves.
- Esc opens the gameplay menu when no overlay is active. It closes the notebook or navigates back in the menu; dialogue and travel sequences block this action.
- Minigame windows remain modal. Do not add main-window resizing behavior during a puzzle without an explicit design change.
- Preview output must use test/ArtifactPaths.java and the external artifacts directory, not a repository-local outputs folder.
- See [current guide index](README.md) and [build paths](../tools/ARTIFACTS.md).
