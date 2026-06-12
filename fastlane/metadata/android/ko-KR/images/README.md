# Play Store 그래픽 에셋 (ko-KR)

Fastlane `supply` 가 이 폴더의 이미지를 업로드한다. 에셋 규격:

| 에셋 | 경로 | 크기 | 포맷 |
|------|------|------|------|
| 앱 아이콘 | `icon.png` | 512×512 | 32-bit PNG |
| 피처 그래픽 | `featureGraphic.png` | 1024×500 | PNG/JPG |
| 폰 스크린샷 | `phoneScreenshots/1.png` … | 1080×1920 (최소 320px) | PNG/JPG, 2~8장 |
| 7" 태블릿 | `sevenInchScreenshots/1.png` … | 선택 | |
| 10" 태블릿 | `tenInchScreenshots/1.png` … | 선택 | |

캡처할 스크린샷(온보딩/PRD §4 기준):
1. 홈 — 새 전사 시작 + 최근 프로젝트
2. 3단 세그먼트 뷰(EN / 한글 / KO) + 하이라이트
3. 플레이어 — A-B 반복, 배속, 필터 칩
4. 설정 — 번역 엔진 + 다크모드
5. 온보딩 — 온디바이스 프라이버시 페이지

> 바이너리 이미지는 기기 캡처/디자인 산출물로 생성하며 저장소에 커밋하지 않는다.
> `fastlane supply` 실행 전 이 폴더에 PNG 를 배치할 것.
