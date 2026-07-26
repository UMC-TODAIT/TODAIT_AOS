package com.umc.todait.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.umc.todait.R
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSummaryDto
import com.umc.todait.ui.theme.CourseActiveGradientEnd
import com.umc.todait.ui.theme.CourseActiveGradientStart
import com.umc.todait.ui.theme.CourseCalmGradientEnd
import com.umc.todait.ui.theme.CourseCalmGradientStart
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseModernGradientEnd
import com.umc.todait.ui.theme.CourseModernGradientStart
import com.umc.todait.ui.theme.CourseQuietGradientEnd
import com.umc.todait.ui.theme.CourseQuietGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart

/** "오늘의 추천 코스" 카드 한 장. */
data class CourseCardUiModel(
    val courseId: Long,
    val title: String,
    val hashtags: List<String>,
    val gradientStart: Color,
    val gradientEnd: Color,
    val imageUrl: String?,
    @DrawableRes val decorationRes: Int,
)

/**
 * moodTag.code(HIP/QUIET/ACTIVE/ROMANTIC/MODERN/CALM) → 카드 그라디언트. 코드가 없거나 매칭 안 되면 CALM으로 대체.
 * internal — [com.umc.todait.feature.home.coursedetail](같은 feature 하위 패키지)에서도 재사용한다.
 */
internal val MOOD_GRADIENTS: Map<String, Pair<Color, Color>> = mapOf(
    "HIP" to (CourseHipGradientStart to CourseHipGradientEnd),
    "QUIET" to (CourseQuietGradientStart to CourseQuietGradientEnd),
    "ACTIVE" to (CourseActiveGradientStart to CourseActiveGradientEnd),
    "ROMANTIC" to (CourseRomanticGradientStart to CourseRomanticGradientEnd),
    "MODERN" to (CourseModernGradientStart to CourseModernGradientEnd),
    "CALM" to (CourseCalmGradientStart to CourseCalmGradientEnd),
)

/**
 * moodTag.code → 카드 장식 문양. 분위기별 고정(피그마 취향설정 카드 문양 에셋 재사용). 매칭 안 되면 CALM.
 * internal — [com.umc.todait.feature.home.coursedetail]에서도 재사용한다.
 */
internal val MOOD_DECORATIONS: Map<String, Int> = mapOf(
    "HIP" to R.drawable.ic_mood_hip,
    "QUIET" to R.drawable.ic_mood_quiet,
    "ACTIVE" to R.drawable.ic_mood_active,
    "ROMANTIC" to R.drawable.ic_mood_romantic,
    "MODERN" to R.drawable.ic_mood_modern,
    "CALM" to R.drawable.ic_mood_calm,
)

fun RecommendedCourseSummaryDto.toUiModel(): CourseCardUiModel {
    // tags[0] = 대표 분위기(MOOD), tags[1] = 대표 장소 세부 카테고리(SUB_CATEGORY) — API 명세 순서 고정.
    val moodCode = tags.firstOrNull { it.type == "MOOD" }?.code
    val (gradientStart, gradientEnd) = MOOD_GRADIENTS[moodCode]
        ?: (CourseCalmGradientStart to CourseCalmGradientEnd)
    return CourseCardUiModel(
        courseId = courseId,
        title = title,
        hashtags = tags.map { "#${it.name.replace(" ", "")}" },
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
        imageUrl = representativeImageUrl,
        decorationRes = MOOD_DECORATIONS[moodCode] ?: R.drawable.ic_mood_calm,
    )
}

/** "오늘의 추천 코스" 섹션 상태. */
sealed interface CoursesState {
    data object Loading : CoursesState
    data class Success(val courses: List<CourseCardUiModel>) : CoursesState
    data class Empty(val message: String) : CoursesState
    data class Error(val message: String) : CoursesState
}

/** "취향 기반 추천 장소" 카드 한 장. */
data class RecommendedPlaceUiModel(
    val placeId: Long,
    val name: String,
    val address: String,
    val imageUrl: String?,
    /** 카드 하단 추천 문구. 서버가 isNearby/지역명 기준으로 생성해 내려준다("현재 위치와 가까워요" / "홍대 추천 장소예요" 등). */
    val recommendReason: String,
)

fun HomeRecommendedPlaceDto.toUiModel(): RecommendedPlaceUiModel = RecommendedPlaceUiModel(
    placeId = placeId,
    name = name,
    address = address,
    imageUrl = imageUrl,
    recommendReason = recommendationReason,
)

data class HomeUiState(
    val coursesState: CoursesState = CoursesState.Loading,
    val placesState: HomePlacesState = HomePlacesState.Loading,
)

/** "취향 기반 추천 장소" 섹션 상태. */
sealed interface HomePlacesState {
    data object Loading : HomePlacesState
    data class Success(val places: List<RecommendedPlaceUiModel>) : HomePlacesState
    data class Empty(val message: String) : HomePlacesState
    data class Error(val message: String) : HomePlacesState
}
