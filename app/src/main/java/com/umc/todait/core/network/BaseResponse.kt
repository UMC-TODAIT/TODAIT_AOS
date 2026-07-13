package com.umc.todait.core.network

import com.google.gson.annotations.SerializedName

/**
 * 백엔드 공통 응답 래퍼. "공통 API 오류/빈 상태 처리" 명세(global 도메인) 기준으로 확정됨.
 *
 * ⚠️ 실패 응답도 HTTP 상태 코드가 아니라 이 body의 isSuccess/code로만 구분된다
 * (예: 인증 실패도 HTTP 200 + isSuccess:false + code:"AUTH401"로 내려옴).
 * HttpException(4xx/5xx) 이 아니라 이 필드들로 성공/실패를 판단해야 한다 — SafeApiCall.kt 참고.
 */
data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T?,
)
