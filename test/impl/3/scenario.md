# 구현 3차 시험 시나리오 — E3 파이프라인

**날짜**: 2026-06-12
**Epic**: E3 — 파이프라인 (PipelineOrchestrator + Flow<PipelineEvent> + TranscriptionWorker)
**PRD 항목**: F-6 파이프라인 실행, ADR-009

## 구현 범위

| 파일 | 설명 |
|------|------|
| `app/.../pipeline/PipelineEvent.kt` | sealed class — 8종 이벤트 |
| `app/.../pipeline/AudioSource.kt` | AudioSource 인터페이스 + DefaultAudioSource |
| `app/.../pipeline/WhisperEngine.kt` | WhisperEngine 인터페이스 + WhisperJniEngine |
| `app/.../pipeline/PipelineOrchestrator.kt` | 디코드→ASR→음차/번역 병렬→Flow 방출, 체크포인트 |
| `app/.../pipeline/TranscriptionWorker.kt` | CoroutineWorker 래퍼 |
| `app/.../translate/TranslatorInterface.kt` | TranslatorInterface + NoOpTranslator |

## 시험 항목

### PipelineEventTest (8)
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-01 | ModelDownloading.progressPct | 저장값 일치 |
| T-02 | AsrProgress.progressPct | 저장값 일치 |
| T-03 | TranslatingSegment.segmentIndex | 저장값 일치 |
| T-04 | Segment 전체 필드 | 모든 필드 일치 |
| T-05 | Error.cause | 동일 예외 인스턴스 |
| T-06 | FileDone 싱글톤 | same instance |
| T-07 | Decoding 싱글톤 | same instance |
| T-08 | Segment 값 동등성 | equals 일치 |

### PipelineOrchestratorTest (6)
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-09 | 빈 세그먼트 | Decoding→AsrProgress(0)→AsrProgress(100)→FileDone |
| T-10 | 단일 세그먼트 | TranslatingSegment(0)→Segment(hangul비어있지않음,textKo=[Hello]) |
| T-11 | 체크포인트 재개 | 이미 처리된 세그먼트 0 건너뜀, 세그먼트 1만 방출 |
| T-12 | 체크포인트 완료 후 삭제 | 파일 없음 |
| T-13 | 체크포인트 순차 기록 | 마지막 값=3 |
| T-14 | 번역기 세그먼트당 1회 호출 | callCount=3 |

## 시험 명령

```bash
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.pipeline.*"
```

## 성공 기준

- 전체 14 케이스 failures=0, errors=0, skipped=0
