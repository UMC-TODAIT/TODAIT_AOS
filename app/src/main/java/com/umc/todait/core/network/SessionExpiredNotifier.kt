package com.umc.todait.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 세션이 끊겨 재로그인이 필요해졌음을 화면에 알리는 통로.
 *
 * 토큰 재발급이 실패하면(refreshToken 만료·폐기) [AuthInterceptor] 가 저장된 토큰을 지우는데,
 * 화면 이동은 UI 레이어의 몫이라 core 에서 직접 할 수 없다. 그래서 이벤트만 흘려보내고
 * NavHost 가 이를 구독해 로그인 화면으로 보낸다.
 *
 * 구독자가 없을 때 발생한 이벤트는 버린다(replay = 0) — 앱이 떠 있지 않은 동안 쌓아 두었다가
 * 나중에 갑자기 로그인 화면으로 튕기는 것을 막기 위함이다.
 */
@Singleton
class SessionExpiredNotifier @Inject constructor() {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    /** 인터셉터(백그라운드 스레드)에서 호출하므로 suspend 가 아닌 tryEmit 을 쓴다. */
    fun notifySessionExpired() {
        _events.tryEmit(Unit)
    }
}
