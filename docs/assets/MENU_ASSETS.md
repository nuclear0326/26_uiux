# 메뉴 자산과 제작 출처

확인 기준: 2026-11-26 / 현재 코드. 소스 경로는 저장소 루트 기준입니다.

## 현재 적용 방식

- 배경: `src/assets/images/menu/menu_background.png`, `menu_unlit.png`.
- 글자와 시계 바늘은 Java2D 레이어로 그립니다.
- 비·번개 데이터와 균열 이미지는 tools/weather 생성기가 빌드 시 준비합니다.
- ClockGlassEffect는 `src/assets/effects/clock_crack_main.png`, `clock_crack_fine.png`를 읽습니다. 실행 시 원본 균열 생성 계산을 반복하지 않습니다.
- 현재 음원 출처·편집 기록은 [메뉴 시작 음원](../../src/assets/audio/MENU_START_CREDITS.txt), [사고 음원](../../src/assets/audio/FIRST_TRAVEL_CREDITS.txt), [추가 메뉴 음원 이용 조건](../../src/assets/audio/sfx/MENU_AUDIO_LICENSE.txt)을 확인합니다.
- Shift 효과 제작 원본은 [design 안내](../../design/README.md)에 분리되어 있습니다.

## 초기 배경 제작 기록

아래는 최초 제작 당시의 기록입니다. 생성 식별자는 로컬 파일 링크가 아니며 현재 음원이나 렌더링 동작의 명세로 사용하지 않습니다.

### Initial provenance
Built-in image_gen used; approved concept 2 is the reference.
Lit background: exec-f3c0e1a1-8dcb-44c0-b3d7-2498dcd140bf.png
Prompt: Preserve exact approved composition, aged laboratory, clock, lamp and rain window. Remove all left UI/title/button text and lines, reconstruct dark plaster wall. Remove only clock hands and central hub while retaining numbers/ticks. No other redesign. Full production background plate.
Unlit background: exec-0768a41c-ef55-459a-9306-44293f7f7b3e.png
Prompt: Lighting-only edit, turn off upper-right lamp and its warm illumination, retain faint blue window light and readable dial. Preserve all objects, camera, framing and empty dial; no text/hands/new objects. Align to lit background for crossfade.
Runtime assets: src/assets/images/menu/menu_background.png and menu_unlit.png.
Menu text, hands, rain, camera movement, lighting blend and glass fractures are Java2D code layers.
Audio: original procedural thunder and glass synthesis, existing game rain ambience.


v29: ClockGlassEffect renders the approved irregular glass fractures in Java2D. Two cached transparent layers; localized central microcracks and subdued edge reflections. No external fracture photo, GIF playback or detached shards. Legacy clock_fracture.png removed from this version.
