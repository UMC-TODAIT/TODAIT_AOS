package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.RecommendationResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 추천 장소 조회 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 추후 AuthInterceptor 에서 일괄 부착 예정.
 */
interface RecommendationService {

    /**
     * 추천 장소 조회 — GET /api/recommendations/places
     * type 은 필수(HOME, POPULAR, HOME_PLACE, NEAR_BASE_PLACE 등).
     * NEAR_BASE_PLACE 유형일 때는 basePlaceId 가 필수다(미지정 시 RECOMMEND400).
     * DISTANCE 계산이 필요하면 latitude·longitude 를 함께 넘긴다.
     *
     * TODO: 명세서 Path Variable 에 courseId(조회할 저장 코스 ID)가 적혀 있으나
     *       uri(/api/recommendations/places)에는 경로 변수가 없다. BE(죠)에 확인 후
     *       필요하면 @Path 로 반영한다. (SearchService.searchPlaceResults 와 동일 이슈)
     */
    @GET("api/recommendations/places")
    suspend fun getRecommendedPlaces(
        @Query("type") type: String,
        @Query("courseDraftId") courseDraftId: Long? = null,
        @Query("basePlaceId") basePlaceId: Long? = null,
        @Query("placeCategoryId") placeCategoryId: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<RecommendationResultDto>
}
