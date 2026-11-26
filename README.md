# TIME ESCAPE

Java 21, Swing, Java2D로 제작 중인 시간 테마 방탈출 게임입니다.
현재 배포 기준은 v99이며, 자세한 구현 현황은 [현재 진행상황](docs/PROJECT_STATUS.md)을 참고하세요.

## 실행

Windows 배포 ZIP 전체를 압축 해제한 뒤 `TimeEscape/TimeEscape.exe`를 실행합니다.
EXE 옆 `app`과 `runtime` 폴더가 필요합니다. 배포본은 Java 실행 환경을 포함합니다.
현재 배포 위치는 저장소 옆 `../26_uiux_artifacts/outputs/uix_v99_windows/TimeEscape`입니다.

## 개발과 빌드

Eclipse에서 Java 프로젝트로 열고 JDK 21 이상을 사용합니다. 원본은 `src`, Eclipse 컴파일 결과는 `bin`입니다.
저장소 루트 PowerShell에서 다음 명령으로 빌드·패키징합니다. 게임은 자동 실행하지 않습니다.

```powershell
& ./tools/build.ps1 -JavaHome "JDK21경로" -Version 99
python tools/package_windows.py --version 99 --base 98 --zip
```

패키징에는 `--base`에 해당하는 기존 배포본의 EXE와 runtime이 필요합니다.
새 환경에서는 기준 배포본을 먼저 외부 outputs에 준비해야 합니다.
`build`와 `outputs`는 저장소 밖 `<저장소명>_artifacts`에 생성합니다.
`build`는 성공한 최신 버전만 유지하고, 기존 배포본은 자동 삭제하지 않습니다.
`bin`, `build`, `outputs`는 Git에서 제외합니다. 상세 옵션은 [결과물 경로 안내](tools/ARTIFACTS.md)에 있습니다.

## 검증

```powershell
python test/run_tests.py --java-home "JDK21경로"
```

검증은 게임 창 없이 수행하고 사용자 저장소와 음량 설정을 격리합니다.
범위와 별도 제작 도구 검사는 [테스트 안내](test/README.md)를 참고하세요.

## 주요 조작과 기능

- 배경 오브젝트 클릭으로 조사하고, 연구 노트에서 목표와 아이템을 확인합니다.
- Shift를 누르면 진행 상태에 맞는 상호작용 위치를 강조합니다.
- ?는 도움말, 햄버거 버튼과 Esc는 게임 메뉴를 엽니다. 열린 노트·메뉴에서는 Esc가 닫기·뒤로 가기로 동작합니다.
- 게임 메뉴 순서: 저장, 불러오기, 설정, 메인 메뉴.
- 자동저장 1개와 수동 저장소 4개를 사용합니다. [저장 안내](docs/SAVE_GUIDE.md)
- 설정에서 배경음악, 환경음, 효과음을 각각 조절하고 종료 후에도 유지합니다.
- 개발용 퍼즐 스킵은 [임시 단축키 안내](docs/TEMPORARY_CHEAT.md)를 참고하세요.

## 문서

[현재 문서 목록](docs/README.md) / [진행 데이터](docs/PROGRESSION.md) / [미니게임 제작](docs/MINIGAME_TEAM_GUIDE.md)

[제작 도구](tools/README.md) / [디자인 원본](design/README.md) / [변경 이력](CHANGELOG.md) / [보관 기록](docs/archive/README.md)
