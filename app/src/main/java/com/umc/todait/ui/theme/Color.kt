package com.umc.todait.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 투데잇 디자인 시스템 컬러 팔레트.
 * 디자인 시안 확정 시 HEX 값만 교체하고, 화면에서는 반드시 이 토큰을 통해 사용한다.
 * (화면 코드에 Color(0xFF...) 직접 사용 금지)
 */

// Brand
val Primary = Color(0xFFFF5D6C)        // 메인 브랜드 (데이트 무드 코랄)
val PrimaryLight = Color(0xFFFFE3E6)
val PrimaryDark = Color(0xFFE04553)
val Secondary = Color(0xFF6C5CE7)

// Grayscale
val Gray900 = Color(0xFF191919)
val Gray700 = Color(0xFF464646)
val Gray600 = Color(0xFF737373)      // 헤더 아이콘 버튼(원형) 배경
val Gray500 = Color(0xFF8A8A8A)
val Gray300 = Color(0xFFD6D6D6)
val Gray200 = Color(0xFFC7C7C7)      // 검색창 placeholder
val Gray100 = Color(0xFFF4F4F4)
val White = Color(0xFFFFFFFF)

// 기준 장소 설정(코스 생성 플로우) 화면 팔레트 — Figma 디자인 시스템 토큰
val Cream = Color(0xFFFDFBF3)               // 화면 배경(웜 아이보리)
val Pink100 = Color(0xFFFFE0DF)             // 상단 헤더 배경
val Pink700 = Color(0xFFF4A7A5)             // 장소 카드 근접 배지 텍스트
val PlaceCardGradientStart = Color(0xFFEABCD2) // 장소 카드 우측 그라데이션 시작(소프트 핑크)
val PlaceCardGradientEnd = Color(0xFFD6B27F)   // 장소 카드 우측 그라데이션 끝(웜 탠)

// 장소 상세(장소카드클릭_기본 / 내부사진전체보기) 화면 — Figma 디자인 시스템 토큰(정확 매칭)
// Pink800(#F09F9D, 칩 텍스트)은 인증 화면 섹션에 동일 값으로 이미 선언돼 있어 재사용한다.
val Gray800 = Color(0xFF222222)             // 장소명·섹션 제목 텍스트 (#222)
val Gray450 = Color(0xFF8E8E8E)             // '전체보기' 텍스트 (Gray-500)
val Gray400 = Color(0xFFA3A3A3)             // 주소·보조 텍스트 (Gray-400)
val OpenGreen = Color(0xFF62AC5E)           // '영업중' 상태 텍스트
val DividerLine = Color(0xFFEEEEEE)         // 섹션 구분선 (Gray-100)

// 기준 장소 시스템 알럿 / 선택 상태 — Figma 디자인 시스템 토큰
val Gray50 = Color(0xFFF5F5F5)              // 알럿 '취소' 버튼 배경 (Gray-50)
val Green700 = Color(0xFF819158)            // 선택된 기준 장소 카드 테두리 (Green-700)

// 코스 구성하기(코스구성하기(카페)_기본) 화면 — Figma 디자인 시스템 토큰(정확 매칭)
val Pink600 = Color(0xFFF9AEAC)             // 선택된 카테고리 탭 배경 (Pink-600)
val CategoryTabTextSelected = Color(0xFF575757) // 선택된 카테고리 탭 텍스트 (Gray-700)

// ─────────────────────────────────────────────────────────────────────────────
// 추천 장소 카드 — 분위기(mood)별 그라데이션 (세로 방향, 위 Start → 아래 End)
//
// 분위기 종류는 "분위기 태그 조회"(GET /api/mood-tags) 명세의 6종과 일치. (아래는 sortOrder 순)
// HEX는 Figma "취향설정" 화면(node 534-12985)에서 추출한 확정값(6종 전부 확정).
// ─────────────────────────────────────────────────────────────────────────────

// 힙한(HIP)
val CourseHipGradientStart = Color(0xFFACC5D1)
val CourseHipGradientEnd = Color(0xFFBEB2D6)

// 조용한(QUIET)
val CourseQuietGradientStart = Color(0xFFC3D2B6)
val CourseQuietGradientEnd = Color(0xFFE0D9BC)

// 활발한(ACTIVE)
val CourseActiveGradientStart = Color(0xFFEEC0B6)
val CourseActiveGradientEnd = Color(0xFFEED1B7)

// 로맨틱(ROMANTIC)
val CourseRomanticGradientStart = Color(0xFFECBBC1)
val CourseRomanticGradientEnd = Color(0xFFDEC4DD)

// 모던한(MODERN)
val CourseModernGradientStart = Color(0xFFBFE1DE)
val CourseModernGradientEnd = Color(0xFF7CA2BA)

// 차분한(CALM)
val CourseCalmGradientStart = Color(0xFFB8DCC4)
val CourseCalmGradientEnd = Color(0xFF99C6CE)

// Semantic
val Error = Color(0xFFF04438)
val Success = Color(0xFF12B76A)
val Background = White
val Surface = White
val OnPrimary = White

// 소셜 로그인 브랜드 컬러 (앱 브랜드 컬러가 아니라 각 제공자 고정 컬러이므로 별도 토큰으로 분리)
val KakaoYellow = Color(0xFFFEE500)
val KakaoBrown = Color(0xFF191919)
val GoogleBadgeBlue = Color(0xFFCEEAFF)     // 구글 간편가입 뱃지 배경 (Figma)
val GoogleBadgeText = Color(0xFF000000)     // 구글 간편가입 뱃지 텍스트 (Figma)

// 배경 그라디언트 (Figma: linear-gradient(180deg, #FFFAF7 0%, #FFEDED 100%))
val BgGradientTop = Color(0xFFFFFAF7)
val BgGradientBottom = Color(0xFFFFEDED)

// 인증 화면(로그인/회원가입) 전용 (Figma 지정값)
val Pink400 = Color(0xFFFFBDBC)             // 로그인/가입완료 버튼 활성화 (Figma: PINK-400)
val Pink800 = Color(0xFFF09F9D)             // 회원가입/이메일 로그인 링크 텍스트 (Figma: PINK-800)
val LoginHeadingPink = Color(0xFFED9896)    // 이메일 로그인 화면 "LOG IN" 타이틀 텍스트
val VerifyPink = Color(0xFFF4A7A5)          // 인증번호 발송/재전송/확인(활성) 버튼
val DisabledButtonGray = Color(0xFFEEEEEE)  // 가입완료 버튼 비활성화
val DisabledConfirmGray = Gray200           // 확인 버튼 비활성화
val SignupBackground = Color(0xFFFDFBF3)    // 회원가입 화면 배경 (그라디언트 아님, 단색)

// 마이페이지
val ProfileCardBackground = Color(0xFFFFEBEB) // 마이페이지 프로필 카드 배경

//Text
val PlaceNumber = Color(0xFFF09F9D) // 저장된 코스 상세 장소 카드 번호(1,2,3...) 텍스트
val TermsText = Color(0xFFB3B3B3) // 이용약관 텍스트
val TextPlaceholder = Color(0xFFA3A3A3) // 메모/입력창 placeholder 텍스트
val TextMuted = Color(0xFF888888) // 로그아웃 등 낮은 강조도의 텍스트