# dev-env 시험 결과

**판정: PASS**
**날짜: 2026-06-12**

## 결과 상세

| # | 항목 | 결과 | 비고 |
|---|------|------|------|
| S-1 | Gradle wrapper 존재 | ✅ PASS | `gradle wrapper --gradle-version 9.5.1` 로 생성 |
| S-2 | `assembleDevDebug` 빌드 성공 | ✅ PASS | `BUILD SUCCESSFUL in 1m 23s` |
| S-3 | APK 생성 확인 | ✅ PASS | `app-dev-debug.apk` 생성 확인 |
| S-4 | 에뮬레이터 AVD 존재 | ✅ PASS | `Medium_Phone` AVD 확인 |
| S-5 | `installDevDebug` 에뮬레이터 설치 | ✅ PASS | `BUILD SUCCESSFUL` |

## 주요 해결 이슈

- **JDK 25 + kapt 호환 버그** → `kapt` 제거, **KSP 2.1.0-1.0.29** 로 전환
- **Gradle wrapper 미생성** → `gradle wrapper --gradle-version 9.5.1` 실행
- **SDK location 미설정** → `local.properties` 에 `sdk.dir` 추가
- **AndroidManifest.xml 누락** → 매니페스트 + 리소스 + Application 클래스 직접 생성
- **JAVA_HOME 잘못된 경로** → `source ~/.zshrc` 로 환경변수 적용

## 특이사항

에뮬레이터는 Claude 세션에서 백그라운드로 유지 불가.
`installDevDebug` 실행 전 반드시 터미널에서 수동으로 에뮬레이터를 기동할 것:
```
~/Library/Android/sdk/emulator/emulator -avd Medium_Phone &
```
