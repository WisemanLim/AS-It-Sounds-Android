# 구현 4차 시험 결과 — E6 번역 4종

**판정: PASS**
**날짜: 2026-06-12**
**실행 시간: ~0.1s (33 케이스)**

## 시험 결과 요약

```
TranslatorParsingTest  tests=21  failures=0  errors=0
TranslatorFactoryTest  tests=12  failures=0  errors=0
합계 33  failures=0  errors=0  skipped=0
BUILD SUCCESSFUL in 12s
```

## 케이스별 결과 (요약)

| 그룹 | 케이스 수 | 판정 |
|------|-----------|------|
| MyMemory 파싱/URL/langCode | 8 | ✅ PASS |
| LibreTranslate 파싱/body/srcCode | 6 | ✅ PASS |
| DeepL 파싱/endpoint/targetCode | 5 | ✅ PASS |
| ML Kit normalize | 2 | ✅ PASS |
| TranslatorFactory create 5종 | 5 | ✅ PASS |
| withFallback 폴백 체인 4종 | 4 | ✅ PASS |
| TranslationEngine enum | 3 | ✅ PASS |

## 디버깅 경과

| # | 증상 | 원인 | 수정 |
|---|------|------|------|
| 1 | (컴파일 전) 테스트 메서드명 공백 | `usesP rimary` 오타 | `usesPrimary` 로 정정 |

## 생성/수정 파일

| 파일 | 내용 |
|------|------|
| `translate/TranslatorInterface.kt` | TranslationEngine enum(OFF/MLKIT/MYMEMORY/LIBRE/DEEPL) 추가 |
| `translate/MlKitTranslator.kt` | 온디바이스 변환 + 언어팩 다운로드(Wi-Fi) suspend 래핑 |
| `translate/MyMemoryTranslator.kt` | 키리스 GET, `buildUrl`/`parseResponse`/`langCode` 순수 함수 |
| `translate/LibreTranslateTranslator.kt` | POST, `buildBody`/`parseResponse`/`srcCode` 순수 함수 |
| `translate/DeepLTranslator.kt` | Auth 헤더, free/pro endpoint, `parseResponse`/`targetCode` |
| `translate/ApiKeyStore.kt` | EncryptedSharedPreferences(AES256) 키 저장 |
| `translate/TranslatorFactory.kt` | 엔진별 생성 + `withFallback` 체인 |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | security-crypto + (test)org.json 추가 |

## 비고

- ADR-006 폴백 체인(ML Kit→MyMemory→끄기) = `TranslatorFactory.withFallback` 으로 구현.
  primary 빈값/예외 모두 fallback 으로 전환, 둘 다 실패 시 빈 문자열.
- ADR-010 시크릿 = EncryptedSharedPreferences(Android Keystore AES256-GCM). 평문/로그 금지.
- 네트워크 응답 파싱은 `org.json` 사용 — 프로덕션은 Android 런타임 실제 구현,
  단위시험은 `testImplementation(org.json:json)` 실제 구현 주입.
- ML Kit 실제 변환·EncryptedSharedPreferences 동작은 에뮬레이터 통합시험 영역.
