package com.umc.todait.core.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Retrofit 호출을 감싸 예외를 ApiResult로 변환하는 공통 에러 핸들러.
 *
 * 사용 예:
 * ```
 * suspend fun getNearbyHotPlaces(): ApiResult<List<PlaceDto>> =
 *     safeApiCall { placeService.getNearbyHotPlaces() }
 * ```
 */
suspend fun <T> safeApiCall(call: suspend () -> BaseResponse<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccess && response.result != null) {
            ApiResult.Success(response.result)
        } else {
            ApiResult.Failure.ServerError(code = response.code, message = response.message)
        }
    } catch (e: HttpException) {
        ApiResult.Failure.ServerError(code = e.code().toString(), message = e.message())
    } catch (e: IOException) {
        ApiResult.Failure.NetworkError(e)
    } catch (e: Throwable) {
        ApiResult.Failure.UnknownError(e)
    }
}
