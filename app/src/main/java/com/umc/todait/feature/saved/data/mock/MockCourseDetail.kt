package com.umc.todait.feature.saved.data.mock

import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.DetailCoursePlaceDto
import com.umc.todait.feature.saved.data.dto.DetailMoodTagDto
import com.umc.todait.feature.saved.data.dto.DetailPlaceCategoryDto

object MockCourseDetail {

    val detail = CourseDetailResponseDto(
        courseId = 1L,
        title = "연희동 데이트 코스",
        savedDate = "2026-06-18",

        representativeMoodTag = DetailMoodTagDto(
            moodTagId = 1L,
            code = "ROMANTIC",
            name = "로맨틱"
        ),

        representativePlaceCategory = DetailPlaceCategoryDto(
            code = "WESTERN",
            name = "양식"
        ),

        memo = "",

        placeCount = 4,
        viewCount = 12,

        places = listOf(
            DetailCoursePlaceDto(
                coursePlaceId = 1L,
                placeId = 1L,
                visitOrder = 1,
                name = "연희동",
                imageUrl = "https://images.unsplash.com/photo-1519501025264-65ba15a82390",
                address = "서울 서대문구 연희동",
                memo = ""
            ),

            DetailCoursePlaceDto(
                coursePlaceId = 2L,
                placeId = 2L,
                visitOrder = 2,
                name = "꿔노이",
                imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4",
                address = "서울 서대문구 연희맛로",
                memo = ""
            ),

            DetailCoursePlaceDto(
                coursePlaceId = 3L,
                placeId = 3L,
                visitOrder = 3,
                name = "코이크",
                imageUrl = "https://images.unsplash.com/photo-1551024506-0bccd828d307",
                address = "서울 서대문구 연희로",
                memo = ""
            ),

            DetailCoursePlaceDto(
                coursePlaceId = 4L,
                placeId = 4L,
                visitOrder = 4,
                name = "121 르말뒤페이",
                imageUrl = "https://images.unsplash.com/photo-1515003197210-e0cd71810b5f",
                address = "서울 서대문구 연희동",
                memo = ""
            )
        )
    )
}