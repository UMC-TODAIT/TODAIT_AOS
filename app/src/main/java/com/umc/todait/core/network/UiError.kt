package com.umc.todait.core.network

/**
 * 화면에 그대로 노출 가능한 형태의 에러.
 * 문구는 요구사항 명세서의 "공통 메시지 문구" 정책을 따른다.
 */
data class UiError(
    val message: String,
    val isRetryable: Boolean = true,
)

fun ApiResult.Failure.toUiError(): UiError = when (this) {
    is ApiResult.Failure.NetworkError ->
        UiError(message = "연결 상태를 확인해주세요.")

    is ApiResult.Failure.ServerError ->
        UiError(message = message ?: "일시적인 오류가 발생했어요. 다시 시도해주세요.")

    is ApiResult.Failure.UnknownError ->
        UiError(message = "일시적인 오류가 발생했어요. 다시 시도해주세요.")
}
