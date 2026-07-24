package com.umc.todait.feature.home.data.service

import com.umc.todait.core.network.BaseResponse
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
     * 홈 화면 추천 장소 목록 조회 — GET /api/recommended-places?page&size&latitude&longitude
     * (TODAIT_BE 스펙 확정본. latitude/longitude는 둘 다 전달하거나 둘 다 생략 — 하나만 보내면 PLACE4002)
     */
    @GET("api/recommended-places")
    suspend fun getRecommendedPlaces(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
    ): BaseResponse<HomeRecommendedPlaceResultDto>

    /**
     * 홈 화면 추천 코스 목록 조회 — GET /api/recommended-courses?page&size
     * (TODAIT_BE 스펙 확정본. 홈 화면은 page=0, size=3 고정 사용 — 서버가 날짜 기준으로 순환 반환)
     */
    @GET("api/recommended-courses")
    suspend fun getRecommendedCourses(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): BaseResponse<RecommendedCourseListResultDto>

    /**
     * 추천 코스 상세 조회 — GET /api/recommended-courses/{courseId}
     * (TODAIT_BE 스펙 확정본. 화면 연결은 아직 안 함 — feature/saved 소유 CourseDetailScreen 조율 후 진행.)
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
