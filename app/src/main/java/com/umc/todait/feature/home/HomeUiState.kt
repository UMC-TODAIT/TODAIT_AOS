package com.umc.todait.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceDto

/** "오늘의 추천 코스" 카드 한 장. */
data class CourseCardUiModel(
    val courseId: Long,
    val title: String,
    val hashtags: List<String>,
    val gradientStart: Color,
    val gradientEnd: Color,
    @DrawableRes val imageRes: Int,
    @DrawableRes val decorationRes: Int,
)

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
    val courses: List<CourseCardUiModel> = emptyList(),
    val placesState: HomePlacesState = HomePlacesState.Loading,
)

/** "취향 기반 추천 장소" 섹션 상태. */
sealed interface HomePlacesState {
    data object Loading : HomePlacesState
    data class Success(val places: List<RecommendedPlaceUiModel>) : HomePlacesState
    data class Empty(val message: String) : HomePlacesState
    data class Error(val message: String) : HomePlacesState
}
