# 구현 2차 시험 시나리오 — E2 ASR (whisper.cpp JNI)

**날짜**: 2026-06-12
**Epic**: E2 — ASR (whisper.cpp Android JNI + ModelManager + AudioDecoder + RamGuard)
**PRD 항목**: F-3 온디바이스 전사(ASR), F-11 모델 매니저, ADR-003, ADR-004

## 구현 범위

| 파일 | 설명 |
|------|------|
| `app/src/main/cpp/CMakeLists.txt` | NDK CMake 빌드 설정 (arm64-v8a + ARM NEON) |
| `app/src/main/cpp/whisper_jni.cpp` | JNI 브릿지 (whisper.cpp API ↔ Kotlin) |
| `app/src/main/java/.../asr/WhisperJni.kt` | JNI Kotlin 래퍼, `parseSegments()` |
| `app/src/main/java/.../asr/WhisperSegment.kt` | 세그먼트 데이터 클래스 |
| `app/src/main/java/.../asr/WhisperModel.kt` | 모델 열거(TINY/BASE/SMALL), SHA256, 다운로드 URL |
| `app/src/main/java/.../asr/AudioDecoder.kt` | MediaExtractor+MediaCodec → 16kHz mono Float32 |
| `app/src/main/java/.../asr/RamGuard.kt` | 기기 RAM → 권장 모델 결정 |
| `app/src/main/java/.../asr/ModelManager.kt` | 모델 캐시 확인, SHA 검증, WorkManager 다운로드 |
| `app/src/main/java/.../asr/ModelDownloadWorker.kt` | CoroutineWorker: OkHttp 다운로드 + SHA 검증 + 진행률 |
| `scripts/setup_whisper.sh` | whisper.cpp 소스 다운로드 스크립트 |

## 시험 항목 (단위 테스트 — JVM, 에뮬레이터 불필요)

### AudioDecoderTest
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-01 | `pcm16ToFloat` 0바이트 | 0.0f 반환 |
| T-02 | `pcm16ToFloat` 최대양수(32767) | ≈ 1.0f |
| T-03 | `pcm16ToFloat` 최대음수(-32768) | -1.0f |
| T-04 | `pcm16ToFloat` 4샘플 | 크기=4 |
| T-05 | `stereoToMono` 채널 평균화 | (1,-1)→0, (0.5,0.5)→0.5 |
| T-06 | `stereoToMono` 동일 채널 | 원본 유지 |
| T-07 | `resample` 동일 레이트 | 원본 반환 |
| T-08 | `resample` 빈 배열 | 빈 배열 |
| T-09 | `resample` 44100→16000 출력 길이 | ≈16000 |
| T-10 | `resample` 상수 신호 | 상수 유지 |
| T-11 | `TARGET_SAMPLE_RATE` 상수 | 16000 |
| T-12 | `TARGET_CHANNELS` 상수 | 1 |

### RamGuardTest
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-13 | 1GB | TINY |
| T-14 | 2GB | TINY |
| T-15 | 2GB+1 | TINY |
| T-16 | 3GB | BASE |
| T-17 | 4GB, 5GB, 6GB-1 | BASE |
| T-18 | 6GB | SMALL |
| T-19 | 8GB, 12GB | SMALL |

### WhisperJniTest
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-20 | `parseSegments("")` | 빈 리스트 |
| T-21 | `parseSegments("  \n ")` | 빈 리스트 |
| T-22 | 단일 세그먼트 파싱 | startMs/endMs/text/lang 정확히 파싱 |
| T-23 | 다중 세그먼트 파싱 | 2개 정확 파싱 |
| T-24 | 파이프 포함 텍스트 | 파싱 오류 없음 |
| T-25 | 잘못된 라인 | 무시, 유효 세그먼트만 반환 |
| T-26 | 숫자 아닌 타임스탬프 | 빈 리스트 |

### ModelManagerTest
| # | 케이스 | 기대 동작 |
|---|--------|-----------|
| T-27 | `sha256` 알려진 내용 | 기대 해시 일치 |
| T-28 | `sha256` 빈 파일 | 기대 해시 일치 |
| T-29 | `sha256` 다른 내용 | 해시 다름 |
| T-30 | `sha256` 1MB 파일 | 예외 없음 |
| T-31 | 다운로드 URL 파일명 포함 | 모든 모델 URL 검증 |
| T-32 | SHA256 길이 64자 | 모든 모델 검증 |
| T-33 | 모델 minRamBytes 오름차순 | TINY < BASE < SMALL |

## 시험 명령

```bash
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.asr.*"
```

## 성공 기준

- 전체 33 케이스 failures=0, errors=0, skipped=0
- NDK/JNI 관련 코드는 단위 테스트 제외(`.so` 불필요), 인테그레이션은 에뮬레이터 필요
