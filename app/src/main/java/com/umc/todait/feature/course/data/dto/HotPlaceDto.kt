package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "지금 내 주변 핫플 조회"(GET /api/course-drafts/{courseDraftId}/hot-places)의 result.
 * (BaseResponse<HotPlaceResultDto> 형태로 내려온다.)
 *
 * 기준 장소 설정 화면에 처음 진입했을 때(검색어 입력 전) 보여줄 추천 목록이다.
 * 검색어를 입력하면 이 API 대신 장소 검색 API(GET /api/places/search?query=)를 호출한다.
 *
 * 위치 권한이 없으면 latitude·longitude 를 빼고 호출하며, 이때 [locationAvailable] 이 false 로
 * 내려오고 거리 관련 필드는 null 이 된다.
 */
data class HotPlaceResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 임시 코스 상태. 이 API 는 조회만 하므로 정상값은 BASE_PLACE_SELECTING 이다.
    // (DB/Entity 는 status 지만 API JSON 은 draftStatus 를 사용한다.)
    @SerializedName("draftStatus") val draftStatus: String,
    // 이번 추천에 사용자 현재 위치가 사용됐는지.
    @SerializedName("locationAvailable") val locationAvailable: Boolean,
    // 추천할 장소가 없어도 null 이 아니라 빈 배열([])로 내려온다. 배열 순서가 곧 노출 순서다.
    @SerializedName("places") val places: List<HotPlaceDto>,
)

/** 주변 핫플 카드 한 장. [rank] 오름차순으로 이미 정렬돼 내려온다. */
data class HotPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("area") val area: AreaSummaryDto,
    @SerializedName("category") val category: PlaceCategorySummaryDto,
    @SerializedName("subCategory") val subCategory: String?,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("rank") val rank: Int,
    // 위치정보 없이 추천한 경우 null.
    @SerializedName("distanceMeters") val distanceMeters: Int?,
    // 직선거리 500m 이내 여부. 위치정보가 없으면 null.
    @SerializedName("isNearby") val isNearby: Boolean?,
    @SerializedName("matchedMoodCount") val matchedMoodCount: Int,
    // 음식 기준을 적용하지 않는 장소는 null.
    @SerializedName("matchedFoodCount") val matchedFoodCount: Int?,
    // 카드에 표시할 추천 문구. 프론트가 직접 계산하지 않고 서버 문자열을 그대로 노출한다.
    @SerializedName("recommendationReason") val recommendationReason: String,
    @SerializedName("detailAvailable") val detailAvailable: Boolean,
)
