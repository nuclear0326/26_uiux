## v82

- Request audio lines using each original WAV format. Add bounded asynchronous recovery for startup clips and scene loops, without duplicate playback or delayed one-shot effects.
- Preserve audio assets and UI timelines byte-for-byte. Improve device-error diagnostics.
- Passed 15 actual EXE runs (10 normal, 3 injected recovery, 2 audio-synchronized intros) and regression checks. Historical hardware exception root cause remains unconfirmed; see docs/AUDIO_RECOVERY_V82.md.

## v81

- Precomputed clock fracture layers, deferred menu effects and overlays, bulk rain decoding, original external fonts.
- Final paired EXE startup median: 1076 ms to 759 ms (29.5% reduction). 18 frames identical. Actual start/save/load/close verified with isolated saves.
- Full results and transient audio limitation: docs/STARTUP_OPTIMIZATION_V81.md.

## v80 cleanup and startup verification


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


