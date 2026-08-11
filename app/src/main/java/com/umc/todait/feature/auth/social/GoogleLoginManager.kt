package com.umc.todait.feature.auth.social

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.umc.todait.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 구글 로그인(Credential Manager + Sign in with Google)을 실행하고 결과를 돌려준다.
 *
 * 백엔드가 발급한 Web Client ID 를 serverClientId 로 넘겨야 서버에서 검증 가능한 idToken 이 발급된다.
 * 성공 시 [GoogleIdTokenCredential] 의 idToken(JWT)을 백엔드로 넘겨 검증/로그인한다.
 * 참고: Credential Manager 는 기본적으로 idToken 을 준다. serverAuthCode/accessToken 은 별도 Authorization API 필요.
 *
 * 요청 방식이 두 가지라 순서대로 시도한다.
 * ① [GetGoogleIdOption] — 기기에 있는 구글 계정을 바로 고르게 하는 방식(바텀시트).
 * ② [GetSignInWithGoogleOption] — "Sign in with Google" 버튼 플로우. 계정 선택 화면을 항상 띄운다.
 *
 * ①이 [NoCredentialException]("No credentials available")으로 실패하는 경우가 있어 ②로 한 번 더 시도한다.
 * `setFilterByAuthorizedAccounts(false)` 로도 ①이 실패할 수 있는데, 이때 ②는 성공하기도 한다.
 */
@Singleton
class GoogleLoginManager @Inject constructor() {

    suspend fun login(context: Context): Result<GoogleIdTokenCredential> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false) // 이 기기에서 처음 로그인하는 계정도 노출
            .setAutoSelectEnabled(false)
            .build()

        return requestCredential(context, googleIdOption)
            .recoverCatching { error ->
                // 자격 증명 없음일 때만 폴백한다 — 사용자가 취소한 경우까지 다시 띄우면 안 된다.
                if (error !is NoCredentialException) throw error
                Log.w(TAG, "GetGoogleIdOption 실패 — SignInWithGoogle 로 재시도")
                val signInOption = GetSignInWithGoogleOption
                    .Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()
                requestCredential(context, signInOption).getOrThrow()
            }
    }

    /** Credential Manager 호출 + 응답을 [GoogleIdTokenCredential] 로 변환하는 공통 부분. */
    private suspend fun requestCredential(
        context: Context,
        option: CredentialOption,
    ): Result<GoogleIdTokenCredential> {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        return try {
            val credential = CredentialManager.create(context).getCredential(context, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                Result.success(GoogleIdTokenCredential.createFrom(credential.data))
            } else {
                Result.failure(IllegalStateException("예상치 못한 자격 증명 타입: ${credential.type}"))
            }
        } catch (e: GetCredentialException) {
            Log.w(TAG, "구글 로그인 실패", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "SocialLogin/Google"
    }
}
