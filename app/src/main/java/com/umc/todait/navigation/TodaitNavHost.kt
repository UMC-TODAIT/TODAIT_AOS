package com.umc.todait.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.umc.todait.feature.mypage.compose.MyPageScreen
import com.umc.todait.feature.mypage.compose.NoticeScreen
import com.umc.todait.feature.auth.login.EmailLoginScreen
import com.umc.todait.feature.auth.login.LoginScreen
import com.umc.todait.feature.auth.onboarding.SignupProvider
import com.umc.todait.feature.auth.onboarding.SocialNicknameScreen
import com.umc.todait.feature.auth.signup.SignupScreen
import com.umc.todait.feature.auth.terms.TermDetailScreen
import com.umc.todait.feature.auth.terms.TermsAgreementScreen
import com.umc.todait.feature.auth.terms.TermsFlow
import com.umc.todait.feature.course.base_place.BasePlaceScreen
import com.umc.todait.feature.course.compose.CourseComposeScreen
import com.umc.todait.feature.course.compose.CourseComposeViewModel
import com.umc.todait.feature.course.compose.SelectedPlacesScreen
import com.umc.todait.feature.course.place_detail.InteriorPhotosScreen
import com.umc.todait.feature.course.place_detail.MenuFullScreen
import com.umc.todait.feature.course.place_detail.PlaceDetailScreen
import com.umc.todait.feature.saved.CourseDetailScreen
import com.umc.todait.feature.saved.SavedCoursesScreen
import com.umc.todait.ui.component.BottomBar
import com.umc.todait.ui.component.PlaceholderScreen
import com.umc.todait.ui.component.TopBar
import com.umc.todait.ui.theme.Cream

// 코스 구성 플로우 중첩 그래프 라우트. 이 그래프 스코프로 CourseComposeViewModel 을 두 화면이 공유한다.
private const val COURSE_COMPOSE_GRAPH = "course/compose_graph"

/**
 * 앱 루트 컴포저블: 하단 탭바 + NavHost.
 * 각 화면 담당자는 PlaceholderScreen을 실제 Screen 컴포저블로 교체한다.
 */
@Composable
fun TodaitApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 하단 탭바를 노출할 화면 (플로우 중간 화면에서는 숨김)
    val bottomBarRoutes = BottomTab.entries.map { it.route }.toSet()
    val showBottomBar =
        currentRoute in bottomBarRoutes || currentRoute?.startsWith("saved/") == true

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopBar()
        },
        bottomBar = {

            if(showBottomBar){

                BottomBar(
                    currentRoute = currentRoute,
                    onTabClick = { tab ->

                        navController.navigate(tab.route){
                            popUpTo(
                                navController.graph.findStartDestination().id
                            ){
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }

                    }
                )

            }

        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---------- Auth ----------
            // 플로우: 로그인/이메일 로그인 → 약관 동의 → 회원가입(이메일) / 닉네임 설정(소셜) → 가입 완료
            composable(Screen.Login.route) {
                LoginScreen(
                    onKakaoLoginClick = {
                        navController.navigate(Screen.TermsAgreement.createRoute(TermsFlow.KAKAO.route))
                    },
                    onGoogleLoginClick = {
                        navController.navigate(Screen.TermsAgreement.createRoute(TermsFlow.GOOGLE.route))
                    },
                    onEmailLoginClick = { navController.navigate(Screen.EmailLogin.route) },
                )
            }
            composable(Screen.EmailLogin.route) {
                EmailLoginScreen(
                    onSignupClick = {
                        navController.navigate(Screen.TermsAgreement.createRoute(TermsFlow.EMAIL.route))
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            // 로그인 성공 후 인증 플로우(Login 포함)를 백스택에서 제거
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Screen.TermsAgreement.route,
                arguments = listOf(
                    navArgument(Screen.TermsAgreement.ARG_FLOW) { type = NavType.StringType },
                ),
            ) {
                TermsAgreementScreen(
                    onBackClick = { navController.popBackStack() },
                    // TODO: 회원가입/닉네임 설정 화면에 실제 signup/onboarding API를 붙일 때
                    //  agreedTerms(TermsAgreementEffect.NavigateNext)도 함께 넘기도록 정리한다.
                    onNext = { flow ->
                        when (flow) {
                            TermsFlow.EMAIL -> navController.navigate(Screen.Signup.route)
                            TermsFlow.KAKAO ->
                                navController.navigate(Screen.SocialNickname.createRoute(SignupProvider.KAKAO.route))
                            TermsFlow.GOOGLE ->
                                navController.navigate(Screen.SocialNickname.createRoute(SignupProvider.GOOGLE.route))
                        }
                    },
                    onViewDetail = { termId ->
                        navController.navigate(Screen.TermDetail.createRoute(termId))
                    },
                )
            }
            composable(
                route = Screen.TermDetail.route,
                arguments = listOf(
                    navArgument(Screen.TermDetail.ARG_TERM_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val termId = backStackEntry.arguments?.getLong(Screen.TermDetail.ARG_TERM_ID) ?: 0L
                TermDetailScreen(
                    termId = termId,
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable(Screen.Signup.route) {
                SignupScreen(
                    onBackClick = { navController.popBackStack() },
                    onSignupComplete = { navController.navigate(Screen.SignupComplete.route) },
                )
            }
            composable(
                route = Screen.SocialNickname.route,
                arguments = listOf(
                    navArgument(Screen.SocialNickname.ARG_PROVIDER) { type = NavType.StringType },
                ),
            ) {
                SocialNicknameScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToComplete = { navController.navigate(Screen.SignupComplete.route) },
                )
            }
            composable(Screen.SignupComplete.route) { PlaceholderScreen("회원가입 완료") }

            // ---------- Home ----------
            composable(Screen.Home.route) { PlaceholderScreen("홈") }

            // ---------- Course 생성 플로우 ----------
            composable(Screen.MoodSelect.route) { PlaceholderScreen("분위기 선택") }
            composable(Screen.FoodSelect.route) { PlaceholderScreen("음식 선택") }
            composable(Screen.BasePlace.route) {
                BasePlaceScreen(
                    onNavigateToCompose = {
                        navController.navigate(Screen.CourseCompose.route)
                    },
                    onNavigateToDetail = { placeId ->
                        navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.PlaceDetail.route,
                arguments = listOf(
                    navArgument(Screen.PlaceDetail.ARG_PLACE_ID) { type = NavType.LongType },
                ),
            ) { backStackEntry ->
                val placeId = backStackEntry.arguments?.getLong(Screen.PlaceDetail.ARG_PLACE_ID) ?: 0L
                PlaceDetailScreen(
                    onBack = { navController.popBackStack() },
                    onSeeAllPhotos = {
                        navController.navigate(Screen.InteriorPhotos.createRoute(placeId))
                    },
                    onSeeAllMenu = {
                        navController.navigate(Screen.MenuFull.createRoute(placeId))
                    },
                )
            }
            composable(
                route = Screen.InteriorPhotos.route,
                arguments = listOf(
                    navArgument(Screen.InteriorPhotos.ARG_PLACE_ID) { type = NavType.LongType },
                ),
            ) {
                InteriorPhotosScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Screen.MenuFull.route,
                arguments = listOf(
                    navArgument(Screen.MenuFull.ARG_PLACE_ID) { type = NavType.LongType },
                ),
            ) {
                MenuFullScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            // 코스 구성 플로우: [장소카드 선택]과 [선택한 장소]가 CourseComposeViewModel(선택 상태)을 공유한다.
            // 두 화면을 중첩 그래프로 묶고, 그래프 back stack entry 에 스코프된 ViewModel 을 함께 쓴다.
            navigation(
                startDestination = Screen.CourseCompose.route,
                route = COURSE_COMPOSE_GRAPH,
            ) {
                composable(Screen.CourseCompose.route) { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry(COURSE_COMPOSE_GRAPH) }
                    val viewModel: CourseComposeViewModel = hiltViewModel(graphEntry)
                    CourseComposeScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { placeId ->
                            navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                        },
                        onNavigateToSelected = {
                            navController.navigate(Screen.SelectedPlaces.route)
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Screen.SelectedPlaces.route) { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry(COURSE_COMPOSE_GRAPH) }
                    val viewModel: CourseComposeViewModel = hiltViewModel(graphEntry)
                    SelectedPlacesScreen(
                        viewModel = viewModel,
                        onNavigateToSave = { navController.navigate(Screen.CourseSave.route) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(Screen.CourseSave.route) { PlaceholderScreen("코스 저장") }

            // ---------- Saved ----------
            composable(Screen.SavedCourses.route) {
                SavedCoursesScreen(navController)
            }
            composable(
                route = Screen.CourseDetail.route
            ) { backStackEntry ->

                val courseId =
                    backStackEntry.arguments
                        ?.getString(Screen.CourseDetail.ARG_COURSE_ID)
                        ?.toLongOrNull()

                CourseDetailScreen(
                    navController = navController,
                    courseId = courseId ?: 0L
                )
            }


            // ---------- MyPage ----------
            composable(Screen.MyPage.route) {
                MyPageScreen(navController = navController)
            }

            composable(Screen.Notice.route) {
                NoticeScreen(navController = navController)
            }
        }
    }
}
