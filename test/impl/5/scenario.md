# 구현 5차 시험 시나리오 — E7 영속/내보내기

**날짜**: 2026-06-12
**Epic**: E7 — 영속/내보내기 (Room 3테이블 + SubtitleExporter SRT/VTT/TXT)
**PRD 항목**: F-9 로컬 영속, F-13 자막 내보내기, ADR-005, §7.4

## 구현 범위

| 파일 | 설명 |
|------|------|
| `core/data/db/Entities.kt` | ProjectEntity/MediaFileEntity/SegmentEntity, FK CASCADE, ProjectStatus |
| `core/data/db/Daos.kt` | ProjectDao/MediaFileDao/SegmentDao, Flow observe, checkpoint lastIndex |
| `core/data/db/AppDatabase.kt` | RoomDatabase v1 + Converters(ProjectStatus) |
| `core/data/export/SubtitleExporter.kt` | SRT/VTT/TXT 생성, EN/한글/KO 콘텐츠 선택 |

## 시험 항목

### SubtitleExporterTest (16)
- 시간 포맷: srtTime(콤마), vttTime(점), 시간 단위 처리 — 3
- SRT: 순차 인덱스, 화살표 타임코드, 텍스트 포함, 3단 스택 — 4
- VTT: WEBVTT 헤더, 점 타임코드, 인덱스 없음 — 3
- TXT: KO 전용, 한글 전용, 타임코드 없음 — 2
- 콘텐츠 필터: 빈 줄 스킵 — 1
- 메타: 확장자, 빈 리스트(SRT/VTT) — 3

### ConvertersTest (3)
- ProjectStatus 라운드트립, fromStatus(name), toStatus(parse) — 3

### Room 스키마 (컴파일 검증)
- KSP `kspDevDebugKotlin` 통과 = Entity/DAO/FK/Index/Converters 스키마 유효성 검증.
- DAO 쿼리 실 동작은 에뮬레이터 instrumentation 영역(`androidTest`).

## 시험 명령

```bash
./gradlew testDevDebugUnitTest --tests "com.asitsounds.android.core.data.*"
```

## 성공 기준

- 전체 19 케이스 failures=0, errors=0, skipped=0
- Room KSP 컴파일 성공(스키마 유효)
