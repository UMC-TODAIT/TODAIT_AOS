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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.component.TasteSelectionCard
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
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

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FoodSelectEffect.NavigateToBasePlace -> onNavigateToBasePlace(effect.courseDraftId)
            }
        }
    }

    FoodSelectContent(
        state = uiState,
        onBack = onBack,
        onToggleFood = viewModel::onToggleFood,
        onConfirmClick = viewModel::onConfirmClick,
        onRetry = viewModel::loadFoodCategories,
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

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.food_select_headline),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.food_select_subtitle, FoodSelectUiState.MIN_SELECTION),
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
            Spacer(Modifier.height(20.dp))
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
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(foods, key = { it.code }) { food ->
            TasteSelectionCard(
                title = food.title,
                hashtags = food.hashtags,
                gradientStart = food.gradientStart,
                gradientEnd = food.gradientEnd,
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
