package com.umc.todait.core.network

import com.google.gson.Gson
import com.umc.todait.BuildConfig
import com.umc.todait.core.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 저장된 accessToken을 요청 헤더(Authorization: Bearer ...)에 자동으로 붙이고,
 * 인증 실패(AUTH401) 응답을 받으면 refreshToken으로 재발급을 한 번 시도한 뒤 원래 요청을 재시도한다.
 *
 * ⚠️ 공통 API 규약(global 도메인 명세) 기준, 서버는 인증 실패도 HTTP 상태 코드가 아니라
 * **HTTP 200 + body의 `isSuccess:false, code:"AUTH401"`**로 내려준다. 그래서 OkHttp의
 * response.code(HTTP 상태)만 보면 이 로직이 절대 안 걸린다 — 반드시 body를 들여다봐서 판단해야 한다.
 *
 * 재발급도 실패하면(refreshToken 자체가 만료/폐기됨) 원래 응답을 그대로 반환한다 —
 * 재로그인 유도(로그인 화면 이동 등)는 이 Interceptor가 아니라 상위 레이어(예: 공통 에러 핸들러)에서 처리한다.
 *
 * 토큰 재발급 호출은 Retrofit이 아니라 별도의 순수 OkHttpClient로 직접 만든다 —
 * Retrofit(AuthService)이 이 Interceptor가 붙은 OkHttpClient에 의존하므로, 여기서 같은
 * Retrofit/OkHttpClient를 다시 타면 순환 의존이 생긴다.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {

    private val refreshClient = OkHttpClient.Builder().build()
    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (isAuthFreeEndpoint(original)) {
            return chain.proceed(original)
        }

        val accessToken = runBlocking { tokenDataStore.getAccessToken() }
        val requestWithToken = if (accessToken != null) {
            original.newBuilder().header("Authorization", "Bearer $accessToken").build()
        } else {
            original
        }

        val response = chain.proceed(requestWithToken)
        if (!isAuthExpiredResponse(response)) return response

        // refreshToken 자체가 만료/폐기됐거나 네트워크 오류면, 원래 응답을 그대로 돌려준다
        // (재로그인 유도는 상위 레이어 몫).
        val newAccessToken = tryRefreshAccessToken() ?: return response

        response.close()
        val retryRequest = original.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
        return chain.proceed(retryRequest)
    }

    /** 로그인/회원가입/토큰재발급 자체 요청에는 accessToken을 붙이지 않는다. */
    private fun isAuthFreeEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.endsWith("/api/auth/login") ||
            path.endsWith("/api/auth/signup") ||
            path.endsWith("/api/auth/token/refresh")
    }

    /**
     * peekBody로 스트림을 소비하지 않고 body를 미리 들여다봐서 토큰 문제(AUTH401/AUTH403)인지 판단한다.
     * 혹시 나중에 서버가 진짜 HTTP 401을 내려주게 바뀌어도 대비해 response.code도 같이 확인한다.
     *
     * ⚠️ AUTH403은 "만료된 accessToken"(GET /api/members/me 명세)과 "권한 없음"(다른 API들) 둘 다에
     * 쓰이는 것으로 보여 경계가 애매함 — 백엔드 확인 전까지는 안전하게 둘 다 재발급 트리거로 취급한다
     * (권한 없음인데 재발급해도 재시도 결과가 똑같이 실패라 실질적 부작용은 적음).
     */
    private fun isAuthExpiredResponse(response: Response): Boolean {
        if (response.code == 401 || response.code == 403) return true
        return runCatching {
            val json = response.peekBody(PEEK_BODY_MAX_BYTES).string()
            val parsed = gson.fromJson(json, BaseResponse::class.java)
            !parsed.isSuccess && (parsed.code == "AUTH401" || parsed.code == "AUTH403")
        }.getOrDefault(false)
    }

    private fun tryRefreshAccessToken(): String? = runCatching {
        val refreshToken = runBlocking { tokenDataStore.getRefreshToken() } ?: return null
        // core는 feature에 의존하지 않으므로(§5) feature의 요청 DTO를 쓰지 않고 JSON을 직접 만든다.
        val body = gson.toJson(mapOf("refreshToken" to refreshToken))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/auth/token/refresh")
            .post(body)
            .build()
        refreshClient.newCall(request).execute().use { res ->
            val json = res.body?.string() ?: return null
            val parsed = gson.fromJson(json, BaseResponse::class.java)
            // 재발급 실패(AUTH401/403/410 등)도 HTTP 200으로 내려올 수 있으므로 isSuccess로 판단한다.
            if (!parsed.isSuccess) return null
            @Suppress("UNCHECKED_CAST")
            val result = parsed.result as? Map<String, Any?>
            val newAccessToken = result?.get("accessToken") as? String
            newAccessToken?.also { token -> runBlocking { tokenDataStore.updateAccessToken(token) } }
        }
    }.getOrNull()

    private companion object {
        const val PEEK_BODY_MAX_BYTES = 2048L
    }
}
