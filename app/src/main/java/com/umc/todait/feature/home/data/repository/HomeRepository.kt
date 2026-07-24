package com.umc.todait.feature.home.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSaveResultDto
import com.umc.todait.feature.home.data.mock.MockHome
import com.umc.todait.feature.home.data.service.HomeService
import javax.inject.Inject

// MVP 시연용: true 면 서버 대신 MockHome 데이터를 반환한다(피그마 확인/오프라인 시연). 실 API 연결 시 false 로.
private const val USE_MOCK = true

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
) {

    suspend fun getRecommendedPlaces(
        page: Int? = null,
        size: Int? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): ApiResult<HomeRecommendedPlaceResultDto> {
        if (USE_MOCK) return ApiResult.Success(MockHome.places)
        return safeApiCall {
            homeService.getRecommendedPlaces(page = page, size = size, latitude = latitude, longitude = longitude)
        }
    }

    suspend fun getRecommendedCourses(page: Int? = null, size: Int? = null): ApiResult<RecommendedCourseListResultDto> {
        if (USE_MOCK) return ApiResult.Success(MockHome.courses)
        return safeApiCall { homeService.getRecommendedCourses(page = page, size = size) }
    }

    // 상세/저장은 데이터 계층만 준비 — 화면 연결은 CourseDetailScreen(feature/saved) 조율 후 진행(TODO).
    suspend fun getRecommendedCourseDetail(courseId: Long): ApiResult<RecommendedCourseDetailDto> =
        safeApiCall { homeService.getRecommendedCourseDetail(courseId) }

    suspend fun saveRecommendedCourse(courseId: Long): ApiResult<RecommendedCourseSaveResultDto> =
        safeApiCall { homeService.saveRecommendedCourse(courseId) }
}
