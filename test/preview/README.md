# 미리보기와 측정 도구

[테스트 안내](../README.md)

- PreviewCursor64: 현재 커서 두 상태 합성. 중복 PreviewCursor63을 통합했습니다.
- PreviewNotebook42: 노트 페이지 전환 프레임 생성.
- PreviewPuzzle65: 공통 미니게임 화면 생성.
- VerifyFontRoles: 폰트 적용 화면 생성.
- VerifyShiftGlint: Shift 빛 효과 프레임 생성과 간단한 수명 확인.
- PreviewLabWeather: 현재 날씨 검증 구현을 재사용해 프레임 생성.
- WeatherStartupProbe: 메뉴 준비·그리기 시간과 선택적 픽셀 해시 기록. 출력 폴더를 인자로 지정합니다.

자바 패키지와 클래스 이름은 유지했습니다. 소스 경로만 이 폴더로 변경되었습니다.
컴파일은 test/run_tests.py에 포함되지만 기본 실행 목록에서는 제외됩니다.
기본 출력은 ArtifactPaths를 통해 외부 outputs에 저장하며, 출력 폴더 인자가 있는 도구도 외부 경로를 지정하세요.
회귀 검증과 이미지 생성은 목적이 다르므로 미리보기 생성만으로 기능 정상 여부를 판단하지 않습니다.
