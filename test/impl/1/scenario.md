# 구현 1차 시험 시나리오 — E1 TranslitEngine

**날짜**: 2026-06-12
**Epic**: E1 — TranslitEngine 포팅 (iOS TranslitKit Swift → Kotlin 1:1)
**PRD 항목**: F-4 한글 음차, ADR-002

## 구현 범위

| 파일 | 설명 |
|------|------|
| `app/src/main/java/com/asitsounds/android/translit/TranslitEngine.kt` | 다국어 음차 엔진 (Latin·Kana·Hangul·Cyrillic·CJK) |
| `app/src/main/java/com/asitsounds/android/translit/TranslitDict.kt` | 영어 고빈도어 사전 (~150 항목) |
| `app/src/test/java/com/asitsounds/android/translit/TranslitEngineTest.kt` | iOS fixture 27 케이스 이식 |

## 시험 항목

| # | 분류 | 입력 | 기대 동작 |
|---|------|------|-----------|
| T-01 | 영어 dict hit | `"Hello, world!"` | "헬로" 포함 |
| T-02 | 영어 fallback rule | `ruleTransliterateEn("crackle")` | 비어있지 않음 |
| T-03 | 영어 숫자 혼합 dict | `"Number 2"` | `"넘버 2"` |
| T-04 | 영어 종성 클러스터 | `ruleTransliterateEn("number")` | `"넘버"` |
| T-05 | 영어 er 이중모음 | `ruleTransliterateEn("butter")` | "어" 로 끝나거나 "터" 포함 |
| T-06 | 영어 복합 문장 dict | electrician/coming/isn't 문장 | 각 단어 한글 포함 |
| T-07 | 일본어 히라가나 기본 | `"こんにちは"` | 콩·치·하 포함 |
| T-08 | 일본어 가타카나 | `"コーヒー"` | 코·히 포함 |
| T-09 | 러시아어 기본 | `"Москва"` | 비어있지 않고 라틴 문자 없음 |
| T-10 | 프랑스어 rule | `ruleTransliterateFr("bonjour")` | 비어있지 않음 |
| T-11 | 언어 감지 (프랑스어 악센트) | `"très bien"` | FRENCH 감지 |
| T-12 | 언어 감지 (독일어 움라우트) | `"schön"` | GERMAN 감지 |
| T-13 | 언어 감지 (스페인어 tilde) | `"mañana"` | SPANISH 감지 |
| T-14 | 한글 통과 | `"안녕하세요"` | 그대로 반환 |
| T-15 | 영어 hard-C | `ruleTransliterateEn("corner")` | "코"로 시작 |
| T-16 | 영어 soft-C | `ruleTransliterateEn("city")` | "시"로 시작 |
| T-17~19 | 영어 알파벳 이름 | A./B./C. | 에이./비./씨. |
| T-20 | 혼합 문장 | `"C. Right around the corner."` | "씨."로 시작, "코너" 포함 |
| T-21 | 축약형 we've | `ruleTransliterateEn("we've")` | "위"로 시작 |
| T-22~24 | receive/received 복합 | ei digraph, -ed 규칙, re- 접두사 | 시/드 포함, "리"로 시작 |
| T-25 | 소문자 a | `ruleTransliterateEn("a")` | 비어있지 않음 |
| T-26 | 장모음 a | `ruleTransliterateEn("catering")` | "케이"로 시작 |
| T-27 | 전체 샘플 문장 | `"A. I don't think we've received it."` | 에이./위브/리시브드 포함 |

## 시험 명령

```bash
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.translit.TranslitEngineTest"
```

## 성공 기준

- tests=27, failures=0, errors=0, skipped=0
- iOS TranslitParityTests 27 케이스와 동일 판정
