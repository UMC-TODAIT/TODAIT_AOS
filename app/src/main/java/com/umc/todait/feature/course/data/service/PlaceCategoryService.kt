package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.PlaceCategoryListResponseDto
import retrofit2.http.GET

/**
 * 장소 대분류(카테고리) 조회 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 */
interface PlaceCategoryService {

    /**
     * 장소 대분류 목록 — GET /api/place-categories
     * 코스 구성하기 화면의 카페/식당/액티비티/술 탭에 사용하는 기준 데이터.
     */
    @GET("api/place-categories")
    suspend fun getPlaceCategories(): BaseResponse<PlaceCategoryListResponseDto>
}
