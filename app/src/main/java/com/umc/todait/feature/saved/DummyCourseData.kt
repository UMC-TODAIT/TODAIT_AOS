package com.umc.todait.feature.saved

import com.umc.todait.R

val recentCourses = listOf(
    CourseUiModel(
        id=1,
        backgroundImage = R.drawable.bg_saved_courses_romantic,
        topImage = R.drawable.ic_mood_romantic,
        title = "연희동 데이트 코스",
        date = "2026.07.08",
        moodTag = R.drawable.ic_tag_romantic,
        foodTag = R.drawable.ic_tag_westernfood,
        places = listOf("꿔노이", "코이크", "121 르말뒤페이", "장소", "장소")
    ),
    CourseUiModel(
        id=2,
        backgroundImage = R.drawable.bg_saved_courses_calm,
        topImage = R.drawable.ic_mood_calm,
        title = "힐링하고 싶은 날",
        date = "2026.07.07",
        moodTag = R.drawable.ic_tag_romantic,
        foodTag = R.drawable.ic_tag_westernfood,
        places = listOf("창덕궁", "이밥", "리와인드 서울", "장소")
    )
)

val popularCourses = recentCourses

val coursePlaces = mapOf(
    1L to listOf(
        PlaceUiModel(
            isStartPlace = true,
            name = "연희동",
            address = "서울 서대문구 연희동",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        ),
        PlaceUiModel(
            isStartPlace = false,
            name = "꿔노이",
            address = "서울 서대문구 연희맛로",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        ),
        PlaceUiModel(
            isStartPlace = false,
            name = "코이크",
            address = "서울 서대문구 연희로",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        ),
        PlaceUiModel(
            isStartPlace = false,
            name = "121 르말뒤페이",
            address = "서울 서대문구...",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        )
    ),

    2L to listOf(
        PlaceUiModel(
            isStartPlace = true,
            name = "창덕궁",
            address = "서울 종로구 율곡로",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        ),
        PlaceUiModel(
            isStartPlace = false,
            name = "이밥",
            address = "서울 종로구...",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        ),
        PlaceUiModel(
            isStartPlace = false,
            name = "리와인드 서울",
            address = "서울 종로구...",
            backgroundImage = R.drawable.bg_saved_courses_romantic
        )
    )
)