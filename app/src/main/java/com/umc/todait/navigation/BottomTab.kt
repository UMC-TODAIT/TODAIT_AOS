package com.umc.todait.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 탭바: 홈 / 코스 생성 / 저장된 코스 / 마이페이지
 */
enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    HOME(Screen.Home.route, "홈", Icons.Filled.Home),
    CREATE(Screen.MoodSelect.route, "코스 생성", Icons.Filled.AddCircle),
    SAVED(Screen.SavedCourses.route, "저장된 코스", Icons.Filled.FavoriteBorder),
    MYPAGE(Screen.MyPage.route, "마이페이지", Icons.Filled.Person),
}
