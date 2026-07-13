package com.umc.todait.feature.auth.login

import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.datastore.TokenDataStore
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
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
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (!state.isLoginEnabled) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(email = state.email, password = state.password)) {
                is ApiResult.Success -> {
                    tokenDataStore.saveTokens(result.data.accessToken, result.data.refreshToken)
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(EmailLoginEffect.NavigateToHome)
                }
                // 서버 인증 실패(AUTH400 등) → 잘못된 이메일/비밀번호 안내
                is ApiResult.Failure.ServerError ->
                    _uiState.update { it.copy(isLoading = false, error = LoginError.InvalidCredentials) }
                // 네트워크/알 수 없는 오류 → 공통 에러 정책 문구
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, error = LoginError.General(result.toUiError().message)) }
            }
        }
    }
}
