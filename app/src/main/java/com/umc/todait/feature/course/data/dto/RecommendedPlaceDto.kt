package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "카테고리별 장소 카드 목록 조회"
 * (GET /api/course-drafts/{courseDraftId}/recommended-places?placeCategoryCode=&size=)의 result.
 * (BaseResponse<RecommendedPlaceResultDto> 형태로 내려온다.)
 *
 * 코스 구성하기 화면의 카테고리 탭(카페/식당/액티비티/바)별 추천 카드에 사용한다.
 * 기준 장소가 확정된 뒤(draftStatus = PLACE_SELECTING) 호출한다.
 */
data class RecommendedPlaceResultDto(
    @SerializedName("recommendationLogId") val recommendationLogId: Long,
    @SerializedName("courseDraftId") val courseDraftId: Long,
    // 조회 API 라 상태를 바꾸지 않는다. 정상값은 PLACE_SELECTING.
    @SerializedName("draftStatus") val draftStatus: String,
    // 이번 추천에 적용된 카테고리.
    @SerializedName("placeCategory") val placeCategory: PlaceCategorySummaryDto,
    // 추천 기준이 된 기준 장소.
    @SerializedName("basePlace") val basePlace: BasePlaceSummaryDto,
    // 추천 결과를 채우기 위해 실제 적용한 조건 완화 단계(1~4). 화면에는 노출하지 않는다.
    @SerializedName("appliedRelaxationLevel") val appliedRelaxationLevel: Int,
    // 추천 결과가 없어도 null 이 아니라 빈 배열([])로 내려온다. 배열 순서가 곧 추천 순위다.
    @SerializedName("places") val places: List<RecommendedPlaceDto>,
)

/** 추천 기준이 되는 기준 장소 요약. */
data class BasePlaceSummaryDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("area") val area: AreaSummaryDto,
)

/** 카테고리별 추천 카드 한 장. */
data class RecommendedPlaceDto(
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
    @SerializedName("distanceMeters") val distanceMeters: Int?,
    @SerializedName("matchedMoodCount") val matchedMoodCount: Int,
    // 실제로 일치한 분위기 태그. 카드 배경색(분위기별 색상) 결정에 사용한다.
    @SerializedName("matchedMoodTags") val matchedMoodTags: List<MoodTagSummaryDto>,
    @SerializedName("matchedFoodCount") val matchedFoodCount: Int?,
    // 분위기·음식·거리·지역 점수 합계(내부 지표). 화면에 표시하지 않는다.
    @SerializedName("internalScore") val internalScore: Int,
    // 충족한 추천 근거 문구 최대 2개. 서버가 준 순서대로 노출한다.
    @SerializedName("recommendationReasons") val recommendationReasons: List<String>,
    // 이미 임시 코스에 담긴 장소인지. 정상 응답에서는 기본적으로 false.
    @SerializedName("alreadySelected") val alreadySelected: Boolean,
    @SerializedName("detailAvailable") val detailAvailable: Boolean,
)
