package com.umc.todait.feature.home.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 홈 화면 전용 Retrofit 서비스. 로그인 필요(Authorization: Bearer, AuthInterceptor가 일괄 부착).
 */
interface HomeService {

    /**
     * 취향 기반 추천 장소 조회 — GET /api/recommendations/places?type=HOME_PLACE
     * (course 도메인의 RecommendationService.getRecommendedPlaces와 동일 엔드포인트)
     */
    @GET("api/recommendations/places")
    suspend fun getRecommendedPlaces(
        @Query("type") type: String,
        @Query("size") size: Int? = null,
    ): BaseResponse<HomeRecommendedPlaceResultDto>
}
