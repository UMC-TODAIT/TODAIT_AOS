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
val Gray100 = Color(0xFFF4F4F4)
val White = Color(0xFFFFFFFF)

// Semantic
val Error = Color(0xFFF04438)
val Success = Color(0xFF12B76A)
val Background = White
val Surface = White
val OnPrimary = White
