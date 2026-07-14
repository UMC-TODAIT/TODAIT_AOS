package com.umc.todait.navigation

import android.R.attr.type
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.umc.todait.feature.mypage.MyPageScreen
import com.umc.todait.feature.mypage.NoticeScreen
import com.umc.todait.feature.auth.login.LoginScreen
import com.umc.todait.feature.course.base_place.BasePlaceScreen
import com.umc.todait.feature.course.place_detail.InteriorPhotosScreen
import com.umc.todait.feature.course.place_detail.MenuFullScreen
import com.umc.todait.feature.course.place_detail.PlaceDetailScreen
import com.umc.todait.feature.saved.CourseDetailScreen
import com.umc.todait.feature.saved.SavedCoursesScreen
import com.umc.todait.ui.component.PlaceholderScreen
import com.umc.todait.ui.component.TopBar

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
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MyPage.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---------- Auth ----------
            composable(Screen.Login.route) {
                LoginScreen(
                    // TODO: 소셜 로그인 SDK 연동 이슈에서 실제 로그인 처리로 교체
                    onKakaoLoginClick = {},
                    onGoogleLoginClick = {},
                    onEmailLoginClick = { navController.navigate(Screen.Signup.route) },
                )
            }
            composable(Screen.Signup.route) { PlaceholderScreen("회원가입") }
            composable(Screen.TermsAgreement.route) { PlaceholderScreen("약관 동의") }
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
            composable(Screen.CourseCompose.route) { PlaceholderScreen("코스 구성하기") }
            composable(Screen.SelectedPlaces.route) { PlaceholderScreen("선택한 장소") }
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
