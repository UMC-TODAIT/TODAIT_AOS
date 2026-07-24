package com.umc.todait.feature.auth.social

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.umc.todait.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 구글 로그인(Credential Manager + Sign in with Google)을 실행하고 결과를 돌려준다.
 *
 * 백엔드가 발급한 Web Client ID 를 serverClientId 로 넘겨야 서버에서 검증 가능한 idToken 이 발급된다.
 * 성공 시 [GoogleIdTokenCredential] 의 idToken(JWT)을 백엔드로 넘겨 검증/로그인한다(계약 확정 후 연결).
 * 참고: Credential Manager 는 기본적으로 idToken 을 준다. serverAuthCode/accessToken 은 별도 Authorization API 필요.
 */
@Singleton
class GoogleLoginManager @Inject constructor() {

    suspend fun login(context: Context): Result<GoogleIdTokenCredential> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false) // 이 기기에서 처음 로그인하는 계정도 노출
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val response = CredentialManager.create(context).getCredential(context, request)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleCredential)
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
