package com.umc.todait.feature.auth.social

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SocialProvider { KAKAO, GOOGLE }

sealed interface SocialLoginEffect {
    /** SDK 로그인 성공. 이후 백엔드 로그인/온보딩 플로우로 이동한다. */
    data class Success(val provider: SocialProvider) : SocialLoginEffect
    data class Failure(val message: String) : SocialLoginEffect
}

data class SocialLoginUiState(val isLoading: Boolean = false)

/**
 * 카카오/구글 소셜 로그인 진입점.
 *
 * 현재는 각 SDK 로그인까지 수행하고 성공/실패 이펙트만 방출한다(반환 토큰은 매니저에서 로그로 확인).
 * 백엔드 소셜 로그인 계약(카카오 accessToken 수신 vs code, 구글 idToken 수신)이 확정되면
 * 성공 시 얻은 토큰을 서버로 넘겨 isNewMember 에 따라 홈/온보딩 분기하도록 확장한다.
 */
@HiltViewModel
class SocialLoginViewModel @Inject constructor(
    private val kakaoLoginManager: KakaoLoginManager,
    private val googleLoginManager: GoogleLoginManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialLoginUiState())
    val uiState: StateFlow<SocialLoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SocialLoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // SDK 로그인 UI(카카오톡/웹, 구글 다이얼로그)는 Activity 컨텍스트가 필요하므로 화면에서 넘겨받는다.
    fun loginWithKakao(context: Context) =
        login(SocialProvider.KAKAO) { kakaoLoginManager.login(context).map { } }

    fun loginWithGoogle(context: Context) =
        login(SocialProvider.GOOGLE) { googleLoginManager.login(context).map { } }

    private fun login(provider: SocialProvider, block: suspend () -> Result<Unit>) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = block()
            _uiState.update { it.copy(isLoading = false) }
            result
                .onSuccess { _effect.send(SocialLoginEffect.Success(provider)) }
                .onFailure { _effect.send(SocialLoginEffect.Failure(it.message ?: "소셜 로그인에 실패했어요.")) }
        }
    }
}
