package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.HotPlaceResultDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceResultDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 추천(recommendation) 도메인 Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * 두 추천 API 모두 임시 코스(course-draft) 기준이라 courseDraftId 를 경로 변수로 받는다.
 * 명세 개편으로 사라진 `/api/recommendations/places` 는 더 이상 사용하지 않는다.
 */
interface RecommendationService {

    /**
     * 지금 내 주변 핫플 조회 — GET /api/course-drafts/{courseDraftId}/hot-places
     *
     * 기준 장소 설정 화면에 진입했을 때(검색어 입력 전) 호출한다.
     * 임시 코스 상태가 BASE_PLACE_SELECTING 이어야 한다(아니면 COURSE409).
     *
     * [latitude]·[longitude] 는 위치 권한이 있을 때만 **함께** 전달한다.
     * 하나만 보내면 LOCATION400 이 내려온다. [size] 는 1~10, 생략 시 서버 기본값 4.
     */
    @GET("api/course-drafts/{courseDraftId}/hot-places")
    suspend fun getHotPlaces(
        @Path("courseDraftId") courseDraftId: Long,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<HotPlaceResultDto>

    /**
     * 카테고리별 장소 카드 목록 조회 — GET /api/course-drafts/{courseDraftId}/recommended-places
     *
     * 코스 구성하기 화면의 카테고리 탭별 추천 카드. 기준 장소 확정 후(PLACE_SELECTING) 호출한다.
     * [placeCategoryCode] 는 장소 대분류 코드(CAFE/RESTAURANT/ACTIVITY/BAR)다.
     */
    @GET("api/course-drafts/{courseDraftId}/recommended-places")
    suspend fun getRecommendedPlaces(
        @Path("courseDraftId") courseDraftId: Long,
        @Query("placeCategoryCode") placeCategoryCode: String,
        @Query("size") size: Int? = null,
    ): BaseResponse<RecommendedPlaceResultDto>
}
