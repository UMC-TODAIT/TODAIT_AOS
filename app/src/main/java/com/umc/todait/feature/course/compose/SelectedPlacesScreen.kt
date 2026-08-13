package com.umc.todait.feature.course.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.todait.R
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.data.dto.CourseDraftStatus
import com.umc.todait.ui.component.ScreenTopBar
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray350
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink100
import com.umc.todait.ui.theme.Pink800
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
 * - 리스트 = [CourseComposeUiState.orderedPlaces] (기준 장소 포함). 기준 장소도 다른 장소와 완전히
 *   같은 행이고, 이름 옆 "기준장소" 칩만 더 붙는다(Figma "코스구성하기_드래그수정" node 534-13891).
 *   "선택한 장소 (N)" 의 N 도 기준 장소를 포함한 전체 개수다.
 * - 모든 행은 핸들(흰 점 6개)을 잡아 드래그하면 순서가 바뀐다
 *   ([CourseComposeViewModel.onMovePlace], sh.calvin.reorderable).
 * - 헤더 ✓ → 코스 저장([onNavigateToSave]).
 *
 * ⚠️ 서버는 아직 기준 장소를 placeRole=BASE / visitOrder 1 고정으로 다루고, 순서 변경 API 명세도
 * "기준 장소 제외 + visitOrder 2부터"다. 기준 장소를 옮긴 순서를 저장하려면 서버가 그 형태를 받아야 한다.
 * 앱은 옮긴 순서를 그대로 보내고(자세한 건 [CourseComposeViewModel.onOrderConfirmed]),
 * 서버가 거부하면 실패 메시지를 그대로 띄운다.
 */
@Composable
fun SelectedPlacesScreen(
    onNavigateToSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseComposeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 코스 저장 화면으로는 순서 저장(PATCH .../places/order) + 저장 단계 전환(PATCH .../saving)이
    // 성공한 뒤에만 넘어간다.
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CourseComposeEffect.NavigateToSave -> onNavigateToSave()
                CourseComposeEffect.NavigateToSelected -> Unit
                // 이전 버튼은 단계 이동 API 를 먼저 부르므로 화면 이동도 ViewModel 이 알려줄 때 한다.
                CourseComposeEffect.NavigateBack -> onBack()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream),
        ) {
            ScreenTopBar(
                title = stringResource(R.string.course_compose_title),
                // 이 화면은 ORDERING 단계 → 장소 선택으로 되돌린다.
                onBack = { viewModel.onBackClick(CourseDraftStatus.ORDERING) },
                onConfirm = viewModel::onOrderConfirmed,
                confirmContentDescription = "확정",
            )

            val lazyListState = rememberLazyListState()
            // 드래그로 위치가 바뀌면 PlaceUiModel.key 로 orderedPlaces 인덱스를 찾아 onMovePlace 호출.
            // 기준 장소도 같은 목록에 있어 다른 장소와 똑같이 옮겨진다.
            val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
                val places = viewModel.uiState.value.orderedPlaces
                val fromIdx = places.indexOfFirst { it.key == from.key }
                val toIdx = places.indexOfFirst { it.key == to.key }
                if (fromIdx in places.indices && toIdx in places.indices) {
                    viewModel.onMovePlace(fromIdx, toIdx)
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    Spacer(Modifier.height(22.dp))
                    CourseMap(
                        places = uiState.orderedPlaces,
                        basePlaceKey = uiState.basePlaceKey,
                        currentLocation = uiState.currentLocation,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                    )
                }
                item {
                    // Figma: 지도 아래 12 → 제목(22 SemiBold, #222) → 4 → 안내(16 SemiBold, Gray-500) → 16 → 목록
                    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)) {
                        Text(
                            // 개수는 기준 장소를 포함한 코스 전체 장소 수.
                            text = stringResource(
                                R.string.course_compose_selected_section,
                                uiState.orderedPlaces.size,
                            ),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Gray800,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.course_compose_selected_desc),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray500,
                        )
                    }
                }
                // 기준 장소를 포함한 코스 전체. 전부 같은 행이고 전부 드래그로 순서를 바꿀 수 있다.
                itemsIndexed(uiState.orderedPlaces, key = { _, place -> place.key }) { index, place ->
                    ReorderableItem(reorderableState, key = place.key) { isDragging ->
                        SelectedPlaceRow(
                            place = place,
                            isBase = place.key == uiState.basePlaceKey,
                            order = index + 1,
                            isDragging = isDragging,
                            handleModifier = Modifier.draggableHandle(),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        // 순서 저장/저장 화면 진입 실패 안내.
        uiState.submitError?.let { message ->
            CommonDialog(
                title = stringResource(R.string.course_compose_submit_error_title) + "\n" + message,
                onConfirm = viewModel::onDismissSubmitError,
                onDismiss = viewModel::onDismissSubmitError,
            )
        }
    }
}

/**
 * 선택한 장소 행(Figma: Pink-100 카드). 좌측 드래그 핸들 + 장소명/주소 + 우측 순번 배지.
 *
 * 기준 장소도 같은 행을 쓰고 배지에 순번([order], 1)을 그대로 노출한다.
 * 다른 점은 이름 옆의 "기준장소" 칩([isBase])뿐이다.
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
            // Figma: 83dp 고정이지만 글자 크기가 커지면 늘어나도록 최소 높이로 잡는다.
            .heightIn(min = 83.dp)
            .shadow(if (isDragging) 6.dp else 0.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Pink100)
            .padding(start = 11.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 드래그 핸들(흰 점 6개). handleModifier(draggableHandle)를 잡아 드래그를 시작한다.
        Image(
            painter = painterResource(id = R.drawable.ic_drag_handle),
            contentDescription = "순서 변경 핸들",
            modifier = handleModifier
                .width(11.dp)
                .height(17.dp),
        )
        // Figma: 핸들과 장소명 사이 27.
        Spacer(Modifier.size(27.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Gray800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                // 기준 장소 표시는 순번 배지가 아니라 이름 옆 칩으로 한다. (Figma node 1678-10775)
                if (isBase) {
                    Spacer(Modifier.size(6.dp))
                    BasePlaceChip()
                }
            }
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = Gray350,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(12.dp))
        // 우측 순번 배지(지도 핀 번호와 대응). 기준 장소도 동일하게 숫자를 노출한다.
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = order.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
        }
    }
}

/** 기준 장소 행 이름 옆의 "기준장소" 칩. 흰 pill + Pink-800 텍스트. (Figma node 1678-10775) */
@Composable
private fun BasePlaceChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(White)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = stringResource(R.string.course_compose_base_place_chip),
            fontSize = 10.sp,
            color = Pink800,
            maxLines = 1,
        )
    }
}

@Preview(name = "선택한 장소 (드래그)", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun SelectedPlacesRowsPreview() {
    val sample = listOf(
        PlaceUiModel(1, "뀌노이", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
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
