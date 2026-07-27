package com.umc.todait.feature.saved.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "저장 코스 목록 조회"(GET /api/courses/me/overview)의 result.
 * (BaseResponse<SavedCoursesResponseDto> 형태로 내려온다.)
 *
 * 저장된 코스가 없어도 null 이 아니라 빈 배열([])로 내려온다.
 */
data class SavedCoursesResponseDto(
    @SerializedName("recentCourses") val recentCourses: List<SavedCourseDto>,
    // 화면의 "자주 본 코스" 섹션. 명세 필드명은 frequentlyViewedCourses 다.
    @SerializedName("frequentlyViewedCourses") val frequentlyViewedCourses: List<SavedCourseDto>,
)

data class SavedCourseDto(
    @SerializedName("courseId") val courseId: Long,
    @SerializedName("title") val title: String,
    // ISO-8601 날짜 문자열(yyyy-MM-dd).
    @SerializedName("savedDate") val savedDate: String,
    @SerializedName("representativeMoodTag") val representativeMoodTag: MoodTagDto?,
    @SerializedName("representativeFoodCategory") val representativeFoodCategory: FoodCategoryDto?,
    @SerializedName("previewPlaces") val previewPlaces: List<PreviewPlaceDto>,
    @SerializedName("remainingPlaceCount") val remainingPlaceCount: Int,
    @SerializedName("placeCount") val placeCount: Int,
    @SerializedName("viewCount") val viewCount: Int,
)

data class MoodTagDto(
    @SerializedName("moodTagId") val moodTagId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

data class FoodCategoryDto(
    @SerializedName("foodCategoryId") val foodCategoryId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
)

data class PreviewPlaceDto(
    @SerializedName("placeId") val placeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("visitOrder") val visitOrder: Int,
)
