package com.umc.todait.core.network

import com.google.gson.annotations.SerializedName

/**
 * 백엔드 공통 응답 래퍼.
 * ⚠️ TODAIT_BE 응답 스펙 확정 후 필드명(isSuccess/code/message/result)을 맞춰서 수정한다.
 */
data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T?,
)
