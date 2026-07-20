package com.umc.todait.feature.mypage.data.dto

data class MyPageResultDto(
    val memberId: Long,
    val email: String,
    val nickname: String,
    val profileImageUrl: String,
    val role: String,
    val status: String,
    val provider: String,
    val notificationSetting: NotificationSettingDto
)

data class NotificationSettingDto(
    val pushEnabled: Boolean,
    val marketingEnabled: Boolean,
    val serviceEnabled: Boolean
)