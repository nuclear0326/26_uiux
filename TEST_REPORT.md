# v61 현재 미니게임 모델 분리

작업 경로: C:/kunsanu/262/git/26_uiux

## 구조
src/timeescape/puzzle/present:
- PresentPuzzle: ID/값 조회/조작/정답/제목/안내/라벨/상태 문구의 공통 인터페이스.
- CyclicValuesPuzzle: 내부 배열 캡슐화, 인덱스 검증, 순환 증가, 방향 표시, 정답 배열 비교. 연동 회전은 afterTurn 훅으로 확장합니다.
- PowerDistributionPuzzle(PZ004): 스위치별 전력 합계와 목표 16 판정.
- ArchiveCodePuzzle(PZ006): 네 자리 숫자와 암호 판정.
- SchematicPuzzle(PZ008): 설계도 조각 방향과 정렬 판정.
- ModuleAssemblyPuzzle(PZ010): 커넥터 방향과 조립 판정.
- TimeCoordinatesPuzzle(PZ011): 누른 링과 다음 링의 연동 회전 및 좌표 판정.
- PresentPuzzles: 안정적인 기존 진행 ID로 새 모델 인스턴스를 생성하는 등록표.

PresentPuzzlePanel은 공통 인터페이스를 사용하고 초기화 시 새 모델을 생성합니다. 상태 문구도 모델에서 제공합니다. 퍼즐별 그림 선택은 UI에 그대로 유지합니다. 기존 PresentPuzzleModel은 외부/기존 테스트 호출을 위한 위임 어댑터로 남기며 중복 규칙은 없습니다. power()는 전력 퍼즐에서만 유효합니다.

현재/과거/미래 진행 ID, 보상, 선행 조건, 저장 형식, 공통 완료·저장 경로는 변경하지 않았습니다. 폰트/이미지/음원/기어/배선 패널도 그대로입니다.

## 검증
- JDK 21 빌드 및 날씨 생성 검증 성공.
- 이전 v60 모델과 10,416개 조작 횟수 조합 및 각 다음 입력을 비교. 값, 라벨, 제목/안내, 정답, 전력, 인스턴스 독립성, 잘못된 인덱스, 호환 어댑터를 포함해 260,121개 비교 통과.
- 패널 검증 40개: 퍼즐 5개의 초기/축소/오답/완료/초기화 25개 화면이 이전 패널과 픽셀 동일. 완료/취소 콜백 검증.
- 기존 미션 44개, 공통 퍼즐 실행 63개, 저장소 20개, 저장/복구 45개 통과.
- assets, core, system 패키지, PuzzleRunner, GameFrame, 기어/배선 패널은 v60 소스 ZIP과 바이트 동일.

게임 Main/JFrame/JDialog/EXE 및 실제 오디오 장치를 실행하지 않았습니다. 실제 네이티브 창의 조작은 실행 검증하지 않았으며 모델과 Swing 패널을 헤드리스로 검증했습니다. 사용자 세이브를 건드리지 않았습니다.

배포: v60 런처/runtime 재사용 + v61 JAR/설정 교체. Git 커밋/푸시는 하지 않았습니다.
