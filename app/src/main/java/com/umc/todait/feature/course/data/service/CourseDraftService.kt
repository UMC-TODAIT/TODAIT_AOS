package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import retrofit2.http.POST

/**
 * 임시 코스(course-draft) Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * ⚠️ 1차 범위는 생성만. 기준/선택 장소 저장·조회·순서변경·최종 저장 엔드포인트는
 * BE 미배포(보류)라 배포 확정 시 이 서비스에 추가한다.
 */
interface CourseDraftService {

    /**
     * 임시 코스 생성 — POST /api/course-drafts
     * 코스 생성 진입 시 호출해 courseDraftId 를 발급받는다. 요청 바디 없음.
     */
    @POST("api/course-drafts")
    suspend fun createCourseDraft(): BaseResponse<CourseDraftCreateResponseDto>
}
