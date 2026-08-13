package com.umc.todait.feature.course.data.service

import com.umc.todait.core.network.BaseResponse
import com.umc.todait.feature.course.data.dto.AbandonCourseDraftResponseDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetRequestDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceAddRequestDto
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceAddResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftSavingEnterResponseDto
import com.umc.todait.feature.course.data.dto.CourseSaveRequestDto
import com.umc.todait.feature.course.data.dto.CourseSaveResponseDto
import com.umc.todait.feature.course.data.dto.CurrentCourseDraftResponseDto
import com.umc.todait.feature.course.data.dto.FoodCategorySaveRequestDto
import com.umc.todait.feature.course.data.dto.MoodTagSaveRequestDto
import com.umc.todait.feature.course.data.dto.OrderingEntryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateRequestDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateResponseDto
import com.umc.todait.feature.course.data.dto.StatusUpdateRequestDto
import com.umc.todait.feature.course.data.dto.StatusUpdateResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * 임시 코스(course-draft) Retrofit 서비스.
 *
 * 로그인 필요(Authorization: Bearer). 토큰 헤더는 AuthInterceptor 에서 일괄 부착된다.
 *
 * 임시 코스는 상태 머신이라 화면을 넘어갈 때마다 상태를 전이시키는 API 를 호출한다.
 * MOOD_SELECTING → FOOD_SELECTING → BASE_PLACE_SELECTING → PLACE_SELECTING → ORDERING → SAVING → COMPLETED
 *
 * ⚠️ 선택 장소 삭제(DELETE .../places/{courseDraftPlaceId})는 명세상 아직 개발 전이라 여기에 없다.
 * 그래서 담은 장소를 빼는 동작은 화면에서만 반영되고 서버 임시 코스에는 남는다.
 */
interface CourseDraftService {

    /**
     * 임시 코스 생성 — POST /api/course-drafts
     * 코스 생성 진입 시 호출해 courseDraftId 를 발급받는다. 요청 바디 없음.
     */
    @POST("api/course-drafts")
    suspend fun createCourseDraft(): BaseResponse<CourseDraftCreateResponseDto>

    /**
     * 분위기 태그 선택 저장 — PUT /api/course-drafts/{courseDraftId}/mood-tags
     * 선택값 전체를 교체 저장한다(최소 2개~최대 6개).
     */
    @PUT("api/course-drafts/{courseDraftId}/mood-tags")
    suspend fun saveMoodTags(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: MoodTagSaveRequestDto,
    ): BaseResponse<CourseDraftMoodTagSaveResponseDto>

    /**
     * 음식 카테고리 선택 저장 — PUT /api/course-drafts/{courseDraftId}/food-categories
     * 선택값 전체를 교체 저장한다(최소 1개).
     */
    @PUT("api/course-drafts/{courseDraftId}/food-categories")
    suspend fun saveFoodCategories(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: FoodCategorySaveRequestDto,
    ): BaseResponse<CourseDraftFoodCategorySaveResponseDto>

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
     * 선택 장소 추가 — POST /api/course-drafts/{courseDraftId}/places
     *
     * 코스 구성하기 화면에서 추천 카드의 '+' 를 눌렀을 때 호출한다. 상태가 PLACE_SELECTING 이어야 하며,
     * 장소를 담아도 상태는 그대로 유지된다. visitOrder 는 서버가 (현재 최댓값 + 1)로 부여한다.
     */
    @POST("api/course-drafts/{courseDraftId}/places")
    suspend fun addPlace(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: CourseDraftPlaceAddRequestDto,
    ): BaseResponse<CourseDraftPlaceAddResponseDto>

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

    /**
     * 코스 저장 요청 — POST /api/course-drafts/{courseDraftId}/courses
     *
     * 저장 화면에서 [확인]을 눌렀을 때 호출한다. 상태가 SAVING 이어야 하며 성공하면 COMPLETED 로 전이한다.
     * 같은 임시 코스는 한 번만 저장할 수 있어(재시도 시 COURSE_DRAFT409) 버튼 중복 탭을 막아야 한다.
     */
    @POST("api/course-drafts/{courseDraftId}/courses")
    suspend fun saveCourse(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: CourseSaveRequestDto,
    ): BaseResponse<CourseSaveResponseDto>

    /**
     * 임시 코스 이전 단계 이동 — PATCH /api/course-drafts/{courseDraftId}/status
     *
     * 이전 버튼(`<`)이 호출한다. 이전 단계 이동은 화면 이동으로 취급하므로 기존 선택 데이터
     * (무드·음식·기준 장소·선택 장소·순서)를 삭제하지 않는다. COMPLETED/ABANDONED 는 되돌릴 수 없다.
     */
    @PATCH("api/course-drafts/{courseDraftId}/status")
    suspend fun updateStatus(
        @Path("courseDraftId") courseDraftId: Long,
        @Body request: StatusUpdateRequestDto,
    ): BaseResponse<StatusUpdateResponseDto>

    /**
     * 진행 중인 임시 코스 조회 — GET /api/course-drafts/current
     *
     * ⚠️ 진행 중인 임시 코스가 없으면 200 OK + `result: null` 이다. 오류가 아니므로
     * 리포지토리에서 safeNullableApiCall 로 받는다.
     */
    @GET("api/course-drafts/current")
    suspend fun getCurrentCourseDraft(): BaseResponse<CurrentCourseDraftResponseDto>

    /**
     * 진행 중인 임시 코스 포기 — DELETE /api/course-drafts/{courseDraftId}
     *
     * 코스 만들기 진입에서 [새로 만들기] 를 골랐을 때 기존 임시 코스를 ABANDONED 로 바꾼다.
     * 이미 COMPLETED/ABANDONED 인 임시 코스는 409 다.
     */
    @DELETE("api/course-drafts/{courseDraftId}")
    suspend fun abandonCourseDraft(
        @Path("courseDraftId") courseDraftId: Long,
    ): BaseResponse<AbandonCourseDraftResponseDto>
}
