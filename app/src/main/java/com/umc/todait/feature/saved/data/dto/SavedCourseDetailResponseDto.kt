package com.umc.todait.feature.saved.data.dto

data class CourseDetailResponseDto(
    val courseId: Long,
    val title: String,
    val savedDate: String,

    val representativeMoodTag: DetailMoodTagDto?,
    val representativeFoodCategory: DetailFoodCategoryDto?,

    val memo: String?,
    val placeCount: Int,
    val viewCount: Int,

    val places: List<DetailCoursePlaceDto>
)

data class DetailMoodTagDto(
    val moodTagId: Long,
    val code: String,
    val name: String
)

data class DetailFoodCategoryDto(
    val foodCategoryId: Long,
    val code: String,
    val name: String
)

data class DetailCoursePlaceDto(
    val coursePlaceId: Long,
    val placeId: Long,
    val visitOrder: Int,
    val name: String,
    val address: String,
    val memo: String?
)