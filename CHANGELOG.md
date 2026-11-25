## v82

- Request audio lines using each original WAV format. Add bounded asynchronous recovery for startup clips and scene loops, without duplicate playback or delayed one-shot effects.
- Preserve audio assets and UI timelines byte-for-byte. Improve device-error diagnostics.
- Passed 15 actual EXE runs (10 normal, 3 injected recovery, 2 audio-synchronized intros) and regression checks. Historical hardware exception root cause remains unconfirmed; see docs/AUDIO_RECOVERY_V82.md.

## v81

- Precomputed clock fracture layers, deferred menu effects and overlays, bulk rain decoding, original external fonts.
- Final paired EXE startup median: 1076 ms to 759 ms (29.5% reduction). 18 frames identical. Actual start/save/load/close verified with isolated saves.
- Full results and transient audio limitation: docs/STARTUP_OPTIMIZATION_V81.md.

## v80


확실히 사용하지 않는 파일 16개(자산 14개, Java 클래스 2개)를 제거했습니다. 삭제 파일은 별도 ZIP과 SHA-256 목록으로 보관했습니다. 이전 배포본, 저장 파일, 라이선스 파일은 삭제하지 않았습니다.

첫 창 표시 전에 연구실 화면과 모든 자산을 준비하던 구조를 변경했습니다. 첫 창이 열린 뒤 배경 작업에서 연구실 이미지, 날씨, 아이템 이미지를 준비하며, Swing 화면 생성과 상태 반영은 EDT에서 수행합니다. 준비 완료 전에 시작하거나 불러오면 완료를 비동기로 기다립니다. 중복 준비, 실패, 종료 중 완료를 처리합니다. 로딩 화면은 추가하지 않았습니다.

시작 화면과 연구실은 읽기 전용 빗방울 데이터를 공유합니다. 연구실에서 불필요한 메뉴 전용 출력 버퍼를 없애고 같은 시각과 전력 상태의 재그리기는 결과를 재사용합니다. 큰 배열과 버퍼 기준 약 10.24 MB의 중복 할당을 줄였습니다. 전체 프로세스 메모리 실측값은 아닙니다.

## 초기 준비 시간

각 버전 별도 JVM에서 headless 구성 요소 준비 시간을 3회 측정했습니다. 게임과 EXE를 실행하지 않았습니다.

|항목|v79|v80|
|---|---:|---:|
|1회|1012.0 ms|658.6 ms|
|2회|994.7 ms|647.7 ms|
|3회|986.3 ms|650.1 ms|
|중앙값|994.7 ms|650.1 ms|

중앙값 약 344.6 ms, 34.6% 감소. EXE 클릭부터 실제 창 표시까지의 시간이 아니며 JVM 시작, OS 디스크 캐시, 보안 검사 등의 시간은 포함하지 않습니다. 준비가 끝나기 전 게임에 진입하면 짧게 기다릴 수 있습니다.

## 기능 및 화면 검증

- Java 21 전체 컴파일과 빌드 시 날씨 데이터 생성 성공.
- 주요 화면 8장(메뉴 3장, 연구실 점등/소등, 연구 기록, 보관함, 메뉴 오버레이) 픽셀 동일.
- 비동기 자산 준비 14개, 비동기 오디오 26개 검증 통과. 실제 오디오 장치 미사용.
- 저장/복구 45개, 저장 큐 16개, 슬롯 20개, 비동기 슬롯 15개, 아이템 확인 상태 21개 검증 통과.
- 단계별 치트 및 일반 완료 210개, 공통 퍼즐 실행 63개, 진행 구조 51개, 새 게임 확인 30개 검증 통과.
- 현재 퍼즐 모델 260121회 비교 통과. 오디오 디코더 18개 및 WAV 20개 검사 통과.
- 시작 화면 빗방울 픽셀 비교, 연구실 창문 하단 적용 범위 검사 통과.
- 삭제된 파일의 소스 및 JAR 부재 확인. ZIP CRC, 배포 JAR 일치와 EXE MZ 서명 확인.

네이티브 게임 창이나 EXE의 실제 실행은 요청에 따라 수행하지 않았습니다. 따라서 실제 환경에서의 클릭, 사운드 장치, 전체 화면 동작까지 무오류를 보장하는 검증은 아닙니다. 저장 형식과 진행 데이터 모델은 변경하지 않았습니다.


## v79

- Narrowed and tapered droplet heads with 1.35x vertical elongation, sharpened tapered tails and increased frequency from four to five streams. Preserved gathering, 1.5x terminal speed, window masking and lightning.

## v78

- Integrated approved visibility tuning: head brightness 80%, tail contribution 0.12 with 38-56px length. Retained 55% head size, four streams, gathering/acceleration, lower-window coverage and 1.5x terminal speed.

## v77

- Reduced head brightness to 58%, separated 28-42px tapered tails from 55% head size, added gathering/acceleration, and reduced density to four streams. Retained lower-window coverage and 1.5x terminal speed.

## v76

- Set laboratory rain to seven staggered event streams (7x baseline frequency). Retained 55% size, 1.5x speed and lower-window coverage.

## v75

- Set lab droplets to 55% linear size and 1.5x original speed. Four event streams and lower-window coverage retained.

## v74

- Set lab droplets to 40% linear size, four staggered event streams and 2x original travel speed.
- Distributed starting heights and removed the menu-specific destination mask; the laboratory mask alone clips frames and foreground objects.
- Original menu effect and laboratory lightning unchanged.

## v73

- Laboratory droplets use 65% linear size and travel distance, with two staggered event streams (17.3-second offset). Original menu rain is unchanged.
- Both streams share the existing per-pane compositing pass; frames and room objects remain masked.

## v72

- Laboratory rain now reuses the actual TextureRain menu engine and precomputed menu data in three individually scaled bays, with 0/1.7/3.4 second phase offsets.
- Uniform XY scaling preserves droplet proportions; source-photo removal is excluded so unrelated laboratory pixels are not darkened.
- Preserved laboratory lightning, power state and object masks.

## v71

- Replaced procedural lab rain lines with original-photo highlight sprites and 284 build-time generated, single-pane paths.
- Added acceleration, staggered timing and fade before pane boundaries.
- Generated textured lightning assets for powered and unpowered backgrounds; menu visuals and thunder timing unchanged.
- Runtime loads prepared assets and draws small sprites; no background analysis or full-frame rain pixel loop.

## v70

- Added rain and window-only lightning to the present laboratory. Pane masks follow the photograph and exclude frames and foreground objects.
- Reuses the menu lightning envelope and 1.2-second thunder delay. Weather stops when the scene is hidden or another era is selected.
- Verified headlessly without starting the game.

# v69
- Remove Ctrl+Shift+F10 binding. Shift+8+9+0 now uses stage skip: easy to hard, then complete.
- Preserve normal completion and prevent held-key multi-stage skipping.

# v68
- Add temporary focused-puzzle Shift+8+9+0 completion chord, including whole wire puzzle.

# v67
- Persist unread item IDs in schema 2, with legacy schema 1 loading.
- Autosave after hover acknowledgement; restore per-slot read states without replaying pickup effects.

# v66
- Fix repair puzzle button contrast with theme-independent rendering.
- Add queued 1.8-second item pickup notices and hover-to-read inventory dots.

# v62
- Derive Shift spotlight/particle hints from current interaction progress.
- Invalidate masks after completion, pickup and restore; preserve fade and save format.

# v61
- Split five present puzzle models behind PresentPuzzle and PresentPuzzles registry.
- Share cyclic controls and target comparison; preserve rules, artwork and stable IDs.

# v60
- Split gears and wires into independent panels with shared layout/lifecycle and dialog host.
- Preserve input, staged completion, developer skip, artwork and save compatibility.

# v59
- Unify puzzle outcomes and dispatch through PuzzleRunner/PuzzleDialogs.
- Remove Rush Hour progress writes; preserve delayed completion and cancellation.

# v58
- Serialize save/load/list IO off the EDT; preserve existing file format and backups.
- Guard repeated input, stale list results, failed saves and final save before exit.

# v57
- Serialize audio IO and playback on a dedicated worker; keep Swing responsive.
- Prewarm intro/thunder, retain audio-clock sync, cancel stale starts and close late loads.

# v56
- Precompute menu rain data and storm mask during build; retain exact approved frames.
- UI initialization probe mean: 4734.51 ms to 862.27 ms; no game launched.

# v54
- Replace reset with in-game load; order Save, Load, Settings, Main Menu.
- Restore existing save slots with the existing design and save format.

# v53
- Replace OS new-game confirmation with compact themed overlay.
- Preserve confirmation, cancel and start-animation timing.

# v52
- Increase era/location captions to 16pt and values to 22pt.
- Increase separation between era and location to 50px; preserve TIME ESCAPE styling and left placement.

# v51
- Apply selected two-line era/location header; preserve title and controls.

# v50
- Replace native help dialog with themed overlay and centered guidance.
- Preserve TIME ESCAPE and display explicit era/place fields in HUD.

# v49
- Apply selected option 1: horizontal return arrow with rounded tip and semicircular bend.

# v48
- Add separator between auto and manual save slots.
- Replace back labels with custom curved return arrows matching menu colors.

# v47
- Expand manual slots to 1–4; preserve automatic and existing slot files.
- Adjust save/load layout to fit five slots with metadata at minimum window size.

# v46
- Add independent manual slots 1/2 below the existing automatic save.
- Continue now opens a slot chooser; manual overwrite requires confirmation.
- Preserve manual checkpoints across new game/reset.
- Reuse translucent menu styling; show location, puzzle count and timestamp.

# v45
- Add UTF-8 file saves, validated restore, automatic checkpoints and backup recovery.
- Enable existing Continue button from saved progress across process exits.
- Restore powered backgrounds without replaying their activation animation.
- Confirm replacement of an existing save before new game.

# v44
- Fix Rush Hour delayed completion after reset/close.
- Separate immutable progression definitions from runtime state.
- Normalize puzzle, item, flag and room IDs; data-driven conditions/rewards/object routing and mission counts.
- Preserve v43 visuals and existing play order.

# v43
- 중앙 반투명 검정 게임 메뉴, 배경 blur 캐시, 글씨형 항목.
- 메인 메뉴/기존 시작 연출 설정/초기화 확인 연결. Esc·포커스·배경 입력 차단.

# v42
- 낡은 종이와 그림 아이템 적용. 전체 노트 양방향 페이지 넘김(460ms), 오입력 및 타이머 정리.

# v40
- 통합 연구 노트, 기록/보관함 탭, 1열 아이템 목록, 도움말/메뉴 아이콘.
- 하단 상시 안내 및 고정 사이드바 제거. 배경 cover 배율과 클릭 좌표 일치.

# v39
역할별 폰트 통일 및 작은 설명 가독성 조정. 폰트/라이선스 내장, OS 설치 불필요.

# v38
장비 연결부 및 황동 장치 부분 수정 배경 적용, 소등 상태 대응. 기존 연출 코드 및 음원 유지.

# v37
시계 바늘과 중심축의 황금색 외곽선을 어두운 금속색으로 변경.

# v36
소등 시 시계 바늘 알파가 35%로 떨어지던 결함 수정. 항상 불투명하게 렌더링.

# v35
승인된 질감 기반 빗방울/번개 통합. 비 음량 유지, 번개 후 1.2초 천둥 및 미리 읽기. 장시간 물방울 재생.

# v34
시제품 메뉴 막대 제거, 창 제목 통일, 메인 배경 cover 및 독립 메뉴 배치.

# v33
창틀/실내 오브젝트를 피하는 유리 물방울, 약한 국소 번개, 실내처럼 둔화한 비와 천둥.

# v32
메인 메뉴/현재/과거의 반복 clockwork 배경음 재생 제거.

# v31
Original-speed lamp audio; sound-driven visuals; glass at 5 seconds; synchronized soundtrack.

# TIME ESCAPE v29

확정한 시계 유리 균열 연출을 실제 게임에 적용했습니다.
가운데의 촘촘한 잔금, 바깥으로 뻗는 불규칙한 균열, 어두운 틈과 일부 가장자리의 약한 반사광을 사용합니다. 파편은 없습니다.

## 시작 연출 시간 (게임 시작 버튼 기준)
- 전등 깜빡임과 완전 소등: 기존 그대로, 1.45초에 소등
- 큰 균열 및 기존 유리 효과음 시작: 2.45초
- 중심부 잔금 포함 보조 균열: 큰 균열 후 0.06초
- 화면 암전 시작: 7.20초 / 게임 전환 완료: 8.10초

## Windows 실행
TIME_ESCAPE_v29_Windows.zip 전체를 압축 해제하고 TimeEscape/TimeEscape.exe를 실행하세요.
TimeEscape.exe 옆의 app 및 runtime 폴더를 함께 유지해야 합니다. 별도 Java 설치는 필요 없습니다.
단독 제공 EXE는 같은 배포본 폴더 안에서 사용하는 실행 파일이며 단일 파일 설치 프로그램은 아닙니다.

## 개발
Java 21 이상 JDK로 tools/build.ps1 -JavaHome <JDK경로>를 실행하면 build/uix_v29.jar가 생성됩니다.
균열은 Java2D 코드로 그리며 GIF를 게임에서 재생하지 않습니다. 이전 균열 PNG는 더 이상 사용하지 않아 새 배포본에서 제외했습니다.
게임 창과 EXE를 실행하지 않고 컴파일, headless 메뉴 검증 및 배포 파일 무결성 검사로 확인했습니다.

---
# TIME ESCAPE v28
시작 연출의 시계 파손 표현을 교체했습니다.
시계판/숫자/바늘은 움직이거나 분해되지 않습니다.
첫 깜빡임은 0.95초에서 0.68초로 줄이고, 후반 점멸 간격은 대체로 0.06초에서 0.11~0.12초로 늘렸습니다. 완전 소등은 기존과 동일한 1.45초입니다. 소등 후 정확히 1.5초를 기다린 2.95초 시점에 균열과 유리 효과음이 시작됩니다. 코드로 그린 균열 대신 불규칙한 미세 분기와 유리 반사 질감이 있는 투명 에셋으로 교체했습니다. 균열 외 영역은 투명하며 시계판과 바늘은 그대로 보입니다. 파편은 없습니다. 전등 타이밍 파일은 v27과 바이트 단위로 동일합니다.
4.7초 시작 전환, 메뉴 기능, 비/천둥/카메라/시계/전등 연출 및 Shift 50% 감광을 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 실제 메뉴 렌더러를 headless 모드로 실행해 PNG/GIF를 출력했습니다.
GIF에는 소리가 없습니다. 기존 유리 효과음은 유지합니다.
ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.

# TIME ESCAPE v27
시작 연출의 시계 파손 표현을 교체했습니다.
시계판/숫자/바늘은 움직이거나 분해되지 않습니다.
첫 깜빡임은 0.95초에서 0.68초로 줄이고, 후반 점멸 간격은 대체로 0.06초에서 0.11~0.12초로 늘렸습니다. 완전 소등은 기존과 동일한 1.45초입니다. 소등 후 정확히 1.5초를 기다린 2.95초 시점에 균열과 유리 효과음이 시작됩니다. 균열의 동심원 격자와 균등한 방사 패턴을 제거하고 길이·각도·굵기가 다른 가는 분기 균열로 변경했습니다. 유리 파편 생성 및 낙하 렌더링은 제거했습니다.
4.7초 시작 전환, 메뉴 기능, 비/천둥/카메라/시계/전등 연출 및 Shift 50% 감광을 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 실제 메뉴 렌더러를 headless 모드로 실행해 PNG/GIF를 출력했습니다.
GIF에는 소리가 없습니다. 기존 유리 효과음은 유지합니다.
ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.

# TIME ESCAPE v26
시작 연출의 시계 파손 표현을 교체했습니다.
시계판/숫자/바늘은 움직이거나 분해되지 않습니다.
전등은 약 1초 동안 천천히 한 번 깜빡인 뒤, 빠르게 타타탁 점멸하며 1.45초에 완전히 꺼집니다. 소등 후 정확히 1.5초를 기다린 2.95초 시점에 균열과 유리 효과음이 시작됩니다. 가늘고 촘촘한 거미줄 균열이 약 0.1초 안에 번지고, 0.02~0.09초의 짧은 지연으로 작은 파편들이 빠르게 낙하합니다.
4.7초 시작 전환, 메뉴 기능, 비/천둥/카메라/시계/전등 연출 및 Shift 50% 감광을 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 실제 메뉴 렌더러를 headless 모드로 실행해 PNG/GIF를 출력했습니다.
GIF에는 소리가 없습니다. 기존 유리 효과음은 유지합니다.
ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.

# TIME ESCAPE v25
시작 연출의 시계 파손 표현을 교체했습니다.
시계판/숫자/바늘은 움직이거나 분해되지 않습니다.
전등은 0.44초 동안 세 차례 빠르게 점멸한 뒤 꺼집니다. 0.48초 시점에 유리가 깨집니다. 가늘고 촘촘한 거미줄 균열이 약 0.1초 안에 번지고, 0.02~0.09초의 짧은 지연으로 작은 파편들이 빠르게 낙하합니다.
기존 2.4초 시작 전환, 메뉴 기능, 비/천둥/카메라/시계/전등 연출 및 Shift 50% 감광을 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 실제 메뉴 렌더러를 headless 모드로 실행해 PNG/GIF를 출력했습니다.
GIF에는 소리가 없습니다. 기존 유리 효과음은 유지합니다.
ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.

# TIME ESCAPE v24
시작 연출의 시계 파손 표현을 교체했습니다.
시계판/숫자/바늘은 움직이거나 분해되지 않습니다.
소등 후 유리 표면에 가느다란 불규칙 균열이 순차적으로 번지고, 작은 반투명 파편 18개만 시간차를 두고 중력 방향으로 떨어집니다.
기존 2.4초 시작 전환, 메뉴 기능, 비/천둥/카메라/시계/전등 연출 및 Shift 50% 감광을 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 실제 메뉴 렌더러를 headless 모드로 실행해 PNG/GIF를 출력했습니다.
GIF에는 소리가 없습니다. 기존 유리 효과음은 유지합니다.
ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.

# TIME ESCAPE v23
메인화면을 승인된 2안(낡은 연구실의 시계 장치)으로 변경했습니다.
게임 시작/이어하기/설정/종료 기능 및 세션 내 이어하기 조건을 유지합니다.

- 창문 영역의 비 애니메이션, 기존 rain.wav를 낮은 음량으로 추가.
- 첫 번개는 메뉴 표시 후 약 12초, 이후 18~30초 간격. 낮은 밝기의 번개 뒤 0.45초 후 천둥.
- 배경만 좌우 5px/상하 3px로 천천히 움직이며 UI/클릭 영역은 고정.
- 소등 전용 이미지로 약하게 빛이 낮아지는 전등 변화.
- OS 현재 지역 시각에 맞는 시침/분침. 시계 원근에 맞춰 그립니다.
- 게임 시작: 0.32초 소등 → 0.48초부터 시계 파손/조각 낙하와 효과음 → 2.4초 후 게임 진입.
- 설정에서 시작 전환 연출을 끄면 즉시 시작. 이어하기/설정/종료에는 파손 연출을 넣지 않습니다.
- 숨겨진 메인화면은 타이머와 대기 콜백을 정리하고 복귀 시 정상 시계로 돌아옵니다.

ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.
게임 창 및 EXE는 실행하지 않았습니다. headless 렌더링/동작 검사를 수행했습니다.
미리보기 GIF에는 소리가 없습니다. 신규 천둥/유리 소리는 직접 합성했으며 실제 장치 청취는 미검증입니다.
기존 Shift 50% 배경 감광과 수평 미션 구조 유지. 영구 저장/추가 방/시대 재방문/미래 USB 활용 미구현.

# TIME ESCAPE v22
Shift를 누르면 상호작용 오브젝트의 원래 밝기를 유지하면서 주변 배경을 약 50% 어둡게 합니다.
오브젝트 주변 경계는 부드럽게 처리하며 기존 Effekseer 반짝임을 유지합니다.
Shift를 떼면 약 0.2초에 걸쳐 원래 밝기로 돌아옵니다. 미션창/UI는 감광하지 않습니다.
전력 복구 전에는 소등 배경 자체를 사용하므로 Shift로 전등이나 컴퓨터가 켜지지 않습니다.
창 포커스 상실, 화면 숨김, 시대 전환 시 강조 상태를 정리합니다.
감광 마스크는 시대별 배경 좌표를 기준으로 생성 후 재사용합니다.

실행: ZIP 전체를 풀고 TimeEscape/TimeEscape.exe를 실행하세요. app/runtime 폴더가 필요합니다.
게임 창 및 EXE는 자동 실행하지 않았습니다. Java headless 검사로 검증했습니다.
기존 수평 미션/전력 복구/퍼즐 규칙은 유지합니다.
영구 저장, 추가 방, 시대 재방문 및 미래 USB 활용은 아직 미구현입니다.

# TIME ESCAPE v21
Shift 광점의 표시 크기를 32~38에서 54~64로 확대했습니다(원본 배경 좌표 기준).
불투명도 배율은 0.88에서 1.0으로, 오브젝트당 최대 광점은 5개에서 6개로 조정했습니다.
개별 광점의 휴지 시간은 1초에서 0.2초로 줄였습니다. 위치는 고정이며 서로 다른 시점에 반짝입니다.
Effekseer 원본 스프라이트의 부드러운 가장자리와 3초 페이드 애니메이션은 유지합니다.
게임 창 및 EXE는 실행하지 않았습니다. 전체 연구실 GIF는 실제 GameScenePanel headless 렌더링입니다.
실행 시 ZIP을 모두 풀고 TimeEscape.exe와 app/runtime 폴더를 함께 유지하세요.

# v20
Shift: 떠다니는 원형 입자 → Effekseer 고정 광점 애니메이션.

# v19
소등 전용 배경, 2회 점멸 후 4.2초에 안정되는 전환 추가. Shift 오픈소스 조사.

# v18
승인된 철제 배전함 배경 적용. PZ004 클릭 영역·명칭 갱신. 코드 스위치/표시등 제거.

# v17
벽면 전원 스위치 배치 및 기존 탁상 영역 제거. Shift 이름표/테두리를 부유 파티클로 교체.

# v16
새 미니게임의 연구실/시계 테마 디자인, 미션 카드, 배경 직접 조사, 배전반 비상 표시등. 하단 개발 버튼 제거.

# v15
수평 미션, 전력 복구, 4종 신규 퍼즐, 아이템 탐색/복사, 우측 목표 안내, 과거 이동 AND 조건 추가.

# CHANGELOG v14
미래 통로 85/124칸으로 단축, 장애물 증가. 개발 세션 전용 Ctrl+Shift+F10 단계 스킵 추가.
기본 비활성, 상단 메뉴에서 켜기, 퍼즐 제목에 개발 모드 표시. 과거 디자인 및 미래 재선택 유지.


# v55
- Swap new-game prompt buttons: Cancel on the left, Start on the right.


