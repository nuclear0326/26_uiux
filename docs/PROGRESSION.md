# 진행 데이터 관리

기준: 2026-11-26 작업 트리 / v92. [실제 초반 흐름](OPENING_TIMELINE.md), [미니게임 제작](MINIGAME_TEAM_GUIDE.md).
이 문서의 소스 경로는 저장소 루트 기준입니다.

## 구성과 책임

| 위치 | 책임 |
|---|---|
| src/assets/progression/game.properties | 방·아이템·미션·단계의 고유 ID, 의존 조건, 보상 |
| ProgressCatalog | 정의 읽기 및 참조·스키마 검증 |
| GameState | 완료, 아이템, 미확인 표시, 환경 상태, 방 개방과 이동 |
| GameFrame | 클릭·아이템 사용 연결, 화면 갱신과 저장 요청 |
| PuzzleDialogs | kind에 따른 미니게임 창 등록 |
| PuzzleRunner | 실행 가능 여부, 중복 실행, 취소·완료와 세션 수명 관리 |
| SaveQueue / SaveStore | 비동기 저장과 복원 |

관리표 XLSX를 자동으로 읽는 기능은 없습니다. 확정된 기획은 game.properties에 반영합니다.

## ID와 선행 조건

ID는 배포 후 다른 의미로 재사용하지 않습니다. `order`는 표시·선택 순서이며 선행 조건을 대신하지 않습니다.
`requires`에 여러 ID가 있으면 모두 완료해야 합니다.
현재 USB 미션과 장치 수리 미션은 병렬이며 `GATE001.requires=PZ007,PZ011`이 과거 개방 조건입니다.
첫 사고 `ACT_START_FAILURE`는 현재에 남는 사건입니다. 취소된 `ACT_FIRST_TRAVEL`은 저장 호환을 위해 남지만 새 실행은 차단됩니다.

| 필드 | 의미 |
|---|---|
| order / name | 정렬 값과 표시 이름 |
| room / mission / object | 소속 방·미션·상호작용 오브젝트 |
| kind | present, rush_hour, gears, wires, temporary_power, confirm, action 등의 구현 종류 |
| requires / items | 완료 단계 조건 / 보유 아이템 조건 |
| consume / grant | 완료 시 아이템 소비 / 지급 |
| flags / unlock | 환경 상태 기록 / 방 개방 |
| automatic | 조건 충족 시 모델에서 자동 처리. collection과 gate 등에 사용 |
| prompt / itemPrompt | 다음 행동 / 부족한 아이템 안내 |

소비 아이템은 items에도 선언합니다. 완료 ID의 재처리로 보상을 중복 지급하지 않습니다.
현재 배전함 `PZ004`는 wires, 과거 `PZ002`는 gears, 미래 `PZ003`은 temporary_power입니다.
모델을 재사용하는 ID와 실제 완료 단계 ID를 구분합니다.

## 퍼즐과 방 확장

1. steps 목록과 고유 단계 정의를 추가합니다.
2. 새로운 구현은 모델·패널·창으로 분리하고 `PuzzleDialogs.launchers()`에 등록합니다.
3. 최종 완료만 SOLVED로 반환합니다. 패널에서 아이템 지급이나 저장을 직접 호출하지 않습니다.
4. 미션 완료 기준과 후속 단계 requires를 함께 검토합니다. 순서 번호만 바꾸어 필수 단계를 삽입할 수는 없습니다.
5. 새 클릭 영역은 HotspotCatalog, 아이템 그림·설명과 사용 처리는 해당 UI에 연결합니다.
6. 새 방은 rooms와 era/title/exits를 정의하고 unlock으로 개방합니다. 실제 이동은 enterRoom이 현재 방의 exits와 개방 여부를 검사합니다.

설정에 새 방을 적는 것만으로 고유 배경이나 이동 UI가 생성되지는 않습니다.
현재 기본 연결은 현재→과거→미래이며, 양방향 재방문과 숨겨진 방은 별도 구현·검증이 필요합니다.

## 저장과 완료 처리

Snapshot은 schemaVersion, roomId, completed, items, flags, unlockedRooms, unreadItems를 담는 불변 복사본입니다.
미완료 퍼즐의 세부 배치는 저장하지 않습니다. 상세 위치·복구 정책은 [저장 안내](SAVE_GUIDE.md)를 참고하세요.
새 게임은 `GameState.newGame()`을 사용합니다. 일반 생성자는 예전 진행과 저장 복원의 기준이므로 용도를 혼동하지 않습니다.
Rush Hour의 지연 완료는 `CompletionReservation`으로 취소·초기화·창 종료 후 콜백을 차단합니다.

현재 검증과 정답 데이터는 [테스트 안내](../test/README.md)에 있습니다.
