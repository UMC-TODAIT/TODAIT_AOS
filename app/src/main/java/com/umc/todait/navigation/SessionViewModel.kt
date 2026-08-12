package com.umc.todait.navigation

import androidx.lifecycle.ViewModel
import com.umc.todait.core.network.SessionExpiredNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * 앱 전역 세션 상태를 화면에 전달하는 ViewModel.
 *
 * 토큰 재발급이 실패하면 [SessionExpiredNotifier] 가 상태를 세우는데,
 * 그 통로는 core 에 있어 컴포저블이 직접 구독할 수 없다. NavHost 가 이 ViewModel 을 통해 구독한다.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionExpiredNotifier: SessionExpiredNotifier,
) : ViewModel() {

    /** 재로그인이 필요해지면 true 가 된다. NavHost 가 받아 로그인 화면으로 보낸다. */
    val isSessionExpired: StateFlow<Boolean> = sessionExpiredNotifier.isSessionExpired

    /** 로그인 화면으로 이동을 마쳤음을 알린다(상태 내림). */
    fun onSessionExpiredHandled() {
        sessionExpiredNotifier.onSessionExpiredHandled()
    }
}
