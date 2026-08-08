package com.umc.todait.feature.saved.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "저장 코스 상세 조회"(GET /api/courses/{courseId})의 result.
 * (BaseResponse<CourseDetailResponseDto> 형태로 내려온다.)
 */
data class CourseDetailResponseDto(
    @SerializedName("courseId") val courseId: Long,
    @SerializedName("title") val title: String,
    // ISO-8601 날짜 문자열(yyyy-MM-dd).
    @SerializedName("savedDate") val savedDate: String,

    @SerializedName("representativeMoodTag") val representativeMoodTag: DetailMoodTagDto?,
    @SerializedName("representativePlaceCategory") val representativePlaceCategory: DetailPlaceCategoryDto?,
    @SerializedName("memo") val memo: String?,
    @SerializedName("placeCount") val placeCount: Int,
    @SerializedName("viewCount") val viewCount: Int,

    // visitOrder 오름차순. 장소가 없어도 빈 배열([])로 내려온다.
    @SerializedName("places") val places: List<DetailCoursePlaceDto>
)

data class DetailMoodTagDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class DetailPlaceCategoryDto(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String
)

data class DetailCoursePlaceDto(
    @SerializedName("coursePlaceId") val coursePlaceId: Long,
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("visitOrder") val visitOrder: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("memo") val memo: String?
)
