package com.umc.todait.feature.saved.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.SavedCoursesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface SavedService {

    @GET("api/courses/me/overview")
    suspend fun getSavedCourses(): BaseResponse<SavedCoursesResponseDto>

    @GET("api/courses/{courseId}")
    suspend fun getCourseDetail(
        @Path("courseId") courseId: Long
    ): BaseResponse<CourseDetailResponseDto>
}