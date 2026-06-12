package com.asitsounds.android.feature.onboarding

// Rich onboarding content matching the iOS intro screens (4 pages).

data class Feature(val emoji: String, val text: String)

data class SegmentPreview(val en: String, val hangul: String, val ko: String)

data class OnboardingPage(
    val emoji: String,            // hero glyph
    val title: String,
    val subtitle: String,
    val quoteEmoji: String? = null,
    val quote: String? = null,    // speech-bubble line
    val quoteCaption: String? = null,
    val features: List<Feature> = emptyList(),
    val previews: List<SegmentPreview> = emptyList(),
    val chips: List<String> = emptyList(),
    val noteTitle: String? = null,
    val noteBody: String? = null
)

object OnboardingPages {
    val pages: List<OnboardingPage> = listOf(
        OnboardingPage(
            emoji = "🎙",
            title = "As It Sounds",
            subtitle = "들리는 그대로, 한글로",
            quoteEmoji = "🎧",
            quote = "영어 강의, 발음을 한글로 바로 확인하고 싶다! 📖",
            quoteCaption = "오디오·영상 → 한글 음차 + 번역 자동 생성",
            features = listOf(
                Feature("🗣", "영어·일본어·프랑스어 등 6개 언어 지원"),
                Feature("🔒", "온디바이스 처리 — 인터넷 불필요"),
                Feature("✈️", "비행기 모드·지하철에서도 동작")
            )
        ),
        OnboardingPage(
            emoji = "✏️",
            title = "전사 · 음차 · 번역",
            subtitle = "세 가지 보기를 한 화면에서",
            previews = listOf(
                SegmentPreview("As it sounds, it works.", "애즈 잇 사운즈, 잇 웍스.", "들리는 대로, 작동합니다."),
                SegmentPreview("Nice to meet you.", "나이스 투 밋 유.", "만나서 반갑습니다.")
            ),
            chips = listOf("🇺🇸 영어", "🇯🇵 일본어", "🇫🇷 프랑스어", "🇪🇸 스페인어", "🇩🇪 독일어", "🇷🇺 러시아어"),
            features = listOf(
                Feature("🧠", "whisper.cpp 온디바이스 음성인식"),
                Feature("🔤", "순수 Kotlin 음차 엔진 — 서버 없이 즉시"),
                Feature("🌐", "ML Kit 번역 기본 (온디바이스, 무료)")
            )
        ),
        OnboardingPage(
            emoji = "📂",
            title = "파일 · 재생 · 반복",
            subtitle = "쉽게 가져오고, 구간 반복으로 학습",
            quoteEmoji = "🎵",
            quote = "이 구간만 계속 들으면서 발음 연습해야지! 🔁",
            quoteCaption = "세그먼트 탭 → 해당 구간 즉시 이동 · A-B 구간 반복",
            features = listOf(
                Feature("📥", "파일앱 · 사진 · 공유시트 가져오기"),
                Feature("🔁", "A-B 구간반복 · 0.5~2× 배속 · ±5초 이동"),
                Feature("📤", "SRT / VTT / TXT 자막 내보내기"),
                Feature("📊", "전사 진행률 알림 (Foreground)")
            )
        ),
        OnboardingPage(
            emoji = "🔐",
            title = "모두 온디바이스",
            subtitle = "음성 데이터는 기기 밖으로 나가지 않습니다",
            quoteEmoji = "🛡",
            quote = "내 음성 파일이 서버에 올라가는 게 싫었는데 다행! 😌",
            quoteCaption = "비행기 모드에서도 전사·음차 동작 (번역=ML Kit 또는 끄기 시)",
            features = listOf(
                Feature("🔒", "음성·텍스트 외부 전송 없음 — 클라우드 업로드 0"),
                Feature("⬇️", "AI 모델은 최초 1회 다운로드 후 기기 내 저장"),
                Feature("☁️", "Google Drive 백업으로 기기 간 동기화 (선택)"),
                Feature("⚙️", "설정에서 번역 엔진·화면 모드 언제든 변경")
            ),
            noteTitle = "첫 전사 전 모델 다운로드",
            noteBody = "whisper base 모델 (~142 MB). Wi-Fi 환경에서 자동으로 다운로드됩니다."
        )
    )
}
