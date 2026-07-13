package com.umc.todait.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.datastore.TokenDataStore
import com.umc.todait.feature.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 이메일 로그인 화면의 상태를 관리한다.
 * 로그인 성공/실패 여부와 무관하게 이메일이 틀렸는지 비밀번호가 틀렸는지는 구분하지 않는다
 * (서버도 AUTH400 하나로만 응답 — 보안상 의도적).
 */
@HiltViewModel
class EmailLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(EmailLoginUiState())
    val uiState: StateFlow<EmailLoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<EmailLoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, isLoginError = false) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, isLoginError = false) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (!state.isLoginEnabled) return

        _uiState.update { it.copy(isLoading = true, isLoginError = false) }
        launchApi(
            apiCall = { authRepository.login(email = state.email, password = state.password) },
            onError = {
                _uiState.update { it.copy(isLoading = false, isLoginError = true) }
            },
            onSuccess = { result ->
                viewModelScope.launch {
                    tokenDataStore.saveTokens(result.accessToken, result.refreshToken)
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(EmailLoginEffect.NavigateToHome)
                }
            },
        )
    }
}
