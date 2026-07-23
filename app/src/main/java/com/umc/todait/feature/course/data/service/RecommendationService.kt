package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.RecommendationResultDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 추천 장소 조회 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * ⚠️ 2차 대기: 명세 개편으로 `/api/recommendations/places` 는 제거됐고, 코스 구성 추천 카드는
 *  `GET /api/course-drafts/{courseDraftId}/places`(카테고리별 장소 카드)로 대체될 예정이다.
 *  현재 이 엔드포인트는 배포 서버에 없어 호출 시 실패한다(추천 목록은 에러 상태로 표시됨).
 *  BE 배포 확정 시 이 서비스를 course-draft 기반으로 교체한다.
 */
interface RecommendationService {

    /**
     * 추천 장소 조회 — GET /api/recommendations/places
     * type 은 필수(HOME, POPULAR, HOME_PLACE, NEAR_BASE_PLACE 등).
     * NEAR_BASE_PLACE 유형일 때는 basePlaceId 가 필수다(미지정 시 RECOMMEND400).
     * DISTANCE 계산이 필요하면 latitude·longitude 를 함께 넘긴다.
     *
     * TODO(2차): 위 클래스 주석대로 course-draft 기반 엔드포인트로 교체 예정.
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
