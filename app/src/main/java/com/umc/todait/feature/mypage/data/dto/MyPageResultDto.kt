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
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("savedCourseCount") val savedCourseCount: Int
)


