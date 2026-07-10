package com.umc.todait.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.umc.todait.ui.component.PlaceholderScreen
import com.umc.todait.feature.mypage.MyPageScreen
import com.umc.todait.feature.mypage.NoticeScreen
import com.umc.todait.feature.saved.SavedCoursesScreen

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
            startDestination = Screen.MyPage.route, // Login으로 되돌려 놓으세요
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---------- Auth ----------
            composable(Screen.Login.route) { PlaceholderScreen("로그인") }
            composable(Screen.Signup.route) { PlaceholderScreen("회원가입") }
            composable(Screen.TermsAgreement.route) { PlaceholderScreen("약관 동의") }
            composable(Screen.SignupComplete.route) { PlaceholderScreen("회원가입 완료") }

            // ---------- Home ----------
            composable(Screen.Home.route) { PlaceholderScreen("홈") }

            // ---------- Course 생성 플로우 ----------
            composable(Screen.MoodSelect.route) { PlaceholderScreen("분위기 선택") }
            composable(Screen.FoodSelect.route) { PlaceholderScreen("음식 선택") }
            composable(Screen.BasePlace.route) { PlaceholderScreen("기준 장소 설정") }
            composable(Screen.CourseCompose.route) { PlaceholderScreen("코스 구성하기") }
            composable(Screen.SelectedPlaces.route) { PlaceholderScreen("선택한 장소") }
            composable(Screen.CourseSave.route) { PlaceholderScreen("코스 저장") }

            // ---------- Saved ----------
            composable(Screen.SavedCourses.route) {
                SavedCoursesScreen()
            }
            composable(Screen.CourseDetail.route) { PlaceholderScreen("코스 상세 정보") }

            // ---------- MyPage ----------
            composable(Screen.MyPage.route) {
                MyPageScreen(navController)
            }
            composable(Screen.Notice.route) {
                NoticeScreen(navController)
            }
        }
    }
}
