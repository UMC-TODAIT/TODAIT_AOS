package com.umc.todait.feature.saved.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.SavedCoursesResponseDto
import com.umc.todait.feature.saved.data.mock.MockCourseDetail
import com.umc.todait.feature.saved.data.mock.SavedCoursesMock
import com.umc.todait.feature.saved.data.service.SavedService
import javax.inject.Inject
private const val USE_MOCK = true
class SavedRepository @Inject constructor(
    private val savedService: SavedService,
) {
    suspend fun getSavedCourses(): ApiResult<SavedCoursesResponseDto> {
        if (USE_MOCK) {
            return ApiResult.Success(SavedCoursesMock.savedCourses)
        }

        return safeApiCall {
            savedService.getSavedCourses()
        }
    }

    suspend fun getCourseDetail(
        courseId: Long
    ): ApiResult<CourseDetailResponseDto> {

        if (USE_MOCK) {
            return ApiResult.Success(MockCourseDetail.detail)
        }

        return safeApiCall {
            savedService.getCourseDetail(courseId)
        }
    }
}