package com.umc.todait.feature.saved

import com.umc.todait.R

val recentCourses = listOf(
    CourseUiModel(
        backgroundImage = R.drawable.bg_saved_courses_romantic,
        topImage = R.drawable.ic_icon_romantic,
        title = "연희동 데이트 코스",
        date = "2026.07.08",
        moodTag = R.drawable.ic_tag_romantic,
        foodTag = R.drawable.ic_tag_westernfood,
        places = listOf("꿔노이", "코이크", "121 르말뒤페이", "장소", "장소")
    ),
    CourseUiModel(
        backgroundImage = R.drawable.bg_saved_courses_calm,
        topImage = R.drawable.ic_icon_calm,
        title = "힐링하고 싶은 날",
        date = "2026.07.07",
        moodTag = R.drawable.ic_tag_romantic,
        foodTag = R.drawable.ic_tag_westernfood,
        places = listOf("창덕궁", "이밥", "리와인드 서울", "장소")
    )
)

val popularCourses = recentCourses
