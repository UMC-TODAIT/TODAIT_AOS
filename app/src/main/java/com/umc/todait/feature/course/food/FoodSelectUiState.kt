package com.umc.todait.feature.course.food

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.umc.todait.R
import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.ui.theme.FoodChineseGradientEnd
import com.umc.todait.ui.theme.FoodChineseGradientStart
import com.umc.todait.ui.theme.FoodChineseSelectedGradientEnd
import com.umc.todait.ui.theme.FoodChineseSelectedGradientStart
import com.umc.todait.ui.theme.FoodDessertGradientEnd
import com.umc.todait.ui.theme.FoodDessertGradientStart
import com.umc.todait.ui.theme.FoodDessertSelectedGradientEnd
import com.umc.todait.ui.theme.FoodDessertSelectedGradientStart
import com.umc.todait.ui.theme.FoodJapaneseGradientEnd
import com.umc.todait.ui.theme.FoodJapaneseGradientStart
import com.umc.todait.ui.theme.FoodJapaneseSelectedGradientEnd
import com.umc.todait.ui.theme.FoodJapaneseSelectedGradientStart
import com.umc.todait.ui.theme.FoodKoreanGradientEnd
import com.umc.todait.ui.theme.FoodKoreanGradientStart
import com.umc.todait.ui.theme.FoodKoreanSelectedGradientEnd
import com.umc.todait.ui.theme.FoodKoreanSelectedGradientStart
import com.umc.todait.ui.theme.FoodSnackGradientEnd
import com.umc.todait.ui.theme.FoodSnackGradientStart
import com.umc.todait.ui.theme.FoodSnackSelectedGradientEnd
import com.umc.todait.ui.theme.FoodSnackSelectedGradientStart
import com.umc.todait.ui.theme.FoodWesternGradientEnd
import com.umc.todait.ui.theme.FoodWesternGradientStart
import com.umc.todait.ui.theme.FoodWesternSelectedGradientEnd
import com.umc.todait.ui.theme.FoodWesternSelectedGradientStart

/**
 * 음식 선택 화면의 UI 상태.
 *
 * 선택지는 `GET /api/food-categories` 로 받아오므로 진입 시 [listState] 가 Loading 으로 시작한다.
 * 저장 요청에는 [FoodOptionUiModel.foodCategoryId] 를 그대로 실어 보낸다.
 */
data class FoodSelectUiState(
    val listState: FoodListState = FoodListState.Loading,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    /**
     * 진입 시점에 서버에 저장돼 있던 음식 카테고리 id. 확인(✅) 시 현재 선택값과 비교해
     * 실제로 바뀌었는지 판단한다. 저장된 값이 없으면 빈 목록이다.
     */
    val savedFoodCategoryIds: Set<Long> = emptySet(),
    /** 임시 코스에 저장된 장소(기준 장소 포함)가 있는지. 초기화 알림 노출 조건. */
    val hasSavedPlaces: Boolean = false,
    /** 음식 취향 변경 시 장소 초기화 확인 알림 노출 여부. */
    val showResetAlert: Boolean = false,
) {
    private val foods: List<FoodOptionUiModel>
        get() = (listState as? FoodListState.Success)?.foods.orEmpty()

    val selectedCount: Int get() = foods.count { it.isSelected }

    /** 선택한 음식 카테고리 id 목록(저장 API 요청값). */
    val selectedFoodCategoryIds: List<Long> get() = foods.filter { it.isSelected }.map { it.foodCategoryId }

    /**
     * 저장된 값에서 실제로 달라졌는지. 순서는 의미가 없어 집합으로 비교한다.
     * 값이 그대로면 저장 API 를 부르지 않고 다음 단계로 넘어간다.
     */
    val isSelectionChanged: Boolean get() = selectedFoodCategoryIds.toSet() != savedFoodCategoryIds

    /**
     * 확인(✅) 시 초기화 알림을 띄워야 하는지.
     * 음식 취향이 바뀌었고 + 저장된 장소가 있을 때만이다(서버가 장소 데이터를 초기화한다).
     */
    val needsResetConfirm: Boolean get() = isSelectionChanged && hasSavedPlaces

    /** 명세: 최소 1개 이상 선택해야 저장할 수 있다. */
    val isConfirmEnabled: Boolean get() = selectedCount >= MIN_SELECTION && !isSubmitting

    companion object {
        const val MIN_SELECTION = 1
    }
}

/** 음식 카테고리 목록 영역의 상태. */
sealed interface FoodListState {
    data object Loading : FoodListState
    data class Success(val foods: List<FoodOptionUiModel>) : FoodListState
    data class Error(val message: String) : FoodListState
}

/**
 * 음식 카드 한 장.
 *
 * [foodCategoryId]·[title] 은 서버(`GET /api/food-categories`)에서 오고,
 * 카드 색상·문양·해시태그는 서버에 없는 값이라 [code] 로 앱 리소스를 찾아 붙인다.
 */
data class FoodOptionUiModel(
    val foodCategoryId: Long,
    val code: String,
    val title: String,
    val hashtags: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val selectedGradientStart: Color,
    val selectedGradientEnd: Color,
    @DrawableRes val decorationRes: Int,
    val isSelected: Boolean = false,
)

/**
 * 음식 카테고리 DTO → 카드 모델. 표시 순서는 서버 sortOrder 를 따르므로 여기서 정렬하지 않는다.
 * [isSelected] 는 화면에서 토글하는 값이라 항상 false 로 시작한다.
 */
fun FoodCategoryDto.toUiModel(): FoodOptionUiModel {
    val visual = FOOD_VISUALS[code] ?: FALLBACK_VISUAL
    return FoodOptionUiModel(
        foodCategoryId = foodCategoryId,
        code = code,
        title = name,
        hashtags = visual.hashtags,
        gradientStart = visual.gradientStart,
        gradientEnd = visual.gradientEnd,
        selectedGradientStart = visual.selectedGradientStart,
        selectedGradientEnd = visual.selectedGradientEnd,
        decorationRes = visual.decorationRes,
    )
}

/** 카드 1장의 시각 요소(Figma "취향설정"). 서버 응답에 없는 값이라 code 로 매칭한다. */
private data class FoodVisual(
    val hashtags: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val selectedGradientStart: Color,
    val selectedGradientEnd: Color,
    @DrawableRes val decorationRes: Int,
)

private val FOOD_VISUALS: Map<String, FoodVisual> = mapOf(
    "KOREAN" to FoodVisual(
        hashtags = "#깔끔한 #든든한",
        gradientStart = FoodKoreanGradientStart, gradientEnd = FoodKoreanGradientEnd,
        selectedGradientStart = FoodKoreanSelectedGradientStart, selectedGradientEnd = FoodKoreanSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_korean,
    ),
    "JAPANESE" to FoodVisual(
        hashtags = "#담백한 #정갈한",
        gradientStart = FoodJapaneseGradientStart, gradientEnd = FoodJapaneseGradientEnd,
        selectedGradientStart = FoodJapaneseSelectedGradientStart,
        selectedGradientEnd = FoodJapaneseSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_japanese,
    ),
    "WESTERN" to FoodVisual(
        hashtags = "#격식 #무드",
        gradientStart = FoodWesternGradientStart, gradientEnd = FoodWesternGradientEnd,
        selectedGradientStart = FoodWesternSelectedGradientStart, selectedGradientEnd = FoodWesternSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_western,
    ),
    "CHINESE" to FoodVisual(
        hashtags = "#마라 #자극적",
        gradientStart = FoodChineseGradientStart, gradientEnd = FoodChineseGradientEnd,
        selectedGradientStart = FoodChineseSelectedGradientStart, selectedGradientEnd = FoodChineseSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_chinese,
    ),
    "SNACK" to FoodVisual(
        hashtags = "#따뜻한 #편한",
        gradientStart = FoodSnackGradientStart, gradientEnd = FoodSnackGradientEnd,
        selectedGradientStart = FoodSnackSelectedGradientStart, selectedGradientEnd = FoodSnackSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_snack,
    ),
    "DESSERT" to FoodVisual(
        hashtags = "#달콤한 #감성",
        gradientStart = FoodDessertGradientStart, gradientEnd = FoodDessertGradientEnd,
        selectedGradientStart = FoodDessertSelectedGradientStart, selectedGradientEnd = FoodDessertSelectedGradientEnd,
        decorationRes = R.drawable.ic_food_dessert,
    ),
)

// 서버에 음식 카테고리가 추가돼 앱에 없는 code 가 내려와도 카드가 비지 않도록 하는 기본값.
private val FALLBACK_VISUAL = FoodVisual(
    hashtags = "",
    gradientStart = FoodKoreanGradientStart,
    gradientEnd = FoodKoreanGradientEnd,
    selectedGradientStart = FoodKoreanSelectedGradientStart,
    selectedGradientEnd = FoodKoreanSelectedGradientEnd,
    decorationRes = R.drawable.ic_food_korean,
)

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface FoodSelectEffect {
    /** 저장 성공 → 기준 장소 설정 화면으로 이동. 코스 생성 플로우 전체가 공유하는 임시 코스 핸들을 이어서 들고 간다. */
    data class NavigateToBasePlace(val courseDraftId: Long) : FoodSelectEffect

    /** 이전 버튼(`<`) → 단계 이동 API 를 부른 뒤 분위기 선택 화면으로 돌아간다. */
    data object NavigateBack : FoodSelectEffect
}
