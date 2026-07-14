
package com.umc.todait.feature.course.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.CategoryTabTextSelected
import com.umc.todait.ui.theme.CourseActiveGradientEnd
import com.umc.todait.ui.theme.CourseActiveGradientStart
import com.umc.todait.ui.theme.CourseCalmGradientEnd
import com.umc.todait.ui.theme.CourseCalmGradientStart
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseModernGradientEnd
import com.umc.todait.ui.theme.CourseModernGradientStart
import com.umc.todait.ui.theme.CourseQuietGradientEnd
import com.umc.todait.ui.theme.CourseQuietGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Green700
import com.umc.todait.ui.theme.Pink600
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 코스 구성하기 - 장소카드 선택 화면(#26, 와이어프레임 "코스구성하기(카페)_기본/선택").
 *
 * 헤더(뒤로/타이틀/✓) + 스크롤 본문[지도 + 카테고리 탭 + 추천 카드]로 구성된다.
 * 추천 카드 '+' 로 코스에 담고, 헤더 ✓(담은 장소 ≥1일 때 활성) → **선택한 장소 화면**([SelectedPlacesScreen])으로 이동한다.
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

    Box(modifier = modifier.fillMaxSize()) {
        CourseComposeContent(
            state = uiState,
            onBack = onBack,
            // ✓ 는 canConfirm(담은 장소 ≥1)일 때만 활성 → 선택한 장소 화면으로 이동.
            onConfirm = onNavigateToSelected,
            onSelectCategory = viewModel::onSelectCategory,
            onPlaceClick = { place -> onNavigateToDetail(place.placeId) },
            onAddPlace = viewModel::onAddPlace,
            onRetry = viewModel::loadRecommendations,
        )

        when (uiState.alert) {
            CourseComposeAlert.Duplicate -> BasePlaceSystemAlert(
                title = stringResource(R.string.course_compose_duplicate_title),
                description = stringResource(R.string.course_compose_duplicate_desc),
                onConfirm = viewModel::onDismissAlert,
                onCancel = viewModel::onDismissAlert,
            )

            null -> Unit
        }
    }
}

@Composable
private fun CourseComposeContent(
    state: CourseComposeUiState,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    onSelectCategory: (CourseCategory) -> Unit,
    onPlaceClick: (PlaceUiModel) -> Unit,
    onAddPlace: (PlaceUiModel) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    // 상단 지도 슬롯. 기본은 실제 카카오맵이며, @Preview 에서는 렌더 불가한 MapView 대신 placeholder 를 주입한다.
    mapContent: @Composable (Modifier) -> Unit = { mapModifier ->
        CourseMap(
            basePlace = state.basePlace,
            selectedPlaces = state.selectedPlaces,
            modifier = mapModifier,
        )
    },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        CourseComposeTopBar(
            title = stringResource(R.string.course_compose_title),
            confirmEnabled = state.canConfirm,
            onBack = onBack,
            onConfirm = onConfirm,
        )

        val selectedIds = state.selectedPlaces.map { it.placeId }.toSet()

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

            item {
                CategoryTabs(
                    selected = state.selectedCategory,
                    onSelect = onSelectCategory,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                )
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
                    itemsIndexed(recommend.places, key = { _, place -> place.placeId }) { index, place ->
                        RecommendCard(
                            place = place,
                            added = place.placeId in selectedIds,
                            // 분위기별 카드 색상. place.moodTags 로 결정하되, 추천 API 는 분위기 태그를 안 줘서
                            // 태그가 없으면 6종을 순번으로 부여(placeholder, 추천 응답에 분위기 필드 추가 시 교체 TODO).
                            mood = CourseMood.fromTags(place.moodTags)
                                ?: fallbackMoods[index % fallbackMoods.size],
                            onClick = { onPlaceClick(place) },
                            onAdd = { onAddPlace(place) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
            }
            // 선택한 장소(드래그 정렬)는 다음 화면 [SelectedPlacesScreen] 에서 처리한다.
        }
    }
}

/**
 * 상단 헤더. Figma("코스구성하기(카페)_기본"): 크림 배경 위에 뒤로가기/타이틀/확정(원형 그레이 버튼),
 * 하단 구분선(Gray-100). 타이틀은 #222(Gray-800) 20sp Medium, 가운데 정렬.
 */
@Composable
private fun CourseComposeTopBar(
    title: String,
    confirmEnabled: Boolean,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(Cream)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CircleIconButton(onClick = onBack, background = Gray600) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = title,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = Gray800,
            )
            CircleIconButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                // Figma 기본 상태는 그레이 원형 체크. 미확정 시엔 연한 그레이로 비활성 표현.
                background = if (confirmEnabled) Gray600 else Gray200,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "확정",
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLine),
        )
    }
}

@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    background: Color = Gray600,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun CategoryTabs(
    selected: CourseCategory,
    onSelect: (CourseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CourseCategory.entries.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    // Figma: 선택 Pink-600(#F9AEAC)/텍스트 Gray-700, 미선택 Gray-200/텍스트 White.
                    .background(if (isSelected) Pink600 else Gray200)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = category.label,
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
 * (Color.kt 의 Course*GradientStart/End 토큰).
 */
private fun CourseMood.gradientColors(): List<Color> = when (this) {
    CourseMood.ROMANTIC -> listOf(CourseRomanticGradientStart, CourseRomanticGradientEnd)
    CourseMood.MODERN -> listOf(CourseModernGradientStart, CourseModernGradientEnd)
    CourseMood.HIP -> listOf(CourseHipGradientStart, CourseHipGradientEnd)
    CourseMood.QUIET -> listOf(CourseQuietGradientStart, CourseQuietGradientEnd)
    CourseMood.ACTIVE -> listOf(CourseActiveGradientStart, CourseActiveGradientEnd)
    CourseMood.CALM -> listOf(CourseCalmGradientStart, CourseCalmGradientEnd)
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
 * 패널을 얹고, 그 위에 장소명·주소(흰색)와 근접 배지를 표시한다. 우상단 '+' 로 코스에 담고,
 * 담긴 상태면 초록 테두리 + 체크로 표시한다. 그라데이션/장식은 [mood] 에 따라 달라진다.
 */
@Composable
private fun RecommendCard(
    place: PlaceUiModel,
    added: Boolean,
    mood: CourseMood,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 디자인: bg-gradient-to-b (수직, 상단 → 하단). 분위기별 색상.
    val gradientColors = mood.gradientColors()
    val gradient = Brush.verticalGradient(colors = gradientColors)
    val decorationRes = mood.decorationRes()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            // 담긴 상태면 초록 테두리(Figma: Green-700).
            .then(
                if (added) {
                    Modifier.border(2.dp, Green700, RoundedCornerShape(12.dp))
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
                    .padding(end = 10.dp, bottom = 8.dp)
                    .size(60.dp),
            )
            // 우측 상단 담기 버튼. 담기면 체크, 아니면 '+'.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center,
            ) {
                if (added) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "담김",
                        tint = White,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Text(
                        text = "+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = White,
                    )
                }
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
                recommendState = RecommendListState.Success(previewPlaces),
                selectedPlaces = listOf(previewPlaces[1]),
            ),
            onBack = {},
            onConfirm = {},
            onSelectCategory = {},
            onPlaceClick = {},
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
                recommendState = RecommendListState.Success(previewPlaces),
                selectedPlaces = emptyList(),
            ),
            onBack = {},
            onConfirm = {},
            onSelectCategory = {},
            onPlaceClick = {},
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
                mood = CourseMood.ROMANTIC,
                onClick = {},
                onAdd = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
            RecommendCard(
                place = previewPlaces[1],
                added = true,
                mood = CourseMood.MODERN,
                onClick = {},
                onAdd = {},
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
                selected = CourseCategory.CAFE,
                onSelect = {},
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}
