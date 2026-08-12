package com.umc.todait.navigation

import androidx.lifecycle.ViewModel
import com.umc.todait.core.network.SessionExpiredNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * 앱 전역 세션 상태를 화면에 전달하는 ViewModel.
 *
 * 토큰 재발급이 실패하면 [SessionExpiredNotifier] 가 이벤트를 흘려보내는데,
 * 그 통로는 core 에 있어 컴포저블이 직접 구독할 수 없다. NavHost 가 이 ViewModel 을 통해 구독한다.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    sessionExpiredNotifier: SessionExpiredNotifier,
) : ViewModel() {

    /** 재로그인이 필요해졌을 때 방출된다. NavHost 가 받아 로그인 화면으로 보낸다. */
    val sessionExpired: SharedFlow<Unit> = sessionExpiredNotifier.events
}
