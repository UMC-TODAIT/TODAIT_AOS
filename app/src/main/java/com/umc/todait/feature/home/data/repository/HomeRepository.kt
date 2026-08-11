package com.umc.todait.feature.home.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.home.data.dto.HomeMemberDto
import com.umc.todait.feature.home.data.dto.HomeRecommendedPlaceResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseDetailDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseListResultDto
import com.umc.todait.feature.home.data.dto.RecommendedCourseSaveResultDto
import com.umc.todait.feature.home.data.service.HomeService
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val homeService: HomeService,
) {

    /** 홈 상단 인사말용 회원 조회 (GET /api/members/me) → 닉네임만 사용한다. */
    suspend fun getMyProfile(): ApiResult<HomeMemberDto> =
        safeApiCall { homeService.getMyProfile() }

    suspend fun getRecommendedPlaces(
        cursor: String? = null,
        size: Int? = null,
        latitude: Double? = null,
        longitude: Double? = null,
    ): ApiResult<HomeRecommendedPlaceResultDto> = safeApiCall {
        homeService.getRecommendedPlaces(cursor = cursor, size = size, latitude = latitude, longitude = longitude)
    }

    suspend fun getRecommendedCourses(cursor: String? = null, size: Int? = null): ApiResult<RecommendedCourseListResultDto> =
        safeApiCall { homeService.getRecommendedCourses(cursor = cursor, size = size) }

    suspend fun getRecommendedCourseDetail(courseId: Long): ApiResult<RecommendedCourseDetailDto> =
        safeApiCall { homeService.getRecommendedCourseDetail(courseId) }

    suspend fun saveRecommendedCourse(courseId: Long): ApiResult<RecommendedCourseSaveResultDto> =
        safeApiCall { homeService.saveRecommendedCourse(courseId) }
}
