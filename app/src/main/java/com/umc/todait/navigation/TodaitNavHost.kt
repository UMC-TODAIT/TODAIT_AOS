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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// androidx.navigation.navigation 이 아니라 compose 버전을 써야 그래프에 arguments 를 선언할 수 있다.
import androidx.navigation.compose.navigation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.umc.todait.feature.auth.social.SocialLoginEffect
import com.umc.todait.feature.auth.social.SocialLoginViewModel
import com.umc.todait.feature.auth.social.SocialProvider
import com.umc.todait.feature.mypage.compose.MyPageScreen
import com.umc.todait.feature.mypage.compose.NoticeScreen
import com.umc.todait.feature.auth.login.EmailLoginScreen
import com.umc.todait.feature.auth.login.LoginScreen
import com.umc.todait.feature.auth.splash.SplashScreen
import com.umc.todait.feature.auth.onboarding.SignupProvider
import com.umc.todait.feature.auth.onboarding.SocialNicknameScreen
import com.umc.todait.feature.auth.signup.SignupScreen
import com.umc.todait.feature.auth.signupcomplete.SignupCompleteScreen
import com.umc.todait.feature.auth.terms.TermDetailScreen
import com.umc.todait.feature.auth.terms.TermsAgreementScreen
import com.umc.todait.feature.auth.terms.TermsFlow
import com.umc.todait.feature.course.base_place.BasePlaceScreen
import com.umc.todait.feature.course.compose.CourseComposeScreen
import com.umc.todait.feature.course.compose.CourseComposeViewModel
import com.umc.todait.feature.course.compose.SelectedPlacesScreen
import com.umc.todait.feature.course.place_detail.InteriorPhotosScreen
import com.umc.todait.feature.course.save.CourseSaveScreen
import com.umc.todait.feature.course.place_detail.MenuFullScreen
import com.umc.todait.feature.course.place_detail.PlaceDetailScreen
import com.umc.todait.feature.home.HomeScreen
import com.umc.todait.feature.home.coursedetail.RecommendedCourseDetailScreen
import com.umc.todait.feature.saved.compose.CourseDetailScreen
import com.umc.todait.feature.saved.compose.SavedCoursesScreen
import com.umc.todait.ui.component.BottomBar
import com.umc.todait.ui.component.PlaceholderScreen
import com.umc.todait.ui.component.TopBar
import com.umc.todait.ui.theme.Cream

// 코스 구성 플로우 중첩 그래프 라우트. 이 그래프 스코프로 CourseComposeViewModel 을 세 화면이 공유한다.
// 경로 변수(courseDraftId/basePlaceId)는 그래프 back stack entry 의 arguments 로 들어가
// 그래프 스코프 ViewModel 의 SavedStateHandle 에서 읽힌다.
private val COURSE_COMPOSE_GRAPH = Screen.CourseComposeGraph.route

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

                        // 탭을 누르면 항상 그 탭의 루트 화면으로 리셋한다(예: 코스 상세까지 들어갔다가
                        // 다른 탭 갔다 "홈"을 다시 누르면 무조건 홈 루트로 — 마지막 화면 복원 아님).
                        // 그래서 saveState/restoreState를 쓰지 않는다.
                        navController.navigate(tab.route){
                            popUpTo(
                                navController.graph.findStartDestination().id
                            )

                            launchSingleTop = true
                        }

                    }
                )

            }

        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---------- Auth ----------
            // 플로우: 스플래시(토큰 확인) → 로그인/이메일 로그인 → 약관 동의 → 회원가입(이메일) / 닉네임 설정(소셜) → 가입 완료
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.Login.route) {
                val context = LocalContext.current
                val socialViewModel: SocialLoginViewModel = hiltViewModel()
                LaunchedEffect(Unit) {
                    socialViewModel.effect.collect { effect ->
                        when (effect) {
                            // 기존 회원 — 이미 토큰 저장됨, 홈으로 직행(인증 플로우는 백스택에서 제거).
                            is SocialLoginEffect.Success ->
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            // 신규 회원 — 약관 동의(→ 닉네임) 온보딩 플로우로 이동. onboardingToken은 온보딩 완료 API 호출까지 들고 간다.
                            is SocialLoginEffect.NeedsOnboarding -> when (effect.provider) {
                                SocialProvider.KAKAO ->
                                    navController.navigate(
                                        Screen.TermsAgreement.createRoute(TermsFlow.KAKAO.route, effect.onboardingToken),
                                    )
                                SocialProvider.GOOGLE ->
                                    navController.navigate(
                                        Screen.TermsAgreement.createRoute(TermsFlow.GOOGLE.route, effect.onboardingToken),
                                    )
                            }
                            // TODO: 실패 안내(스낵바) 연결.
                            is SocialLoginEffect.Failure -> Unit
                        }
                    }
                }
                LoginScreen(
                    onKakaoLoginClick = { socialViewModel.loginWithKakao(context) },
                    onGoogleLoginClick = { socialViewModel.loginWithGoogle(context) },
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
                    navArgument(Screen.TermsAgreement.ARG_TOKEN) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val onboardingToken = backStackEntry.arguments?.getString(Screen.TermsAgreement.ARG_TOKEN).orEmpty()
                TermsAgreementScreen(
                    onBackClick = { navController.popBackStack() },
                    onNext = { flow, agreedTerms ->
                        // 회원가입(signup)·온보딩 모두 termType 기반 TermAgreementDto를 그대로 실어 보낸다.
                        val termsJson = Gson().toJson(agreedTerms)
                        when (flow) {
                            TermsFlow.EMAIL ->
                                navController.navigate(Screen.Signup.createRoute(termsJson))
                            TermsFlow.KAKAO ->
                                navController.navigate(
                                    Screen.SocialNickname.createRoute(
                                        provider = SignupProvider.KAKAO.route,
                                        token = onboardingToken,
                                        termsJson = termsJson,
                                    ),
                                )
                            TermsFlow.GOOGLE ->
                                navController.navigate(
                                    Screen.SocialNickname.createRoute(
                                        provider = SignupProvider.GOOGLE.route,
                                        token = onboardingToken,
                                        termsJson = termsJson,
                                    ),
                                )
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
            composable(
                route = Screen.Signup.route,
                arguments = listOf(
                    navArgument(Screen.Signup.ARG_TERMS) { type = NavType.StringType },
                ),
            ) {
                SignupScreen(
                    onBackClick = { navController.popBackStack() },
                    onSignupComplete = {
                        navController.navigate(Screen.SignupComplete.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Screen.SocialNickname.route,
                arguments = listOf(
                    navArgument(Screen.SocialNickname.ARG_PROVIDER) { type = NavType.StringType },
                    navArgument(Screen.SocialNickname.ARG_TOKEN) { type = NavType.StringType },
                    navArgument(Screen.SocialNickname.ARG_TERMS) { type = NavType.StringType },
                ),
            ) {
                SocialNicknameScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToComplete = {
                        navController.navigate(Screen.SignupComplete.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.SignupComplete.route) {
                SignupCompleteScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignupComplete.route) { inclusive = true }
                        }
                    },
                )
            }

            // ---------- Home ----------
            composable(Screen.Home.route) {
                HomeScreen(
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.RecommendedCourseDetail.createRoute(courseId))
                    },
                    onPlaceClick = { placeId ->
                        navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                    },
                    // TODO: 알림 화면 없음(스코프 밖) — 생기면 연결.
                    onNotificationClick = {},
                    onProfileClick = { navController.navigate(Screen.MyPage.route) },
                )
            }
            composable(
                route = Screen.RecommendedCourseDetail.route,
                arguments = listOf(
                    navArgument(Screen.RecommendedCourseDetail.ARG_COURSE_ID) { type = NavType.LongType },
                ),
            ) {
                RecommendedCourseDetailScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToPlaceDetail = { placeId ->
                        navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                    },
                    onNavigateToSavedCourses = {
                        // "저장된 코스"로 이동은 하단 탭 전환과 동일하게 취급한다 — BottomBar.onTabClick과
                        // 완전히 동일한 NavOptions를 써서 일관되게 유지한다(saveState/restoreState는 쓰지
                        // 않는다: 탭 전환은 항상 그 탭의 루트 화면으로 리셋되는 게 맞다).
                        navController.navigate(Screen.SavedCourses.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                )
            }

            // ---------- Course 생성 플로우 ----------
            composable(Screen.MoodSelect.route) { PlaceholderScreen("분위기 선택") }
            composable(Screen.FoodSelect.route) { PlaceholderScreen("음식 선택") }
            composable(Screen.BasePlace.route) {
                BasePlaceScreen(
                    // 기준 장소가 서버에 저장된 뒤에만 넘어온다(PATCH .../base-place 성공).
                    onNavigateToCompose = { courseDraftId, basePlaceId ->
                        navController.navigate(
                            Screen.CourseComposeGraph.createRoute(courseDraftId, basePlaceId),
                        )
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
                arguments = listOf(
                    navArgument(Screen.CourseComposeGraph.ARG_COURSE_DRAFT_ID) { type = NavType.LongType },
                    navArgument(Screen.CourseComposeGraph.ARG_BASE_PLACE_ID) { type = NavType.LongType },
                ),
            ) {
                composable(Screen.CourseCompose.route) { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry(COURSE_COMPOSE_GRAPH) }
                    val viewModel: CourseComposeViewModel = hiltViewModel(graphEntry)
                    // 화면 이동은 단계 전환 API(ordering) 성공 후에만 일어나므로 effect 로 받는다.
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
                // 코스 저장도 같은 그래프에 둔다. 경로 미리보기에 쓸 코스 순서(기준 장소 + 담은 장소)를
                // 공유 ViewModel 에서 그대로 받아야 하기 때문.
                composable(Screen.CourseSave.route) { entry ->
                    val graphEntry = remember(entry) { navController.getBackStackEntry(COURSE_COMPOSE_GRAPH) }
                    val composeViewModel: CourseComposeViewModel = hiltViewModel(graphEntry)
                    val composeState by composeViewModel.uiState.collectAsStateWithLifecycle()
                    CourseSaveScreen(
                        places = listOfNotNull(composeState.basePlace) + composeState.selectedPlaces,
                        // 저장 완료 후에는 코스 생성 플로우를 백스택에서 비운다.
                        onNavigateToSavedCourses = {
                            navController.navigate(Screen.SavedCourses.route) {
                                popUpTo(COURSE_COMPOSE_GRAPH) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(COURSE_COMPOSE_GRAPH) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

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
