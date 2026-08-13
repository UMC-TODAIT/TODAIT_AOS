package com.umc.todait.feature.course.base_place

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
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
import com.umc.todait.feature.course.compose.CourseMood
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.theme.CourseActiveGradientEnd
import com.umc.todait.ui.theme.CourseActiveGradientStart
import com.umc.todait.ui.theme.CourseActiveSelectedGradientEnd
import com.umc.todait.ui.theme.CourseActiveSelectedGradientStart
import com.umc.todait.ui.theme.CourseCalmGradientEnd
import com.umc.todait.ui.theme.CourseCalmGradientStart
import com.umc.todait.ui.theme.CourseCalmSelectedGradientEnd
import com.umc.todait.ui.theme.CourseCalmSelectedGradientStart
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseHipSelectedGradientEnd
import com.umc.todait.ui.theme.CourseHipSelectedGradientStart
import com.umc.todait.ui.theme.CourseModernGradientEnd
import com.umc.todait.ui.theme.CourseModernGradientStart
import com.umc.todait.ui.theme.CourseModernSelectedGradientEnd
import com.umc.todait.ui.theme.CourseModernSelectedGradientStart
import com.umc.todait.ui.theme.CourseQuietGradientEnd
import com.umc.todait.ui.theme.CourseQuietGradientStart
import com.umc.todait.ui.theme.CourseQuietSelectedGradientEnd
import com.umc.todait.ui.theme.CourseQuietSelectedGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart
import com.umc.todait.ui.theme.CourseRomanticSelectedGradientEnd
import com.umc.todait.ui.theme.CourseRomanticSelectedGradientStart
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Green700
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.SearchIconCircle
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 기준 장소 설정 화면(와이어프레임 1.1).
 *
 * 상단 헤더(뒤로가기/타이틀/확인) + 검색창 + "지금 내 주변 핫플" 추천/검색 결과 목록으로 구성된다.
 * 장소 카드는 탭하면 그 자리에서 기준 장소로 선택되고(Figma: 장소카드 Click 배리언트),
 * 길게 누르면 장소 상세 화면으로 진입한다. 확정은 헤더 체크로 한다.
 */
@Composable
fun BasePlaceScreen(
    // 기준 장소 저장 성공 시 (courseDraftId, basePlaceId) 를 받아 코스 구성 플로우로 넘긴다.
    onNavigateToCompose: (courseDraftId: Long, basePlaceId: Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BasePlaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BasePlaceEffect.NavigateToCompose ->
                    onNavigateToCompose(effect.courseDraftId, effect.basePlaceId)
                // 이전 버튼은 단계 이동 API 를 먼저 부르므로 화면 이동도 ViewModel 이 알려줄 때 한다.
                BasePlaceEffect.NavigateBack -> onBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BasePlaceContent(
            state = uiState,
            onBack = viewModel::onBackClick,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onSearch = viewModel::onSearch,
            onClearSearch = viewModel::onClearSearch,
            // 카드 길게 누르기 → 장소 상세 화면 진입.
            // 내부 DB에 없는 카카오 검색 결과(placeId 없음)나 detailAvailable=false 인 장소는 상세가 없다.
            onPlaceLongClick = { place ->
                place.placeId?.takeIf { place.detailAvailable }?.let(onNavigateToDetail)
            },
            // 카드 탭 → 기준 장소 선택.
            onSelectPlace = viewModel::onSelectPlace,
            // 헤더 체크 → 확정 알럿.
            onConfirmClick = viewModel::onConfirmClick,
            onRetry = viewModel::loadNearbyHotPlaces,
        )

        // 시스템 알럿 오버레이. 시안(컴포넌트_System 시스템알럿)이 다른 화면과 같은 흰색 알럿이라 CommonDialog 를 쓴다.
        when (val alert = uiState.alert) {
            is BasePlaceAlert.SelectRequired -> CommonDialog(
                title = stringResource(R.string.base_place_select_required_title) + "\n" +
                    stringResource(R.string.base_place_select_required_desc),
                onConfirm = viewModel::onDismissAlert,
                onDismiss = viewModel::onDismissAlert,
            )

            is BasePlaceAlert.Confirm -> CommonDialog(
                // 확정 검증에 실패하면(지원 지역 외 등) 사유를 대신 보여준다.
                title = uiState.confirmError
                    ?: stringResource(
                        R.string.base_place_confirm_title,
                        alert.place.name,
                        euroParticle(alert.place.name),
                    ),
                onConfirm = viewModel::onConfirmSelection,
                onDismiss = viewModel::onDismissAlert,
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
    onPlaceLongClick: (PlaceUiModel) -> Unit,
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
        ScreenTopBar(
            title = stringResource(R.string.base_place_title),
            onBack = onBack,
            // 헤더 체크 → 확정(선택 여부에 따라 시스템알럿1/2).
            onConfirm = onConfirmClick,
        )

        // Figma: 구분선 아래 23 → 검색창 → 27 → 섹션 제목(22 SemiBold, #222) → 8 → 안내(16 SemiBold, Gray-400) → 24 → 목록
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(23.dp))
            SearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                onClear = onClearSearch,
            )

            Spacer(Modifier.height(27.dp))
            Text(
                text = if (state.isSearching) {
                    stringResource(R.string.base_place_section_search)
                } else {
                    stringResource(R.string.base_place_section_nearby)
                },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Gray800,
            )
            // 길게 누르기로 상세를 볼 수 있다는 안내는 추천(핫플) 목록에만 있다.
            if (!state.isSearching) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.base_place_long_press_hint),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray400,
                )
            }
            Spacer(Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (val listState = state.listState) {
                    is PlaceListState.Loading -> LoadingIndicator()
                    is PlaceListState.Empty -> PlaceEmptyState(
                        title = listState.title,
                        description = listState.description ?: if (state.isSearching) {
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
                        selectedPlaceKey = state.selectedPlace?.key,
                        onPlaceLongClick = onPlaceLongClick,
                        onSelectPlace = onSelectPlace,
                    )
                }
            }
        }
    }
}

/**
 * 검색창. 흰색 pill 형태 + 좌측 돋보기 아이콘(Gray-100 원 + 글리프) + placeholder.
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
            .height(45.dp),
        shape = RoundedCornerShape(percent = 50),
        color = White,
        shadowElevation = 4.dp,
    ) {
        Row(
            // Figma: 돋보기 원이 검색창 좌측에서 7dp, 원과 입력 텍스트 사이 24dp.
            modifier = Modifier.padding(start = 7.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchIcon()
            Spacer(Modifier.width(24.dp))
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

/**
 * 검색창 좌측 돋보기. Figma(node 534:13530/534:13535): Gray-100 원 35dp 위에 18dp 글리프.
 */
@Composable
private fun SearchIcon() {
    Box(
        modifier = Modifier
            .size(35.dp)
            .clip(CircleShape)
            .background(SearchIconCircle),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_common_search),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
}

// 분위기 태그가 없을 때(핫플/검색 응답) 순번으로 부여할 fallback. 색이 확정된 6종을 순환한다.
private val fallbackMoods = CourseMood.entries

/**
 * 분위기(mood)별 카드 그라데이션(bg-gradient-to-b). [selected] 면 Figma 장소카드의
 * Property 1=Click 배리언트 색(기본색보다 채도가 높다)을 쓴다.
 */
private fun CourseMood.gradientColors(selected: Boolean): List<Color> = when {
    !selected -> when (this) {
        CourseMood.ROMANTIC -> listOf(CourseRomanticGradientStart, CourseRomanticGradientEnd)
        CourseMood.MODERN -> listOf(CourseModernGradientStart, CourseModernGradientEnd)
        CourseMood.HIP -> listOf(CourseHipGradientStart, CourseHipGradientEnd)
        CourseMood.QUIET -> listOf(CourseQuietGradientStart, CourseQuietGradientEnd)
        CourseMood.ACTIVE -> listOf(CourseActiveGradientStart, CourseActiveGradientEnd)
        CourseMood.CALM -> listOf(CourseCalmGradientStart, CourseCalmGradientEnd)
    }

    else -> when (this) {
        CourseMood.ROMANTIC ->
            listOf(CourseRomanticSelectedGradientStart, CourseRomanticSelectedGradientEnd)

        CourseMood.MODERN ->
            listOf(CourseModernSelectedGradientStart, CourseModernSelectedGradientEnd)

        CourseMood.HIP -> listOf(CourseHipSelectedGradientStart, CourseHipSelectedGradientEnd)
        CourseMood.QUIET -> listOf(CourseQuietSelectedGradientStart, CourseQuietSelectedGradientEnd)

        CourseMood.ACTIVE ->
            listOf(CourseActiveSelectedGradientStart, CourseActiveSelectedGradientEnd)

        CourseMood.CALM -> listOf(CourseCalmSelectedGradientStart, CourseCalmSelectedGradientEnd)
    }
}

/** 분위기별 우측 하단 아이콘(장식). 6종 각각의 전용 아이콘(ic_mood_*, 분위기별 색/모양)을 쓴다. */
private fun CourseMood.decorationRes(): Int = when (this) {
    CourseMood.HIP -> R.drawable.ic_mood_hip
    CourseMood.QUIET -> R.drawable.ic_mood_quiet
    CourseMood.ACTIVE -> R.drawable.ic_mood_active
    CourseMood.ROMANTIC -> R.drawable.ic_mood_romantic
    CourseMood.MODERN -> R.drawable.ic_mood_modern
    CourseMood.CALM -> R.drawable.ic_mood_calm
}

@Composable
private fun PlaceList(
    places: List<PlaceUiModel>,
    // 내부 미등록 장소도 섞이므로 placeId 가 아니라 PlaceUiModel.key 로 비교한다.
    selectedPlaceKey: String?,
    onPlaceLongClick: (PlaceUiModel) -> Unit,
    onSelectPlace: (PlaceUiModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        itemsIndexed(places, key = { _, place -> place.key }) { index, place ->
            PlaceCard(
                place = place,
                // 분위기별 카드 색상. 핫플/검색 응답에는 분위기 태그가 없어 대부분 fallback 을 탄다.
                mood = CourseMood.fromTags(place.moodTags)
                    ?: fallbackMoods[index % fallbackMoods.size],
                isSelected = place.key == selectedPlaceKey,
                onSelect = { onSelectPlace(place) },
                onLongClick = { onPlaceLongClick(place) },
            )
        }
    }
}

/**
 * 장소 카드. 좌측 장소 이미지 위에 우측으로 이어지는 그라데이션 패널을 얹고,
 * 그 위에 장소명·주소·근접 배지를 흰색 텍스트로 표시한다.
 * 명세 정책상 별점/평점/점수는 표시하지 않는다.
 *
 * 탭하면 기준 장소로 선택되고(Green-700 2dp 테두리 + Click 그라데이션),
 * 길게 누르면 장소 상세로 넘어간다. (Figma: 장소카드 Default/Click 배리언트)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaceCard(
    place: PlaceUiModel,
    mood: CourseMood,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLongClick: () -> Unit,
) {
    // 디자인: bg-gradient-to-b (수직, 상단 → 하단). 분위기별 + 선택 여부별 색상.
    val gradientColors = mood.gradientColors(selected = isSelected)
    val gradient = Brush.verticalGradient(colors = gradientColors)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            // 선택 시 초록 테두리(Figma: Green-700, 2dp).
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Green700, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onLongClick,
            ),
    ) {
        // 좌측: 장소 이미지 (약 1/3)
        Box(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(gradientColors.first()), // 이미지가 없을 때의 배경(분위기 시작색)
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
                painter = painterResource(id = mood.decorationRes()),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 11.dp, bottom = 11.dp)
                    .size(60.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Figma: 텍스트 좌측이 카드 기준 135px(그라데이션 패널 안쪽 11), 상단 10, 하단 12.
                    .padding(start = 11.dp, top = 10.dp, end = 10.dp, bottom = 12.dp),
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
        // Figma: 돋보기 38 → 16 → 제목(16 SemiBold) → 8 → 설명(12 SemiBold), 모두 Gray-500
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(38.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
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

/** 흰색 pill 배지. 추천 이유(없으면 카테고리)를 강조 텍스트로 노출한다. (Figma: Pink-800) */
@Composable
private fun ProximityBadge(text: String) {
    Surface(
        color = White,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            style = TextStyle(fontSize = 10.sp),
            color = Pink800,
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
            onPlaceLongClick = {},
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
                listState = PlaceListState.Empty(
                    title = "검색 결과가 없어요",
                    description = "다른 검색어로 다시 검색해보세요.",
                ),
            ),
            onBack = {},
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceLongClick = {},
            onSelectPlace = {},
            onConfirmClick = {},
            onRetry = {},
        )
    }
}

@Preview(name = "지원 장소 없음", showBackground = true)
@Composable
private fun BasePlaceContentUnsupportedAreaPreview() {
    TodaitTheme {
        BasePlaceContent(
            state = BasePlaceUiState(
                searchQuery = "강남역",
                listState = PlaceListState.Empty(
                    title = stringResource(R.string.base_place_unsupported_area_title),
                    description = stringResource(R.string.base_place_unsupported_area_desc),
                ),
            ),
            onBack = {},
            onSearchQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onPlaceLongClick = {},
            onSelectPlace = {},
            onConfirmClick = {},
            onRetry = {},
        )
    }
}

/**
 * 장소 이름 뒤에 붙는 조사 "로"/"으로" 를 고른다.
 *
 * 받침이 없거나 받침이 'ㄹ' 이면 "로"(예: 뀌노이로, 신촌서울로), 그 외에는 "으로"(예: 광장으로).
 * 한글이 아닌 글자로 끝나면(영문·숫자 상호 등) 시안 문구를 그대로 살리기 위해 "로" 로 둔다.
 */
private fun euroParticle(name: String): String {
    val last = name.lastOrNull() ?: return "로"
    if (last !in '가'..'힣') return "로"
    val finalConsonant = (last - '가') % 28
    // 0 = 받침 없음, 8 = 받침 'ㄹ'
    return if (finalConsonant == 0 || finalConsonant == 8) "로" else "으로"
}
