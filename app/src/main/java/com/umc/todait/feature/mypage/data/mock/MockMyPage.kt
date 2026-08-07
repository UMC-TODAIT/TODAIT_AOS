package com.umc.todait.feature.mypage.data.mock

import com.umc.todait.feature.mypage.data.dto.MyPageResultDto

object MockMyPage {
    val myPage = MyPageResultDto(
        memberId = 1L,
        email = "todait@naver.com",
        nickname = "투데잇",
        profileImageUrl = "",
        savedCourseCount = 0
    )
}