package com.umc.todait.feature.home.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "홈 화면 추천 장소 목록 조회"(GET /api/recommended-places) 의 result.
 *
 * ⚠️ 필드명은 "JSON 필드 사전 v1.0"(2026-07-25) 기준. 필드 추가/변경 시 사전과 대조해 수정한다.
 */
data class HomeRecommendedPlaceResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("places") val places: List<HomeRecommendedPlaceDto>,
    @SerializedName("locationAvailable") val locationAvailable: Boolean,
    @SerializedName("size") val size: Int,
    // 추천 코스 목록과 동일하게 커서 방식이다. 마지막 페이지면 nextCursor 가 null 이다.
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("nextCursor") val nextCursor: String?,
)

data class HomeRecommendedPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("area") val area: HomeAreaDto,
    @SerializedName("category") val category: PlaceCategoryDto,
    @SerializedName("subCategory") val subCategory: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("rank") val rank: Int,
    @SerializedName("distanceMeters") val distanceMeters: Int?,
    @SerializedName("isNearby") val isNearby: Boolean?,
    @SerializedName("recommendationReason") val recommendationReason: String,
    @SerializedName("detailAvailable") val detailAvailable: Boolean,
)

data class HomeAreaDto(
    @SerializedName("areaId") val areaId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)
