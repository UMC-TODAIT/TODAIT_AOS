package com.umc.todait.feature.mypage.data.mock

import com.umc.todait.feature.mypage.data.dto.MyPageResultDto
import com.umc.todait.feature.mypage.data.dto.NotificationSettingDto

object MockMyPage {

    val myPage = MyPageResultDto(
        memberId = 1L,
        email = "todait@naver.com",
        nickname = "투데잇",
        profileImageUrl = "",
        role = "USER",
        status = "ACTIVE",
        provider = "KAKAO",
        notificationSetting = NotificationSettingDto(
            pushEnabled = true,
            marketingEnabled = true,
            serviceEnabled = true
        )
    )
}