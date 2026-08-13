package com.umc.todait.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * 사용자의 탭 한 번이 화면 하나만 열도록 보장하는 [NavController.navigate].
 *
 * 장소 카드처럼 목록에 나란히 놓인 항목을 빠르게 두 번 누르면(또는 서로 다른 카드를 연달아 누르면)
 * 화면 전환 애니메이션이 끝나기 전에 클릭이 한 번 더 들어와 같은 화면이 두 장 쌓인다. 사용자에게는
 * "창이 여러 개 열리는" 것으로 보이고, 뒤로가기를 그만큼 눌러야 빠져나올 수 있다.
 *
 * 화면 전환이 시작되면 목록 화면의 back stack entry 는 RESUMED 아래로 내려간다. 그 사이에 들어온
 * 두 번째 클릭은 여기서 걸러진다. 전환이 끝나 새 화면이 자리를 잡으면 목록 화면은 더 이상
 * currentBackStackEntry 가 아니므로, 정상적인 다음 이동까지 막지는 않는다.
 *
 * ⚠️ 화면을 연달아 쌓는 **프로그램적 이동**에는 쓰면 안 된다. 첫 번째 navigate 직후 새 entry 가
 * 아직 RESUMED 가 아니라 두 번째부터 전부 막힌다(예: 진행 중 임시 코스를 이어서 할 때 거쳐 온
 * 화면들을 한 번에 쌓는 [navigateToDraftStep]). 그런 곳은 [NavController.navigate] 를 그대로 쓴다.
 */
fun NavController.navigateOnce(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    // currentBackStackEntry 가 null 인 경우(그래프 초기화 직전)는 막을 이유가 없어 그대로 보낸다.
    val entry = currentBackStackEntry ?: run {
        navigate(route, builder)
        return
    }
    if (entry.lifecycle.currentState != Lifecycle.State.RESUMED) return
    navigate(route, builder)
}
