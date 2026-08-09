package com.umc.todait.feature.course.food

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.umc.todait.R
import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.ui.theme.FoodChineseGradientEnd
import com.umc.todait.ui.theme.FoodChineseGradientStart
import com.umc.todait.ui.theme.FoodDessertGradientEnd
import com.umc.todait.ui.theme.FoodDessertGradientStart
import com.umc.todait.ui.theme.FoodJapaneseGradientEnd
import com.umc.todait.ui.theme.FoodJapaneseGradientStart
import com.umc.todait.ui.theme.FoodKoreanGradientEnd
import com.umc.todait.ui.theme.FoodKoreanGradientStart
import com.umc.todait.ui.theme.FoodSnackGradientEnd
import com.umc.todait.ui.theme.FoodSnackGradientStart
import com.umc.todait.ui.theme.FoodWesternGradientEnd
import com.umc.todait.ui.theme.FoodWesternGradientStart

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
) {
    private val foods: List<FoodOptionUiModel>
        get() = (listState as? FoodListState.Success)?.foods.orEmpty()

    val selectedCount: Int get() = foods.count { it.isSelected }

    /** 선택한 음식 카테고리 id 목록(저장 API 요청값). */
    val selectedFoodCategoryIds: List<Long> get() = foods.filter { it.isSelected }.map { it.foodCategoryId }

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
        decorationRes = visual.decorationRes,
    )
}

/** 카드 1장의 시각 요소(Figma "취향설정"). 서버 응답에 없는 값이라 code 로 매칭한다. */
private data class FoodVisual(
    val hashtags: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    @DrawableRes val decorationRes: Int,
)

private val FOOD_VISUALS: Map<String, FoodVisual> = mapOf(
    "KOREAN" to FoodVisual(
        "#깔끔한 #든든한", FoodKoreanGradientStart, FoodKoreanGradientEnd, R.drawable.ic_food_korean,
    ),
    "JAPANESE" to FoodVisual(
        "#담백한 #정갈한", FoodJapaneseGradientStart, FoodJapaneseGradientEnd, R.drawable.ic_food_japanese,
    ),
    "WESTERN" to FoodVisual(
        "#격식 #무드", FoodWesternGradientStart, FoodWesternGradientEnd, R.drawable.ic_food_western,
    ),
    "CHINESE" to FoodVisual(
        "#마라 #자극적", FoodChineseGradientStart, FoodChineseGradientEnd, R.drawable.ic_food_chinese,
    ),
    "SNACK" to FoodVisual(
        "#따뜻한 #편한", FoodSnackGradientStart, FoodSnackGradientEnd, R.drawable.ic_food_snack,
    ),
    "DESSERT" to FoodVisual(
        "#달콤한 #감성", FoodDessertGradientStart, FoodDessertGradientEnd, R.drawable.ic_food_dessert,
    ),
)

// 서버에 음식 카테고리가 추가돼 앱에 없는 code 가 내려와도 카드가 비지 않도록 하는 기본값.
private val FALLBACK_VISUAL = FoodVisual(
    hashtags = "",
    gradientStart = FoodKoreanGradientStart,
    gradientEnd = FoodKoreanGradientEnd,
    decorationRes = R.drawable.ic_food_korean,
)

/** 화면 밖으로 나가는 일회성 효과(네비게이션 등). */
sealed interface FoodSelectEffect {
    /** 저장 성공 → 기준 장소 설정 화면으로 이동. 코스 생성 플로우 전체가 공유하는 임시 코스 핸들을 이어서 들고 간다. */
    data class NavigateToBasePlace(val courseDraftId: Long) : FoodSelectEffect
}
