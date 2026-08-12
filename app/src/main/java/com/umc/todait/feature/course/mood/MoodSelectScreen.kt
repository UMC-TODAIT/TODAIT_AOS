package com.umc.todait.feature.course.mood

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
import com.umc.todait.feature.course.data.dto.MoodTagDto
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.component.TasteSelectionCard
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 분위기 선택 화면(취향 설정 1/2). 코스 생성 플로우의 진입 화면이자 하단 탭 "코스 생성"의 루트다.
 */
@Composable
fun MoodSelectScreen(
    onBack: () -> Unit,
    onNavigateToFood: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MoodSelectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MoodSelectEffect.NavigateToFood -> onNavigateToFood(effect.courseDraftId)
            }
        }
    }

    MoodSelectContent(
        state = uiState,
        onBack = onBack,
        onToggleMood = viewModel::onToggleMood,
        onConfirmClick = viewModel::onConfirmClick,
        onRetry = viewModel::loadMoodTags,
        modifier = modifier,
    )
}

@Composable
private fun MoodSelectContent(
    state: MoodSelectUiState,
    onBack: () -> Unit,
    onToggleMood: (String) -> Unit,
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
            title = stringResource(R.string.mood_select_title),
            onBack = onBack,
            onConfirm = onConfirmClick,
            confirmEnabled = state.isConfirmEnabled,
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.mood_select_headline),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.mood_select_subtitle, MoodSelectUiState.MIN_SELECTION),
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
                is MoodListState.Loading -> LoadingIndicator()

                is MoodListState.Error -> ErrorContent(
                    error = UiError(message = listState.message),
                    onRetry = onRetry,
                )

                is MoodListState.Success -> MoodGrid(
                    moods = listState.moods,
                    onToggleMood = onToggleMood,
                )
            }
        }
    }
}

@Composable
private fun MoodGrid(
    moods: List<MoodOptionUiModel>,
    onToggleMood: (String) -> Unit,
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
        items(moods, key = { it.code }) { mood ->
            TasteSelectionCard(
                title = mood.title,
                hashtags = mood.hashtags,
                gradientStart = mood.gradientStart,
                gradientEnd = mood.gradientEnd,
                selectedGradientStart = mood.selectedGradientStart,
                selectedGradientEnd = mood.selectedGradientEnd,
                decorationRes = mood.decorationRes,
                isSelected = mood.isSelected,
                onClick = { onToggleMood(mood.code) },
            )
        }
    }
}

// ---------- Preview ----------

/**
 * Interactive Mode(프리뷰 상단 재생 아이콘)로 실행하면 실제로 카드를 눌러 선택을 토글해볼 수 있다.
 * ViewModel 없이 로컬 상태로만 흉내 낸 것이라 확인 버튼 클릭 시 API 호출은 일어나지 않는다.
 */
@Preview(name = "분위기 선택", showBackground = true, heightDp = 1200)
@Composable
private fun MoodSelectContentPreview() {
    var state by remember { mutableStateOf(MoodSelectUiState(listState = MoodListState.Success(previewMoods()))) }
    TodaitTheme {
        MoodSelectContent(
            state = state,
            onBack = {},
            onToggleMood = { code ->
                val current = state.listState as? MoodListState.Success ?: return@MoodSelectContent
                state = state.copy(
                    listState = current.copy(
                        moods = current.moods.map {
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

/** 프리뷰용 분위기 6종. 실제 화면은 GET /api/mood-tags 응답으로 채운다. */
private fun previewMoods(): List<MoodOptionUiModel> = listOf(
    "HIP" to "힙한",
    "QUIET" to "조용한",
    "ACTIVE" to "활발한",
    "ROMANTIC" to "로맨틱",
    "MODERN" to "모던한",
    "CALM" to "차분한",
).mapIndexed { index, (code, name) ->
    MoodTagDto(
        moodTagId = index + 1L,
        code = code,
        name = name,
        description = null,
        sortOrder = index + 1,
    ).toUiModel()
}
