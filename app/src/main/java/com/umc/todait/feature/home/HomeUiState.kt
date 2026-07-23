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

/** moodTag.code(HIP/QUIET/ACTIVE/ROMANTIC/MODERN/CALM) → 카드 그라디언트. 코드가 없거나 매칭 안 되면 CALM으로 대체. */
private val MOOD_GRADIENTS: Map<String, Pair<Color, Color>> = mapOf(
    "HIP" to (CourseHipGradientStart to CourseHipGradientEnd),
    "QUIET" to (CourseQuietGradientStart to CourseQuietGradientEnd),
    "ACTIVE" to (CourseActiveGradientStart to CourseActiveGradientEnd),
    "ROMANTIC" to (CourseRomanticGradientStart to CourseRomanticGradientEnd),
    "MODERN" to (CourseModernGradientStart to CourseModernGradientEnd),
    "CALM" to (CourseCalmGradientStart to CourseCalmGradientEnd),
)

/**
 * 카드 배경 장식 도형. API가 도형을 내려주지 않아 courseId 순서로 기존 에셋을 순환 배정한다
 * (분위기별 고정 매핑 규칙 미확정 — 디자인 확인 필요).
 */
private val COURSE_DECORATIONS: List<Int> = listOf(
    R.drawable.ic_home_course_flower,
    R.drawable.ic_home_deco_1,
    R.drawable.ic_home_deco_2,
    R.drawable.ic_home_deco_3,
    R.drawable.ic_home_deco_4,
)

fun RecommendedCourseSummaryDto.toUiModel(index: Int): CourseCardUiModel {
    val (gradientStart, gradientEnd) = MOOD_GRADIENTS[representativeMoodTag?.code]
        ?: (CourseCalmGradientStart to CourseCalmGradientEnd)
    return CourseCardUiModel(
        courseId = courseId,
        title = title,
        hashtags = listOfNotNull(
            representativeMoodTag?.name?.let { "#$it" },
            representativePlaceCategory?.name?.let { "#${it.replace(" ", "")}" },
        ),
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
        imageUrl = representativeImageUrl,
        decorationRes = COURSE_DECORATIONS[index % COURSE_DECORATIONS.size],
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
    imageUrl = representativeImageUrl,
    recommendReason = recommendReason,
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
