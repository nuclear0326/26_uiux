> 과거 작업 기록입니다. 현재 상태는 [진행상황](../../PROJECT_STATUS.md), 사용 안내는 [문서 목록](../../README.md)을 참고하세요. 당시 검증 결과와 기획을 보존하며 최신 상태를 의미하지 않습니다.

# 현재 작업: 새 시나리오 초반 이동 (2026-11-25)

새 게임은 켜진 현재 연구실에서 시작한다. 왼쪽 보관 상자에서 시간이동장치를 획득하고 연구 노트의 보관함에서 사용하면, 고장 상태를 기록하며 과거로 즉시 이동한다. 소등과 시간이동 연출은 아직 적용하지 않았다. 상세 범위와 검증: [INTRO_SEQUENCE.md](../cancelled/INTRO_SEQUENCE.md).

첫 시작에는 제목 없는 시네마형 대화창으로 3페이지 안내를 표시한다. 문장은 `src/assets/dialogue/ko.txt`에서 관리한다. 배포 시 `app/dialogue/ko.txt`로 복사해 코드 재빌드 없이 편집할 수 있다. [대사 편집 안내](../../DIALOGUE_GUIDE.md). VerifyDialogue 14개 및 VerifyIntroSequence 29개 검증 통과. 1500x880, 1020x650 화면을 headless 렌더링해 확인했으며 게임 창은 실행하지 않았다.

직전 배포 기준은 v82다. 이번 작업은 소스 반영 및 headless 검증이며 기존 EXE는 재배포하지 않았다.

v82: Startup audio recovery fixed. 15 native EXE runs passed, including failure recovery and audio-driven intro checks. See docs/archive/history/AUDIO_RECOVERY_V82.md for evidence and limitations.


## v83 첫 시간이동 연출
확정한 v6 혼합 음원과 소등 및 과거 전환을 최초 장치 사용에 연결했다. 상세 타임라인과 검증은 docs/archive/cancelled/FIRST_TRAVEL_EFFECT.md 참조.


## v84 revised opening
First activation fails and remains in the unpowered present. Restore power, obtain/analyze logs and repair the device before past travel. See docs/OPENING_TIMELINE.md.
