package com.umc.todait.feature.auth.social

import android.content.Context
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.umc.todait.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 카카오 로그인(네이티브 SDK, `com.kakao.sdk:v2-user`)을 실행하고 결과 토큰을 돌려준다.
 *
 * 카카오톡 앱이 설치돼 있으면 앱 로그인, 없거나 실패하면 카카오계정(웹) 로그인으로 폴백한다.
 * 성공 시 [OAuthToken] 에는 accessToken/refreshToken/idToken(OIDC 활성화 시)/만료시각/scopes 가 들어온다.
 * 이후 이 accessToken 을 백엔드로 넘겨 서버 로그인/온보딩을 진행한다(백엔드 계약 확정 후 연결).
 */
@Singleton
class KakaoLoginManager @Inject constructor() {

    suspend fun login(context: Context): Result<OAuthToken> {
        // 네이티브 앱 키 미설정 시 KakaoSdk.init 이 스킵되어(TodaitApplication) UserApiClient 접근이
        // UninitializedPropertyAccessException 으로 앱 전체를 죽인다 — 크래시 대신 실패로 안내한다.
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isEmpty()) {
            return Result.failure(IllegalStateException("카카오 네이티브 앱 키가 설정되지 않았습니다."))
        }
        return loginInternal(context)
    }

    private suspend fun loginInternal(context: Context): Result<OAuthToken> = suspendCancellableCoroutine { cont ->
        // 카카오톡/카카오계정 공통 콜백
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                error != null -> {
                    Log.w(TAG, "카카오 로그인 실패", error)
                    if (cont.isActive) cont.resume(Result.failure(error))
                }
                token != null ->
                    if (cont.isActive) cont.resume(Result.success(token))
                else -> if (cont.isActive) {
                    cont.resume(Result.failure(IllegalStateException("카카오 토큰이 비어 있습니다.")))
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    // 사용자가 카카오톡 로그인 화면에서 '취소'한 경우는 폴백하지 않고 종료
                    error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                        if (cont.isActive) cont.resume(Result.failure(error))
                    // 그 외 오류(카카오톡 미로그인 등)는 카카오계정 로그인으로 폴백
                    error != null ->
                        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                    token != null ->
                        if (cont.isActive) cont.resume(Result.success(token))
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    companion object {
        private const val TAG = "SocialLogin/Kakao"
    }
}
