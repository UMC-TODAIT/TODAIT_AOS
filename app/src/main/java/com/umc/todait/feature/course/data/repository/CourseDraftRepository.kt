package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.service.CourseDraftService
import javax.inject.Inject

/**
 * 임시 코스(course-draft) 데이터 접근 계층.
 * Service 호출을 safeApiCall 로 감싸 ViewModel 에는 ApiResult 만 노출한다.
 *
 * 생성자에 @Inject 를 달아 Hilt 가 CourseDraftService 를 주입한다. (CourseModule 참고)
 */
class CourseDraftRepository @Inject constructor(
    private val courseDraftService: CourseDraftService,
) {

    /** 임시 코스 생성 (POST /api/course-drafts) → courseDraftId 발급 */
    suspend fun createCourseDraft(): ApiResult<CourseDraftCreateResponseDto> = safeApiCall {
        courseDraftService.createCourseDraft()
    }
}
