# v44 진행 데이터 관리

## 수정 위치

- `src/assets/progression/game.properties`: UTF-8 콘텐츠 정의. 퍼즐·미션·아이템·방의 고유 ID, 조건, 보상.
- `src/timeescape/core/ProgressCatalog.java`: 정의 읽기와 검증. 게임 시작 시 한 번 읽어 불변 데이터로 보관.
- `src/timeescape/core/GameState.java`: 현재 방, 완료 ID, 아이템 ID, 환경 플래그, 열린 방 ID만 관리.
- `src/timeescape/ui/GameFrame.java`: 오브젝트 ID → 현재 방의 단계 → kind에 맞는 퍼즐 화면 연결.
- `src/timeescape/ui/MissionPanel.java`: 미션 제목·단계 수·완료 수·목표 문구를 정의에서 조회.

기존 관리표를 기획 원본으로 유지할 수 있습니다. 현재 XLSX를 자동으로 읽는 기능은 없으므로 확정된 변경은 game.properties에 반영하고 재빌드해야 합니다.

## 순서와 의존성

`order`는 표시 순서입니다. 번호를 바꿔도 완료 ID나 선행 조건이 바뀌지 않습니다.
실제 플레이 순서는 `requires`로 지정합니다. 쉼표로 나열하면 모두 완료해야 합니다.
현재 두 미션은 서로 독립적이며, GATE001의 `requires=PZ007,PZ011`이 출발 조건입니다.
기존 PZ001 등의 ID는 유지했습니다. ID를 이름이나 표시 순서로 사용하지 마세요.

| 필드 | 뜻 |
|---|---|
| step.ID.order / name | 표시 순서 / 퍼즐 이름 |
| room | present_1, past_1, future_1 등 고유 방 ID |
| mission | 소속 수평 미션. 독립 퍼즐은 생략 |
| object | Hotspot의 ID. 보관함 장치는 DEVICE |
| kind | present, rush_hour, gears, wires, confirm, collection, action, gate |
| requires | 선행 완료 단계 ID 목록 |
| items | 완료에 필요한 보유 아이템 |
| consume / grant | 완료 시 소비 / 지급할 아이템 |
| flags | 완료 시 켜지는 환경·기믹 상태 |
| unlock | 완료 시 열리는 방 ID |
| automatic | 조건이 충족되면 자동 완료. 수집 단계·게이트에 사용 |
| prompt / itemPrompt | 다음 행동 안내 / 필요 아이템이 없을 때 안내 |

소비 아이템은 반드시 items에도 선언합니다. 같은 단계를 다시 완료해도 보상은 재지급되지 않습니다.
과거 톱니바퀴 PZ002와 미래 배선 PZ003도 현재 퍼즐과 같은 완료 ID 구조를 사용합니다.
전력 복구는 power_restored, 손목 장치 수리는 watch_repaired 등의 플래그로 관리합니다.

## 퍼즐·방 추가 절차

1. steps 목록에 새 고유 ID를 추가하고 step.ID.*를 작성합니다.
2. 미션에 넣으면 연구 기록의 단계 수와 완료 수에 자동 반영됩니다. 미션 마지막 조건을 바꾸려면 mission.ID.completion과 관련 게이트 requires도 명시적으로 수정합니다.
3. 기존 종류의 퍼즐은 kind로 연결합니다. 새로운 퍼즐 규칙·화면은 별도 모델/패널을 구현하고 GameFrame의 kind 분기에 등록합니다. present 종류는 PresentPuzzleModel/Panel에도 새 문제 정의가 필요합니다.
4. 배경에서 클릭할 위치는 HotspotCatalog에 등록합니다. 새 아이템의 그림·설명은 InventoryPanel에도 등록해야 합니다. 진행 데이터만 추가했다고 새 그림이나 클릭 영역이 생성되지는 않습니다.
5. 방은 rooms 목록과 room.ID.era/title/exits에 추가합니다. 입구를 여는 단계의 unlock에 방 ID를 넣습니다.
6. 실제 이동은 GameState.enterRoom(ID)를 호출합니다. 현재 방의 exits와 열린 방 목록을 모두 검사합니다. 돌아가기를 허용하려면 반대 방향 exits도 작성합니다.

현재 기본 데이터는 기존의 현재→과거→미래 이동을 유지합니다. 이번 변경은 재방문 UI를 추가하지 않습니다.
새 방의 고유 배경·Hotspot 배치는 별도의 화면 연결 작업이 필요하며, 현재 화면은 여전히 시간대별 배경을 사용합니다.

## 상태와 저장 범위

snapshot()은 schemaVersion, roomId, completed, items, flags, unlockedRooms의 변경 불가능한 복사본을 제공합니다.
아이템 소비와 환경 변화, 열린 방이 같은 상태 객체 안에서 함께 갱신됩니다.
v45에서는 SaveStore가 snapshot을 파일로 저장하고 GameState.restore로 복원합니다. 경로와 복구 정책은 SAVE_GUIDE.md를 참조하세요.

## Rush Hour 완료 예약

- 완료 후 기존 600ms 대기와 화면은 유지합니다.
- 초기화 버튼과 R 키는 동일한 resetPuzzle()을 호출합니다.
- 초기화 시 타이머와 드래그 상태를 해제합니다. dispose() 시 예약을 영구 취소합니다.
- CompletionReservation의 세대 번호로 이미 큐에 들어간 오래된 이벤트도 무시합니다.
- 완료 직전에 퍼즐 해답·완료 상태·창 생존 여부를 다시 검사합니다.
- 테스트는 가짜 스케줄러로 취소된 이벤트까지 직접 전달해 중복/지연 완료를 검사합니다. 게임 창을 띄우지 않습니다.

## 검증

VerifyCompletionReservation: 초기화, 닫기, 재도전, 중복 이벤트, 유효성 재검사, 실제 GameState 보상.
VerifyProgression: 두 미션 순서, 조건/보상/소비/해금, ID 유지, 표시 순서 변경, 테스트용 새 방과 퍼즐 추가, 잘못된 참조와 순환 조건 거부.
기존 VerifyMissions, VerifyNotebook, VerifyGameMenu도 헤드리스로 검사합니다.
