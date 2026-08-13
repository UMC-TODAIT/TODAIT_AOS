package com.umc.todait.feature.auth.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.datastore.TokenDataStore
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.toUiError
import com.umc.todait.feature.auth.data.dto.TermAgreementDto
import com.umc.todait.feature.auth.data.repository.AuthRepository
import com.umc.todait.feature.auth.onboarding.NicknameStatus
import com.umc.todait.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 회원가입 화면의 상태를 관리한다.
 *
 * 흐름: 이름(닉네임)/이메일 입력 → 이메일 인증번호 발송/확인 → 비밀번호 입력 →
 * 회원가입(POST /api/auth/signup) 호출. 약관 동의는 이전 화면(TermsAgreementScreen)에서
 * 이미 받아 nav 인자로 전달된다.
 */
@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val agreedTerms: List<TermAgreementDto> = runCatching {
        val termsJson: String = savedStateHandle[Screen.Signup.ARG_TERMS] ?: "[]"
        val listType = object : TypeToken<List<TermAgreementDto>>() {}.type
        Gson().fromJson<List<TermAgreementDto>>(termsJson, listType)
    }.getOrDefault(emptyList())

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SignupEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var countdownJob: Job? = null

    fun onNicknameChange(value: String) {
        // 입력이 바뀌면 직전 검사 결과를 초기화해 메시지를 지운다(닉네임 설정 화면과 동일).
        _uiState.update { it.copy(nickname = value, nicknameStatus = NicknameStatus.IDLE) }
    }

    /** 닉네임 중복 확인(GET /api/members/nickname-availability, 인증 불필요). */
    fun onCheckNicknameClick() {
        val nickname = _uiState.value.nickname
        // 형식 위반(특수문자·길이)은 서버 호출 없이 즉시 사용 불가로 처리한다.
        if (!_uiState.value.isNicknameValid) {
            _uiState.update { it.copy(nicknameStatus = NicknameStatus.UNAVAILABLE) }
            return
        }
        _uiState.update { it.copy(isCheckingNickname = true) }
        viewModelScope.launch {
            val result = authRepository.checkNicknameAvailability(nickname)
            _uiState.update { state ->
                val status = when (result) {
                    is ApiResult.Success ->
                        if (result.data.available) NicknameStatus.AVAILABLE else NicknameStatus.UNAVAILABLE
                    // 네트워크/서버 오류는 우선 사용 불가로 처리한다(닉네임 설정 화면과 동일).
                    is ApiResult.Failure -> NicknameStatus.UNAVAILABLE
                }
                state.copy(isCheckingNickname = false, nicknameStatus = status)
            }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, sendCodeError = null) }
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value, verifyCodeError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onPasswordConfirmChange(value: String) {
        _uiState.update { it.copy(passwordConfirm = value) }
    }

    fun onSendCodeClick() {
        val state = _uiState.value
        if (!state.canSendCode) return
        _uiState.update { it.copy(isSendingCode = true, sendCodeError = null) }
        viewModelScope.launch {
            val result = authRepository.sendSignupEmailCode(state.email)
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSendingCode = false,
                            code = "",
                            verificationState = EmailVerificationState.CodeSent(CODE_TIMEOUT_SECONDS),
                        )
                    }
                    startCountdown()
                }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSendingCode = false, sendCodeError = result.toUiError().message) }
            }
        }
    }

    fun onVerifyCodeClick() {
        val state = _uiState.value
        if (!state.canVerifyCode) return
        _uiState.update { it.copy(isVerifyingCode = true, verifyCodeError = null) }
        viewModelScope.launch {
            val result = authRepository.verifySignupEmailCode(state.email, state.code)
            _uiState.update { it.copy(isVerifyingCode = false) }
            when (result) {
                is ApiResult.Success -> {
                    countdownJob?.cancel()
                    _uiState.update { it.copy(verificationState = EmailVerificationState.Verified) }
                }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(verifyCodeError = result.toUiError().message) }
            }
        }
    }

    fun onSubmitClick() {
        val state = _uiState.value
        if (!state.isSignupEnabled) return
        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val result = authRepository.signup(
                email = state.email,
                password = state.password,
                nickname = state.nickname,
                termAgreements = agreedTerms,
            )
            when (result) {
                is ApiResult.Success -> {
                    tokenDataStore.saveTokens(result.data.accessToken, result.data.refreshToken)
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(SignupEffect.NavigateToComplete(state.nickname))
                }
                is ApiResult.Failure ->
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.toUiError().message) }
            }
        }
    }

    /** 인증번호 유효시간(180초) 카운트다운. 0이 되면 만료 상태로 전환한다. */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value.verificationState as? EmailVerificationState.CodeSent ?: return@launch
                if (current.secondsLeft <= 1) {
                    _uiState.update { it.copy(verificationState = EmailVerificationState.Expired) }
                    return@launch
                }
                _uiState.update {
                    it.copy(verificationState = current.copy(secondsLeft = current.secondsLeft - 1))
                }
            }
        }
    }

    private companion object {
        const val CODE_TIMEOUT_SECONDS = 180
    }
}
