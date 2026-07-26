package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "카카오 API 장소 검색 결과 조회"(GET /api/places/search?query=)의 result.
 * (BaseResponse<PlaceSearchResultDto> 형태로 내려온다.)
 *
 * 기준 장소 설정 화면에서 검색어를 입력했을 때 사용한다. 서버가 카카오 Local API 결과를
 * 지원 지역·카테고리 필터를 거쳐 최대 10개까지 반환하며, **카카오 관련도 순서를 그대로 유지**한다.
 * 프론트에서 거리순/이름순으로 다시 정렬하지 않는다.
 */
data class PlaceSearchResultDto(
    // 앞뒤 공백을 제거한 뒤 실제 검색에 사용한 키워드.
    @SerializedName("query") val query: String,
    // places 배열의 개수(필터 적용 후). 카카오 원본 결과 수가 아니다. 0~10.
    @SerializedName("resultCount") val resultCount: Int,
    // 검색 결과가 없어도 null 이 아니라 빈 배열([])로 내려온다.
    @SerializedName("places") val places: List<SearchPlaceDto>,
)

/**
 * 검색 결과 카드 한 장.
 *
 * ⚠️ 내부 DB 미등록 장소도 함께 내려오므로 [placeId] 가 null 일 수 있다.
 * - [placeId] 가 있으면 기준 장소 설정 API 에 placeId 를 전달한다.
 * - null 이면 [externalPlaceId] 를 externalPlace.sourcePlaceId 로 전달한다.
 */
data class SearchPlaceDto(
    // 카카오가 부여한 외부 장소 ID. 내부 등록 여부와 무관하게 항상 존재한다.
    @SerializedName("externalPlaceId") val externalPlaceId: String,
    // 내부 DB에 등록된 장소면 place.id, 미등록이면 null.
    @SerializedName("placeId") val placeId: Long?,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("phone") val phone: String?,
    // 카카오 장소 상세 페이지 URL.
    @SerializedName("sourceUrl") val sourceUrl: String?,
    // 서비스 내부 지원 지역(홍대/연남/성수)으로 변환된 결과. 지원 지역 밖 장소는 응답에서 제외된다.
    @SerializedName("area") val area: AreaSummaryDto,
    // 서비스 내부 대분류(CAFE/RESTAURANT/ACTIVITY/BAR)로 변환된 결과.
    @SerializedName("category") val category: PlaceCategorySummaryDto,
    @SerializedName("subCategory") val subCategory: String?,
    // 내부 DB 등록 여부.
    @SerializedName("isRegistered") val isRegistered: Boolean,
    // 내부 대표 이미지 또는 카테고리 기본 이미지. 항상 값이 있다.
    @SerializedName("imageUrl") val imageUrl: String,
    // imageUrl 의 출처. PLACE_IMAGE(내부 대표 이미지) 또는 CATEGORY_DEFAULT(카테고리 기본 이미지).
    @SerializedName("imageType") val imageType: String,
    // 내부 장소 상세 화면으로 이동할 수 있는지. false 면 카드 탭 시 상세로 진입하지 않는다.
    @SerializedName("detailAvailable") val detailAvailable: Boolean,
)

/** 지원 지역 요약(홍대/연남/성수). */
data class AreaSummaryDto(
    @SerializedName("areaId") val areaId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

/** 장소 대분류(카페/식당/액티비티/바) 요약. */
data class PlaceCategorySummaryDto(
    @SerializedName("placeCategoryId") val placeCategoryId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

/** 음식 카테고리 요약. */
data class FoodCategorySummaryDto(
    @SerializedName("foodCategoryId") val foodCategoryId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

/** 분위기 태그 요약. */
data class MoodTagSummaryDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)
