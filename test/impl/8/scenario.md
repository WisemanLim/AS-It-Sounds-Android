# 구현 8차 시험 시나리오 — E8 플랫폼 통합

**날짜**: 2026-06-12
**Epic**: E8 — 플랫폼 통합 (WorkManager·ForegroundService·Shortcuts·실시간 STT)
**PRD 항목**: F-14 백그라운드 처리, F-16 App Shortcuts, F-17 전사 진행률 알림, F-18 실시간 마이크 STT

## 구현 범위

| 파일 | 설명 |
|------|------|
| `asr/RollingBuffer.kt` | 5초 링버퍼(16kHz Float32) — 실시간 STT |
| `platform/NotificationHelper.kt` | 채널 + 진행률 알림, 진행률 텍스트/퍼센트 순수 함수 |
| `platform/ForegroundTranscribeService.kt` | Foreground Service(dataSync) + 진행률 |
| `platform/TranscriptionScheduler.kt` | WorkManager 고유 작업 enqueue/cancel |
| `platform/AppShortcuts.kt` | 동적 Shortcut(최근 프로젝트), id 매핑 |
| `platform/RealtimeSttController.kt` | AudioRecord → 롤링버퍼 → Flow<FloatArray> |
| `AndroidManifest.xml` | ForegroundTranscribeService(dataSync) 선언 |

## 시험 항목

### RollingBufferTest (8)
- 빈 버퍼, 용량 이하 유지, 정확히 용량(full), 초과 시 최신 유지, 다중 wrap 시간순, clear, forWindow 사이즈/기본 5초

### PlatformLogicTest (12)
- NotificationHelper: progressText(0/n), progressPercent(0/half/full/clamp) — 6
- AppShortcuts: id 포맷/라운드트립/무효/MAX=4 — 4
- TranscriptionWorker.workName: 파일별/유일 — 2

### 컴파일 검증
- Foreground Service(ServiceCompat dataSync) + AudioRecord(ENCODING_PCM_FLOAT) + ShortcutManagerCompat 타입 검증.

## 시험 명령

```bash
./gradlew compileDevDebugKotlin testDevDebugUnitTest --tests "com.asitsounds.android.platform.*"
```

## 성공 기준

- 전체 20 케이스 failures=0, errors=0, skipped=0
- 마이크 캡처·Foreground Service·Shortcut 등록 실행은 에뮬레이터 instrumentation 영역
