package com.umc.todait.feature.home.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSaveResultDto
import com.umc.todait.feature.home.data.service.HomeService
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
) {

    suspend fun getRecommendedPlaces(
        page: Int? = null,
        size: Int? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): ApiResult<HomeRecommendedPlaceResultDto> = safeApiCall {
        homeService.getRecommendedPlaces(page = page, size = size, latitude = latitude, longitude = longitude)
    }

    suspend fun getRecommendedCourses(page: Int? = null, size: Int? = null): ApiResult<RecommendedCourseListResultDto> =
        safeApiCall { homeService.getRecommendedCourses(page = page, size = size) }

    // 상세/저장은 데이터 계층만 준비 — 화면 연결은 CourseDetailScreen(feature/saved) 조율 후 진행(TODO).
    suspend fun getRecommendedCourseDetail(courseId: Long): ApiResult<RecommendedCourseDetailDto> =
        safeApiCall { homeService.getRecommendedCourseDetail(courseId) }

    suspend fun saveRecommendedCourse(courseId: Long): ApiResult<RecommendedCourseSaveResultDto> =
        safeApiCall { homeService.saveRecommendedCourse(courseId) }
}
