# TIME ESCAPE 팀원용 미니게임 개발 가이드

기준: v65 / Java 21 / Swing + Java2D

이 문서는 새 미니게임을 만드는 팀원과 게임에 연결하는 통합 담당자를 위한 가이드입니다. 예제는 `docs/examples/frequency`에만 보관하며 현재 게임 진행에는 등록하지 않았습니다.

## 1. 먼저 알아야 할 구조

```text
배경 오브젝트 클릭
  → GameFrame이 진행 설정에서 실행할 단계를 찾음
  → PuzzleRunner가 선행 조건과 중복 실행을 확인
  → PuzzleDialogs가 kind에 맞는 창을 실행
  → 퍼즐 패널이 모델을 조작하고 정답을 확인
  → 창이 SOLVED 또는 CANCELLED 반환
  → PuzzleRunner가 완료 처리
  → GameState가 설정된 보상, 아이템 소비, 환경 상태, 방 개방 반영
  → GameFrame이 화면 갱신과 저장 처리
```

퍼즐은 자기 규칙만 처리합니다. 퍼즐 코드에서 GameState.complete(), 아이템 지급, 파일 저장을 직접 호출하지 마세요. 공통 완료 처리와 중복되면 보상이나 저장 문제가 생깁니다.

## 2. 팀원이 수정할 파일

| 위치 | 담당 내용 | 새 퍼즐 작업 |
|---|---|---|
| `src/timeescape/puzzle/` | 규칙, 현재 값, 정답 판정 | 새 모델 추가 |
| `src/timeescape/ui/` | 화면, 입력, 창과 결과 반환 | 새 패널과 필요한 창 추가 |
| `src/timeescape/ui/PuzzleDialogs.java` | kind와 실행 창 연결 | 통합 담당자가 등록 |
| `src/assets/progression/game.properties` | ID, 방, 오브젝트, 선행 조건, 보상 | 통합 담당자가 등록 |
| `src/timeescape/scene/HotspotCatalog.java` | 배경의 클릭 영역 | 새 클릭 지점이 필요할 때만 수정 |
| `src/assets/` | 이미지, 폰트, 소리, 설정 | 필요한 자산만 추가 |
| `test/` | 규칙과 입력, 완료 검증 | 새 검증 코드 추가 |

### 보통 수정하지 않는 파일

- `PuzzleRunner.java`: 중복 실행 방지와 완료 처리를 공통 관리합니다.
- `GameState.java`, `SaveStore.java`, `SaveQueue.java`: 기존 완료 ID와 보상 구조를 사용한다면 변경하지 않습니다.
- `GameFrame.java`: 기존 kind 등록 구조로 연결할 수 있으면 분기를 추가하지 않습니다.
- `PuzzleChrome.java`, `GameFonts.java`: 디자인 담당자가 관리합니다. 팀원은 호출해 사용합니다.
- `.project`, `.classpath`: 공통 Eclipse 설정입니다. 개인 작업 때문에 프로젝트 이름이나 라이브러리 경로를 바꾸지 않습니다.

## 3. 작업 전에 합의할 내용

아래 내용을 담당자에게 전달한 뒤 시작하세요.

| 항목 | 예시 |
|---|---|
| 미니게임 이름 | 주파수 맞추기 |
| 고유 단계 ID | PZ012, 사용 여부 확인 후 확정 |
| 실행 종류 kind | frequency |
| 방 | present_1 |
| 클릭 오브젝트 | 기존 MEMO 재사용 또는 신규 오브젝트 ID |
| 시작 조건 | PZ006 완료 |
| 조작 | 감소 버튼, 증가 버튼 |
| 완료 조건 | 목표 주파수 일치 |
| 나갈 때 | 미완료 상태는 버리고 다음 진입 시 초기화 |
| 완료 후 | 어떤 후속 단계가 열리는지 명시 |
| 필요한 자산 | 이미지나 효과음 목록 |

ID는 표시 순서와 다릅니다. order를 바꾸더라도 ID는 유지합니다. 배포 후 사용한 ID를 다른 퍼즐에 재사용하지 마세요.

## 4. 복사해서 시작하는 예제

예제 폴더: `docs/examples/frequency/`

| 예제 파일 | 복사 위치 | 역할 |
|---|---|---|
| FrequencyPuzzle.java | src/timeescape/puzzle/ | 0~10 눈금 조절, 목표 7, 초기화 |
| FrequencyPuzzlePanel.java | src/timeescape/ui/ | 공통 헤더와 버튼, 입력, 성공/닫기 콜백 |
| FrequencyPuzzleDialog.java | src/timeescape/ui/ | 장식 없는 모달 창, Esc, 결과 반환 |
| VerifyFrequencyExample.java | test/ | 규칙, 중복 완료, 취소, 헤드리스 화면 검사 |

예제의 숫자 맞추기는 구조 설명용입니다. 완성된 콘텐츠나 확정 기획이 아닙니다.

### 모델의 원칙

- Swing, 이미지, 오디오, 저장 코드에 의존하지 않습니다.
- 입력 함수, 현재 상태 조회, solved(), reset()을 제공합니다.
- 소수 주파수라면 0.1 단위를 정수 눈금으로 표현하면 부동소수점 정답 비교를 피할 수 있습니다.
- UI가 모델의 내부 배열이나 값을 임의로 바꾸지 않도록 합니다.
- 잘못된 입력과 범위 밖 입력을 어떻게 처리할지 정합니다.

### 패널의 원칙

- 모델을 생성하고 사용자의 입력을 모델 함수로 전달합니다.
- 정답이면 onSolved 콜백을 한 번만 호출합니다.
- 종료는 onClose 콜백으로 처리합니다. 종료를 성공으로 처리하지 않습니다.
- paintComponent에서 정답 판정, 파일 읽기, 소리 재생, 상태 변경을 하지 않습니다.
- 그림을 다시 그릴 때는 repaint()를 사용합니다.
- Swing 컴포넌트 변경은 EDT에서 수행합니다. 긴 계산과 파일 작업을 EDT에서 실행하지 않습니다.
- Timer나 작업 스레드를 추가하면 종료 시 반드시 정리하고 늦게 도착한 콜백을 무시합니다.

### 창의 원칙

- 기본 결과는 CANCELLED입니다. 정답 콜백에서만 SOLVED로 바꿉니다.
- 기존 PuzzleRunner는 모달 창이 닫힌 후 결과를 받습니다. 창만 비동기로 띄우고 바로 결과를 반환하지 마세요.
- setUndecorated(true)는 창이 표시되기 전에 호출합니다.
- 자체 닫기 버튼과 Esc를 연결합니다. System.exit()는 사용하지 않습니다.
- DeveloperMode.bind()는 임시 개발 빌드의 Shift+8+9+0을 연결합니다. 배선 퍼즐은 단계별 스킵을 사용합니다. 정식 배포 전 TEMPORARY_PUZZLE_CHEAT를 false로 변경하세요.

현재 RepairPuzzlePanel.build()는 시계 제목 여부에 따라 과거/미래 설명을 선택합니다. 새 퍼즐에 그대로 상속하면 잘못된 부제목이 나올 수 있습니다. 예제는 이 부분을 복사하지 않고 PuzzleChrome을 직접 사용합니다. 기존 수리 패널을 확장하려면 통합 담당자가 context를 인자로 받도록 일반화한 뒤 사용하세요.

## 5. 공통 디자인 사용 방법

예제는 실제 공통 클래스를 사용합니다.

```java
PuzzleChrome.decorate(this);
add(PuzzleChrome.header(
    "주파수 맞추기",
    "현재 / 연구실",
    "주파수를 조절해 신호를 맞추세요.",
    this::cancel
), BorderLayout.NORTH);
```

- 제목: GameFonts.title(size), Noto Serif KR SemiBold
- 설명과 작은 글씨: GameFonts.body(size), Pretendard Regular
- 버튼: GameFonts.button(size), Pretendard SemiBold
- 버튼 공통 스타일: PuzzleChrome.styleButton(button)
- 커서: GameCursors.normal(), GameCursors.interact()
- 문구 사이에 가운데점을 사용하지 않습니다. 문장, 줄바꿈, 슬래시를 사용합니다.
- 창 제목은 중앙에 두고 양옆에 얇은 선을 사용합니다. 황동 명판은 추가하지 않습니다.
- PuzzleChrome과 LabTheme은 현재 package-private입니다. 이를 쓰는 패널은 `package timeescape.ui;`에 두세요. 파일을 임의의 하위 패키지에 넣으면 접근 오류가 납니다.

## 6. 실행 등록: 통합 담당자

`src/timeescape/ui/PuzzleDialogs.java`의 launchers()에 아래 항목을 추가합니다. 기존 Map.of 마지막 항목 뒤 쉼표도 확인하세요.

```java
"frequency", step -> FrequencyPuzzleDialog.play(owner)
```

kind는 퍼즐 구현 종류입니다. 단계 ID와 같은 값일 필요는 없습니다. 같은 구현을 여러 단계에서 재사용하려면 step.id()나 설정을 창과 모델에 전달하도록 설계합니다.

Map.of는 최대 10개 항목까지 지원합니다. 등록 수가 이를 넘으면 Map.ofEntries(Map.entry(...), ...)로 변경하세요.

## 7. 진행 설정 등록: 통합 담당자

아래는 기존 MEMO를 재사용하는 예시이며 실제 기획 확정이 필요합니다.

1. `steps=` 목록 끝에 `PZ012`를 추가합니다. 목록을 통째로 교체하지 않습니다.
2. 다음 정의를 추가합니다.

```properties
step.PZ012.order=15
step.PZ012.name=주파수 맞추기
step.PZ012.room=present_1
step.PZ012.mission=M_USB
step.PZ012.object=MEMO
step.PZ012.kind=frequency
step.PZ012.requires=PZ006
step.PZ012.prompt=연구 메모의 단서를 이용해 주파수를 맞추세요.
```

이것만 추가하면 'PZ006 완료 후 수행 가능한 단계'가 생깁니다. **기존 Rush Hour의 선행 조건은 자동으로 변경되지 않습니다.**

주파수 퍼즐을 반드시 풀고 Rush Hour로 진행하게 하려면 다음도 변경합니다.

```properties
step.PZ001.requires=PZ012
```

반대로 독립적인 선택 퍼즐이라면 후속 단계 조건을 강제로 바꾸지 않습니다. 미션 소속, 완료 조건과 진행률 표시는 함께 검토합니다.

### 필드 의미

| 필드 | 뜻 |
|---|---|
| order | 표시와 단계 선택에 쓰이는 정렬 값. 선행 조건을 대신하지 않음 |
| requires | 먼저 완료해야 하는 단계 ID. 쉼표로 여러 개 지정하면 모두 필요 |
| items | 실행에 필요한 아이템 ID |
| consume | 완료 시 소비하는 아이템. items에도 포함되어야 함 |
| grant | 완료 시 지급하는 아이템 ID |
| flags | 완료 시 기록하는 환경 상태 |
| unlock | 완료 시 개방하는 방 ID |
| automatic | 조건 충족 시 자동 처리할 단계인지 여부 |

새 아이템은 `items=` 목록과 관련 정의에도 등록해야 합니다. 보관함 이름과 그림, 사용 처리도 확인하세요. 새 flag나 unlock을 적는 것만으로 새로운 배경 그림과 이동 화면이 자동 구현되지는 않습니다.

새 오브젝트는 HotspotCatalog에 추가하고 방의 실제 이미지 좌표에 맞춥니다. 같은 오브젝트에 여러 단계를 연결했다면 앞 단계 완료 이후 새 단계가 선택되는지도 확인하세요.

### 기존 저장 데이터 주의

완료한 ID와 아이템 중심의 저장 구조는 새 퍼즐마다 바꿀 필요가 없습니다. 다만 기존 단계 중간에 새 필수 퍼즐을 넣으면 이미 그 구간을 지난 저장 파일의 진행 규칙이 달라질 수 있습니다. 새 게임과 기존 저장 불러오기를 모두 확인하고, 기존 플레이어에게 새 퍼즐을 요구할지 별도로 결정해야 합니다. 저장 파일을 임의로 초기화하지 마세요.

## 8. 개발 역할과 Git 작업

| 담당 | 작업 범위 |
|---|---|
| 퍼즐 담당 | 새 모델, 새 패널, 새 창, 테스트, 전용 자산 |
| 디자인 담당 | PuzzleChrome, GameFonts, 공통 버튼과 프레임 |
| 통합 담당 | PuzzleDialogs, game.properties, HotspotCatalog, 저장 호환성 검토 |

새 패널의 시각 디자인을 따로 작업한다면 먼저 모델 함수 이름과 콜백 형태를 합의합니다.

- 공통 기준 브랜치의 최신 변경을 받은 뒤 `feature/frequency`처럼 작업 브랜치를 만듭니다.
- 브랜치에 모든 파일이 들어 있어도 문제없습니다. 실제 수정한 파일만 변경 사항으로 기록됩니다.
- 전달 전 `git status`와 `git diff --stat`로 불필요한 변경을 확인합니다.
- build, outputs, bin, 개인 세이브와 개인 도구 경로는 커밋하지 않습니다.
- 공유 파일은 통합 담당자에게 등록 내용으로 전달하거나 별도 커밋으로 분리합니다.
- Merge 충돌이 나면 기능과 디자인 양쪽을 살려 정리합니다. 파일 전체를 덮어쓰지 않습니다.
- 자동 병합 성공과 기능 정상 동작은 다릅니다. 반드시 컴파일과 검증을 수행합니다.

## 9. 검증 방법

JDK 21과 JAVA_HOME 설정이 필요합니다. 저장소 루트 PowerShell에서 실행합니다.

전체 게임 빌드만 수행하며 게임 창은 열지 않는 명령:

```powershell
& ./tools/build.ps1 -JavaHome $env:JAVA_HOME
```

예제를 src로 복사하지 않고 검증하는 명령:

```powershell
& ./docs/examples/frequency/verify.ps1 -JavaHome $env:JAVA_HOME
```

예제 검증은 프로젝트 소스와 예제를 별도 build 디렉터리에 컴파일합니다. 모델, 중복 완료, 취소 이후 완료 방지, 패널 헤드리스 렌더를 검사합니다. EXE와 JFrame/JDialog를 실행하지 않으며 사용자 저장 파일을 열지 않습니다. 예제를 src/test에 옮긴 뒤에는 중복 클래스가 되므로 이 예제 전용 스크립트 대신 정식 테스트 목록에 넣으세요.

### 팀원 제출 체크리스트

- [ ] 정답일 때만 성공하고 성공은 한 번만 전달됨
- [ ] 오답이면 계속 조작 가능
- [ ] 초기화가 모델과 화면을 함께 초기화함
- [ ] X, 조사 종료, Esc로 나가면 미완료로 처리됨
- [ ] 취소 이후 Timer나 지연 콜백이 완료를 전달하지 않음
- [ ] 퍼즐을 다시 열면 미완료 중간 상태가 초기화됨
- [ ] 선행 조건, 아이템 지급과 소비, 방 개방을 통합 담당자가 확인함
- [ ] 새 게임과 기존 저장 데이터에서 진행 가능
- [ ] 작은 창에서 버튼과 설명이 잘리지 않음
- [ ] 지정 폰트 사용, 가운데점 없는 문구, 공통 제목 디자인 적용
- [ ] 외부 이미지와 효과음의 출처 및 사용 조건 기록
- [ ] 변경 파일, 조작법, 완료 조건, 실행한 테스트, 미확인 항목을 전달

## 10. 전달 양식

```text
퍼즐 이름:
브랜치:
담당자:
단계 ID / kind:
추가 파일:
수정 파일:
조작 방법:
정답 조건:
선행 조건:
완료 보상과 후속 단계:
필요한 자산과 출처:
검증 명령과 결과:
실제 실행 확인 여부:
남은 문제:
```

현재 예제는 기능 구현의 출발점입니다. 게임 등록과 저장 조건 변경은 포함하지 않습니다.
