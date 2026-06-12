# 구현 6차 시험 시나리오 — E4 UI 셸

**날짜**: 2026-06-12
**Epic**: E4 — UI 셸 (Home/Library/Project/Settings 4탭 + 온보딩, Phone BottomNav / Tablet NavigationRail)
**PRD 항목**: F-1 4탭 셸, F-7 3단 세그먼트 뷰, F-10 설정, F-20 온보딩, ADR-009

## 구현 범위

| 파일 | 설명 |
|------|------|
| `core/ui/theme/Theme.kt` | Material3 테마, HangulBlue, ThemeMode(시스템/라이트/다크) |
| `core/data/ProjectRepository.kt` | DAO 래핑 Repository (Hilt @Singleton) |
| `core/data/SettingsRepository.kt` | DataStore(테마/엔진/자동재생/속도/첫실행) |
| `core/di/DataModule.kt` | Hilt: AppDatabase + 3 DAO 제공 |
| `feature/navigation/Destinations.kt` | TopDestination 3탭, Routes(projectDetail/extractId) |
| `feature/navigation/AppShell.kt` | 적응형 셸(Phone BottomNav / Tablet≥600dp NavigationRail) + NavHost |
| `feature/home/{HomeViewModel,HomeScreen}.kt` | 최근 3 프로젝트, 새 전사 버튼 |
| `feature/library/{LibraryFilter,LibraryViewModel,LibraryScreen}.kt` | 검색 + 상태칩 필터 |
| `feature/project/{SegmentFilter,PlayerViewModel,ProjectDetailScreen}.kt` | 3단 세그먼트, 필터칩, 하이라이트, 재생로직 |
| `feature/settings/{SettingsViewModel,SettingsScreen}.kt` | 테마/엔진/자동재생 |
| `feature/onboarding/{OnboardingPages,OnboardingPagerScreen}.kt` | 4장 Pager |
| `MainActivity.kt` | 테마 + AppShell 연결 |

## 시험 항목

### LibraryFilterTest (9)
- 전체/DONE/PROCESSING(PENDING포함)/ERROR 상태 필터, 검색(대소문자/트림/조합/무매치), matches 로직

### PlayerLogicTest (16)
- activeSegmentIndex(첫/경계/마지막/초과/빈/갭), clampSpeed(0.5~2.0), seekRelative(±5초 클램프), SegmentFilter 가시성(ALL/EN/HANGUL/KO)

### NavigationTest (8)
- projectDetail 라우트 빌드/추출, 3탭 라우트 유일성, 온보딩 4장/비어있지않음/브랜드

### 컴파일 검증
- `compileDevDebugKotlin` 성공 = 전체 Compose UI + Hilt ViewModel + NavHost + DataStore 타입 검증.

## 시험 명령

```bash
./gradlew compileDevDebugKotlin testDevDebugUnitTest --tests "com.asitsounds.android.feature.*"
```

## 성공 기준

- 전체 33 케이스 failures=0, errors=0, skipped=0
- Compose/Hilt 컴파일 성공
- Compose UI 런타임 동작은 에뮬레이터 ComposeTestRule 영역
