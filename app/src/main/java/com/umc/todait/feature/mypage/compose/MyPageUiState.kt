package com.umc.todait.feature.mypage.compose

import com.umc.todait.core.network.UiError

data class MyPageUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    // 이메일 미제공 소셜 회원은 null — 표시부에서 안내 문구로 대체한다.
    val email: String? = null,
    val profileImageUrl: String? = null,
    val savedCourseCount: Int = 0,
    val error: UiError? = null,
    val isLoggedOut: Boolean = false,
    val isLogoutCompleted: Boolean = false
)
