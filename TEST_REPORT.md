## v79 rain tuning

- Build, menu parity, lower-pane coverage and 12 weather checks passed. Rendered production HUD headlessly; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature verified.

## v78 rain tuning

- Build, menu parity, lower-pane coverage and 12 weather checks passed. Rendered production HUD headlessly; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature verified.

## v77 rain tuning

- Build, menu parity, lower-pane coverage and 12 weather checks passed. Rendered production HUD headlessly; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature verified.

## v76 rain tuning

- Build, menu parity, lower-pane coverage and 12 weather checks passed. Rendered production HUD headlessly; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature verified.

## v75 rain tuning

- Build, menu parity, lower-pane coverage and 12 weather checks passed. Rendered production HUD headlessly; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature verified.

## v74 rain coverage

- Java 21 build passed. VerifyRainCoverage74 confirmed menu pixel equality at six timestamps and visible rain in all three exposed lower window areas.
- Existing 12 weather checks passed. Production HUD/scene rendered headlessly to GIF; game and audio not launched.
- ZIP CRC, packaged JAR equality and launcher signature passed.

## v73 distant rain

- Java 21 build passed. Menu parity passed at six timestamps; all three lab panes show rain.
- Existing 12 weather checks passed. Actual GameHud/GameScenePanel rendered headlessly to GIF, including lightning. No game window or audio launched.
- Windows ZIP CRC, JAR equality and launcher signature verified.

## v72 three-window menu rain

- Java 21 compile/package passed. VerifyMenuRainParity72: menu pixel equality at six timestamps through one hour; rain visible in all three laboratory bays.
- VerifyLabWeather72: 12 checks passed for window clipping, foreground exclusions, hidden timer state and other-era stability.
- Rendered actual GameHud/GameScenePanel with production rain and lightning to a 96-frame GIF; no game window/EXE/audio launched.
- ZIP CRC, embedded JAR equality and EXE signature verified. Actual OS rendering/performance remains untested.

## v71 precomputed laboratory weather

- Java 21 build passed. GenerateLabWeather produced 284 paths and approximately 1.05 MB of compressed rain/lightning assets.
- VerifyLabWeather71 passed 12367 assertions, including per-path boundary checks, rain visibility, masking, hidden timers and other-era stability.
- Rendered full-scene 1280x720 GIF and power-off/alternate-aspect PNGs headlessly. No game window, EXE or audio playback launched.
- ZIP CRC, packaged JAR equality and launcher MZ signature passed. No end-to-end startup speed claim; desktop playback not tested.

## v70 present laboratory weather

- Build: Java 21 compilation and packaging passed.
- VerifyLabWeather70: 12 assertions passed, covering window masking, foreground exclusions, other-era rendering and hidden timer state.
- Headless previews: 16:9 animation, 16:10, ultrawide and unpowered laboratory rendered without opening a game window or playing audio.
- Windows ZIP CRC, packaged JAR equality and launcher signature passed. Actual desktop playback and sound were not run.

# v69
210 stage/normal-completion checks, 97 chord checks, 63 runner checks, 45 save checks passed. F10 mapping absent in both developer-mode states. No game window, audio or EXE launched.

# v67
21 unread-state checks, 45 save checks, 16 asynchronous queue checks and 20 slot checks passed. Separate headless JVM verified process-restart restoration. Legacy schema, checksum and backup verified with temporary test saves only. No game window, EXE or audio launched.

# v66
16 acquisition/queue/expiry/hover/reload/reward/native-theme checks and 45 save compatibility checks passed. Actual panels rendered headlessly. No game or audio launched. Unread indicators are session UI state, reset on loading another GameState; save schema unchanged.

# v62 진행 상태별 Shift 강조

InteractionHint는 기존 GameState와 진행 설정에서 NONE/LATER/READY를 계산합니다.
- 완료/획득/소비 후 재획득 불가: 강조 없음(배경과 함께 50% 어두워짐).
- 지금 클릭하여 수행 가능한 단계/획득: 기존 조명 유지 + 파티클.
- 선행 조건/아이템/전력 조건이 남은 대상: 조명만 유지.

수평 미션의 모든 실행 가능한 대상은 READY입니다. 작업대/컴퓨터는 남은 후속 단계가 있으면 완료로 처리하지 않습니다. 소지한 아이템과 조립 후 소비된 부품함은 강조하지 않습니다. 현재 UI의 실제 클릭 처리와 같은 다음 단계 판정을 사용합니다. 저장 필드는 추가하지 않으며 불러온 GameState에서 다시 계산합니다. 새 진행으로 바뀌면 대비 마스크를 무효화합니다. 완료/대기 대상은 마우스 hover 파티클도 표시하지 않습니다. 클릭과 설명 기능은 그대로입니다.

검증: 진행/마스크/파티클/복원 52개, Shift 50% 대비와 해제 페이드 14개, 저장/백업/별도 프로세스 복원 45개. 총 111개 통과. 실제 패널 헤드리스 렌더링으로 완료 대상의 마스크 알파 128, 현재/대기 대상 알파 0과 현재 대상만 입자 프레임이 변하는 것을 확인했습니다.

게임 Main/JFrame/EXE는 실행하지 않았고 사용자 세이브는 건드리지 않았습니다. v61 런처/runtime을 재사용하고 v62 JAR/설정을 교체합니다. Git 커밋/푸시는 하지 않았습니다.
