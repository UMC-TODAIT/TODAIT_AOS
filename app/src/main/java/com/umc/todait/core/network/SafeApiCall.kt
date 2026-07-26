package com.umc.todait.core.network

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/**
 * Retrofit 호출을 감싸 예외를 ApiResult로 변환하는 공통 에러 핸들러.
 *
 * 공통 응답 규약(global 도메인 "공통 API 오류/빈 상태 처리" 명세) 기준:
 * - 실패는 HTTP 4xx/5xx 로 내려오고, body 에도 `isSuccess:false` 와 도메인 응답 코드가 함께 담긴다.
 *   (AUTH401 / AUTH403 / COURSE404 / PLACE_SEARCH400 / KAKAO_API502 ...)
 * - 프론트는 HTTP Status 와 body 의 code 를 **함께** 확인한다. 그래서 HttpException 이면
 *   errorBody 를 [BaseResponse] 로 파싱해 code·message 를 살려서 넘긴다.
 *   (파싱 실패 시에만 HTTP 상태 코드로 대체)
 * - 실패 응답의 result 는 항상 null 이다.
 *
 * 사용 예:
 * ```
 * suspend fun searchPlaces(query: String): ApiResult<PlaceSearchResultDto> =
 *     safeApiCall { searchService.searchPlaces(query) }
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
        e.toServerError()
    } catch (e: IOException) {
        ApiResult.Failure.NetworkError(e)
    } catch (e: Throwable) {
        ApiResult.Failure.UnknownError(e)
    }
}

/**
 * HTTP 오류 응답의 body(공통 Wrapper)를 파싱해 서버가 준 code·message 를 살린다.
 * body 가 없거나 규약과 다른 형태면 HTTP 상태 코드만 담아 반환한다.
 */
private fun HttpException.toServerError(): ApiResult.Failure.ServerError {
    val status = code()
    val parsed = runCatching {
        response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }?.let { body ->
            errorBodyGson.fromJson(body, BaseResponse::class.java)
        }
    }.getOrNull()

    return ApiResult.Failure.ServerError(
        code = parsed?.code,
        message = parsed?.message,
        httpStatus = status,
    )
}

private val errorBodyGson = Gson()
