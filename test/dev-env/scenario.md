# dev-env 시험 시나리오

## 목적
Android dev 환경이 올바르게 구성되었는지 확인.

## 환경
- macOS Darwin 25.4.0
- JDK 25 (시스템) + Gradle 9.5.1 wrapper + KSP 2.1.0-1.0.29
- Android SDK: ~/Library/Android/sdk
- AGP 8.7.0, Kotlin 2.1.0, Compose BOM 2025.01.00

## 시험 항목

| # | 항목 | 명령 | 기대 결과 |
|---|------|------|-----------|
| S-1 | Gradle wrapper 존재 | `ls gradlew` | 파일 존재 |
| S-2 | `assembleDevDebug` 빌드 성공 | `./gradlew assembleDevDebug` | BUILD SUCCESSFUL, APK 생성 |
| S-3 | APK 생성 확인 | `ls app/build/outputs/apk/dev/debug/` | `app-dev-debug.apk` 존재 |
| S-4 | 에뮬레이터 AVD 존재 | `emulator -list-avds` | `Medium_Phone` 출력 |
| S-5 | `installDevDebug` 에뮬레이터 설치 | `./gradlew installDevDebug` (에뮬레이터 실행 후) | BUILD SUCCESSFUL |
