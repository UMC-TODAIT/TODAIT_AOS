package com.umc.todait.feature.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.todait.core.datastore.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 화면 밖으로 나가는 일회성 효과(네비게이션). */
sealed interface SplashEffect {
    data object NavigateToHome : SplashEffect
    data object NavigateToLogin : SplashEffect
}

/** 저장된 토큰 유무를 확인해 홈/로그인 화면으로 자동 분기한다. */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : ViewModel() {

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val effect = if (tokenDataStore.isLoggedIn()) {
                SplashEffect.NavigateToHome
            } else {
                SplashEffect.NavigateToLogin
            }
            _effect.send(effect)
        }
    }
}
