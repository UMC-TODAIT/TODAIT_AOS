package com.umc.todait.feature.saved.data.repository

import android.util.Log
import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.SavedCoursesResponseDto
import com.umc.todait.feature.saved.data.dto.DeleteSavedCourseResponseDto
import com.umc.todait.feature.saved.data.dto.UpdateCourseMemoResponseDto
import com.umc.todait.feature.saved.data.dto.UpdateCoursePlaceMemoResponseDto
import com.umc.todait.feature.saved.data.dto.UpdateMemoRequestDto
import com.umc.todait.feature.saved.data.mock.MockCourseDetail
import com.umc.todait.feature.saved.data.mock.SavedCoursesMock
import com.umc.todait.feature.saved.data.service.SavedService
import javax.inject.Inject
private const val USE_MOCK = false
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

    suspend fun deleteSavedCourse(
        courseId: Long
    ): ApiResult<DeleteSavedCourseResponseDto> {
        return safeApiCall {
            savedService.deleteSavedCourse(courseId)
        }
    }

    suspend fun updateCourseMemo(
        courseId: Long,
        memo: String?
    ): ApiResult<UpdateCourseMemoResponseDto> =
        safeApiCall {
            savedService.updateCourseMemo(
                courseId = courseId,
                request = UpdateMemoRequestDto(memo = memo)
            )
        }

    suspend fun updateCoursePlaceMemo(
        courseId: Long,
        coursePlaceId: Long,
        memo: String?
    ): ApiResult<UpdateCoursePlaceMemoResponseDto> =
        safeApiCall {
            savedService.updateCoursePlaceMemo(
                courseId = courseId,
                coursePlaceId = coursePlaceId,
                request = UpdateMemoRequestDto(memo = memo)
            )
        }
}