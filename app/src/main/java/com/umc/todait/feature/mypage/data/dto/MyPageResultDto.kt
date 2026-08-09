package com.umc.todait.feature.mypage.data.dto

import com.google.gson.annotations.SerializedName

/**
 * "마이페이지 사용자 정보 조회"(GET /api/members/me)의 result.
 * (BaseResponse<MyPageResultDto> 형태로 내려온다.)
 */
data class MyPageResultDto(
    @SerializedName("memberId") val memberId: Long,
    // 소셜 회원이 카카오 이메일 제공에 동의하지 않으면 서버가 null 을 내려준다(명세상 정상 응답).
    @SerializedName("email") val email: String?,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("savedCourseCount") val savedCourseCount: Int
)


