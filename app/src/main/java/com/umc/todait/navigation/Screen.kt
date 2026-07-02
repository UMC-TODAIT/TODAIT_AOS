package com.umc.todait.navigation

/**
 * 앱의 전체 화면 라우트 정의.
 * README의 "화면 목록 & 담당자" 표와 1:1로 대응한다.
 */
sealed class Screen(val route: String) {
    // Auth (팀원2)
    data object Login : Screen("login")
    data object SignupComplete : Screen("signup_complete")

    // Home (팀원2)
    data object Home : Screen("home")

    // Course 생성 플로우
    data object MoodSelect : Screen("course/mood")            // 팀원3
    data object FoodSelect : Screen("course/food")            // 팀원3
    data object BasePlace : Screen("course/base_place")       // 강서윤
    data object CourseCompose : Screen("course/compose")      // 강서윤
    data object SelectedPlaces : Screen("course/selected")    // 강서윤
    data object CourseSave : Screen("course/save")            // 팀원3

    // Saved (팀원3)
    data object SavedCourses : Screen("saved")
    data object CourseDetail : Screen("saved/{courseId}") {
        const val ARG_COURSE_ID = "courseId"
        fun createRoute(courseId: Long) = "saved/$courseId"
    }

    // MyPage (팀원2)
    data object MyPage : Screen("mypage")
}
