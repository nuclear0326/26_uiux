> 과거 작업 기록입니다. 현재 상태는 [진행상황](../../PROJECT_STATUS.md), 사용 안내는 [문서 목록](../../README.md)을 참고하세요. 당시 검증 결과와 기획을 보존하며 최신 상태를 의미하지 않습니다.

# Shift 효과 오픈소스 검토
확인일: 2026-11-19

1. Effekseer (추천 경로)
https://github.com/effekseer/Effekseer
저장소 표기 MIT. 파티클 효과 제작 도구.
https://effekseer.github.io/Help_Tool/en/ToolReference/record.html
공식 Recorder는 투명 배경의 스프라이트 시트/연속 이미지를 출력한다.
판단: 효과를 제작·출력한 뒤 Java2D로 프레임을 재생하면 Swing 구조를 유지할 수 있다.
직접 Swing 런타임 연동이 제공된다는 뜻은 아니다.

2. libGDX Particle Editor / ParticleEffect
https://libgdx.com/wiki/graphics/2d/2d-particleeffects
https://libgdx.com/funding/
Apache 2.0. 에디터, 입자 수명/블렌딩, 풀링 지원.
판단: libGDX의 Texture/Batch 경로에 맞춰진 효과이므로 기존 Java2D 화면의 간단한 교체 부품은 아니다.

3. Talos
https://github.com/rockbite/talos
Apache 2.0. 노드 기반 VFX 편집기와 libGDX 런타임 제공.
판단: 복잡한 효과 편집에는 적합하지만 현재 Swing 프로젝트에 즉시 통합하기보다는 렌더러 연동 검토가 필요하다.

권장 디자인: 큰 원형 점을 줄이고 가장자리가 흐린 작은 광점, 짧은 수명, 느린 이동,
약한 밝기 변화로 오브젝트 실루엣 안에서만 나타나게 한다.
v20: 공식 Effekseer 1.80.8 도구로 자체 효과를 제작하고 Recorder 출력물을 사용했습니다. 게임 배포물에는 자체 출력 이미지와 Java 재생 코드만 포함합니다.
