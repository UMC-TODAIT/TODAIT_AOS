package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "홈 화면 추천 장소 목록 조회"(GET /api/recommended-places) 의 result.
 *
 * ⚠️ TODAIT_BE 스펙 확정본(2026-07-22) 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class HomeRecommendedPlaceResultDto(
    @SerializedName("places") val places: List<HomeRecommendedPlaceDto>,
    @SerializedName("locationApplied") val locationApplied: Boolean,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
)

data class HomeRecommendedPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("representativeImageUrl") val representativeImageUrl: String?,
    @SerializedName("imageType") val imageType: String,
    @SerializedName("area") val area: HomeAreaDto,
    @SerializedName("distance") val distance: Int?,
    @SerializedName("isNearby") val isNearby: Boolean?,
    @SerializedName("recommendReason") val recommendReason: String,
)

data class HomeAreaDto(
    @SerializedName("areaId") val areaId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)
