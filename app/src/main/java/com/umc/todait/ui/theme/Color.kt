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
val Gray500 = Color(0xFF8A8A8A)
val Gray300 = Color(0xFFD6D6D6)
val Gray200 = Color(0xFFC7C7C7)
val Gray100 = Color(0xFFF4F4F4)
val White = Color(0xFFFFFFFF)

// Semantic
val Error = Color(0xFFF74141)
val Success = Color(0xFF62AC5E)
val Background = White
val Surface = White
val OnPrimary = White

// 소셜 로그인 브랜드 컬러 (앱 브랜드 컬러가 아니라 각 제공자 고정 컬러이므로 별도 토큰으로 분리)
val KakaoYellow = Color(0xFFFEE500)
val KakaoBrown = Color(0xFF191919)

// 배경 그라디언트 (Figma: linear-gradient(180deg, #FFFAF7 0%, #FFEDED 100%))
val BgGradientTop = Color(0xFFFFFAF7)
val BgGradientBottom = Color(0xFFFFEDED)

// 회원가입 폼 전용 (Figma 지정값)
val Pink400 = Color(0xFFFFBDBC)             // 가입완료 버튼 활성화 (Figma: PINK-400)
val VerifyPink = Color(0xFFF4A7A5)          // 인증번호 발송/재전송/확인(활성) 버튼
val DisabledButtonGray = Color(0xFFEEEEEE)  // 가입완료 버튼 비활성화
val DisabledConfirmGray = Gray200           // 확인 버튼 비활성화
val SignupBackground = Color(0xFFFDFBF3)    // 회원가입 화면 배경 (그라디언트 아님, 단색)
