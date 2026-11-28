# 테스트 안내

## 폴더 역할

- 루트: 현재 기능 회귀 검증, 공통 도우미, 모델 비교 기준.
- [preview](preview/README.md): 화면·프레임 생성 및 프로파일링 도구. 기본 검증에서 실행하지 않습니다.
- [archive](archive/README.md): 이전 UI와의 픽셀 비교, 취소된 레이아웃 검증. 현재 화면에 대한 합격 기준이 아닙니다.
- fixtures: 배선 쉬움·어려움 정답 데이터. 현재 정상 풀이 검증에서 사용하므로 유지합니다.

버전 번호는 도입 시점이며 폐기 여부를 뜻하지 않습니다. `LegacyPresentPuzzleModel`은 현재 모델 규칙 비교에서 계속 사용합니다.
`ArtifactPaths`는 모든 미리보기의 외부 출력 경로를 계산하는 테스트 전용 도우미입니다.

## 게임을 실행하지 않는 검증

저장소 루트에서 다음 명령을 실행합니다.

```powershell
python test/run_tests.py --java-home "JDK21경로"
```

전체 Java 소스, 테스트, preview, archive, 날씨 생성 도구를 컴파일합니다.
실행은 `suite.txt`에 명시한 55개 검증만 수행합니다. 각 프로세스는 headless 모드이며 저장·음량 설정은 임시 경로로 격리됩니다.
실제 게임 Main, EXE, JFrame/JDialog를 실행하지 않습니다. 오디오 검증은 가상 장치를 사용합니다.
테스트당 제한 시간은 90초입니다. 한 항목이 실패해도 나머지를 확인한 뒤 실패 종료 코드를 반환합니다.

일부 항목만 다시 확인하려면 `--only VerifyMaze VerifyProgression`을 추가합니다.
`--artifacts-root`로 결과물 위치를 바꿀 수 있습니다.
컴파일 결과는 외부 `build/v<현재버전>-tests/organized`, 보고서는 외부 `outputs/test-validation`에 저장합니다.
자동 정리 도구 자체는 `python test/verify_build_cleanup.py`로 임시 폴더에서 별도로 검증합니다.

## 통합한 진입점

| 이전 이름 | 현재 사용 |
|---|---|
| PreviewCursor63 | preview/PreviewCursor64.java |
| VerifyLabWeather70 | VerifyLabWeather72.java (마스크·타이머·다른 시대 검증) |
| VerifyLabWeather71 | VerifyRainCoverage74.java (기존 단순 호출 래퍼 제거) |
| VerifyMenuRainParity72 | VerifyRainCoverage74.java (동일 비교 + 하단 빗방울 확인) |
| 이전 VerifyRepairPanels 픽셀 비교 | archive/ArchivedVerifyRepairPanels.java |

현재 `VerifyRepairPanels`에는 클릭·드래그, 초기화, 쉬움→어려움 전환, 정상 완료, 중복 완료 방지와 취소 검사를 남겼습니다.
이전 `VerifyPresentPanelParity`와 Legacy UI는 archive에 함께 보관합니다.
기존 보고서의 과거 테스트 이름은 당시 기록이며 위 표의 현재 진입점으로 대응합니다.

## 이번 정리에서 발견한 오래된 기대값

- 실행 관리자 가상 등록에 `temporary_power`를 추가했습니다.
- 전력 합계 퍼즐의 현재 제목 `전원 켜기 (임시)`를 명시적으로 검사하며 규칙 전수 비교는 유지합니다.
- 진행 카탈로그의 튜토리얼 포함 19개 단계와 `steps=` 확장·중복 검사를 현재 목록에 맞췄습니다.
- 배선 검증은 현 미로와 정답 fixture에서 직선 구간을 골라 드래그를 검사합니다. 이전의 고정 직선 통로 가정은 사용하지 않습니다.

게임 기능이나 정답 데이터는 변경하지 않았습니다. 보관·미리보기 코드는 컴파일만 확인하며 현재 회귀 검증 통과를 의미하지 않습니다.

음원 제작 도구의 별도 안전성 검증은 `python test/verify_audio_authoring.py`로 실행합니다. NumPy가 필요하며 재생 없이 임시 폴더에 두 세트의 WAV를 생성하고 기존 게임 음원 보존과 출력 경로 차단을 확인합니다.


### v100 중복 처리 검증
VerifyInteractions97은 장치 동작마다 저장 요청이 한 번인지와 엔딩 안내 이전 저장을 확인합니다. VerifyRefresh100은 메시지 단독 갱신, 연속 메시지 병합, 전체 갱신 후 예약 취소, 로드된 상태와 렌더링 픽셀 동등성을 검증합니다. 전체 40개 검증을 게임 창 없이 실행합니다.

### v106 하단 레이아웃 검증
VerifyPuzzleLayout106은 배선과 PC의 지원 크기별 하단 안내/버튼 경계, 배선의 실제 드래그 풀이와 단계별 완료, PC 해독표 복귀를 검사하고 실제 패널 이미지를 v106-preview에 생성합니다. 게임 창은 실행하지 않습니다.

### v108 사고 로그 입력 및 완료 검증
VerifyAccidentDrag108은 두 크기에서 카드 6개의 모든 출발/도착 조합, 범위 밖 드롭, Esc, 포커스 상실과 종료 후 늦은 입력을 검사합니다. 실제 패널과 PuzzleRunner/SaveQueue를 연결해 최종 로그 대조에만 보상과 자동저장이 발생하고 다시 불러와도 유지되는지 확인합니다. 이미지는 외부 outputs/v108-preview에 생성하며 실제 게임 창은 열지 않습니다.

### v109 기록 열람 검증
VerifyLogEvidence109는 정상/사고 자료 탭, 두 지원 크기의 가독성, 배경 입력 및 스킵 차단, Esc/X 복귀, 순서/단계/선택/오류 메시지 보존과 종료 후 재진입 차단을 검사합니다. VerifyAccidentDrag108의 실제 패널/실행 관리자/저장 연계 검증에도 기록 열람을 포함했습니다. 이미지는 outputs/v109-preview에 생성합니다.


### v110 설계도 검증
VerifySchematic110은 9조각/18칸의 모델과 실제 마우스 이동, 점유 칸 교환, 보관 영역 복귀, 범위 밖 드롭/Esc/포커스 취소, 잘못된 배치, 확인 전 비완료, 완료/스킵 1회, 종료 후 입력 차단을 검사합니다. 기존 PZ008 완료 효과와 자동저장, 재로드 후 PZ010 및 기존 부품 소비를 대조합니다. 두 크기의 이미지는 outputs/v110-preview에 생성합니다. VerifyPresentModels의 회전 규칙 비교에서 PZ008을 제외하고 새 조립 검증으로 대체했습니다.


### v111 모듈 조립 검증
VerifyModule111은 기존 회전 모델과 64개 조합을 대조하고 실제 버튼 클릭, 회전 주기, 오른쪽 클릭/범위 밖 해제, 초기화, Esc, 개발 스킵과 완료 후 입력 차단을 검사합니다. 세 화면 크기에서 안내/버튼/클릭 영역과 접근성 이름을 확인합니다. 기존 PZ010 완료 효과와 부품 소비/DEVICE 보상, PZ011 개방, 자동저장 1회와 재로드를 실제 패널→PuzzleRunner→SaveQueue 흐름으로 검증합니다. 이미지는 outputs/v111-preview에 생성합니다.


### v112 시간 좌표 검증
VerifyCoordinates112는 기존 모델과 64개 회전 입력 조합 및 각 다음 조작을 대조합니다. C→A 연동, 실제 마우스/Space 버튼 액션, 포커스/마우스 선택 시 비회전, 연동 안내, 초기화/취소/개발 스킵과 중복 완료 방지를 검사합니다. 세 화면 크기의 배치/문구와 PZ011 완료→과거 개방→자동저장→재로드→과거 이동을 검증합니다. 이미지는 outputs/v112-preview에 생성합니다.


### v113 강조 효과 검증
VerifyHighlight113은 실제 GameScenePanel을 창 없이 그려 세 시대와 첫 튜토리얼의 강조 조건, 진행 복원, 배경 50% 마스크, Shift/휠 버튼 혼합 입력과 해제, 부드러운 복원 및 원본 화면과의 픽셀 일치, 메뉴/포커스/시대 전환, 도형 계산 재사용을 확인합니다. 추가 효과 전체가 밝기 전환에 맞춰 사라지는지도 검사합니다. 화면은 외부 outputs/v113-preview에 생성합니다.


### v114 장치 창 검증
VerifyDevice114는 LegacyDevice113의 이전 규칙과 21개 진행 상태/부품 선택 여부별 조건, 설명, 실행 결과를 대조합니다. 실제 ItemUseHandler 콜백과 SaveQueue를 연결해 시험 작동의 지연 반영, 장착/이동/엔딩의 자동저장 1회와 재로드, 부품 소비 및 방 개방 유지, 취소와 중복 실행 차단을 확인합니다. DevicePanel을 세 크기로 배치하고 실제 화면을 outputs/v114-preview에 생성합니다. LegacyDevice113은 비교 기준이므로 현재 검증에 필요합니다.


### v115 USB 복사 확인창 검증
VerifyUsbCopy115는 실제 UsbCopyPanel을 PuzzleRunner와 SaveQueue에 연결합니다. 복사/취소/X/Esc/창 종료, Enter 선택, 중복 입력, 선행 조건과 USB 보유 조건, 세션 교체 및 종료 후 결과 차단을 확인합니다. 이전 YES 결과와 진행 스냅샷을 대조하고 자동저장 한 번과 재로드 후 분석 개방 및 수리 잠금 유지까지 검사합니다. 세 화면 크기에서 글자/버튼 경계를 확인하며 실제 화면은 outputs/v115-preview에 생성합니다.


### v116 종료 저장 검증
VerifyExit116은 실제 확인 패널 결과를 기존 GameController와 GameSession에 연결합니다. 취소/X/Esc/창 종료, 중복 입력, 진행 저장 후 설정 저장 대기, 저장 실패 시 창 유지와 재시도, 성공 시 단일 종료를 확인합니다. 현재 보이는 화면의 캡처와 세 화면 크기를 검사합니다.

### v116 시작 연출 체크박스 검증
VerifyIntroCheck116은 클릭, Space/Enter, 설정 재진입, 메인/인게임 설정 공유, 숨김/비활성 상태, 음량 값 보존과 세 크기의 배치를 검증합니다. 기존 VerifyGameMenu의 버튼 검색은 체크박스도 찾을 수 있도록 AbstractButton으로 변경했습니다. 실제 화면은 outputs/v116-preview에 생성합니다.

- VerifyEnding117: 엔딩 완료 후 저장 요청 순서, 확인/X/Esc/Enter/해제와 중복 입력, 실제 저장 재로드 및 세 크기의 안내창 배치.

- v118: VerifyLogEvidence109에 정상 절차 6단계 순서와 본문 전체 표시 검증을 추가했습니다. 기존 기록 열람 차단/복원과 로그 완료/저장 검증도 유지합니다.
