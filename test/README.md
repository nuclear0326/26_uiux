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
실행은 `suite.txt`에 명시한 33개 검증만 수행합니다. 각 프로세스는 headless 모드이며 저장·음량 설정은 임시 경로로 격리됩니다.
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
