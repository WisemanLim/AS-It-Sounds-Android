# 구현 4차 시험 시나리오 — E6 번역 4종

**날짜**: 2026-06-12
**Epic**: E6 — 번역 (ML Kit/MyMemory/LibreTranslate/DeepL + 폴백 + EncryptedSharedPreferences)
**PRD 항목**: F-5 한국어 번역, ADR-006, ADR-010

## 구현 범위

| 파일 | 설명 |
|------|------|
| `translate/TranslatorInterface.kt` | 인터페이스 + NoOpTranslator + TranslationEngine enum |
| `translate/MlKitTranslator.kt` | 온디바이스(기본), 언어팩 다운로드 + suspend 변환 |
| `translate/MyMemoryTranslator.kt` | 키리스 HTTP, URL/응답 파싱 분리 |
| `translate/LibreTranslateTranslator.kt` | self-host POST, body/응답 파싱 분리 |
| `translate/DeepLTranslator.kt` | API 키 인증, free/pro 엔드포인트 |
| `translate/ApiKeyStore.kt` | EncryptedSharedPreferences 키 저장 |
| `translate/TranslatorFactory.kt` | 엔진별 생성 + ML Kit→MyMemory 폴백 체인 |

## 시험 항목

### TranslatorParsingTest (21)
- MyMemory: 응답 파싱(정상/빈/malformed/누락), langCode(auto→en, 절단), buildUrl(쿼리/langpair/인코딩) — 8
- LibreTranslate: 응답 파싱(정상/빈), buildBody(필수필드/apiKey 유무), srcCode(auto) — 6
- DeepL: 응답 파싱(첫번역/빈배열/malformed), endpoint(free/pro), targetCode(대문자) — 5
- ML Kit: normalize(auto→en, 절단) — 2

### TranslatorFactoryTest (12)
- create: OFF→NoOp, MLKIT→provider, MYMEMORY/LIBRE/DEEPL 타입 — 5
- withFallback: primary성공/primary빈값→fallback/primary예외→fallback/둘다실패→빈 — 4
- engine enum: fromId(known/unknown→mlkit), requiresApiKey(DeepL만) — 3

## 시험 명령

```bash
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.translate.*"
```

## 성공 기준

- 전체 33 케이스 failures=0, errors=0, skipped=0
- ML Kit 실제 변환 / EncryptedSharedPreferences 는 에뮬레이터 통합시험 영역(단위 제외)
