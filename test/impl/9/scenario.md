# 구현 9차 시험 시나리오 — E9 완성도/배포

**날짜**: 2026-06-12
**Epic**: E9 — 완성도/배포 (i18n + 다크모드 3모드 + 접근성 + adaptive icon + Play Store)
**PRD 항목**: F-19 완성도, F-21 Play Store 제출 준비, §7.6 접근성(KWCAG/WCAG)

## 구현 범위

| 파일 | 설명 |
|------|------|
| `res/values/strings.xml` | 영어 기본 문자열(탭/홈/필터/설정/알림/접근성) |
| `res/values-ko/strings.xml` | 한국어 문자열 오버라이드 |
| `core/ui/SegmentAccessibility.kt` | 세그먼트 결합 contentDescription(타임코드+EN+한글+KO) |
| `ProjectDetailScreen.kt` | 세그먼트 카드에 `semantics{contentDescription}` 적용 |
| `fastlane/metadata/android/{en-US,ko-KR}/` | Play Store 리스팅(제목/짧은설명/전체설명) |
| `cpp/CMakeLists.txt` + `whisper_jni_stub.cpp` | whisper.cpp 소스 부재 시 JNI 스텁 빌드 → APK 조립 가능 |

## 시험 항목

### SegmentAccessibilityTest (4)
| # | 케이스 | 기대 |
|---|--------|------|
| T-01 | describe 전체 라인 결합 | "00:00, Hello, 헬로, 안녕" |
| T-02 | 빈 라인 스킵 | "01:05, Hi, 안녕" |
| T-03 | 전부 빈 라인 | 타임코드만 |
| T-04 | timecode 분:초 포맷 | 00:00/00:05/01:05/10:00 |

### 전체 통합 (assembleDevDebug)
- 전체 모듈 컴파일 + 리소스 병합(en/ko) + 네이티브 스텁 빌드 + APK 패키징.
- 전체 단위시험 208 케이스 회귀.

## 시험 명령

```bash
./gradlew assembleDevDebug testDevDebugUnitTest
```

## 성공 기준

- assembleDevDebug 성공 → `app-dev-debug.apk` 생성
- 전체 208 케이스 failures=0
- i18n en/ko 리소스 병합 성공
