package com.umc.todait.feature.mypage.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.mypage.data.dto.MyPageResultDto
import retrofit2.http.GET

interface MyPageService {

    @GET("api/members/me")
    suspend fun getMyPage(): BaseResponse<MyPageResultDto>
}