package com.umc.todait.feature.saved.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.saved.data.dto.CourseDetailResponseDto
import com.umc.todait.feature.saved.data.dto.SavedCoursesResponseDto
import com.umc.todait.feature.saved.data.dto.DeleteSavedCourseResponseDto
import com.umc.todait.feature.saved.data.dto.UpdateCourseMemoResponseDto
import com.umc.todait.feature.saved.data.dto.UpdateMemoRequestDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PATCH
import retrofit2.http.Body

interface SavedService {

    @GET("api/courses/me/overview")
    suspend fun getSavedCourses(): BaseResponse<SavedCoursesResponseDto>

    @GET("api/courses/{courseId}")
    suspend fun getCourseDetail(
        @Path("courseId") courseId: Long
    ): BaseResponse<CourseDetailResponseDto>

    @DELETE("api/courses/{courseId}")
    suspend fun deleteSavedCourse(
        @Path("courseId") courseId: Long
    ): BaseResponse<DeleteSavedCourseResponseDto>

    @PATCH("api/courses/{courseId}/memo")
    suspend fun updateCourseMemo(
        @Path("courseId") courseId: Long,
        @Body request: UpdateMemoRequestDto
    ): BaseResponse<UpdateCourseMemoResponseDto>
}