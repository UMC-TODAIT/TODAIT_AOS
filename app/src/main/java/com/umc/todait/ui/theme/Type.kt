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
 * res/font 에 Regular/Medium/SemiBold/Bold 4종이 있다. 시안이 쓰는 굵기는 이 4종이 전부이므로
 * 합성(synthetic) 굵기로 대체되는 텍스트는 없다.
 */
val Suit = FontFamily(
    Font(R.font.suit_regular, FontWeight.Normal),
    Font(R.font.suit_medium, FontWeight.Medium),
    Font(R.font.suit_semibold, FontWeight.SemiBold),
    Font(R.font.suit_bold, FontWeight.Bold),
)

/** Material3 기본 타이포그래피(크기·자간은 그대로, 폰트만 SUIT 로 바꿔 쓰기 위한 원본). */
private val Material = Typography()

private fun TextStyle.suit() = copy(fontFamily = Suit)

/**
 * 투데잇 타이포그래피. 전역 폰트는 [Suit].
 *
 * ⚠️ 15개 슬롯을 **모두** 채운다. 일부만 채우면 나머지 슬롯은 Material 기본값(Roboto)으로 남고,
 * 그 슬롯을 쓰는 화면(예: titleSmall 을 쓰는 홈 코스/장소 카드, bodyLarge 를 쓰는 OutlinedTextField)만
 * 폰트가 다르게 나오는 문제가 생긴다. 시안에 정의된 5종만 값을 커스텀하고,
 * 나머지는 Material 기본 크기 그대로 폰트만 SUIT 로 교체한다.
 */
val TodaitTypography = Typography(
    displayLarge = Material.displayLarge.suit(),
    displayMedium = Material.displayMedium.suit(),
    displaySmall = Material.displaySmall.suit(),
    headlineLarge = Material.headlineLarge.suit(),
    headlineMedium = Material.headlineMedium.suit(),
    // 화면 타이틀 (예: "코스 구성하기")
    headlineSmall = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = Material.titleLarge.suit(),
    // 섹션 타이틀 (예: "지금 내 주변 핫플")
    titleMedium = TextStyle(
        fontFamily = Suit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = Material.titleSmall.suit(),
    bodyLarge = Material.bodyLarge.suit(),
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
    labelMedium = Material.labelMedium.suit(),
    labelSmall = Material.labelSmall.suit(),
)
