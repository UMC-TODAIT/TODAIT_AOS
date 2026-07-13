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
