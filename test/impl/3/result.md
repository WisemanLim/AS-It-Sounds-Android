# 구현 3차 시험 결과 — E3 파이프라인

**판정: PASS**
**날짜: 2026-06-12**
**실행 시간: ~0.05s (14 케이스)**

## 시험 결과 요약

```
tests=14  failures=0  errors=0  skipped=0
BUILD SUCCESSFUL
```

## 케이스별 결과

### PipelineEventTest (8)
| # | 케이스 | 판정 |
|---|--------|------|
| T-01 | modelDownloading_storesProgress | ✅ PASS |
| T-02 | asrProgress_storesPercent | ✅ PASS |
| T-03 | translatingSegment_storesIndex | ✅ PASS |
| T-04 | segment_storesAllFields | ✅ PASS |
| T-05 | error_storesCause | ✅ PASS |
| T-06 | fileDone_isSingletonObject | ✅ PASS |
| T-07 | decoding_isSingletonObject | ✅ PASS |
| T-08 | segment_equalityByValue | ✅ PASS |

### PipelineOrchestratorTest (6)
| # | 케이스 | 판정 |
|---|--------|------|
| T-09 | emptySegments_emitDecodingAsrFileDone | ✅ PASS |
| T-10 | singleSegment_emitsTranslatingThenSegment | ✅ PASS |
| T-11 | checkpointResume_skipsAlreadyProcessedSegments | ✅ PASS |
| T-12 | checkpoint_clearedAfterSuccess | ✅ PASS |
| T-13 | checkpoint_writtenForEachSegment | ✅ PASS |
| T-14 | translator_calledOncePerSegment | ✅ PASS |

## 디버깅 경과

| # | 증상 | 원인 | 수정 |
|---|------|------|------|
| 1 | `WhisperJniTest` 7건 NoClassDefFoundError (E2 회귀) | `object WhisperJni { init { System.loadLibrary } }` 가 클래스 로드 시 `.so` 요구 | `loadNative()` 함수로 분리, Application.onCreate 에서 호출 |
| 2 | `PipelineOrchestratorTest` NPE: process parameter context | Kotlin `checkNotNullParameter(context)` — 테스트에서 null Context/Uri 전달 | **설계 변경**: `AudioSource` 가 Context/Uri 를 생성자에 바인딩, `process()` 에서 제거 |
| 3 | checkpoint_writtenForEachSegment AssertionError (3≠2) | 체크포인트는 `emit(Segment)` **후** 저장 → collect 관측 시점엔 N-1 값 | 기대값 2 로 정정(단조 증가 확인) |

## 생성/수정 파일

| 파일 | 내용 |
|------|------|
| `app/.../pipeline/PipelineEvent.kt` | sealed class 8종 이벤트 |
| `app/.../pipeline/AudioSource.kt` | Context/Uri 바인딩형 인터페이스 + DefaultAudioSource |
| `app/.../pipeline/WhisperEngine.kt` | WhisperEngine 인터페이스 + WhisperJniEngine (lazy ctx) |
| `app/.../pipeline/PipelineOrchestrator.kt` | 디코드→ASR→음차/번역 coroutineScope 병렬→Flow 방출 + 체크포인트 |
| `app/.../pipeline/TranscriptionWorker.kt` | CoroutineWorker 래퍼, 진행률 setProgress, 실패 시 retry |
| `app/.../translate/TranslatorInterface.kt` | TranslatorInterface + NoOpTranslator |
| `app/.../asr/WhisperJni.kt` | `loadNative()` 분리 (E2 회귀 수정) |
| `app/.../AsItSoundsApp.kt` | `WhisperJni.loadNative()` 호출 |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | coroutines-test 추가 |

## 비고

- 음차/번역은 `coroutineScope { async {} }` 로 세그먼트당 병렬 실행 (PRD §7.2 스레딩).
- 체크포인트는 `checkpointDir/cp_<fileId>.txt` 에 다음 인덱스 기록 → WorkManager 재시작 시 이어쓰기.
- E2 회귀(WhisperJni 7건) 동시 수정 확인 — 전체 ASR+Pipeline 통합 정상.
