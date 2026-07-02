package com.umc.todait.core.network

import java.io.IOException

/**
 * 네트워크 호출 결과를 표현하는 공통 타입.
 * ViewModel은 Response/Exception을 직접 다루지 않고 ApiResult만 다룬다.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>

    sealed interface Failure : ApiResult<Nothing> {
        /** 서버가 응답했지만 실패 (4xx/5xx 또는 isSuccess = false) */
        data class ServerError(val code: String?, val message: String?) : Failure
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
