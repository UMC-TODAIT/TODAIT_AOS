package com.umc.todait.feature.home.coursedetail

import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCoursePlaceDto

/**
 * 지도 핀 + 리스트 한 행에 쓰는 장소 하나.
 * [coursePlaceId](코스-장소 관계 ID)와 [placeId](장소 마스터 ID)는 다른 식별자다 — 혼동 금지(JSON 필드 사전 §3).
 * 장소 상세 화면 이동에는 반드시 [placeId]를 쓴다.
 */
data class CourseDetailPlaceUiModel(
    val coursePlaceId: Long,
    val placeId: Long?,
    val visitOrder: Int,
    val name: String,
    val address: String,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
)

fun RecommendedCoursePlaceDto.toUiModel(): CourseDetailPlaceUiModel = CourseDetailPlaceUiModel(
    coursePlaceId = coursePlaceId,
    placeId = placeId,
    visitOrder = visitOrder,
    name = name,
    address = address,
    imageUrl = representativeImageUrl,
    latitude = latitude,
    longitude = longitude,
)

/** 추천 코스 상세 화면 상태. */
data class RecommendedCourseDetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val hashtags: List<String> = emptyList(),
    /** 장소 카드 그라디언트에 쓰는 대표 분위기 코드(HIP/QUIET/... ). 모르는 값·null 이면 CALM 색으로 대체된다. */
    val moodTagCode: String? = null,
    val places: List<CourseDetailPlaceUiModel> = emptyList(),
    val loadError: String? = null,
    val isSaveConfirmDialogVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSavedDialogVisible: Boolean = false,
) {
    val placeCount: Int get() = places.size
}

fun RecommendedCourseDetailDto.toUiState(): RecommendedCourseDetailUiState = RecommendedCourseDetailUiState(
    isLoading = false,
    title = title,
    hashtags = listOfNotNull(
        representativeMoodTag?.name?.let { "#$it" },
        representativePlaceCategory?.name?.let { "#${it.replace(" ", "")}" },
    ),
    moodTagCode = representativeMoodTag?.code,
    places = places.map { it.toUiModel() },
)

/** 화면 밖으로 나가는 일회성 효과(네비게이션). */
sealed interface RecommendedCourseDetailEffect {
    data object NavigateToSavedCourses : RecommendedCourseDetailEffect
}
