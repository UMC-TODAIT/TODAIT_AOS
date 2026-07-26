package com.umc.todait.core.network

import java.io.IOException

/**
 * 네트워크 호출 결과를 표현하는 공통 타입.
 * ViewModel은 Response/Exception을 직접 다루지 않고 ApiResult만 다룬다.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing> {
        /**
         * 서버가 응답했지만 실패한 경우.
         *
         * 공통 응답 규약(global 도메인 명세)상 실패는 HTTP 4xx/5xx 와 body 의 [code] 를 함께 내려준다.
         * [code] 는 body 의 응답 코드(AUTH401, COURSE404, PLACE_SEARCH400 ...),
         * [httpStatus] 는 HTTP 상태 코드다. body 를 파싱하지 못하면 [code] 가 null 일 수 있다.
         */
        data class ServerError(
            val code: String?,
            val message: String?,
            val httpStatus: Int? = null,
        ) : Failure
        /** 네트워크 연결 실패 (오프라인, 타임아웃 등) */
        data class NetworkError(val throwable: IOException) : Failure
        /** 파싱 실패 등 알 수 없는 오류 */
        data class UnknownError(val throwable: Throwable) : Failure
    }
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onFailure(action: (ApiResult.Failure) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(this)
    return this
}
