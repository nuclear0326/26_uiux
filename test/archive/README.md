# 이전 화면 검증 보관

[테스트 안내](../README.md)

- [VerifyPresentPanelParity](VerifyPresentPanelParity.java)와 [LegacyPresentPuzzlePanel](LegacyPresentPuzzlePanel.java): 이전 헤더·배치 픽셀 비교.
- [ArchivedVerifyRepairPanels](ArchivedVerifyRepairPanels.java)와 [LegacyRepairPanel](LegacyRepairPanel.java): 분리 이전 기어·배선 UI 비교. 현재 기능 검사는 루트의 [VerifyRepairPanels](../VerifyRepairPanels.java)로 분리했습니다.
- [VerifyLabDesign](VerifyLabDesign.java): 과거 고정 사이드바와 contain 좌표 계산 기준.

패키지는 유지하고 이전 수리 비교 클래스만 이름을 바꿔 전체 컴파일 시 충돌을 방지했습니다.
파일은 이력 확인용이며 기본 suite에는 포함하지 않습니다. UI 차이가 의도된 것이므로 옛 픽셀 비교의 실패를 현 게임 버그로 해석하지 않습니다.
