package com.umc.todait.feature.mypage.compose

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
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
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        getMyPage()
    }

    private fun getMyPage() {
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
                            isLoading = false,
                            nickname = result.data.nickname,
                            email = result.data.email,
                            profileImageUrl = result.data.profileImageUrl,
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}