package com.umc.todait.feature.mypage.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.mypage.data.dto.MyPageResultDto
import com.umc.todait.feature.mypage.data.mock.MockMyPage
import com.umc.todait.feature.mypage.data.service.MyPageService
import javax.inject.Inject

private const val USE_MOCK = true
class MyPageRepository @Inject constructor(
    private val myPageService: MyPageService,
) {
    suspend fun getMyPage(): ApiResult<MyPageResultDto> {

        if (USE_MOCK) {
            return ApiResult.Success(MockMyPage.myPage)
        }

        return safeApiCall {
            myPageService.getMyPage()
        }
    }
}