package com.umc.todait.feature.course.data.repository

import com.umc.todait.core.network.ApiResult
import com.umc.todait.core.network.safeApiCall
import com.umc.todait.feature.course.data.dto.BasePlaceSetRequestDto
import com.umc.todait.feature.course.data.dto.BasePlaceSetResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftSavingEnterResponseDto
import com.umc.todait.feature.course.data.dto.ExternalPlaceDto
import com.umc.todait.feature.course.data.dto.FoodCategorySaveRequestDto
import com.umc.todait.feature.course.data.dto.MoodTagSaveRequestDto
import com.umc.todait.feature.course.data.dto.OrderingEntryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceOrderItemDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateRequestDto
import com.umc.todait.feature.course.data.dto.PlaceOrderUpdateResponseDto
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
     * [selectedPlaceIds] 는 **기준 장소를 제외한** 선택 장소들의 courseDraftPlaceId 를 화면에 보이는
     * 최종 순서대로 담은 목록이다. visitOrder 는 기준 장소가 1번이므로 2부터 매긴다.
     */
    suspend fun updatePlaceOrder(
        courseDraftId: Long,
        selectedPlaceIds: List<Long>,
    ): ApiResult<PlaceOrderUpdateResponseDto> {
        val placeOrders = selectedPlaceIds.mapIndexed { index, id ->
            PlaceOrderItemDto(courseDraftPlaceId = id, visitOrder = index + FIRST_SELECTED_VISIT_ORDER)
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

    private companion object {
        // 기준 장소가 visitOrder = 1 을 고정으로 차지하므로 선택 장소는 2번부터 시작한다.
        const val FIRST_SELECTED_VISIT_ORDER = 2
    }
}
