# COMPLIANCE.md — as-it-sounds-android

> 도메인: `edtech` (KSIC P — 교육서비스업)
> 생성: `wise-dev-standard` 플러그인 / 도메인 오버레이 `edtech`
> 근거: PRD.md §7.6 edtech 오버레이

## 요약

본 앱은 **온디바이스 개인용 도구**다. 에듀테크 서버측 규제(LMS·라이브클래스·WebRTC·버스트스케일)는 **대부분 비해당**. 적용 항목만 아래에 명시한다.

---

## 적용 규제

### 한국 규제

| 규제 | 적용 여부 | 조치 |
|------|----------|------|
| **개인정보보호법(PIPA)** | 경량 적용 | 사용자 콘텐츠 온디바이스 only. 외부 수집 = 익명 진단(Firebase Crashlytics)만. PII 로깅 금지. 번역 API 전송 텍스트 = 사용자 콘텐츠, ATS 등가(HTTPS) 강제. |
| **만14세 미만 보호자 동의 (PIPA §22-2)** | 조건부 | `가정:` 직접 타깃 아님. Play Store 연령등급으로 결정. 직접 타깃 시 보호자 동의 게이트 + 만14세 미만 PII 마스킹 필수. |
| **KWCAG / 웹접근성** | 적용 | TalkBack 대응, 터치 타깃 48dp 이상, 색 대비 4.5:1 이상, `contentDescription` 결합 라벨(타임코드+EN+한글+KO). |

### 국제 규제

| 규제 | 적용 여부 | 조치 |
|------|----------|------|
| **COPPA (만13세 미만, 2026-04-22 완전준수)** | 조건부 | `가정:` 직접 타깃 아님. 직접 타깃 시 부모 동의 및 제3자 공유 전 동의 필수. |
| **GDPR** | 경량 적용 | 온디바이스 처리 = 개인정보 처리 최소화. EU 사용자 대상 시 DPIA 검토. |
| **FERPA** | 비해당 | 미국 학생 기록 처리 없음. |

---

## 데이터 등급

| 데이터 | 등급 | 저장 위치 | 비고 |
|--------|------|----------|------|
| 음성/미디어 파일 | 일반 (사용자 콘텐츠) | `filesDir` 내부 사본 | 외부 전송 없음 |
| 전사 텍스트 / 세그먼트 | 일반 (사용자 콘텐츠) | Room (기기 내) | PII 포함 가능 → 로그 출력 금지 |
| 번역 API 전송 텍스트 | 일반 (외부 전송 있음) | 전송 후 미저장 | HTTPS 필수, 사용자 안내 |
| 번역 API 키 | 시크릿 | Android Keystore + EncryptedSharedPreferences | 평문/로그/커밋 금지 |
| 익명 진단 (크래시) | 진단 | Firebase Crashlytics | PII 제거 후 전송 |
| 미성년자 정보 | **규제대상** | — | 직접 타깃 시 보호자 동의 연동 필수 |

> **규제대상 데이터는 비-프로덕션(dev/staging/test) 환경 반입 금지. CI fixture는 합성 데이터만 사용.**

---

## 비해당 규제 (근거 명시)

| 규제 | 비해당 근거 |
|------|-----------|
| LMS / SCORM / xAPI / LTI | 서버 없음, 학습관리 시스템 미운영 |
| 라이브클래스 / WebRTC / HLS | 실시간 영상 스트리밍 없음 |
| 버스트 스케일 백엔드 | 서버 없음, 온디바이스 처리 |
| 미성년자 가입 게이트 | 직접 타깃 아님 (`가정:`, Play Store 연령등급으로 결정) |
| 학습기록/성취도 서버 저장 | 기기 내 로컬 저장만 |

---

## Play Store 데이터 안전 선언

```
데이터 수집 여부: 없음 (익명 진단 제외)
제3자 데이터 공유: 없음
데이터 암호화: 전송 시 HTTPS, 저장 시 Android Keystore
데이터 삭제 요청: 앱 삭제 시 기기 데이터 자동 삭제
```

---

## 참고 출처

- [PIPA §22-2 만14세 미만 보호자 동의](https://easylaw.go.kr/CSP/CnpClsMain.laf?popMenu=ov&csmSeq=659)
- [COPPA 2025/2026 완전준수](https://complydog.com/blog/edtech-saas-compliance-student-privacy-gdpr-implementation)
- [KWCAG 2.2](https://www.wah.or.kr/board/boardView.asp?brd_sn=5&brd_idx=1088)
