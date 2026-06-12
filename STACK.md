# STACK.md — as-it-sounds-android

> 생성: `wise-dev-standard` 플러그인 / 프로파일 `android-compose` / 도메인 `edtech`
> 갱신: `/wise-dev-standard:standardize`

## 현재 스택

| 항목 | 선택 | 근거 |
|------|------|------|
| 종류 | Android 네이티브 앱 (서버 없음, 온디바이스) | PRD: 서버 비용 0 + 프라이버시 |
| 언어 | Kotlin | Android 공식 1순위 언어 |
| UI | Jetpack Compose (Material 3) | 최신 Android 표준, 선언형 UI |
| 상태 관리 | ViewModel + StateFlow (MVVM) | Compose 공식 권장 패턴 |
| 내비게이션 | Navigation Compose | 단일 NavHost, sealed route |
| ASR | whisper.cpp Android JNI | 온디바이스, MIT, ggml 모델 공유 |
| 오디오 디코드 | MediaExtractor + MediaCodec | ffmpeg 미사용, 순수 Android API |
| 미디어 재생 | ExoPlayer (Media3) | A-B반복·배속·시크 지원 |
| 번역 (기본) | ML Kit Translation | 온디바이스, 무료, iOS Apple Translation 대응 |
| 번역 (폴백) | MyMemory → LibreTranslate → DeepL → 끄기 | 폴백 체인 |
| 로컬 DB | Room (SQLite) | iOS GRDB 스키마 1:1 대응 |
| 설정 영속 | DataStore (Preferences) | iOS UserDefaults 대응 |
| 백그라운드 | WorkManager (CoroutineWorker) | iOS BGProcessingTask 대응 |
| DI | Hilt | Compose 공식 DI |
| 시크릿 | Android Keystore + EncryptedSharedPreferences | iOS Keychain 대응. 평문/로그 금지 |
| 빌드 | Gradle KTS + libs.versions.toml | Version Catalog, Gradle Wrapper 커밋 |
| 플레이버 | dev / staging / prod | iOS Config/*.xcconfig 대응 |
| CI | GitHub Actions (ubuntu-latest) | 서명키 CI secret 주입 |
| 배포 | Fastlane supply → Play Store AAB | iOS Fastlane match+pilot 대응 |
| 테스트 | JUnit5 + Turbine + Compose UI Test | test/impl/<Nth>/ 증빙 |

## 모바일 규칙

- compose/Docker/서버 DB(postgres) **미사용**. 온디바이스 전용.
- 사용자 파일은 `filesDir` 내부 사본만 (외부 경로 저장 금지).
- 모델은 `noBackupFilesDir/models/` (`WorkManager` 다운로드, 번들 미포함).
- 시크릿은 코드/로그 하드코딩 금지 — Android Keystore 주입.
- 외부 전송은 번역 API 호출 한정. PII 로깅 금지.

## 코드 생성 정책

1. Android 전용 — iOS/크로스플랫폼/서버 코드 생성 금지.
2. UI = Jetpack Compose, 라우팅 = sealed route + `NavHost`.
3. 비동기 = Kotlin Coroutines (`suspend`/`Flow`). DB 접근은 `Dispatchers.IO`.
4. 음차는 **재구현 금지** — `TranslitEngine` 사용. 변경 시 iOS fixture 패리티 재확인.
5. 외부 의존 추가는 `libs.versions.toml` + 사용자 승인.

## iOS → Android 기술 매핑

| iOS | Android |
|-----|---------|
| WhisperKit (Core ML) | whisper.cpp Android JNI |
| AVFoundation | MediaExtractor + MediaCodec |
| SwiftUI @Observable | Jetpack Compose + StateFlow |
| GRDB.swift | Room |
| BGProcessingTask | WorkManager CoroutineWorker |
| Keychain | Android Keystore + EncryptedSharedPreferences |
| iCloud CloudKit | 미결정 (Google Drive 또는 로컬 전용) |
| Live Activity | Foreground Service + Notification |
| Share Extension | Intent filter ACTION_SEND |
| Apple Translation | ML Kit Translation |
