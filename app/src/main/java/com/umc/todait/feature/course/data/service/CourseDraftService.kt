package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.BasePlaceSetRequestDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftSavingEnterResponseDto
import com.umc.todait.feature.course.data.dto.OrderingEntryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateRequestDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateResponseDto
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 임시 코스(course-draft) Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * 임시 코스는 상태 머신이라 화면을 넘어갈 때마다 상태를 전이시키는 API 를 호출한다.
 * MOOD_SELECTING → FOOD_SELECTING → BASE_PLACE_SELECTING → PLACE_SELECTING → ORDERING → SAVING → COMPLETED
 *
 * ⚠️ 선택 장소 추가/삭제(POST·DELETE .../places)와 최종 저장(POST .../courses)은
 * 명세상 아직 "진행 중"이라 여기에 없다. 개발 완료되면 이 서비스에 추가한다.
 */
interface CourseDraftService {

    /**
     * 임시 코스 생성 — POST /api/course-drafts
     * 코스 생성 진입 시 호출해 courseDraftId 를 발급받는다. 요청 바디 없음.
     */
    @POST("api/course-drafts")
    suspend fun createCourseDraft(): BaseResponse<CourseDraftCreateResponseDto>

    /**
     * 임시 코스 기준 장소 설정 — PATCH /api/course-drafts/{courseDraftId}/base-place
     *
     * 기준 장소 확인 모달 [확인] 시 호출한다. 상태가 BASE_PLACE_SELECTING 이어야 하며,
     * 성공하면 PLACE_SELECTING 으로 전이하고 course_draft_place 에 BASE(visitOrder 1) 행이 함께 생긴다.
     */
    @PATCH("api/course-drafts/{courseDraftId}/base-place")
    suspend fun setBasePlace(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: BasePlaceSetRequestDto,
    ): BaseResponse<BasePlaceSetResponseDto>

    /**
     * 임시 코스 순서 설정 화면 진입 — PATCH /api/course-drafts/{courseDraftId}/ordering
     *
     * 장소 선택 화면에서 [선택 완료]를 눌렀을 때 호출한다. 요청 바디 없음.
     * 순서를 바꾸는 API 가 아니라 단계 전환 + 장소 목록 조회다(순서 변경은 [updatePlaceOrder]).
     */
    @PATCH("api/course-drafts/{courseDraftId}/ordering")
    suspend fun enterOrdering(
        @Path("courseDraftId") courseDraftId: Long,
    ): BaseResponse<OrderingEntryResponseDto>

    /**
     * 선택 장소 순서 변경 — PATCH /api/course-drafts/{courseDraftId}/places/order
     *
     * 드래그가 끝났을 때 선택 장소 전체의 최종 순서를 한 번에 보낸다(기준 장소는 요청에서 제외).
     */
    @PATCH("api/course-drafts/{courseDraftId}/places/order")
    suspend fun updatePlaceOrder(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: PlaceOrderUpdateRequestDto,
    ): BaseResponse<PlaceOrderUpdateResponseDto>

    /**
     * 임시 코스 저장 화면 진입 — PATCH /api/course-drafts/{courseDraftId}/saving
     *
     * 순서 설정 화면에서 [완료]를 눌러 코스 저장 화면으로 넘어갈 때 호출한다. 요청 바디 없음.
     * 이 호출로 최종 course 가 만들어지지는 않는다(코스 저장 요청 API 담당).
     */
    @PATCH("api/course-drafts/{courseDraftId}/saving")
    suspend fun enterSaving(
        @Path("courseDraftId") courseDraftId: Long,
    ): BaseResponse<CourseDraftSavingEnterResponseDto>
}
