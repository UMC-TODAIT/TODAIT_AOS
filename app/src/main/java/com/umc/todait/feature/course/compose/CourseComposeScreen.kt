
package com.umc.todait.feature.course.compose

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.feature.course.base_place.BasePlaceSystemAlert
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.theme.CategoryTabTextSelected
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
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Green700
import com.umc.todait.ui.theme.Pink600
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 코스 구성하기 - 장소카드 선택 화면(#26, 와이어프레임 "코스구성하기(카페)_기본/선택").
 *
 * 헤더(뒤로/타이틀/✓) + 스크롤 본문[지도 + 카테고리 탭 + 추천 카드]로 구성된다.
 * 추천 카드를 탭하면 코스에 담기고(길게 누르면 장소 상세), 헤더 ✓(담은 장소 ≥1일 때 활성) → **선택한 장소 화면**([SelectedPlacesScreen])으로 이동한다.
 * 선택 상태는 상위 그래프 스코프 [CourseComposeViewModel] 을 통해 다음 화면과 공유된다.
 *
 * ⚠️ 지도는 카카오맵 v2, 드래그 순서 변경은 다음 화면에서 처리(제스처는 TODO).
 */
@Composable
fun CourseComposeScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSelected: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseComposeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 지도 "현재 위치" 마커용 위치 권한. 이 앱에서 위치를 실제로 쓰는 첫 화면이라 여기서 받는다.
    // 거부해도 마커만 빠지고 코스 구성은 그대로 쓸 수 있어 별도 안내는 띄우지 않는다.
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.any { it }) viewModel.loadCurrentLocation()
    }
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    // 순서 설정 화면으로는 ordering 단계 전환(PATCH .../ordering)이 성공한 뒤에만 넘어간다.
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CourseComposeEffect.NavigateToSelected -> onNavigateToSelected()
                CourseComposeEffect.NavigateToSave -> Unit
                // 이전 버튼은 단계 이동 API 를 먼저 부르므로 화면 이동도 ViewModel 이 알려줄 때 한다.
                CourseComposeEffect.NavigateBack -> onBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CourseComposeContent(
            state = uiState,
            // 이 화면은 PLACE_SELECTING 단계 → 기준 장소 설정으로 되돌린다.
            onBack = { viewModel.onBackClick(CourseDraftStatus.PLACE_SELECTING) },
            // ✓ 는 canConfirm(담은 장소 ≥1)일 때만 활성 → 순서 설정 단계로 전환 요청.
            onConfirm = viewModel::onSelectionConfirmed,
            onSelectCategory = viewModel::onSelectCategory,
            // 카드 길게 누르기 → 장소 상세 화면 진입. detailAvailable=false 인 장소는 상세가 없다.
            onPlaceLongClick = { place ->
                place.placeId?.takeIf { place.detailAvailable }?.let(onNavigateToDetail)
            },
            onAddPlace = viewModel::onAddPlace,
            onRetry = viewModel::retry,
        )

        when (val alert = uiState.alert) {
            CourseComposeAlert.Duplicate -> BasePlaceSystemAlert(
                title = stringResource(R.string.course_compose_duplicate_title),
                description = stringResource(R.string.course_compose_duplicate_desc),
                onConfirm = viewModel::onDismissAlert,
                onCancel = viewModel::onDismissAlert,
            )

            // 선택 장소 추가 실패. 서버가 준 문구(중복·기준 장소·최대 개수 등)를 그대로 보여준다.
            is CourseComposeAlert.AddFailed -> BasePlaceSystemAlert(
                title = stringResource(R.string.course_compose_add_error_title),
                description = alert.message,
                onConfirm = viewModel::onDismissAlert,
                onCancel = viewModel::onDismissAlert,
            )

            null -> Unit
        }

        // 단계 전환 실패(권한/상태 충돌 등) 안내. 확인만 있는 단일 알럿으로 띄운다.
        uiState.submitError?.let { message ->
            BasePlaceSystemAlert(
                title = stringResource(R.string.course_compose_submit_error_title),
                description = message,
                onConfirm = viewModel::onDismissSubmitError,
                onCancel = viewModel::onDismissSubmitError,
            )
        }
    }
}

@Composable
private fun CourseComposeContent(
    state: CourseComposeUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onSelectCategory: (Long) -> Unit,
    onPlaceLongClick: (PlaceUiModel) -> Unit,
    onAddPlace: (PlaceUiModel) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    // 상단 지도 슬롯. 기본은 실제 카카오맵이며, @Preview 에서는 렌더 불가한 MapView 대신 placeholder 를 주입한다.
    mapContent: @Composable (Modifier) -> Unit = { mapModifier ->
        CourseMap(
            places = state.orderedPlaces,
            basePlaceKey = state.basePlaceKey,
            currentLocation = state.currentLocation,
            modifier = mapModifier,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.course_compose_title),
            onBack = onBack,
            // 담은 장소가 없으면(확정 불가) 흐리게 표시하고 클릭도 막는다.
            onConfirm = onConfirm,
            confirmEnabled = state.canConfirm,
            confirmContentDescription = "확정",
        )

        val selectedKeys = state.selectedPlaces.map { it.key }.toSet()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // 상단 지도(기준 장소 + 선택 장소 핀). 카카오맵 v2.
            item {
                mapContent(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
            }

            // 카테고리 탭은 서버(place-categories) 로드 후에만 노출한다.
            if (state.categories.isNotEmpty()) {
                item {
                    CategoryTabs(
                        categories = state.categories,
                        selectedId = state.selectedCategoryId,
                        onSelect = onSelectCategory,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    )
                }
            }

            // 추천 장소 목록.
            when (val recommend = state.recommendState) {
                is RecommendListState.Loading ->
                    item { StatusBox { LoadingIndicator() } }

                is RecommendListState.Empty ->
                    item { StatusBox { EmptyText(recommend.message) } }

                is RecommendListState.Error ->
                    item {
                        StatusBox {
                            ErrorContent(
                                error = UiError(message = recommend.message),
                                onRetry = onRetry,
                            )
                        }
                    }

                is RecommendListState.Success ->
                    itemsIndexed(recommend.places, key = { _, place -> place.key }) { index, place ->
                        RecommendCard(
                            place = place,
                            added = place.key in selectedKeys,
                            // 담기 요청이 끝나기 전에는 카드를 다시 누를 수 없다(중복 추가 방지).
                            adding = place.key in state.addingPlaceKeys,
                            // 분위기별 카드 색상. 추천 응답의 matchedMoodTags 로 결정하되,
                            // 일치한 분위기가 없을 수 있어 그때는 6종을 순번으로 부여한다.
                            mood = CourseMood.fromTags(place.moodTags)
                                ?: fallbackMoods[index % fallbackMoods.size],
                            onAdd = { onAddPlace(place) },
                            onLongClick = { onPlaceLongClick(place) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
            }
            // 선택한 장소(드래그 정렬)는 다음 화면 [SelectedPlacesScreen] 에서 처리한다.
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<PlaceCategoryUiModel>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            val isSelected = category.id == selectedId
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    // Figma: 선택 Pink-600(#F9AEAC)/텍스트 Gray-700, 미선택 Gray-200/텍스트 White.
                    .background(if (isSelected) Pink600 else Gray200)
                    .clickable { onSelect(category.id) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.name,
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
                    color = if (isSelected) CategoryTabTextSelected else White,
                )
            }
        }
    }
}

// 분위기 태그가 없을 때(추천 API) 순번으로 부여할 fallback. 색이 확정된 6종을 순환한다.
private val fallbackMoods = CourseMood.entries

/**
 * 분위기(mood)별 카드 그라데이션 색상(bg-gradient-to-b). 6종 모두 Figma "취향설정" 화면 확정값
 * (Color.kt 의 Course*GradientStart/End 토큰). [selected] 면 Figma 장소카드의
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

/**
 * 추천 장소 카드. Figma("코스구성하기(카페)_기본")와 동일하게 좌측 장소 이미지 위로 우측 그라데이션
 * 패널을 얹고, 그 위에 장소명·주소(흰색)와 근접 배지를 표시한다. 그라데이션/장식은 [mood] 에 따라 달라진다.
 *
 * 기준 장소 설정 화면의 장소 카드와 동일하게, 탭하면 그 자리에서 코스에 담기고
 * (Green-700 2dp 테두리 + Click 그라데이션), 길게 누르면 장소 상세로 넘어간다.
 * (Figma: 장소카드 Default/Click 배리언트)
 *
 * [adding] 이면 담기 API 응답을 기다리는 중이라 우상단에 스피너를 노출하고 탭을 막는다.
 * 이미 담은 카드([added])의 탭은 무시한다(중복 담기 방지).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecommendCard(
    place: PlaceUiModel,
    added: Boolean,
    adding: Boolean,
    mood: CourseMood,
    onAdd: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 디자인: bg-gradient-to-b (수직, 상단 → 하단). 분위기별 + 담긴 여부별 색상.
    val gradientColors = mood.gradientColors(selected = added)
    val gradient = Brush.verticalGradient(colors = gradientColors)
    val decorationRes = mood.decorationRes()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            // 담긴 상태면 초록 테두리(Figma: Green-700, 2dp).
            .then(
                if (added) {
                    Modifier.border(2.dp, Green700, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                onClick = { if (!added && !adding) onAdd() },
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
                painter = painterResource(id = decorationRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 11.dp, bottom = 11.dp)
                    .size(60.dp),
            )
            // 담기 API 응답 대기 표시. 완료되면 테두리·그라데이션이 담긴 상태로 바뀐다.
            if (adding) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 10.dp)
                        .size(16.dp),
                    color = White,
                    strokeWidth = 2.dp,
                )
            }
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
                place.reasonText?.takeIf { it.isNotBlank() }?.let { reason ->
                    ProximityBadge(text = reason)
                }
            }
        }
    }
}

/** 흰색 pill 배지. 추천 이유(예: "현재 위치와 가까워요")를 강조 텍스트로 노출한다. (Figma: Pink-800) */
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

@Composable
private fun StatusBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun EmptyText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = Gray500,
    )
}

// ---------- Preview ----------

// @Preview 는 실제 MapView 를 렌더할 수 없으므로 지도 자리에 넣는 placeholder.
@Composable
private fun PreviewMapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Gray200),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "지도 미리보기", color = White, style = MaterialTheme.typography.bodyMedium)
    }
}

private val previewCategories = listOf(
    PlaceCategoryUiModel(1, "CAFE", "카페"),
    PlaceCategoryUiModel(2, "ACTIVITY", "액티비티"),
    PlaceCategoryUiModel(3, "RESTAURANT", "식당"),
    PlaceCategoryUiModel(4, "BAR", "술"),
)

private val previewPlaces = listOf(
    PlaceUiModel(1, "Everyday HappyBirthDay", "서울 마포구 연희로 33 3층", "카페", "연남", null, "현재 위치와 가까워요", 37.56, 126.92),
    PlaceUiModel(2, "코이크", "서울 마포구 동교로 39길 8", "카페", "연남", null, "현재 위치와 가까워요", 37.56, 126.92),
    PlaceUiModel(3, "겸사서울", "서울 마포구 성미산로 184", "카페", "연남", null, null, 37.56, 126.92),
)

@Preview(name = "코스 구성 - 추천 목록", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CourseComposeContentPreview() {
    TodaitTheme {
        CourseComposeContent(
            state = CourseComposeUiState(
                courseDraftId = 15,
                categories = previewCategories,
                selectedCategoryId = previewCategories.first().id,
                recommendState = RecommendListState.Success(previewPlaces),
                orderedPlaces = listOf(previewPlaces[1]),
            ),
            onBack = {},
            onConfirm = {},
            onSelectCategory = {},
            onPlaceLongClick = {},
            onAddPlace = {},
            onRetry = {},
            mapContent = { PreviewMapPlaceholder(it) },
        )
    }
}

@Preview(name = "코스 구성 - 선택 없음", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CourseComposeContentEmptySelectionPreview() {
    TodaitTheme {
        CourseComposeContent(
            state = CourseComposeUiState(
                courseDraftId = 15,
                categories = previewCategories,
                selectedCategoryId = previewCategories.first().id,
                recommendState = RecommendListState.Success(previewPlaces),
                orderedPlaces = emptyList(),
            ),
            onBack = {},
            onConfirm = {},
            onSelectCategory = {},
            onPlaceLongClick = {},
            onAddPlace = {},
            onRetry = {},
            mapContent = { PreviewMapPlaceholder(it) },
        )
    }
}

@Preview(name = "추천 카드", showBackground = true, widthDp = 393)
@Composable
private fun RecommendCardPreview() {
    TodaitTheme {
        Column(modifier = Modifier.background(Cream).padding(vertical = 12.dp)) {
            RecommendCard(
                place = previewPlaces[0],
                added = false,
                adding = false,
                mood = CourseMood.ROMANTIC,
                onAdd = {},
                onLongClick = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
            RecommendCard(
                place = previewPlaces[1],
                added = true,
                adding = false,
                mood = CourseMood.MODERN,
                onAdd = {},
                onLongClick = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }
    }
}

@Preview(name = "카테고리 탭", showBackground = true, widthDp = 393)
@Composable
private fun CategoryTabsPreview() {
    TodaitTheme {
        Box(modifier = Modifier.background(Cream)) {
            CategoryTabs(
                categories = previewCategories,
                selectedId = previewCategories.first().id,
                onSelect = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}
