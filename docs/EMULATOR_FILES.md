# 에뮬레이터로 오디오/비디오 파일 넣기 (macOS)

개발 중 "Files 에서 가져오기"로 실제 파일을 테스트하려면, 먼저 에뮬레이터에 파일을 복사해야 한다.
(데모만 필요하면 홈 → 파일 가져오기 → **데모: 전사 시작 (Mock ASR)** 사용 — 파일 불필요.)

## 1) adb push 로 복사 (가장 간단)

```bash
ADB=~/Library/Android/sdk/platform-tools/adb

# 에뮬레이터 실행 확인
$ADB devices

# 샘플 오디오를 Download 폴더로 push
$ADB push ~/Music/sample.mp3 /sdcard/Download/
$ADB push ~/Movies/clip.mp4 /sdcard/Download/

# 미디어 스캐너에 알려 Files 앱/사진앱에서 보이게 함
$ADB shell "content call --uri content://media/external/file --method scan_volume --arg external_primary" 2>/dev/null \
  || $ADB shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/sample.mp3
```

앱에서: 홈 → **파일 가져오기** → **Files 에서 가져오기** → Download 에서 선택.

## 2) 드래그 앤 드롭 (Android Studio)

Android Studio → **Device Manager → 에뮬레이터 ▸ → Device File Explorer** →
`/sdcard/Download/` 에 Finder 에서 파일을 드래그.

## 3) 에뮬레이터 안에서 직접 다운로드

에뮬레이터의 Chrome 으로 오디오/비디오 URL 접속 → 다운로드 → Download 폴더에 저장.

## 참고

- 지원 형식: `audio/*`, `video/*` (mp3·m4a·wav·ogg·flac·mp4·mov·mkv·webm).
- 가져온 파일은 앱 내부 `filesDir/imports/` 로 **사본 복사**된다(원본 불변, SAF Uri 의존 회피).
- 16KB 페이지 경고 다이얼로그(`libwhisper.so ... not aligned`)는 에뮬레이터 개발 경고이며 동작에는 영향 없음. 정식 배포(Android 15+) 전 16KB 정렬 빌드 필요.
- AVD: `Medium_Phone` — 실행: `~/Library/Android/sdk/emulator/emulator -avd Medium_Phone &`
