# 구현 5차 시험 결과 — E7 영속/내보내기

**판정: PASS**
**날짜: 2026-06-12**
**실행 시간: ~0.02s (19 케이스)**

## 시험 결과 요약

```
SubtitleExporterTest  tests=16  failures=0  errors=0
ConvertersTest        tests=3   failures=0  errors=0
합계 19  failures=0  errors=0  skipped=0
Room KSP 컴파일 성공 (스키마 유효)
BUILD SUCCESSFUL in 3s
```

## 케이스별 결과 (요약)

| 그룹 | 케이스 수 | 판정 |
|------|-----------|------|
| 시간 포맷(SRT 콤마 / VTT 점 / 시:분:초) | 3 | ✅ PASS |
| SRT 생성(인덱스/타임코드/텍스트/3단) | 4 | ✅ PASS |
| VTT 생성(헤더/점타임코드/무인덱스) | 3 | ✅ PASS |
| TXT 생성(KO/한글/무타임코드) | 2 | ✅ PASS |
| 콘텐츠 필터(빈 줄 스킵) | 1 | ✅ PASS |
| 포맷 메타/빈 리스트 | 3 | ✅ PASS |
| Converters 라운드트립 | 3 | ✅ PASS |

## 생성/수정 파일

| 파일 | 내용 |
|------|------|
| `core/data/db/Entities.kt` | 3 테이블 + FK CASCADE + ProjectStatus enum (iOS 스키마 1:1) |
| `core/data/db/Daos.kt` | 3 DAO, Flow observe, recent LIMIT, `lastIndex`(checkpoint) |
| `core/data/db/AppDatabase.kt` | RoomDatabase v1, Converters TypeConverter |
| `core/data/export/SubtitleExporter.kt` | SRT/VTT/TXT + ExportContent(EN/한글/KO/3단) |

## 비고

- ADR-005 Room 스키마 iOS 1:1: Project{id,name,createdAt,updatedAt,status},
  MediaFile{id,projectId,path,displayName,mime,durationMs,status,error,createdAt},
  Segment{id,fileId,idx,startMs,endMs,textEn,textHangul,textKo}. FK onDelete=CASCADE.
- `SegmentDao.lastIndex(fileId)` 로 백그라운드 체크포인트 이어쓰기(E3) 연동.
- SRT 타임코드는 `HH:MM:SS,mmm`(콤마), VTT 는 `HH:MM:SS.mmm`(점) — 표준 준수.
- DAO 실 쿼리·CASCADE 동작은 에뮬레이터 instrumentation(`androidTest`) 영역.
