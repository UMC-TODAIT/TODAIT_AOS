package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.core.network.safeNullableApiCall
import com.umc.todait.feature.course.data.dto.AbandonCourseDraftResponseDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetRequestDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceAddRequestDto
import com.umc.todait.feature.course.data.dto.CourseDraftPlaceAddResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftSavingEnterResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.feature.course.data.dto.CourseSaveRequestDto
import com.umc.todait.feature.course.data.dto.CourseSaveResponseDto
import com.umc.todait.feature.course.data.dto.CurrentCourseDraftResponseDto
import com.umc.todait.feature.course.data.dto.ExternalPlaceDto
import com.umc.todait.feature.course.data.dto.FoodCategorySaveRequestDto
import com.umc.todait.feature.course.data.dto.MoodTagSaveRequestDto
import com.umc.todait.feature.course.data.dto.OrderingEntryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceOrderItemDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateRequestDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateResponseDto
import com.umc.todait.feature.course.data.dto.StatusUpdateRequestDto
import com.umc.todait.feature.course.data.dto.StatusUpdateResponseDto
import com.umc.todait.feature.course.data.mock.MockCourse
import com.umc.todait.feature.course.data.mock.USE_COURSE_MOCK
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
    suspend fun createCourseDraft(): ApiResult<CourseDraftCreateResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.courseDraft)
        return safeApiCall { courseDraftService.createCourseDraft() }
    }

    /**
     * 분위기 태그 선택 저장 (PUT /api/course-drafts/{courseDraftId}/mood-tags).
     * [moodTagIds] 는 `GET /api/mood-tags` 로 받아온 id 그대로다(2~6개, 중복 불가).
     * 부분 추가가 아니라 현재 선택값 전체를 교체 저장한다.
     */
    suspend fun saveMoodTags(
        courseDraftId: Long,
        moodTagIds: List<Long>,
    ): ApiResult<CourseDraftMoodTagSaveResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.moodTagsSaveResult(moodTagIds))
        return safeApiCall { courseDraftService.saveMoodTags(courseDraftId, MoodTagSaveRequestDto(moodTagIds)) }
    }

    /**
     * 음식 카테고리 선택 저장 (PUT /api/course-drafts/{courseDraftId}/food-categories).
     * [foodCategoryIds] 는 `GET /api/food-categories` 로 받아온 id 그대로다(1개 이상).
     * 분위기 태그와 마찬가지로 선택값 전체를 교체 저장한다.
     */
    suspend fun saveFoodCategories(
        courseDraftId: Long,
        foodCategoryIds: List<Long>,
    ): ApiResult<CourseDraftFoodCategorySaveResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.foodCategoriesSaveResult(foodCategoryIds))
        return safeApiCall {
            courseDraftService.saveFoodCategories(courseDraftId, FoodCategorySaveRequestDto(foodCategoryIds))
        }
    }

    /**
     * 기준 장소 설정 (PATCH /api/course-drafts/{courseDraftId}/base-place).
     *
     * 내부 DB 에 이미 있는 장소(주변 핫플·운영자 등록·등록된 카카오 장소)라 placeId 를 아는 경우에 쓴다.
     * 미등록 카카오 검색 장소는 [setBasePlaceFromExternal] 을 쓴다.
     */
    suspend fun setBasePlace(
        courseDraftId: Long,
        placeId: Long,
    ): ApiResult<BasePlaceSetResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.basePlaceSetResult(placeId))
        return safeApiCall {
            courseDraftService.setBasePlace(courseDraftId, BasePlaceSetRequestDto(placeId = placeId))
        }
    }

    /**
     * 기준 장소 설정 (PATCH .../base-place) — 내부 DB 미등록 카카오 검색 장소 버전.
     *
     * 서버가 (dataSourceCode + sourcePlaceId)로 기존 장소를 찾고 없으면 새로 만든 뒤 내부 placeId 를 돌려준다.
     * [externalPlace] 의 areaCode·categoryCode 는 앱이 변환하지 않고 검색 API 응답 값을 그대로 넘긴다.
     */
    suspend fun setBasePlaceFromExternal(
        courseDraftId: Long,
        externalPlace: ExternalPlaceDto,
    ): ApiResult<BasePlaceSetResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.basePlaceSetResult(externalPlace))
        return safeApiCall {
            courseDraftService.setBasePlace(
                courseDraftId,
                BasePlaceSetRequestDto(externalPlace = externalPlace),
            )
        }
    }

    /**
     * 선택 장소 추가 (POST /api/course-drafts/{courseDraftId}/places).
     *
     * [placeId] 는 카테고리별 추천 응답의 `places[].placeId`(내부 place.id)다.
     * 카카오 외부 장소는 이 API 로 담을 수 없다(추천 카드는 항상 내부 등록 장소다).
     *
     * 중복 선택(COURSE_PLACE409)·기준 장소 재선택·최대 개수 초과는 서버가 최종 검증한다.
     */
    suspend fun addPlace(
        courseDraftId: Long,
        placeId: Long,
    ): ApiResult<CourseDraftPlaceAddResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.placeAddResult(courseDraftId, placeId))
        return safeApiCall {
            courseDraftService.addPlace(courseDraftId, CourseDraftPlaceAddRequestDto(placeId))
        }
    }

    /**
     * 순서 설정 화면 진입 (PATCH /api/course-drafts/{courseDraftId}/ordering).
     *
     * 멱등이라 화면 재진입/재시도로 다시 불려도 안전하다. 응답 places 에는 순서 변경에 필요한
     * courseDraftPlaceId 가 들어 있어, 이 값을 받아 두어야 [updatePlaceOrder] 를 호출할 수 있다.
     */
    suspend fun enterOrdering(courseDraftId: Long): ApiResult<OrderingEntryResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.orderingEntry(courseDraftId))
        return safeApiCall { courseDraftService.enterOrdering(courseDraftId) }
    }

    /**
     * 선택 장소 순서 변경 (PATCH /api/course-drafts/{courseDraftId}/places/order).
     *
     * [orderedPlaceIds] 는 화면에 보이는 최종 순서대로의 courseDraftPlaceId 목록이고,
     * visitOrder 는 [FIRST_VISIT_ORDER] 부터 1씩 증가시켜 붙인다.
     *
     * ⚠️ 기준 장소를 **포함한 전체 목록**을 받아야 한다. 명세에는 기준 장소를 빼고 2번부터
     * 보내라고 돼 있지만 배포 서버가 그 형태를 COURSE_ORDER400 으로 거부한다 — 1번이 비면
     * 순서가 끊긴 것으로 본다.
     */
    suspend fun updatePlaceOrder(
        courseDraftId: Long,
        orderedPlaceIds: List<Long>,
    ): ApiResult<PlaceOrderUpdateResponseDto> {
        val placeOrders = orderedPlaceIds.mapIndexed { index, id ->
            PlaceOrderItemDto(courseDraftPlaceId = id, visitOrder = index + FIRST_VISIT_ORDER)
        }
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.placeOrderUpdateResult(courseDraftId, placeOrders))
        return safeApiCall {
            courseDraftService.updatePlaceOrder(courseDraftId, PlaceOrderUpdateRequestDto(placeOrders))
        }
    }

    /**
     * 저장 화면 진입 (PATCH /api/course-drafts/{courseDraftId}/saving).
     * ordering 과 마찬가지로 멱등이며, 응답 routePreview 를 코스 저장 화면 경로 미리보기에 그대로 쓴다.
     */
    suspend fun enterSaving(courseDraftId: Long): ApiResult<CourseDraftSavingEnterResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.savingEntry(courseDraftId))
        return safeApiCall { courseDraftService.enterSaving(courseDraftId) }
    }

    /**
     * 코스 저장 요청 (POST /api/course-drafts/{courseDraftId}/courses).
     *
     * 저장 화면 입력값만 보낸다 — 음식 카테고리·장소 목록·방문 순서는 이미 임시 코스에 저장돼 있다.
     * [memo] 는 선택값이라 공백만 입력한 경우 null 로 정규화해 보낸다.
     * [moodTagIds] 는 `GET /api/mood-tags` 의 id 로, 2개 이상 6개 이하여야 한다.
     */
    suspend fun saveCourse(
        courseDraftId: Long,
        title: String,
        memo: String?,
        moodTagIds: List<Long>,
    ): ApiResult<CourseSaveResponseDto> {
        val request = CourseSaveRequestDto(
            title = title.trim(),
            memo = memo?.trim()?.takeIf { it.isNotEmpty() },
            moodTagIds = moodTagIds,
        )
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.courseSaveResult(request))
        return safeApiCall { courseDraftService.saveCourse(courseDraftId, request) }
    }

    /**
     * 이전 단계 이동 (PATCH /api/course-drafts/{courseDraftId}/status).
     *
     * 이전 버튼(`<`)이 부른다. 화면 이동으로만 취급되어 서버도 무드·음식·기준 장소·선택 장소·
     * 순서를 그대로 둔다. 되돌릴 곳이 없는 단계(MOOD_SELECTING·터미널)는 호출하지 않는다.
     */
    suspend fun moveToStatus(
        courseDraftId: Long,
        targetStatus: CourseDraftStatus,
    ): ApiResult<StatusUpdateResponseDto> {
        if (USE_COURSE_MOCK) {
            return ApiResult.Success(MockCourse.statusUpdateResult(courseDraftId, targetStatus.name))
        }
        return safeApiCall {
            courseDraftService.updateStatus(courseDraftId, StatusUpdateRequestDto(targetStatus.name))
        }
    }

    /**
     * 진행 중인 임시 코스 조회 (GET /api/course-drafts/current).
     *
     * 취향 설정 화면이 기존 선택값을 되살리고, 무드/음식 변경 시 초기화 알림을 띄울지
     * (=저장된 장소가 있는지) 판단하는 데 쓴다.
     *
     * ⚠️ 진행 중인 임시 코스가 없으면 **성공 + null** 이다(오류가 아니다). 호출부는
     * `ApiResult.Success(null)` 을 "새로 시작"으로 다뤄야 한다.
     */
    suspend fun getCurrentCourseDraft(): ApiResult<CurrentCourseDraftResponseDto?> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.currentCourseDraft)
        return safeNullableApiCall { courseDraftService.getCurrentCourseDraft() }
    }

    /**
     * 진행 중인 임시 코스 포기 (DELETE /api/course-drafts/{courseDraftId}).
     *
     * 코스 만들기 진입에서 [새로 만들기] 를 골랐을 때 부른다. 성공한 뒤에만 새 임시 코스를
     * 만들어야 한다 — 실패하면 기존 임시 코스가 그대로 남아 다음 진입에서 또 물어보게 된다.
     */
    suspend fun abandonCourseDraft(courseDraftId: Long): ApiResult<AbandonCourseDraftResponseDto> {
        if (USE_COURSE_MOCK) return ApiResult.Success(MockCourse.abandonResult(courseDraftId))
        return safeApiCall { courseDraftService.abandonCourseDraft(courseDraftId) }
    }

    companion object {
        /** 코스의 첫 장소. 순서 변경 요청은 기준 장소를 포함한 전체 목록을 이 번호부터 매긴다. */
        const val FIRST_VISIT_ORDER = 1
    }
}
