package com.umc.todait.feature.mypage.compose

import com.umc.todait.core.network.UiError

data class MyPageUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val error: UiError? = null,
)
