package com.umc.todait.navigation

/**
 * 앱의 전체 화면 라우트 정의.
 * README의 "화면 목록 & 담당자" 표와 1:1로 대응한다.
 */
sealed class Screen(val route: String) {
    // Auth (무즈/김규리)
    data object Login : Screen("login")
    data object TermsAgreement : Screen("terms_agreement")     // 카카오 로그인 최초 가입 시
    data object SignupComplete : Screen("signup_complete")

    // Home (무즈/김규리)
    data object Home : Screen("home")

    // Course 생성 플로우
    data object MoodSelect : Screen("course/mood")            // 무즈/김규리
    data object FoodSelect : Screen("course/food")            // 무즈/김규리
    data object BasePlace : Screen("course/base_place")       // 티아/강서윤
    data object CourseCompose : Screen("course/compose")      // 티아/강서윤
    data object SelectedPlaces : Screen("course/selected")    // 티아/강서윤
    data object CourseSave : Screen("course/save")            // 티아/강서윤

    // Saved (지니/황지희)
    data object SavedCourses : Screen("saved")
    data object CourseDetail : Screen("saved/{courseId}") {
        const val ARG_COURSE_ID = "courseId"
        fun createRoute(courseId: Long) = "saved/$courseId"
    }

    // MyPage (지니/황지희)
    data object MyPage : Screen("mypage")
}
