package com.umc.todait.feature.mypage.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.mypage.data.dto.MyPageResultDto
import com.umc.todait.feature.mypage.data.service.MyPageService
import javax.inject.Inject

class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService,
) {
    suspend fun getMyPage(): ApiResult<MyPageResultDto> =
        safeApiCall {
            myPageService.getMyPage()
        }
}