package com.umc.todait.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.umc.todait.R

/**
 * 투데잇 기본 폰트 — SUIT (Figma 시안 폰트).
 * res/font 에 Regular/Medium/SemiBold 3종이 있으며, 그 외 굵기(Bold 등)는
 * 가장 가까운 굵기에서 합성(synthetic)된다.
 */
val Suit = FontFamily(
    Font(R.font.suit_regular, FontWeight.Normal),
    Font(R.font.suit_medium, FontWeight.Medium),
    Font(R.font.suit_semibold, FontWeight.SemiBold),
)

/**
 * 투데잇 타이포그래피. 전역 폰트는 [Suit].
 * (bare Text 에도 적용되도록 TodaitTheme 에서 LocalTextStyle 로도 함께 주입한다.)
 */
val TodaitTypography = Typography(
    // 화면 타이틀 (예: "코스 구성하기")
    headlineSmall = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    // 섹션 타이틀 (예: "지금 내 주변 핫플")
    titleMedium = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    // 본문 (장소명, 설명 문구)
    bodyMedium = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // 보조 텍스트 (주소, 추천 이유)
    bodySmall = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    // 버튼 / 태그 칩
    labelLarge = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
)