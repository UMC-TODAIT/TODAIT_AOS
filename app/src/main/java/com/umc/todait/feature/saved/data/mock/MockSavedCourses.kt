package com.umc.todait.feature.saved.data.mock

import com.umc.todait.feature.saved.data.dto.*

object SavedCoursesMock {
    private val courseList = listOf(

        SavedCourseDto(
            courseId = 1,
            title = "연희동 데이트 코스",
            savedDate = "2026.06.18",

            representativeMoodTag = MoodTagDto(
                moodTagId = 1,
                code = "ROMANTIC",
                name = "로맨틱"
            ),

            representativeFoodCategory = FoodCategoryDto(
                foodCategoryId = 1,
                code = "WESTERN",
                name = "양식"
            ),

            previewPlaces = listOf(
                PreviewPlaceDto(1, "꿔노이", 1),
                PreviewPlaceDto(2, "코이크", 2),
                PreviewPlaceDto(3, "121 르말뒤페이", 3),
                PreviewPlaceDto(4, "장소", 4),
                PreviewPlaceDto(5, "장소", 5)
            ),

            remainingPlaceCount = 0,
            placeCount = 5,
            viewCount = 10
        ),

        SavedCourseDto(
            courseId = 2,
            title = "힐링하고 싶은 날",
            savedDate = "2026.06.22",

            representativeMoodTag = MoodTagDto(
                moodTagId = 2,
                code = "CALM",
                name = "차분한"
            ),

            representativeFoodCategory = FoodCategoryDto(
                foodCategoryId = 2,
                code = "KOREAN",
                name = "한식"
            ),

            previewPlaces = listOf(
                PreviewPlaceDto(11, "창덕궁", 1),
                PreviewPlaceDto(12, "이밥", 2),
                PreviewPlaceDto(13, "리와인드 서울", 3),
                PreviewPlaceDto(14, "장소", 4)
            ),

            remainingPlaceCount = 0,
            placeCount = 4,
            viewCount = 6
        )
    )

    val savedCourses = SavedCoursesResponseDto(
        recentCourses = courseList,
        frequentlyViewedCourses = courseList
    )
}