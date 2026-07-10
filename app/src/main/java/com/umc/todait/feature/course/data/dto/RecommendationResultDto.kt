package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "추천 장소 조회"(GET /api/recommendations/places)의 result.
 *
 * 사용자 위치·분위기/음식 카테고리·코스 초안(draft)을 기반으로 한 추천 장소 목록.
 * (BaseResponse<RecommendationResultDto> 형태로 내려온다.)
 *
 * recommendationLogId 는 추천 요청 1건마다 생성되며, 이후 장소 선택/저장 로그(place_action_log)와
 * 연결하기 위해 응답에 포함된다.
 *
 * ⚠️ TODAIT_BE 스펙 확정본 기준. 필드 추가/변경 시 명세서와 대조해 수정한다.
 */
data class RecommendationResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("recommendationType") val recommendationType: String,
    // 조건에 맞는 추천 장소가 없으면 RECOMMEND404 로 처리된다.
    @SerializedName("places") val places: List<RecommendedPlaceDto>,
)

/**
 * 추천 결과의 장소 카드 한 장. rankNo(rank_no) 오름차순으로 내려온다.
 * matchedMoodCount / matchedFoodCount 는 선택한 태그와 몇 개 일치했는지 나타낸다.
 */
data class RecommendedPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("rankNo") val rankNo: Int,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("reasonText") val reasonText: String?,
    // latitude/longitude 파라미터가 있을 때만 계산돼 내려오며, 없으면 null.
    @SerializedName("distanceMeters") val distanceMeters: Int? = null,
    @SerializedName("matchedMoodCount") val matchedMoodCount: Int,
    @SerializedName("matchedFoodCount") val matchedFoodCount: Int,
)
