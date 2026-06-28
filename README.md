# as-it-sounds-android

Jetpack Compose 기반 Android 네이티브 앱 — 어떤 언어의 오디오/비디오든 온디바이스로 전사하여 한글 음차와 한국어 번역을 함께 보여줍니다.

> **스캐폴딩 정보**: `wise-dev-standard` 플러그인 / 프로파일 `android-compose` / 도메인 `edtech` 으로 생성됨.

## 기술 스택

| 구분 | 도구 |
|------|------|
| 언어 | Kotlin |
| UI 프레임워크 | Jetpack Compose (Material 3) |
| 상태 관리 | ViewModel + StateFlow (MVVM) |
| 내비게이션 | Navigation Compose |
| 로컬 DB | Room (SQLite) |
| 설정 영속 | DataStore (Preferences) |
| ASR | whisper.cpp Android JNI (ggerganov/whisper.cpp) |
| 오디오 디코드 | MediaExtractor + MediaCodec → 16kHz mono |
| 미디어 재생 | ExoPlayer (Media3) |
| 번역 | ML Kit Translation (온디바이스) + MyMemory / LibreTranslate / DeepL |
| 백그라운드 | WorkManager (CoroutineWorker) |
| DI | Hilt |
| 빌드 시스템 | Gradle (Kotlin DSL) + Version Catalog |
| 최소 SDK | API 26 (Android 8.0) |
| 배포 자동화 | Fastlane (supply) |
| 배포 채널 | Play Store internal → production |
| CI/CD | GitHub Actions |

## 사전 요구사항

- **Android Studio Meerkat 이상** (AVD Manager + SDK 포함) — 권장
- **JDK**: 시스템 JDK 25/26 사용 가능 (Gradle 9.5.1 + KSP 조합으로 호환 확인됨)
  - ⚠️ `JAVA_HOME` 이 존재하지 않는 경로를 가리키면 빌드 실패. 아래 트러블슈팅 참조.
- **Android SDK**: `~/Library/Android/sdk` (Android Studio 설치 시 자동)
- Ruby & Bundler (`gem install bundler`) + Fastlane (`bundle install`) _(배포 시)_
- Google Play 개발자 계정 + 서비스 계정 JSON _(Play Store 배포 시)_

## 빠른 시작

### 1단계 — 에뮬레이터 실행

`make dev` 는 연결된 기기/에뮬레이터에 APK를 설치한다. **반드시 먼저 에뮬레이터를 실행해야 한다.**

```bash
# 방법 A: 터미널에서 직접 (백그라운드)
~/Library/Android/sdk/emulator/emulator -avd Medium_Phone &

# 방법 B: Android Studio
# Device Manager → AVD 목록 → ▶ 실행
```

에뮬레이터가 없으면 Android Studio → **Device Manager → Create Device** 로 먼저 생성.

사용 가능한 AVD 목록 확인:
```bash
~/Library/Android/sdk/emulator/emulator -list-avds
```

### 2단계 — 빌드 및 설치

```bash
# 에뮬레이터 부팅 완료 후:
make dev     # 빌드 + 에뮬레이터/기기에 설치
```

에뮬레이터 없이 APK만 빌드:
```bash
./gradlew assembleDevDebug
# 결과물: app/build/outputs/apk/dev/debug/app-dev-debug.apk
```

Android Studio 열기: `File > Open > as-it-sounds-android`

## 트러블슈팅

### `JAVA_HOME is set to an invalid directory` 에러

```
ERROR: JAVA_HOME is set to an invalid directory: /path/to/nonexistent/jdk
```

`~/.zshrc` (또는 `~/.bashrc`) 의 `JAVA_HOME` 이 존재하지 않는 경로를 가리킬 때 발생.

```bash
# 설치된 JDK 목록 확인
/usr/libexec/java_home -V

# ~/.zshrc 에서 존재하는 경로로 수정 (예시)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home

# 적용
source ~/.zshrc
```

### `No connected devices!` 에러

```
DeviceException: No connected devices!
```

에뮬레이터 미실행 또는 부팅 미완료. 1단계(에뮬레이터 실행)를 먼저 수행할 것.

연결 기기 확인:
```bash
~/Library/Android/sdk/platform-tools/adb devices
```

### Gradle + JDK 25/26 호환성 메모

본 프로젝트는 `kapt` 대신 **KSP** (Kotlin Symbol Processing) 를 사용한다.  
`kapt` 는 JDK 25+ 에서 `javacOptions` 파싱 버그가 있으며, KSP 로 마이그레이션하여 해결함.  
Gradle wrapper 버전: **9.5.1** (임베디드 Kotlin이 JDK 25 버전 문자열 파싱 지원).

## Make 사용법

| 명령 | 설명 |
|------|------|
| `make dev` | dev 플레이버 디버그 빌드 및 설치 (`./gradlew installDevDebug`) |
| `make test` | 단위 + 인스트루멘테이션 테스트 |
| `make build` | prod 플레이버 릴리스 번들 생성 (`./gradlew bundleProdRelease`) |
| `make deploy` | Fastlane `beta` lane — Play Store internal 트랙 업로드 |

### Gradle 직접 사용

```bash
./gradlew tasks --all
./gradlew installDevDebug
./gradlew testDevDebugUnitTest
./gradlew connectedDevDebugAndroidTest
./gradlew bundleProdRelease
./gradlew lintDevDebug
```

## 빌드 플레이버

| 플레이버 | 용도 | API Base |
|----------|------|----------|
| `dev` | 로컬 개발 / 에뮬레이터 | `http://10.0.2.2:8000` |
| `staging` | QA / 내부 테스트 | `https://stg.api.example` |
| `prod` | Play Store 제출 | `https://api.example` |

## 환경 변수

| 파일 | 용도 |
|------|------|
| `.env.local` | 로컬 개발 (git 제외) |
| `.env.dev` | Dev 환경 |
| `.env.staging` | Staging 환경 |
| `.env.prod` | Production 환경 |

시크릿(키스토어 / Play JSON / API 키)은 **절대 커밋 금지** — CI 시크릿 등록.

## 빌드 & 배포

```bash
make build
# 결과물: app/build/outputs/bundle/prodRelease/app-prod-release.aab

make deploy
# 또는: cd fastlane && bundle exec fastlane android beta
```

CI Repository Secrets 등록 목록:
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
- `PLAY_STORE_JSON_KEY`

## 구현 현황

전 Epic(E0~E9) 구현 완료. 단위시험 **208/208 PASS**, `assembleDevDebug` APK 조립 성공.

| Epic | 항목 | 상태 | 단위시험 |
|------|------|------|----------|
| E1 | TranslitEngine (음차) | ✅ 완료 | 27 (iOS fixture 100% 패리티) |
| E2 | ASR (whisper.cpp JNI) | ✅ 완료 | 33 (AudioDecoder·ModelManager·RamGuard·JNI 파싱) |
| E3 | 파이프라인 | ✅ 완료 | 14 (PipelineOrchestrator·체크포인트) |
| E4 | UI 셸 (3탭 적응형) | ✅ 완료 | 33 (Library 필터·Player 로직·Navigation) |
| E5 | 파일 입수 | ✅ 완료 | 25 (ImportLogic·ShareIntake) |
| E6 | 번역 4종 | ✅ 완료 | 33 (4 백엔드 파싱·폴백·Factory) |
| E7 | 영속/내보내기 | ✅ 완료 | 19 (SubtitleExporter·Converters, Room KSP) |
| E8 | 플랫폼 통합 | ✅ 완료 | 20 (RollingBuffer·Notification·Shortcuts) |
| E9 | 완성도/배포 | ✅ 완료 | 4 (Accessibility) + i18n·Play 리스팅 |
| F1 | **전사 진행률 표시** | ✅ 완료 | `TranslationProgress` PipelineEvent + `LibraryScreen` 인디케이터 |
| F2 | **재시도/재개** | ✅ 완료 | `LibraryViewModel.retryProject()` + "재개" 버튼, WorkManager 체크포인트 이어쓰기 |
| F3 | **고품질 모델** | ✅ 완료 | `MEDIUM`(1.5GB) + `LARGE_V3_TURBO`(809MB), RAM 가드 |

UI 보강·검증은 10~14차에서 진행(실제 whisper.cpp 빌드, 계측시험, 온보딩/설정 iOS 패리티,
아이콘화, 릴리스 lint). 상세: `test/impl/<N>/scenario.md` + `result.md` (1~14차).

> **네이티브 빌드**: 기본은 JNI 스텁(소스 부재 시)으로 APK 조립. 실제 ASR 은
> `bash scripts/setup_whisper.sh` (whisper.cpp v1.7.4 clone) 후 `./gradlew assembleDevDebug`.
> → `libwhisper.so` + `libwhisper_jni.so`(arm64-v8a) 동봉. 검증 완료(10차).

### 계측시험 (에뮬레이터)

```bash
# 에뮬레이터 실행 후
./gradlew connectedDevDebugAndroidTest   # Room DAO·CASCADE 계측 5/5 PASS
```
Compose UI 계측은 현 emulator(API 36)에서 espresso 비호환(`InputManager.getInstance` 제거) — 로직은 단위시험으로 커버.

### UI 구성 (iOS 패리티)

- **온보딩 4장**: 첫 실행 · 앱 업데이트 · **개발 재설치 시마다** 재노출(`lastUpdateTime` 게이트).
- **4탭**: 홈 / 라이브러리 / 프로젝트 / 설정 (실제 Material 아이콘, Phone 하단바 · Tablet Rail).
- **홈**: 마이크(→ 실시간 전사 화면, RECORD_AUDIO) · 설정 · 파일 가져오기 · 음성 인식 모델 카드 · 최근 프로젝트.
- **프로젝트 상세**: 3단 세그먼트(EN / 한글음차(파란색) / KO), 재생 위치 하이라이트, 필터 칩(모두/영문/한글발음/번역), `ArrowBack` 아이콘 뒤로가기.
- **설정**(iOS 패리티, 스크롤): 모델 · 번역 · 동기화 · 학습 환경 · 정보 섹션.
  - 모델 관리(tiny/base/small, 다운로드 아이콘) · 실제 음성 인식 토글(Mock↔real)
  - 번역 엔진(ML Kit/MyMemory/LibreTranslate/DeepL/끄기) · 폴백 체인
  - Google Drive 백업(Auto Backup) · 자동 재생 · 기본 속도(±아이콘) · **화면 모드(드롭다운 셀렉트박스)**
  - 서브화면: **접근성** · **앱 정보**(버전/빌드/앱 ID) · **오픈소스 라이선스**
- **온보딩 4장**: 히어로 이모지·말풍선·기능 리스트·3단 미리보기·언어칩·모델 다운로드 카드·페이지 닷.
- **아이콘화**: 모든 뒤로가기 = `ArrowBack`, 다운로드 = `Download`/`CloudDone`, 속도 = `Remove`/`Add`.
- **개발용 데모**: 홈 → 파일 가져오기 → **데모: 전사 시작 (Mock ASR)** — 파일 없이 합성 세그먼트(실제 TranslitEngine 음차) 즉시 시연. debug 빌드에서만 노출.
- **릴리스**: `./gradlew lintVitalProdRelease` 통과(백업 규칙 정리). Auto Backup = DB·설정·imports 만, 모델·키 제외.

### 에뮬레이터에 실제 파일 넣기 (macOS)

실제 "Files 에서 가져오기" 테스트는 [docs/EMULATOR_FILES.md](docs/EMULATOR_FILES.md) 참조.
요약: `adb push ~/Music/sample.mp3 /sdcard/Download/` 후 앱에서 Files 로 선택.

## 테스트

```bash
# 전체 단위 테스트 (208 케이스)
./gradlew testDevDebugUnitTest

# 특정 모듈
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.translit.*"
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.asr.*"

# APK 조립
./gradlew assembleDevDebug
```

## 디렉터리 구조

```
as-it-sounds-android/
├── app/
│   ├── src/
│   │   ├── main/java/com/asitsounds/android/
│   │   │   ├── feature/   {home, library, project, settings, onboarding}
│   │   │   ├── core/      {ui, data, domain, common}
│   │   │   ├── translit/  TranslitEngine (Kotlin 음차)
│   │   │   ├── asr/       WhisperJni 래퍼, ModelManager, AudioDecoder, RollingBuffer
│   │   │   ├── translate/ TranslatorInterface + 4 구현체 + Factory/폴백
│   │   │   ├── pipeline/  PipelineOrchestrator, TranscriptionWorker
│   │   │   └── platform/  ForegroundService, Scheduler, Shortcuts, 실시간 STT
│   │   ├── main/cpp/      CMakeLists + whisper_jni(.cpp/스텁)
│   │   ├── main/res/
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── fastlane/
│   ├── Fastfile
│   └── Appfile
├── .github/workflows/ci.yml
├── .docs/PRD.md
├── test/
│   ├── dev-env/           환경 검증 증빙
│   └── impl/              구현 차수별 증빙 (<Nth>/scenario.md, result.md)
├── build.gradle.kts
├── settings.gradle.kts
├── Makefile
├── STACK.md
└── COMPLIANCE.md
```
