package com.umc.todait.navigation

import java.net.URLEncoder

/**
 * 앱의 전체 화면 라우트 정의.
 * README의 "화면 목록 & 담당자" 표와 1:1로 대응한다.
 */
sealed class Screen(val route: String) {
    // Auth (무즈/김규리)
    data object Splash : Screen("splash")                       // 앱 최초 진입 — 저장된 토큰 확인 후 홈/로그인으로 자동 분기
    data object Login : Screen("login")
    data object EmailLogin : Screen("email_login")             // 첫화면 "이메일로 로그인/회원가입" 클릭 시
    data object TermsAgreement : Screen("terms_agreement/{flow}?token={token}") { // 로그인 화면(카카오/구글) 또는 이메일 로그인 화면(회원가입) 진입 시
        const val ARG_FLOW = "flow"                            // "email" | "kakao" | "google"
        const val ARG_TOKEN = "token"                          // 소셜 온보딩 임시 토큰(온보딩 완료 API 호출까지 들고 감). 이메일 플로우는 빈 문자열.
        fun createRoute(flow: String, token: String = "") =
            "terms_agreement/$flow?token=${URLEncoder.encode(token, "UTF-8")}"
    }
    data object TermDetail : Screen("terms_agreement/detail/{termId}") { // 약관 동의 화면에서 필수 약관 항목(화살표) 탭 시
        const val ARG_TERM_ID = "termId"
        fun createRoute(termId: Long) = "terms_agreement/detail/$termId"
    }
    data object Signup : Screen("signup?terms={terms}") {      // 약관 동의 완료(이메일 플로우)
        const val ARG_TERMS = "terms"                          // TermAgreementDto 리스트를 JSON 문자열로 직렬화한 값
        fun createRoute(termsJson: String) = "signup?terms=${URLEncoder.encode(termsJson, "UTF-8")}"
    }
    data object SocialNickname : Screen("onboarding/nickname/{provider}?token={token}&terms={terms}") { // 약관 동의 완료(소셜 플로우)
        const val ARG_PROVIDER = "provider"                    // "kakao" | "google"
        const val ARG_TOKEN = "token"                          // 온보딩 임시 토큰
        const val ARG_TERMS = "terms"                          // TermAgreementDto 리스트를 JSON 문자열로 직렬화한 값
        fun createRoute(provider: String, token: String, termsJson: String) =
            "onboarding/nickname/$provider" +
                "?token=${URLEncoder.encode(token, "UTF-8")}" +
                "&terms=${URLEncoder.encode(termsJson, "UTF-8")}"
    }
    data object SignupComplete : Screen("signup_complete")

    // Home (무즈/김규리)
    data object Home : Screen("home")
    data object RecommendedCourseDetail : Screen("home/recommended-courses/{courseId}") { // 홈 "오늘의 추천 코스" 카드 탭
        const val ARG_COURSE_ID = "courseId"
        fun createRoute(courseId: Long) = "home/recommended-courses/$courseId"
    }

    // Course 생성 플로우
    data object MoodSelect : Screen("course/mood")            // 무즈/김규리
    data object FoodSelect : Screen("course/food/{courseDraftId}") { // 무즈/김규리 — 분위기 선택에서 발급된 임시 코스 핸들을 이어받는다.
        const val ARG_COURSE_DRAFT_ID = "courseDraftId"
        fun createRoute(courseDraftId: Long) = "course/food/$courseDraftId"
    }
    data object BasePlace : Screen("course/base_place/{courseDraftId}") { // 티아/강서윤 — 음식 선택에서 발급된 임시 코스 핸들을 이어받는다.
        const val ARG_COURSE_DRAFT_ID = "courseDraftId"
        fun createRoute(courseDraftId: Long) = "course/base_place/$courseDraftId"
    }
    data object PlaceDetail : Screen("course/place/{placeId}") { // 티아/강서윤 — 장소 카드 탭
        const val ARG_PLACE_ID = "placeId"
        fun createRoute(placeId: Long) = "course/place/$placeId"
    }
    data object InteriorPhotos : Screen("course/place/{placeId}/photos") { // 티아/강서윤 — 내부 사진 전체보기
        const val ARG_PLACE_ID = "placeId"
        fun createRoute(placeId: Long) = "course/place/$placeId/photos"
    }
    data object MenuFull : Screen("course/place/{placeId}/menus") { // 티아/강서윤 — 메뉴 전체보기
        const val ARG_PLACE_ID = "placeId"
        fun createRoute(placeId: Long) = "course/place/$placeId/menus"
    }
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
    object Notice : Screen("notice")
}
