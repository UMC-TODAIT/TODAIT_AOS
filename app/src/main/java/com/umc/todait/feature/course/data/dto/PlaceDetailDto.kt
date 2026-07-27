package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "장소 카드 상세 조회"(GET /api/places/{placeId})의 result.
 * (BaseResponse<PlaceDetailDto> 형태로 내려온다.)
 *
 * 장소 상세 화면은 이 응답 하나만으로 구성한다(사진·메뉴·내부 사진 포함).
 * internal_score / popularity_score 등 내부 지표는 응답에 노출되지 않는다.
 */
data class PlaceDetailDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("phone") val phone: String?,
    @SerializedName("subCategory") val subCategory: String?,
    // imageUrls 가 비었을 때 사용할 대표·대체 이미지.
    @SerializedName("defaultImageUrl") val defaultImageUrl: String?,
    // 서버가 영업시간과 현재 시각을 비교해 계산한 값. OPEN / CLOSED.
    @SerializedName("businessStatus") val businessStatus: String?,
    // "HH:mm" 문자열. 라스트오더 정보가 없는 장소는 null.
    @SerializedName("lastOrderTime") val lastOrderTime: String?,
    @SerializedName("placeCategory") val placeCategory: PlaceCategorySummaryDto,
    @SerializedName("primaryFoodCategory") val primaryFoodCategory: FoodCategorySummaryDto?,
    @SerializedName("foodCategories") val foodCategories: List<FoodCategorySummaryDto>,
    @SerializedName("moodTags") val moodTags: List<MoodTagSummaryDto>,
    // 상세 상단 캐러셀용 이미지(place_image 중 image_type = MAIN).
    @SerializedName("imageUrls") val imageUrls: List<String>,
    // "내부 사진" 섹션 전용 이미지(place_image 중 image_type = INTERIOR).
    @SerializedName("interiorImageUrls") val interiorImageUrls: List<String>,
    @SerializedName("menus") val menus: List<PlaceMenuDto>,
    @SerializedName("defaultRecommendReason") val defaultRecommendReason: String?,
)

/** 메뉴 한 개. [price] 가 null 이면 가격 변동 메뉴로, 화면에는 "변동"으로 표시한다. */
data class PlaceMenuDto(
    @SerializedName("placeMenuId") val placeMenuId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int?,
    @SerializedName("imageUrl") val imageUrl: String?,
)
