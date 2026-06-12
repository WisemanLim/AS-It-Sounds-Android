# Play Store graphic assets (en-US)

Fastlane `supply` uploads images placed here. Required asset specs:

| Asset | Path | Size | Format |
|-------|------|------|--------|
| App icon | `icon.png` | 512×512 | 32-bit PNG |
| Feature graphic | `featureGraphic.png` | 1024×500 | PNG/JPG |
| Phone screenshots | `phoneScreenshots/1.png` … | 1080×1920 (min 320px) | PNG/JPG, 2–8 images |
| 7" tablet | `sevenInchScreenshots/1.png` … | optional | |
| 10" tablet | `tenInchScreenshots/1.png` … | optional | |

Screenshots to capture (per onboarding/PRD §4):
1. Home — new transcription + recent projects
2. 3-line segment view (EN / Hangul / KO) with highlight
3. Player — A-B repeat, speed, filter chips
4. Settings — translation engine + dark mode
5. Onboarding — on-device privacy page

> Binary images are produced from device captures / design tooling, not committed here.
> Drop the PNGs into this folder before running `fastlane supply`.
