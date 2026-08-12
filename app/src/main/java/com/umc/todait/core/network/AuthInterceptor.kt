package com.umc.todait.core.network

import com.google.gson.Gson
import com.umc.todait.BuildConfig
import com.umc.todait.core.datastore.TokenDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 * 인증 실패(AUTH401/AUTH403) 응답을 받으면 refreshToken으로 재발급을 한 번 시도한 뒤 원래 요청을 재시도한다.
 *
 * ⚠️ 공통 API 규약(global 도메인 명세) 기준:
 * - AUTH401(HTTP 401) = 인증 정보 없음(헤더 누락 등)
 * - AUTH403(HTTP 403) = **유효하지 않거나 만료된 accessToken** → 재발급 대상이 여기다
 * - 도메인별 403(COURSE403 등) = 리소스 소유권 문제라 재발급해도 소용없으므로 트리거하지 않는다
 *
 * HTTP 상태 코드와 body의 code를 함께 확인한다 — 서버가 상태 코드를 정확히 내려주지 않는 경우에도
 * body의 code로 판단할 수 있어야 하기 때문이다.
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
    private val sessionExpiredNotifier: SessionExpiredNotifier,
) : Interceptor {

    private val refreshClient = OkHttpClient.Builder().build()

    // 동시에 401 이 여러 건 발생해도 재발급 요청은 한 번만 나가도록 직렬화한다.
    private val refreshMutex = Mutex()
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

        // refreshToken 자체가 만료/폐기됐거나 네트워크 오류면, 원래 응답을 그대로 돌려준다.
        // (이때 토큰은 지워지고 세션 만료 이벤트가 나가 화면이 로그인으로 이동한다)
        val newAccessToken = refreshTokens(usedAccessToken = accessToken) ?: return response

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
            path.endsWith("/api/auth/kakao/login") ||
            path.endsWith("/api/auth/google/login") ||
            path.endsWith("/api/auth/signup") ||
            path.endsWith("/api/auth/token/refresh") ||
            // 명세상 인증 헤더 불필요(public). accessToken이 남아있어도 붙이지 않는다.
            path.endsWith("/api/members/nickname-availability") ||
            // Bearer temporaryToken을 @Header로 직접 싣는 요청 — accessToken으로 덮어쓰면 안 된다.
            path.endsWith("/api/members/me/onboarding")
    }

    /**
     * peekBody로 스트림을 소비하지 않고 body를 미리 들여다봐서 accessToken 만료(AUTH401/AUTH403)인지 판단한다.
     *
     * HTTP 403은 도메인별 소유권 오류(COURSE403 등)로도 내려오므로, 상태 코드만 보고 재발급하지 않고
     * body의 code가 AUTH403일 때만 트리거한다. 401은 인증 체계 오류 전용이라 상태 코드로도 판단한다.
     *
     * ⚠️ 단, 배포 서버는 토큰이 없거나 유효하지 않을 때 Spring Security 기본 동작으로
     * **HTTP 403 + 빈 body**를 내려준다(공통 Wrapper가 아니다). 이 경우 body로 구분할 수 없으므로
     * 인증 오류로 보고 재발급을 시도한다 — 실제로 도메인 403이었다면 재시도에서 같은 403이 오고 끝난다.
     */
    private fun isAuthExpiredResponse(response: Response): Boolean {
        if (response.code == 401) return true
        if (response.code != 403 && response.code != 200) return false

        val json = runCatching { response.peekBody(PEEK_BODY_MAX_BYTES).string() }.getOrNull()
        if (json.isNullOrBlank()) return response.code == 403

        return runCatching {
            val parsed = gson.fromJson(json, BaseResponse::class.java)
            !parsed.isSuccess && (parsed.code == CODE_AUTH_401 || parsed.code == CODE_AUTH_403)
            // 공통 Wrapper 형태가 아니면(파싱 실패) 403일 때만 인증 오류로 본다.
        }.getOrDefault(response.code == 403)
    }

    /**
     * 토큰 재발급. 성공하면 새 accessToken 을 돌려주고, 실패하면 저장된 인증 정보를 지운 뒤
     * 세션 만료를 알린다(→ 로그인 화면 이동).
     *
     * **동시에 여러 요청이 401 을 받아도 재발급은 한 번만 실행된다.** [refreshMutex] 로 직렬화하고,
     * 락을 잡은 뒤 저장된 accessToken 이 내가 보냈던 값([usedAccessToken])과 달라졌으면
     * 다른 요청이 이미 갱신을 끝낸 것이므로 그 값을 그대로 쓴다.
     */
    private fun refreshTokens(usedAccessToken: String?): String? = runBlocking {
        refreshMutex.withLock {
            val current = tokenDataStore.getAccessToken()
            if (current != null && current != usedAccessToken) return@withLock current

            val refreshToken = tokenDataStore.getRefreshToken()
            val newTokens = refreshToken?.let { requestNewTokens(it) }
            if (newTokens == null) {
                // Rotation 적용 후에는 재발급 실패 = refreshToken 도 못 쓰는 상태라 재로그인 외에 방법이 없다.
                tokenDataStore.clearTokens()
                sessionExpiredNotifier.notifySessionExpired()
                return@withLock null
            }
            // Rotation: 요청에 쓴 기존 refreshToken 은 서버에서 즉시 폐기되므로 둘 다 교체 저장해야 한다.
            tokenDataStore.saveTokens(newTokens.accessToken, newTokens.refreshToken)
            newTokens.accessToken
        }
    }

    private data class TokenPair(val accessToken: String, val refreshToken: String)

    private fun requestNewTokens(refreshToken: String): TokenPair? = runCatching {
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
            // 재발급 실패는 HTTP 4xx로 오지만, 상태 코드와 무관하게 body의 isSuccess로 판단한다.
            if (!parsed.isSuccess) return null
            @Suppress("UNCHECKED_CAST")
            val result = parsed.result as? Map<String, Any?>
            val newAccessToken = result?.get("accessToken") as? String ?: return null
            val newRefreshToken = result["refreshToken"] as? String ?: return null
            TokenPair(newAccessToken, newRefreshToken)
        }
    }.getOrNull()

    private companion object {
        const val PEEK_BODY_MAX_BYTES = 2048L
        const val CODE_AUTH_401 = "AUTH401"
        const val CODE_AUTH_403 = "AUTH403"
    }
}
