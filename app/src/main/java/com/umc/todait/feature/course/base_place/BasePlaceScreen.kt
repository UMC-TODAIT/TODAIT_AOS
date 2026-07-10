package com.umc.todait.feature.course.base_place

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.ui.component.EmptyContent
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.Gray100
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Primary
import com.umc.todait.ui.theme.PrimaryLight
import com.umc.todait.ui.theme.PrimaryDark
import com.umc.todait.ui.theme.TodaitTheme
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

/**
 * 기준 장소 설정 화면(와이어프레임 1.1).
 * 검색창 + "지금 내 주변 핫플" 추천/검색 결과 목록으로 구성되며,
 * 장소 카드 탭 시 확인 모달(1.2)을 띄운다.
 */
@Composable
fun BasePlaceScreen(
    onNavigateToCompose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BasePlaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BasePlaceEffect.NavigateToCompose -> onNavigateToCompose()
            }
        }
    }

    BasePlaceContent(
        state = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearch = viewModel::onSearch,
        onClearSearch = viewModel::onClearSearch,
        onPlaceClick = viewModel::onPlaceClick,
        onRetry = viewModel::loadNearbyHotPlaces,
        modifier = modifier,
    )

    uiState.pendingPlace?.let { place ->
        BasePlaceConfirmDialog(
            placeName = place.name,
            errorMessage = uiState.confirmError,
            onConfirm = viewModel::onConfirmSelection,
            onDismiss = viewModel::onDismissConfirm,
        )
    }
}

@Composable
private fun BasePlaceContent(
    state: BasePlaceUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.base_place_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Gray900,
        )
        state.currentAreaName?.let { area ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = area,
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
        }

        Spacer(Modifier.height(16.dp))
        SearchBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = onSearch,
            onClear = onClearSearch,
        )

        Spacer(Modifier.height(20.dp))
        if (!state.isSearching) {
            Text(
                text = stringResource(R.string.base_place_section_nearby),
                style = MaterialTheme.typography.titleMedium,
                color = Gray900,
            )
            Spacer(Modifier.height(12.dp))
        }

        Box(modifier = Modifier.weight(1f)) {
            when (val listState = state.listState) {
                is PlaceListState.Loading -> LoadingIndicator()
                is PlaceListState.Empty -> EmptyContent(message = listState.message)
                is PlaceListState.Error -> ErrorContent(
                    error = UiError(message = listState.message),
                    onRetry = onRetry,
                )

                is PlaceListState.Success -> PlaceList(
                    places = listState.places,
                    onPlaceClick = onPlaceClick,
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.base_place_search_hint)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text(text = "지우기", color = Gray500)
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
private fun PlaceList(
    places: List<PlaceUiModel>,
    onPlaceClick: (PlaceUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(places, key = { it.placeId }) { place ->
            PlaceCard(place = place, onClick = { onPlaceClick(place) })
        }
    }
}

/**
 * 장소 카드. 명세 정책상 별점/평점/점수는 표시하지 않고,
 * 카테고리·지역 태그와 추천 이유로만 신뢰도를 표현한다.
 */
@Composable
private fun PlaceCard(
    place: PlaceUiModel,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Gray100),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (place.imageUrl != null) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TagChip(text = place.category)
                    if (place.areaName.isNotBlank()) {
                        TagChip(text = place.areaName)
                    }
                }
                if (!place.reasonText.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = place.reasonText,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Surface(
        color = PrimaryLight,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

// ---------- Preview ----------

private val previewPlaces = listOf(
    PlaceUiModel(
        placeId = 1,
        name = "연남동 감성카페",
        address = "서울 마포구 성미산로 21",
        category = "카페",
        areaName = "연남",
        imageUrl = null,
        reasonText = "잔잔한 분위기로 데이트 시작에 딱이에요",
        latitude = 37.56,
        longitude = 126.92,
    ),
    PlaceUiModel(
        placeId = 2,
        name = "성수 브런치 식당",
        address = "서울 성동구 연무장길 33",
        category = "식당",
        areaName = "성수",
        imageUrl = null,
        reasonText = null,
        latitude = 37.54,
        longitude = 127.05,
    ),
)

@Preview(name = "추천 목록", showBackground = true)
@Composable
private fun BasePlaceContentSuccessPreview() {
    TodaitTheme {
        BasePlaceContent(
            state = BasePlaceUiState(listState = PlaceListState.Success(previewPlaces)),
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceClick = {},
            onRetry = {},
        )
    }
}

@Preview(name = "검색 결과 없음", showBackground = true)
@Composable
private fun BasePlaceContentEmptyPreview() {
    TodaitTheme {
        BasePlaceContent(
            state = BasePlaceUiState(
                searchQuery = "강남",
                listState = PlaceListState.Empty("'강남'에 대한 검색 결과가 없어요."),
            ),
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceClick = {},
            onRetry = {},
        )
    }
}
