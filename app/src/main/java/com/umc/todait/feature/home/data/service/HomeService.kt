package com.umc.todait.feature.home.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.home.data.dto.HomeMemberDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSaveResultDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 홈 화면 전용 Retrofit 서비스. 로그인 필요(Authorization: Bearer, AuthInterceptor가 일괄 부착).
 */
interface HomeService {

    /**
     * 홈 화면 상단 인사말용 회원 조회 — GET /api/members/me
     *
     * 명세상 홈은 통합 API 없이 닉네임·추천 코스·추천 장소를 각각 호출한다.
     * 닉네임 전용 `GET /api/members/me/nickname` 은 폐지되어 이 엔드포인트로 통합됐다.
     * 응답에 "님"은 포함되지 않으므로 화면에서 붙인다.
     */
    @GET("api/members/me")
    suspend fun getMyProfile(): BaseResponse<HomeMemberDto>

    /**
     * 홈 화면 추천 장소 목록 조회 — GET /api/recommended-places?cursor&size&latitude&longitude
     * (TODAIT_BE 스펙 확정본. latitude/longitude는 둘 다 전달하거나 둘 다 생략 — 하나만 보내면 PLACE4002)
     */
    @GET("api/recommended-places")
    suspend fun getRecommendedPlaces(
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): BaseResponse<HomeRecommendedPlaceResultDto>

    /**
     * 홈 화면 추천 코스 목록 조회 — GET /api/recommended-courses?cursor&size
     * (홈 화면은 size=3 만 보내고 페이지네이션은 하지 않는다. 다음 페이지는 응답의 nextCursor 를 cursor 로 넘긴다)
     */
    @GET("api/recommended-courses")
    suspend fun getRecommendedCourses(
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<RecommendedCourseListResultDto>

    /**
     * 추천 코스 상세 조회 — GET /api/recommended-courses/{courseId}
     * 홈 "오늘의 추천 코스" 카드 탭 → 추천 코스 상세 화면(#55, feature/home 소유·저장코스 상세와 별개 화면)에서 사용.
     */
    @GET("api/recommended-courses/{courseId}")
    suspend fun getRecommendedCourseDetail(
        @Path("courseId") courseId: Long,
    ): BaseResponse<RecommendedCourseDetailDto>

    /**
     * 추천 코스 저장(로그인 사용자 코스로 복사) — POST /api/recommended-courses/{courseId}/save
     */
    @POST("api/recommended-courses/{courseId}/save")
    suspend fun saveRecommendedCourse(
        @Path("courseId") courseId: Long,
    ): BaseResponse<RecommendedCourseSaveResultDto>
}
