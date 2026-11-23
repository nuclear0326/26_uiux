# UI design guidelines

- Do not use middle dots as text separators. Prefer natural sentences, line breaks or slashes.
- Titles: Noto Serif KR SemiBold. Body and small labels: Pretendard Regular. Buttons: Pretendard SemiBold.
- Puzzle headers use a centered title between fine muted brass rules, without a raised brass nameplate.
- PuzzleChrome owns the common frame, header, close button and instruction styling.
- Current puzzle hosts remain modal JDialogs, now undecorated. This preserves existing runner completion flow. They are not yet in-scene overlays.
- Do not launch the game for previews. Render panels headlessly. OS window, focus and DPI behavior needs a user-run visual check.
