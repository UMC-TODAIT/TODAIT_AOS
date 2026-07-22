package com.umc.todait.feature.saved.data.dto

data class SavedCoursesResponseDto(
    val recentCourses: List<SavedCourseDto>,
    val frequentlyViewedCourses: List<SavedCourseDto>,
)

data class SavedCourseDto(
    val courseId: Long,
    val title: String,
    val savedDate: String,
    val representativeMoodTag: MoodTagDto?,
    val representativeFoodCategory: FoodCategoryDto?,
    val previewPlaces: List<PreviewPlaceDto>,
    val remainingPlaceCount: Int,
    val placeCount: Int,
    val viewCount: Int,
)

data class MoodTagDto(
    val moodTagId: Long,
    val code: String,
    val name: String,
)

data class FoodCategoryDto(
    val foodCategoryId: Long,
    val code: String,
    val name: String,
)

data class PreviewPlaceDto(
    val placeId: Long,
    val name: String,
    val visitOrder: Int,
)