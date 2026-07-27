package com.umc.todait.core.network

/**
 * 화면에 그대로 노출 가능한 형태의 에러.
 * 문구는 요구사항 명세서의 "공통 메시지 문구" 정책을 따른다.
 */
data class UiError(
    val message: String,
    val isRetryable: Boolean = true,
)

/**
 * 공통 응답 규약(global 도메인 명세)의 "프론트엔드 처리 원칙"에 맞춘 매핑.
 *
 * - 인증(AUTH401/AUTH403): 재로그인·토큰 재발급 흐름이 필요하므로 재시도 버튼을 노출하지 않는다.
 * - 404: 상세 리소스 없음 → 재시도해도 결과가 같다.
 * - 409: 현재 상태와 요청 동작 충돌 → 사용자가 단계를 다시 확인해야 한다.
 * - 400: 요청값 오류 → 서버 message 를 그대로 노출한다(검색어 2자 미만 등 안내 문구가 담긴다).
 * - 500 / 502 / 429: 일시적 오류 → 재시도 안내.
 *
 * 서버 message 가 있으면 우선 사용하고, 없을 때만 공통 문구로 대체한다.
 */
fun ApiResult.Failure.toUiError(): UiError = when (this) {
    is ApiResult.Failure.NetworkError ->
        UiError(message = "연결 상태를 확인해주세요.")

    is ApiResult.Failure.ServerError -> toUiError()

    is ApiResult.Failure.UnknownError ->
        UiError(message = DEFAULT_ERROR_MESSAGE)
}

private fun ApiResult.Failure.ServerError.toUiError(): UiError {
    val fallback = when {
        isAuthError -> "다시 로그인해주세요."
        httpStatus == 404 || code?.endsWith("404") == true -> "요청한 정보를 찾을 수 없어요."
        httpStatus == 409 || code?.endsWith("409") == true -> "현재 단계에서는 처리할 수 없어요. 다시 확인해주세요."
        else -> DEFAULT_ERROR_MESSAGE
    }
    return UiError(
        message = message?.takeIf { it.isNotBlank() } ?: fallback,
        isRetryable = isRetryable,
    )
}

/** 인증 체계 오류(재로그인/토큰 재발급 대상). 도메인 403(리소스 소유권)은 제외한다. */
private val ApiResult.Failure.ServerError.isAuthError: Boolean
    get() = code == CODE_AUTH_401 || code == CODE_AUTH_403

/** 재시도해서 결과가 달라질 수 있는 오류인지. 인증·404·409·400 은 그대로 재시도해도 의미가 없다. */
private val ApiResult.Failure.ServerError.isRetryable: Boolean
    get() = when {
        isAuthError -> false
        httpStatus == null -> true
        httpStatus in 400..499 -> httpStatus == 429
        else -> true
    }

private const val CODE_AUTH_401 = "AUTH401"
private const val CODE_AUTH_403 = "AUTH403"
private const val DEFAULT_ERROR_MESSAGE = "일시적인 오류가 발생했어요. 다시 시도해주세요."
