package com.umc.todait.feature.course.base_place

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Green700
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink100
import com.umc.todait.ui.theme.Pink700
import com.umc.todait.ui.theme.PlaceCardGradientEnd
import com.umc.todait.ui.theme.PlaceCardGradientStart
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 기준 장소 설정 화면(와이어프레임 1.1).
 *
 * 상단 헤더(뒤로가기/타이틀/확인) + 검색창 + "지금 내 주변 핫플" 추천/검색 결과 목록으로 구성되며,
 * 장소 카드 탭 시 확인 모달(1.2)을 띄운다.
 */
@Composable
fun BasePlaceScreen(
    onNavigateToCompose: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
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

    Box(modifier = modifier.fillMaxSize()) {
        BasePlaceContent(
            state = uiState,
            onBack = onBack,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onSearch = viewModel::onSearch,
            onClearSearch = viewModel::onClearSearch,
            // 카드 본문 탭 → 장소 상세 화면 진입 (기획 문서 기준).
            onPlaceClick = { place -> onNavigateToDetail(place.placeId) },
            // 카드 우상단 '+' → 기준 장소 선택.
            onSelectPlace = viewModel::onSelectPlace,
            // 헤더 체크 → 확정 알럿.
            onConfirmClick = viewModel::onConfirmClick,
            onRetry = viewModel::loadNearbyHotPlaces,
        )

        // 시스템 알럿(딤 배경 + 커스텀 다이얼로그) 오버레이.
        when (val alert = uiState.alert) {
            is BasePlaceAlert.SelectRequired -> BasePlaceSystemAlert(
                title = stringResource(R.string.base_place_select_required_title),
                description = stringResource(R.string.base_place_select_required_desc),
                onConfirm = viewModel::onDismissAlert,
                onCancel = viewModel::onDismissAlert,
            )

            is BasePlaceAlert.Confirm -> BasePlaceSystemAlert(
                title = stringResource(R.string.base_place_confirm_title, alert.place.name),
                description = stringResource(R.string.base_place_confirm_desc, alert.place.name),
                errorMessage = uiState.confirmError,
                onConfirm = viewModel::onConfirmSelection,
                onCancel = viewModel::onDismissAlert,
            )

            null -> Unit
        }
    }
}

@Composable
private fun BasePlaceContent(
    state: BasePlaceUiState,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onSelectPlace: (PlaceUiModel) -> Unit,
    onConfirmClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        BasePlaceTopBar(
            title = stringResource(R.string.base_place_title),
            onBack = onBack,
            // 헤더 체크 → 확정(선택 여부에 따라 시스템알럿1/2).
            onConfirm = onConfirmClick,
        )

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                onClear = onClearSearch,
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = if (state.isSearching) {
                    stringResource(R.string.base_place_section_search)
                } else {
                    stringResource(R.string.base_place_section_nearby)
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900,
            )
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (val listState = state.listState) {
                    is PlaceListState.Loading -> LoadingIndicator()
                    is PlaceListState.Empty -> PlaceEmptyState(
                        title = listState.message,
                        description = if (state.isSearching) {
                            stringResource(R.string.base_place_empty_search_desc)
                        } else {
                            null
                        },
                    )

                    is PlaceListState.Error -> ErrorContent(
                        error = UiError(message = listState.message),
                        onRetry = onRetry,
                    )

                    is PlaceListState.Success -> PlaceList(
                        places = listState.places,
                        selectedPlaceId = state.selectedPlace?.placeId,
                        onPlaceClick = onPlaceClick,
                        onSelectPlace = onSelectPlace,
                    )
                }
            }
        }
    }
}

/**
 * 상단 헤더(Pink100 배경). 좌측 뒤로가기, 가운데 타이틀, 우측 확인 버튼.
 */
@Composable
private fun BasePlaceTopBar(
    title: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Pink100)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CircleIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로가기",
            onClick = onBack,
        )
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
            color = Gray900,
        )
        CircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            imageVector = Icons.Filled.Check,
            contentDescription = "확인",
            onClick = onConfirm,
        )
    }
}

@Composable
private fun CircleIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Gray600)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = White,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * 검색창. 흰색 pill 형태 + 좌측 돋보기 아이콘 + placeholder.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Gray900,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.base_place_search_hint),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        color = Gray200,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )
            }
            if (query.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "지우기",
                    tint = Gray200,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onClear),
                )
            }
        }
    }
}

// 카드 우측 하단 파스텔 장식(3종)을 순서대로 순환 적용한다. (Figma 목업의 장식 실루엣)
private val placeDecorations = listOf(
    R.drawable.ic_place_deco_1,
    R.drawable.ic_place_deco_2,
    R.drawable.ic_place_deco_3,
)

@Composable
private fun PlaceList(
    places: List<PlaceUiModel>,
    selectedPlaceId: Long?,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onSelectPlace: (PlaceUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        itemsIndexed(places, key = { _, place -> place.placeId }) { index, place ->
            PlaceCard(
                place = place,
                decorationRes = placeDecorations[index % placeDecorations.size],
                isSelected = place.placeId == selectedPlaceId,
                onClick = { onPlaceClick(place) },
                onSelect = { onSelectPlace(place) },
            )
        }
    }
}

/**
 * 장소 카드. 좌측 장소 이미지 위에 우측으로 이어지는 그라데이션 패널을 얹고,
 * 그 위에 장소명·주소·근접 배지를 흰색 텍스트로 표시한다.
 * 명세 정책상 별점/평점/점수는 표시하지 않는다.
 */
@Composable
private fun PlaceCard(
    place: PlaceUiModel,
    decorationRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
) {
    // 디자인: bg-gradient-to-b (수직, 상단 → 하단)
    val gradient = Brush.verticalGradient(
        colors = listOf(PlaceCardGradientStart, PlaceCardGradientEnd),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            // 선택 시 초록 테두리(Figma: Green-700).
            .then(
                if (isSelected) {
                    Modifier.border(1.dp, Green700, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick),
    ) {
        // 좌측: 장소 이미지 (약 1/3)
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(PlaceCardGradientStart), // 이미지가 없을 때의 배경
        ) {
            if (place.imageUrl != null) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        // 우측: 수직 그라데이션 패널 (약 2/3) + 텍스트
        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .background(gradient),
        ) {
            // 우측 하단 파스텔 장식(텍스트 뒤에 배치)
            Image(
                painter = painterResource(id = decorationRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 8.dp)
                    .size(60.dp),
            )
            // 우측 상단 '+' 선택 버튼 (Figma: 기준 장소 선택). 탭 시 초록 테두리로 선택 표시.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSelect),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = White,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                ProximityBadge(text = place.reasonText?.takeIf { it.isNotBlank() } ?: place.category)
            }
        }
    }
}

/**
 * 빈 상태(검색 결과 없음 / 추천 없음). 돋보기 아이콘 + 안내 문구(제목 + 선택적 설명).
 */
@Composable
private fun PlaceEmptyState(
    title: String,
    description: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Gray500,
        )
        if (description != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray500,
            )
        }
    }
}

/** 흰색 pill 배지. 추천 이유(없으면 카테고리)를 강조 텍스트로 노출한다. */
@Composable
private fun ProximityBadge(text: String) {
    Surface(
        color = White,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 10.sp),
            color = Pink700,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

// ---------- Preview ----------

private val previewPlaces = listOf(
    PlaceUiModel(
        placeId = 1,
        name = "애몽",
        address = "서울특별시 마포구 연남로3길 13",
        category = "카페",
        areaName = "연남",
        imageUrl = null,
        reasonText = "현재 위치와 가까워요",
        latitude = 37.56,
        longitude = 126.92,
    ),
    PlaceUiModel(
        placeId = 2,
        name = "뀌노이",
        address = "서울 마포구 신수동 42-5",
        category = "식당",
        areaName = "성수",
        imageUrl = null,
        reasonText = "현재 위치와 가까워요",
        latitude = 37.54,
        longitude = 127.05,
    ),
)

@Preview(name = "추천 목록", showBackground = true)
@Composable
private fun BasePlaceContentSuccessPreview() {
    TodaitTheme {
        BasePlaceContent(
            state = BasePlaceUiState(
                listState = PlaceListState.Success(previewPlaces),
                selectedPlace = previewPlaces[1],
            ),
            onBack = {},
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceClick = {},
            onSelectPlace = {},
            onConfirmClick = {},
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
                searchQuery = "샴푸",
                listState = PlaceListState.Empty("검색 결과가 없어요"),
            ),
            onBack = {},
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceClick = {},
            onSelectPlace = {},
            onConfirmClick = {},
            onRetry = {},
        )
    }
}
