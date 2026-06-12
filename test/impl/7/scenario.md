# 구현 7차 시험 시나리오 — E5 파일 입수

**날짜**: 2026-06-12
**Epic**: E5 — 파일 입수 (SAF/PickVisualMedia/ACTION_SEND + 내부 복사 + BottomSheet)
**PRD 항목**: F-2 파일 입수 다중 경로, F-12 공유받기, §7.4 저장 위치

## 구현 범위

| 파일 | 설명 |
|------|------|
| `feature/import_/ImportLogic.kt` | 순수 헬퍼: mime 지원, 파일명 정제/유일화, 확장자 매핑 |
| `feature/import_/MediaImporter.kt` | SAF Uri → filesDir/imports 내부 복사, displayName 조회 |
| `feature/import_/ShareIntake.kt` | ACTION_SEND/SEND_MULTIPLE Uri 추출(API33+ 분기) |
| `feature/import_/ImportSheet.kt` | ModalBottomSheet: OpenDocument + PickVisualMedia |

## 시험 항목

### ImportLogicTest (25)
- isSupportedMime: audio/video 허용, image/text/null 거부 — 5
- sanitizeFileName: 경로 제거(/, \\), 불법문자 치환, 한글 유지, 빈값 폴백 — 5
- uniqueName: 무충돌/_1/_2/확장자없음 — 4
- extensionForMime: mp3/m4a/mp4/unknown — 4
- displayNameOrDefault: 제공값/null/blank 폴백 — 3
- ShareIntake.isShareIntent: SEND/SEND_MULTIPLE/MAIN/null — 4

### 컴파일 검증
- `compileDevDebugKotlin` 성공 = ActivityResultContracts(OpenDocument/PickVisualMedia) + ModalBottomSheet 타입 검증.

## 시험 명령

```bash
./gradlew compileDevDebugKotlin testDevDebugUnitTest --tests "com.asitsounds.android.feature.import_.*"
```

## 성공 기준

- 전체 25 케이스 failures=0, errors=0, skipped=0
- SAF 실제 picker·ContentResolver 복사는 에뮬레이터 instrumentation 영역
