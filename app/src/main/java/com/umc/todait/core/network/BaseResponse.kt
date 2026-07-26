package com.umc.todait.core.network

import com.google.gson.annotations.SerializedName

/**
 * 백엔드 공통 응답 래퍼. "공통 API 오류/빈 상태 처리" 명세(global 도메인) 기준으로 확정됨.
 *
 * 성공/실패 모두 이 네 필드를 사용한다.
 * - 성공: `isSuccess: true`, 도메인 성공 코드(COMMON200/COURSE201/PLACE200 ...), result 에 데이터
 * - 실패: HTTP 4xx/5xx + `isSuccess: false`, 도메인 오류 코드, **result 는 항상 null**
 *
 * HTTP 상태 코드와 body 의 [code] 를 함께 확인해야 한다 — 상태 코드만으로는
 * AUTH403(만료 토큰)과 COURSE403(소유권 없음)처럼 대응이 다른 오류를 구분할 수 없다.
 * (실패 응답 처리는 SafeApiCall.kt 참고)
 */
data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T?,
)
