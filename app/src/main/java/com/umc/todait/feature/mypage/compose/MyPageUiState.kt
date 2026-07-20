package com.umc.todait.feature.mypage.compose

data class MyPageUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
)