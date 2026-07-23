package com.umc.todait.feature.saved.data.mock

import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.DetailCoursePlaceDto
import com.umc.todait.feature.saved.data.dto.DetailFoodCategoryDto
import com.umc.todait.feature.saved.data.dto.DetailMoodTagDto

object MockCourseDetail {
    val detail = CourseDetailResponseDto(
        courseId = 1,
        title = "연희동 데이트 코스",
        savedDate = "2026.06.18",

        representativeMoodTag = DetailMoodTagDto(
            moodTagId = 1,
            code = "ROMANTIC",
            name = "로맨틱"
        ),

        representativeFoodCategory = DetailFoodCategoryDto(
            foodCategoryId = 1,
            code = "WESTERN",
            name = "양식"
        ),

        "",

        placeCount = 4,
        viewCount = 12,

        places = listOf(
            DetailCoursePlaceDto(
                coursePlaceId = 1,
                placeId = 1,
                visitOrder = 1,
                name = "연희동",
                address = "서울 서대문구 연희동",
                memo = ""
            ),
            DetailCoursePlaceDto(
                coursePlaceId = 2,
                placeId = 2,
                visitOrder = 2,
                name = "꿔노이",
                address = "서울 서대문구 연희맛로",
                memo = ""
            ),
            DetailCoursePlaceDto(
                coursePlaceId = 3,
                placeId = 3,
                visitOrder = 3,
                name = "코이크",
                address = "서울 서대문구 연희로",
                memo = ""
            ),
            DetailCoursePlaceDto(
                coursePlaceId = 4,
                placeId = 4,
                visitOrder = 4,
                name = "121 르말뒤페이",
                address = "서울 서대문구...",
                memo = ""
            )
        )
    )
}