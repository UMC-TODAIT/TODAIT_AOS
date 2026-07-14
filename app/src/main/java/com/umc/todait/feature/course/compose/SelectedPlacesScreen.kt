package com.umc.todait.feature.course.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.umc.todait.R
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink100
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 코스 구성하기 - 선택한 장소 화면(#26, 와이어프레임 "코스구성하기_드래그수정").
 *
 * [CourseComposeScreen] 에서 장소를 담고 ✓ 를 누르면 진입하는 두 번째 단계.
 * 헤더(뒤로/타이틀/✓) + 지도 + "선택한 장소 (N)" 드래그 정렬 리스트로 구성된다.
 * 선택 상태는 상위 그래프 스코프 [CourseComposeViewModel] 을 [CourseComposeScreen] 과 공유한다.
 *
 * - 리스트 = 기준 장소(로고 배지, 고정) + 담은 장소들(순번 배지 2·3·4…, 드래그로 순서 변경). 순서 = 코스 동선.
 * - 담은 장소는 ≡ 핸들을 잡아 드래그하면 순서가 바뀐다([CourseComposeViewModel.onMovePlace], sh.calvin.reorderable).
 * - 헤더 ✓ → 코스 저장([onNavigateToSave]).
 *
 * ⚠️ 기준 장소 배지 아이콘은 임시(ic_place_deco_1) — 실제 todait 로고 마크로 교체 예정.
 */
@Composable
fun SelectedPlacesScreen(
    onNavigateToSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseComposeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        SelectedPlacesTopBar(
            title = stringResource(R.string.course_compose_title),
            onBack = onBack,
            onConfirm = onNavigateToSave,
        )

        // 기준 장소(있으면)는 고정, 담은 장소들만 드래그로 순서 변경.
        val hasBase = uiState.basePlace != null
        val baseOffset = if (hasBase) 1 else 0

        val lazyListState = rememberLazyListState()
        // 드래그로 위치가 바뀌면 placeId(key)로 selectedPlaces 인덱스를 찾아 onMovePlace 호출.
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val selected = viewModel.uiState.value.selectedPlaces
            val fromIdx = selected.indexOfFirst { it.placeId == from.key }
            val toIdx = selected.indexOfFirst { it.placeId == to.key }
            if (fromIdx in selected.indices && toIdx in selected.indices) {
                viewModel.onMovePlace(fromIdx, toIdx)
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                CourseMap(
                    basePlace = uiState.basePlace,
                    selectedPlaces = uiState.selectedPlaces,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                )
            }
            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                    Text(
                        text = stringResource(R.string.course_compose_selected_section, uiState.selectedPlaces.size),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Gray900,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.course_compose_selected_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                }
            }
            // 기준 장소(고정, 드래그 불가).
            uiState.basePlace?.let { base ->
                item(key = "base_place") {
                    SelectedPlaceRow(
                        place = base,
                        isBase = true,
                        order = 1,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }
            }
            // 담은 장소(드래그로 순서 변경). 배지 순번은 기준 장소가 있으면 2부터.
            itemsIndexed(uiState.selectedPlaces, key = { _, place -> place.placeId }) { index, place ->
                ReorderableItem(reorderableState, key = place.placeId) { isDragging ->
                    SelectedPlaceRow(
                        place = place,
                        isBase = false,
                        order = index + 1 + baseOffset,
                        isDragging = isDragging,
                        handleModifier = Modifier.draggableHandle(),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedPlacesTopBar(
    title: String,
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
            CircleButton(onClick = onBack) {
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
            CircleButton(onClick = onConfirm) {
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
private fun CircleButton(
    onClick: () -> Unit,
    background: Color = Gray600,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * 선택한 장소 행(와이어프레임: Pink-100 카드). 좌측 드래그 핸들 + 장소명/주소 + 우측 배지.
 * 배지는 기준 장소면 로고, 아니면 순번([order]).
 *
 * [handleModifier] 는 ≡ 핸들에 붙는 드래그 핸들 modifier(ReorderableItem 스코프의 draggableHandle).
 * 기준 장소 등 드래그 불가 행은 기본값(Modifier)으로 핸들만 표시한다.
 */
@Composable
private fun SelectedPlaceRow(
    place: PlaceUiModel,
    isBase: Boolean,
    order: Int,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    handleModifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isDragging) 6.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Pink100)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 드래그 핸들(≡). 담은 장소 행에서는 handleModifier(draggableHandle)로 드래그를 시작한다.
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = "순서 변경 핸들",
            tint = Gray500,
            modifier = handleModifier.size(20.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Gray900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(12.dp))
        // 우측 배지(지도 핀과 대응): 기준 장소=로고, 나머지=순번.
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(White),
            contentAlignment = Alignment.Center,
        ) {
            if (isBase) {
                Image(
                    painter = painterResource(id = R.drawable.ic_place_deco_1),
                    contentDescription = "기준 장소",
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Text(
                    text = order.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Gray900,
                )
            }
        }
    }
}

@Preview(name = "선택한 장소 (드래그)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SelectedPlacesRowsPreview() {
    val sample = listOf(
        PlaceUiModel(1, "꿔노이", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
        PlaceUiModel(2, "코이크", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
        PlaceUiModel(3, "121르말뒤페이", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
    )
    TodaitTheme {
        Column(modifier = Modifier.background(Cream).padding(vertical = 12.dp)) {
            SelectedPlaceRow(place = sample[0], isBase = true, order = 1, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
            SelectedPlaceRow(place = sample[1], isBase = false, order = 2, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
            SelectedPlaceRow(place = sample[2], isBase = false, order = 3, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        }
    }
}
