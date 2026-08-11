package com.umc.todait.feature.mypage.compose

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.datastore.TokenDataStore
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.auth.data.repository.AuthRepository
import com.umc.todait.feature.mypage.data.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repository: MyPageRepository,
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        getMyPage()
    }

    fun getMyPage() {
        _uiState.update {
            it.copy(
                isLoading = true
            )
        }

        viewModelScope.launch {
            when (val result = repository.getMyPage()) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            error = null,
                            isLoading = false,
                            nickname = result.data.nickname,
                            email = result.data.email,
                            profileImageUrl = result.data.profileImageUrl?.let { url ->
                                if (url.startsWith("http://")) {
                                    "https://${url.removePrefix("http://")}"
                                } else {
                                    url
                                }
                            },
                            savedCourseCount = result.data.savedCourseCount
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.toUiError()
                        )
                    }
                }
            }
        }
    }
    fun clearError() {
        _uiState.update {
            it.copy(
                error = null
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                tokenDataStore.getRefreshToken()?.let { refreshToken ->
                    authRepository.logout(refreshToken)
                }
            } finally {
                tokenDataStore.clearTokens()

                _uiState.update {
                    it.copy(isLogoutCompleted = true)
                }
            }
        }
    }
}