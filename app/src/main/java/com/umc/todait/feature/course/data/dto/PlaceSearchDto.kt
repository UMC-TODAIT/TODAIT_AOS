package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "장소명 검색"(GET /api/places/search?keyword=)의 result 래퍼.
 * (BaseResponse<PlaceSearchResponseDto> 형태로 내려온다.)
 *
 * ⚠️ 배포 스펙(Swagger) 기준. 이전 명세의 `/api/search/places`(검색 결과 목록)와
 * 페이징 필드는 현재 서버 응답에 없다. keyword 파라미터 하나만 받는다.
 */
data class PlaceSearchResponseDto(
    // 검색 결과가 없으면 빈 배열([])로 내려온다.
    @SerializedName("places") val places: List<PlaceResponseDto>,
)

/**
 * 검색 결과 카드 한 장. 배포 서버 `PlaceResponse` 스키마.
 *
 * - category/카테고리는 문자열이 아니라 [placeCategory] 객체로 내려온다.
 * - imageUrl 이 아니라 [defaultImageUrl] 이다.
 * - moodTags 는 문자열 배열이 아니라 [MoodTagSummaryDto] 객체 배열이다.
 * - areaName(지역명)은 이 응답에 없다(지원 지역 검증은 GET /api/areas 로 별도 처리 예정).
 */
data class PlaceResponseDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("defaultImageUrl") val defaultImageUrl: String?,
    @SerializedName("placeCategory") val placeCategory: PlaceCategorySummaryDto?,
    @SerializedName("primaryFoodCategory") val primaryFoodCategory: FoodCategorySummaryDto?,
    @SerializedName("moodTags") val moodTags: List<MoodTagSummaryDto>? = null,
)

/** 장소 대분류(카페/식당 등) 요약. */
data class PlaceCategorySummaryDto(
    @SerializedName("placeCategoryId") val placeCategoryId: Long,
    @SerializedName("name") val name: String,
)

/** 음식 카테고리 요약. */
data class FoodCategorySummaryDto(
    @SerializedName("foodCategoryId") val foodCategoryId: Long,
    @SerializedName("name") val name: String,
)

/** 분위기 태그 요약. */
data class MoodTagSummaryDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("name") val name: String,
)
