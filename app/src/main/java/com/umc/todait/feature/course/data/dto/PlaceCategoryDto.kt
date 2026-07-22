package com.umc.todait.feature.course.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "장소 대분류 목록 조회"(GET /api/place-categories)의 result 래퍼.
 * (BaseResponse<PlaceCategoryListResponseDto> 형태로 내려온다.)
 *
 * 코스 구성하기 화면의 카페/식당/액티비티/술 탭 기준 데이터. 배포 스펙(Swagger) 기준.
 */
data class PlaceCategoryListResponseDto(
    @SerializedName("placeCategories") val placeCategories: List<PlaceCategoryResponseDto>,
)

/** 장소 대분류 한 건. [sortOrder] 오름차순으로 탭을 노출한다. */
data class PlaceCategoryResponseDto(
    @SerializedName("placeCategoryId") val placeCategoryId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("sortOrder") val sortOrder: Int,
)
