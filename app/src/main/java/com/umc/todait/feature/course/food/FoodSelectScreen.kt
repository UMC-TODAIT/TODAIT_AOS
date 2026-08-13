package com.umc.todait.feature.course.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.component.TasteSelectionCard
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 음식 선택 화면(취향 설정 2/2). 분위기 선택 다음 단계이며, 완료 시 기준 장소 설정으로 이동한다.
 */
@Composable
fun FoodSelectScreen(
    onBack: () -> Unit,
    onNavigateToBasePlace: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodSelectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 뒤 단계에서 이전 버튼으로 돌아왔을 때 저장 기준값을 다시 받아온다(장소 보유 여부가 바뀌었을 수 있다).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onScreenResumed() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FoodSelectEffect.NavigateToBasePlace -> onNavigateToBasePlace(effect.courseDraftId)
                // 이전 버튼은 단계 이동 API 를 먼저 부르므로 화면 이동도 ViewModel 이 알려줄 때 한다.
                FoodSelectEffect.NavigateBack -> onBack()
            }
        }
    }

    FoodSelectContent(
        state = uiState,
        onBack = viewModel::onBackClick,
        onToggleFood = viewModel::onToggleFood,
        onConfirmClick = viewModel::onConfirmClick,
        onRetry = viewModel::loadFoodCategories,
        onResetAlertConfirm = viewModel::onResetAlertConfirm,
        onResetAlertDismiss = viewModel::onResetAlertDismiss,
        modifier = modifier,
    )
}

@Composable
private fun FoodSelectContent(
    state: FoodSelectUiState,
    onBack: () -> Unit,
    onToggleFood: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onRetry: () -> Unit,
    onResetAlertConfirm: () -> Unit,
    onResetAlertDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.food_select_title),
            onBack = onBack,
            onConfirm = onConfirmClick,
            confirmEnabled = state.isConfirmEnabled,
        )

        // Figma: 구분선 아래 31 → 제목(22 SemiBold, #222) → 8 → 안내(16 SemiBold, Gray-400) → 39 → 카드 그리드
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(31.dp))
            Text(
                text = stringResource(R.string.food_select_headline),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray800,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.food_select_subtitle, FoodSelectUiState.MIN_SELECTION),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray400,
            )
            Spacer(Modifier.height(39.dp))
            if (state.submitError != null) {
                Text(
                    text = state.submitError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val listState = state.listState) {
                is FoodListState.Loading -> LoadingIndicator()

                is FoodListState.Error -> ErrorContent(
                    error = UiError(message = listState.message),
                    onRetry = onRetry,
                )

                is FoodListState.Success -> FoodGrid(
                    foods = listState.foods,
                    onToggleFood = onToggleFood,
                )
            }
        }
    }

    // 음식 취향을 실제로 바꿨고 임시 코스에 저장된 장소가 있을 때만 뜬다.
    // [확인] 이 저장 API 를 부르고, 그 결과로 서버가 장소 데이터를 초기화한다.
    if (state.showResetAlert) {
        CommonDialog(
            title = stringResource(R.string.food_select_reset_alert),
            confirmText = stringResource(R.string.preference_reset_alert_confirm),
            onConfirm = onResetAlertConfirm,
            onDismiss = onResetAlertDismiss,
        )
    }
}

@Composable
private fun FoodGrid(
    foods: List<FoodOptionUiModel>,
    onToggleFood: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(foods, key = { it.code }) { food ->
            TasteSelectionCard(
                title = food.title,
                hashtags = food.hashtags,
                gradientStart = food.gradientStart,
                gradientEnd = food.gradientEnd,
                selectedGradientStart = food.selectedGradientStart,
                selectedGradientEnd = food.selectedGradientEnd,
                decorationRes = food.decorationRes,
                isSelected = food.isSelected,
                onClick = { onToggleFood(food.code) },
            )
        }
    }
}

// ---------- Preview ----------

/**
 * Interactive Mode(프리뷰 상단 재생 아이콘)로 실행하면 실제로 카드를 눌러 선택을 토글해볼 수 있다.
 * ViewModel 없이 로컬 상태로만 흉내 낸 것이라 확인 버튼 클릭 시 API 호출은 일어나지 않는다.
 */
@Preview(name = "음식 선택", showBackground = true, heightDp = 1200)
@Composable
private fun FoodSelectContentPreview() {
    var state by remember { mutableStateOf(FoodSelectUiState(listState = FoodListState.Success(previewFoods()))) }
    TodaitTheme {
        FoodSelectContent(
            state = state,
            onBack = {},
            onToggleFood = { code ->
                val current = state.listState as? FoodListState.Success ?: return@FoodSelectContent
                state = state.copy(
                    listState = current.copy(
                        foods = current.foods.map {
                            if (it.code == code) it.copy(isSelected = !it.isSelected) else it
                        },
                    ),
                )
            },
            onConfirmClick = {},
            onRetry = {},
            onResetAlertConfirm = {},
            onResetAlertDismiss = {},
        )
    }
}

/** 프리뷰용 음식 6종. 실제 화면은 GET /api/food-categories 응답으로 채운다. */
private fun previewFoods(): List<FoodOptionUiModel> = listOf(
    "KOREAN" to "한식",
    "JAPANESE" to "일식",
    "WESTERN" to "양식",
    "CHINESE" to "중식",
    "SNACK" to "분식",
    "DESSERT" to "디저트",
).mapIndexed { index, (code, name) ->
    FoodCategoryDto(
        foodCategoryId = index + 1L,
        code = code,
        name = name,
        description = null,
        sortOrder = index + 1,
    ).toUiModel()
}
