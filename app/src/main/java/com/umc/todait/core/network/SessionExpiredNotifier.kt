package com.umc.todait.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션이 끊겨 재로그인이 필요해졌음을 화면에 알리는 통로.
 *
 * 토큰 재발급이 실패하면(refreshToken 만료·폐기) [AuthInterceptor] 가 저장된 토큰을 지우는데,
 * 화면 이동은 UI 레이어의 몫이라 core 에서 직접 할 수 없다. 그래서 상태만 세워 두고
 * NavHost 가 이를 구독해 로그인 화면으로 보낸다.
 *
 * 일회성 이벤트(SharedFlow)가 아니라 **상태**로 들고 있는 이유는, 화면 회전 등으로 구독자가
 * 잠깐 떨어진 사이에 만료가 발생하면 이벤트가 그대로 사라져 인증이 필요한 화면에 남아 있게 되기
 * 때문이다. NavHost 가 로그인 화면으로 보낸 뒤 [onSessionExpiredHandled] 로 직접 내린다.
 */
@Singleton
class SessionExpiredNotifier @Inject constructor() {

    private val _isSessionExpired = MutableStateFlow(false)
    val isSessionExpired: StateFlow<Boolean> = _isSessionExpired.asStateFlow()

    /** 인터셉터(백그라운드 스레드)에서 호출하므로 suspend 가 아닌 값 대입으로 처리한다. */
    fun notifySessionExpired() {
        _isSessionExpired.value = true
    }

    /** 로그인 화면으로 이동을 마친 뒤 호출한다. 내리지 않으면 다음 구독에서 또 튕긴다. */
    fun onSessionExpiredHandled() {
        _isSessionExpired.value = false
    }
}
