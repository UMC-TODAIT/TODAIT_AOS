package com.umc.todait.feature.mypage.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "마이페이지 사용자 정보 조회"(GET /api/members/me)의 result.
 * (BaseResponse<MyPageResultDto> 형태로 내려온다.)
 */
data class MyPageResultDto(
    @SerializedName("memberId") val memberId: Long,
    @SerializedName("email") val email: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String,
    // 회원 권한(USER 등).
    @SerializedName("role") val role: String,
    // 회원 상태(ACTIVE 등).
    @SerializedName("status") val status: String,
    // 가입 경로(EMAIL/KAKAO/GOOGLE).
    @SerializedName("provider") val provider: String,
    @SerializedName("notificationSetting") val notificationSetting: NotificationSettingDto
)

data class NotificationSettingDto(
    @SerializedName("pushEnabled") val pushEnabled: Boolean,
    @SerializedName("marketingEnabled") val marketingEnabled: Boolean,
    @SerializedName("serviceEnabled") val serviceEnabled: Boolean
)
