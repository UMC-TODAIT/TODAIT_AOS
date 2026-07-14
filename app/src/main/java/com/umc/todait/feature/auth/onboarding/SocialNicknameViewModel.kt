package com.umc.todait.feature.auth.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.base.BaseViewModel
import com.umc.todait.core.network.ApiResult
import com.umc.todait.feature.auth.data.repository.AuthRepository
import com.umc.todait.navigation.Screen
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
 * 소셜 간편가입 닉네임 설정 화면의 상태를 관리한다.
 *
 * 형식 검증(2~12자, 특수문자 불가)은 클라이언트에서 먼저 걸러내고,
 * 통과한 닉네임만 서버(GET /api/members/nickname-availability)로 중복 여부를 확인한다.
 * 약관 동의는 이 화면 진입 전(TermsAgreementScreen)에서 이미 끝났으므로,
 * 실제 온보딩 완료(PATCH /api/members/me/onboarding) 호출은 이후 API 연동 시 이 화면에서 처리한다.
 */
@HiltViewModel
class SocialNicknameViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(
        SocialNicknameUiState(
            provider = SignupProvider.fromRoute(savedStateHandle[Screen.SocialNickname.ARG_PROVIDER]),
        ),
    )
    val uiState: StateFlow<SocialNicknameUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SocialNicknameEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onNicknameChange(value: String) {
        // 입력이 바뀌면 직전 검사 결과를 초기화해 메시지를 지운다.
        _uiState.update { it.copy(nickname = value, status = NicknameStatus.IDLE) }
    }

    fun onCheckDuplicate() {
        val nickname = _uiState.value.nickname
        // 형식 위반(특수문자·길이)은 서버 호출 없이 즉시 사용 불가로 처리한다.
        if (!NICKNAME_REGEX.matches(nickname)) {
            _uiState.update { it.copy(status = NicknameStatus.UNAVAILABLE) }
            return
        }
        _uiState.update { it.copy(isChecking = true) }
        viewModelScope.launch {
            val result = authRepository.checkNicknameAvailability(nickname)
            _uiState.update { state ->
                when (result) {
                    is ApiResult.Success -> state.copy(
                        isChecking = false,
                        status = if (result.data.available) NicknameStatus.AVAILABLE else NicknameStatus.UNAVAILABLE,
                    )
                    // 네트워크/서버 오류는 우선 사용 불가로 처리한다(백엔드 연결 후 재검토).
                    is ApiResult.Failure -> state.copy(
                        isChecking = false,
                        status = NicknameStatus.UNAVAILABLE,
                    )
                }
            }
        }
    }

    fun onStartClick() {
        val state = _uiState.value
        if (state.status != NicknameStatus.AVAILABLE) return
        viewModelScope.launch {
            _effect.send(SocialNicknameEffect.NavigateToComplete)
        }
    }

    private companion object {
        // 2~12자, 한글/영문/숫자만 허용(특수문자·공백 불가).
        val NICKNAME_REGEX = Regex("^[0-9A-Za-z가-힣]{2,12}$")
    }
}
