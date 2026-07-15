package com.umc.todait.navigation

import com.umc.todait.R

/**
 * 하단 탭바: 홈 / 코스 생성 / 저장된 코스 / 마이페이지
 */
enum class BottomTab(
    val route: String,
    val label: String,
    val iconRes: Int,
) {
    HOME(
        Screen.Home.route,
        "홈",
        R.drawable.ic_bottom_home
    ),

    CREATE(
        Screen.MoodSelect.route,
        "코스 생성",
        R.drawable.ic_bottom_course
    ),

    SAVED(
        Screen.SavedCourses.route,
        "저장된 코스",
        R.drawable.ic_bottom_saved_course
    ),

    MYPAGE(
        Screen.MyPage.route,
        "마이페이지",
        R.drawable.ic_bottom_mypage
    )
}
