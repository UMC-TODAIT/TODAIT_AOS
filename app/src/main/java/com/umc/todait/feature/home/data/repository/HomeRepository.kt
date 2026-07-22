package com.umc.todait.feature.home.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
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

    // 아래 둘은 데이터 계층만 준비 — 화면 연결은 "추천 코스 목록 조회" API 확정 후 진행(TODO).
    suspend fun getRecommendedCourseDetail(courseId: Long): ApiResult<RecommendedCourseDetailDto> =
        safeApiCall { homeService.getRecommendedCourseDetail(courseId) }

    suspend fun saveRecommendedCourse(courseId: Long): ApiResult<RecommendedCourseSaveResultDto> =
        safeApiCall { homeService.saveRecommendedCourse(courseId) }
}
