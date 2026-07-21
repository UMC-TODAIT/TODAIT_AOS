package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "추천 장소 조회"(GET /api/recommendations/places) 의 result. 홈 "취향 기반 추천 장소" 섹션 전용.
 *
 * feature 간 직접 참조 금지 컨벤션 때문에 course 도메인의 RecommendationResultDto/RecommendedPlaceDto와
 * 동일한 API를 별도로 선언한다(중복 소지 있음 — 추후 공통 API 레이어를 core로 승격하면 정리 대상).
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class HomeRecommendedPlaceResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("places") val places: List<HomeRecommendedPlaceDto>,
)

data class HomeRecommendedPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("imageUrl") val imageUrl: String?,
)
