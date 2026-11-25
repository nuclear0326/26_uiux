# 개발 도구

| 경로 | 역할 | 일반 빌드 사용 |
|---|---|---|
| build.ps1 | Java 컴파일, 효과 데이터 사전 생성, JAR 제작 | 사용 |
| package_windows.py | 기존 실행 런타임을 이용한 EXE 패키지와 ZIP 제작 | 사용 |
| clean_build.ps1 | 성공한 최신 빌드 유지, 이전 결과 정리 | 사용 |
| weather/ | 비·번개·균열 생성기와 생성에 필요한 Legacy 구현 | 사용 |
| verify_audio.py | 현재 WAV 형식 검사, 재생 없음 | 수동 검증 |
| [authoring](authoring/README.md) | 합성 음원 초안 생성 | 자동 호출하지 않음 |

효과 편집 원본은 [design](../design/README.md)에 있습니다.
이전 tools/generate_audio.py는 tools/authoring/generate_audio.py로,
이전 tools/shift-effekseer는 design/effects/shift-effekseer로 이동했습니다.
일반 빌드가 수동 제작 음원으로 게임 자산을 덮어쓰지 않도록 경로와 실행 단계를 분리했습니다.

[빌드·배포 경로](ARTIFACTS.md) / [테스트 안내](../test/README.md)
