# 구현 10차 시험 시나리오 — 마감(실제 ASR·계측시험·F-15·Play 에셋)

**날짜**: 2026-06-12
**범위**: whisper.cpp 실제 네이티브 빌드 + 에뮬레이터 instrumentation 시험 + F-15 동기화(Google Drive Backup) + Play Store 이미지 에셋 구조
**PRD 항목**: F-3/ADR-003(실제 ASR), F-9(DB 계측), F-15(동기화), F-21(Play)

## 구현/변경 범위

| 파일 | 설명 |
|------|------|
| `scripts/setup_whisper.sh` | whisper.cpp v1.7.4 git clone(공식 add_subdirectory 패턴) |
| `cpp/CMakeLists.txt` | `add_subdirectory(whisper)` → libwhisper 빌드 + JNI 링크, 소스 부재 시 스텁 |
| `cpp/whisper_jni.cpp` | API 정정: `whisper_full_lang_id`+`whisper_lang_str` |
| `res/xml/backup_rules.xml`, `data_extraction_rules.xml` | F-15 Auto Backup(Drive) + device-transfer, 모델/키 제외 |
| `AndroidManifest.xml` | `allowBackup=true` + backup/extraction 규칙 연결 |
| `androidTest/.../db/AppDatabaseTest.kt` | Room 계측: insert/observe/Converter/CASCADE/lastIndex |
| `fastlane/metadata/android/{en-US,ko-KR}/images/` | Play 그래픽 에셋 규격/스크린샷 가이드 |
| `.gitignore` | `app/src/main/cpp/whisper/`(MIT 소스 비커밋) |

## 시험 항목

### 실제 네이티브 빌드 (assembleDevDebug)
- whisper.cpp v1.7.4 clone → ggml/ggml-base/ggml-cpu/whisper/whisper_jni 빌드.
- 기대: `libwhisper.so` + `libwhisper_jni.so`(arm64-v8a) 포함 APK 생성.

### AppDatabaseTest (5, 에뮬레이터 instrumentation)
| # | 케이스 | 기대 |
|---|--------|------|
| I-01 | insertProject_retrievable | 삽입 후 조회 일치 |
| I-02 | observeRecent_ordersByUpdatedDesc | updatedAt 내림차순 |
| I-03 | statusConverter_persists | ProjectStatus 변환 영속 |
| I-04 | cascadeDelete_removesFilesAndSegments | 프로젝트 삭제 → 파일·세그먼트 CASCADE |
| I-05 | lastIndex_returnsMaxSegmentIndex | 최대 idx 반환(체크포인트) |

## 시험 명령

```bash
bash scripts/setup_whisper.sh          # whisper.cpp 소스 fetch
./gradlew assembleDevDebug             # 실제 네이티브 빌드
./gradlew connectedDevDebugAndroidTest # 에뮬레이터 계측시험
```

## 성공 기준

- assembleDevDebug 성공 + libwhisper.so 포함
- AppDatabaseTest 5/5 PASS (emulator-5554, Medium_Phone API 36)
- F-15 backup 규칙 manifest 병합 성공
