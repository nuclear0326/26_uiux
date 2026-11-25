# 수동 음원 제작

이 도구는 현재 게임 음원을 재구성하는 전체 배포 파이프라인이 아니라 합성 음원 초안을 만드는 제작 도구입니다.
기존 승인 음원에는 별도로 편집한 파일이 포함되어 있으므로 생성 결과로 일괄 교체하지 않습니다.

```powershell
python tools/authoring/generate_audio.py
```

Python과 NumPy가 필요합니다. 실행 위치와 무관하게 저장소 위치를 기준으로 경로를 계산합니다.
기본 출력은 `../26_uiux_artifacts/outputs/audio-authoring/<실행시각-고유번호>/`입니다.
매번 새 폴더를 만들며 WAV와 `audio_validation.json`을 같은 폴더에 기록합니다.
`--artifacts-root`로 외부 결과물 위치를 지정할 수 있지만 저장소 내부 출력은 거부합니다.
음원을 재생하지 않고 파일만 생성합니다. 생성 결과는 확인 후 필요한 파일만 별도로 게임에 적용합니다.
일반 빌드·패키징에서는 이 도구를 호출하지 않습니다.

안전성 검증: `python test/verify_audio_authoring.py`
현재 게임 음원 형식 검증: `python tools/verify_audio.py`
