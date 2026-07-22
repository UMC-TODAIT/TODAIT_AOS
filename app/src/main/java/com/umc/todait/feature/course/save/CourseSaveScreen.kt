package com.umc.todait.feature.course.save

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import com.umc.todait.R
import com.umc.todait.feature.course.base_place.PlaceUiModel
import com.umc.todait.feature.course.compose.CourseMood
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.component.CourseSaveDialog
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
import com.umc.todait.ui.theme.Gray350
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Green500
import com.umc.todait.ui.theme.Pink500
import com.umc.todait.ui.theme.TextPlaceholder
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 코스 저장 화면(#7, Figma "코스저장" node 534:13926).
 *
 * [com.umc.todait.feature.course.compose.SelectedPlacesScreen] 에서 ✓ 를 누르면 진입하는 마지막 단계.
 * 헤더(뒤로/타이틀/✓) + 이름 · 메모 · 태그 입력 + 경로 미리보기로 구성된다.
 *
 * - 이름은 필수. 미입력 상태로 ✓ 를 누르면 안내 알럿(CommonDialog)이 뜬다([CourseSaveViewModel.onSave]).
 * - 태그는 '+' 로 바텀시트를 열어 프리셋 분위기 6종 중 골라 추가한다(선택 시 배경이 그라데이션으로 토글).
 * - [places] 는 경로 미리보기용 코스 순서(기준 장소 + 담은 장소)로, 코스 구성 그래프의 공유
 *   ViewModel 에서 받아 넘긴다. 입력 상태만 [viewModel] 이 소유한다.
 *
 * 시안 상단의 Pink-100 밴드와 하단 GNB 는 이 화면이 아니라 [com.umc.todait.navigation.TodaitApp] 의
 * Scaffold 가 그린다(TopBar 는 전역, BottomBar 는 탭 라우트에서만 — 코스 생성 플로우에서는 숨김).
 *
 * ⚠️ 코스 저장 API 미정으로 ✓ 는 검증 후 완료 다이얼로그만 띄운다. (CourseSaveViewModel 의 TODO)
 */
@Composable
fun CourseSaveScreen(
    places: List<PlaceUiModel>,
    onNavigateToSavedCourses: () -> Unit,
    onNavigateToHome: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseSaveViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CourseSaveEffect.NavigateToSavedCourses -> onNavigateToSavedCourses()
                CourseSaveEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    if (uiState.isNameErrorDialogVisible) {
        CommonDialog(
            title = stringResource(R.string.course_save_name_required),
            onConfirm = viewModel::onDismissNameErrorDialog,
            onDismiss = viewModel::onDismissNameErrorDialog,
        )
    }

    if (uiState.isSaveConfirmDialogVisible) {
        CommonDialog(
            title = stringResource(R.string.course_save_confirm_message),
            onConfirm = viewModel::onConfirmSave,
            onDismiss = viewModel::onDismissSaveConfirm,
        )
    }

    if (uiState.isSavedDialogVisible) {
        CourseSaveDialog(
            onMoveClick = viewModel::onMoveToSavedCourses,
            onSkipClick = viewModel::onSkipSavedDialog,
        )
    }

    if (uiState.isTagSheetVisible) {
        TagBottomSheet(
            draftTags = uiState.draftTags,
            onToggle = viewModel::onToggleTag,
            onConfirm = viewModel::onConfirmTags,
            onDismiss = viewModel::onDismissTagSheet,
        )
    }

    CourseSaveContent(
        uiState = uiState,
        places = places,
        onNameChange = viewModel::onNameChange,
        onMemoChange = viewModel::onMemoChange,
        onStartAddTag = viewModel::onStartAddTag,
        onSave = viewModel::onSave,
        onBack = onBack,
        modifier = modifier,
    )
}

/** 상태를 주입받는 순수 UI. Preview 와 화면이 공유한다. */
@Composable
private fun CourseSaveContent(
    uiState: CourseSaveUiState,
    places: List<PlaceUiModel>,
    onNameChange: (String) -> Unit,
    onMemoChange: (String) -> Unit,
    onStartAddTag: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        CourseSaveTopBar(
            title = stringResource(R.string.course_save_title),
            onBack = onBack,
            onSave = onSave,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SCREEN_PADDING)
                .padding(top = 28.dp, bottom = 40.dp),
        ) {
            SectionLabel(stringResource(R.string.course_save_section_name))
            Spacer(Modifier.height(LABEL_GAP))
            CourseNameField(
                value = uiState.name,
                onValueChange = onNameChange,
            )

            Spacer(Modifier.height(SECTION_GAP))
            SectionLabel(stringResource(R.string.course_save_section_memo))
            Spacer(Modifier.height(LABEL_GAP))
            CourseMemoField(
                value = uiState.memo,
                onValueChange = onMemoChange,
            )

            Spacer(Modifier.height(SECTION_GAP))
            SectionLabel(stringResource(R.string.course_save_section_tag))
            Spacer(Modifier.height(LABEL_GAP))
            CourseTagRow(
                tags = uiState.orderedTags,
                onStartAddTag = onStartAddTag,
            )

            Spacer(Modifier.height(SECTION_GAP))
            SectionLabel(stringResource(R.string.course_save_section_route))
            Spacer(Modifier.height(LABEL_GAP))
            CourseRoutePreview(places = places)
        }
    }
}

/** 헤더: 좌측 뒤로가기, 가운데 타이틀, 우측 저장(✓). 아래 구분선까지 포함한다. */
@Composable
private fun CourseSaveTopBar(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(Cream)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 11.dp),
        ) {
            HeaderCircleButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onBack,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_common_chevron_left),
                    contentDescription = stringResource(R.string.course_save_back),
                    modifier = Modifier.height(16.dp),
                )
            }
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
                color = Gray800,
            )
            HeaderCircleButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onSave,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_common_check),
                    contentDescription = stringResource(R.string.course_save_confirm),
                    modifier = Modifier.width(16.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SCREEN_PADDING)
                .height(1.dp)
                .background(DividerLine),
        )
    }
}

/** 시안의 헤더 버튼: Cream 원 + Gray-200 테두리 (node 1678:9035). */
@Composable
private fun HeaderCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Cream)
            .border(1.dp, Gray200, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = Gray800,
    )
}

/**
 * 이름 입력(pill 형태, 1줄).
 * Material 의 TextField 는 자체 최소 높이·패딩이 있어 시안의 53dp/28dp 패딩과 어긋나므로
 * [BasicTextField] 에 컨테이너를 직접 그린다. (메모 입력도 동일)
 */
@Composable
private fun CourseNameField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    FieldContainer(
        modifier = Modifier.height(53.dp),
        shape = RoundedCornerShape(50.dp),
        contentPadding = Modifier.padding(horizontal = 28.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        PlaceholderTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(R.string.course_save_name_placeholder),
            singleLine = true,
        )
    }
}

/** 메모 입력(여러 줄, 상단 정렬). */
@Composable
private fun CourseMemoField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    FieldContainer(
        modifier = Modifier.height(140.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = Modifier.padding(horizontal = 27.dp, vertical = 18.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        PlaceholderTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(R.string.course_save_memo_placeholder),
            singleLine = false,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** 흰 배경 + Gray-200 테두리 입력 컨테이너. */
@Composable
private fun FieldContainer(
    shape: RoundedCornerShape,
    contentPadding: Modifier,
    contentAlignment: Alignment,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White)
            .border(1.dp, Gray200, shape)
            .then(contentPadding),
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}

/** placeholder 를 직접 겹쳐 그리는 12sp 입력 필드. */
@Composable
private fun PlaceholderTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodySmall,
                color = TextPlaceholder,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall.copy(color = Gray800),
            singleLine = singleLine,
            cursorBrush = SolidColor(Pink500),
        )
    }
}

/** 선택된 분위기 태그 칩 목록 + '+' 추가 버튼. 칩은 무드 그라데이션 배경으로 노출한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseTagRow(
    tags: List<CourseMood>,
    onStartAddTag: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tags.forEach { mood ->
            MoodTagChip(mood = mood, selected = true)
        }
        AddTagButton(onClick = onStartAddTag)
    }
}

/**
 * 분위기 태그 칩. [selected] 이면 무드별 그라데이션, 아니면 회색(Gray-200) 배경.
 * [showSelectionBorder] 이면 선택된 칩에 초록 테두리(Green-500)를 그린다(시트의 선택 표시용).
 * [onClick] 이 있으면 탭으로 선택을 토글한다(바텀시트). 없으면 표시 전용(메인 화면).
 */
@Composable
private fun MoodTagChip(
    mood: CourseMood,
    selected: Boolean,
    onClick: (() -> Unit)? = null,
    showSelectionBorder: Boolean = false,
) {
    val background =
        if (selected) Modifier.background(Brush.horizontalGradient(mood.tagGradientColors()))
        else Modifier.background(Gray200)
    val selectionBorder =
        if (selected && showSelectionBorder) Modifier.border(2.dp, Green500, CircleShape)
        else Modifier
    Box(
        modifier = Modifier
            .height(TAG_CHIP_HEIGHT)
            .clip(CircleShape)
            .then(background)
            .then(selectionBorder)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.course_save_tag_prefix, mood.label),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 태그 추가 바텀시트 (Figma node 1243-6057 "코스저장_태그추가").
 * 헤더(X / 타이틀 / ✓) + 안내 문구 + 프리셋 6종 그리드. 태그를 탭하면 배경색이 토글된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagBottomSheet(
    draftTags: Set<CourseMood>,
    onToggle: (CourseMood) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
        ) {
            // 헤더: 좌측 X(닫기) / 가운데 타이틀 / 우측 ✓(확인)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SCREEN_PADDING, vertical = 16.dp),
            ) {
                HeaderCircleButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = onDismiss,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_common_close),
                        contentDescription = stringResource(R.string.course_save_tag_sheet_close),
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.course_save_tag_sheet_title),
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    color = Gray800,
                )
                HeaderCircleButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onConfirm,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_common_check),
                        contentDescription = stringResource(R.string.course_save_tag_sheet_confirm),
                        modifier = Modifier.width(16.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.course_save_tag_sheet_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SCREEN_PADDING),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = Gray400,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SCREEN_PADDING)
                    .height(1.dp)
                    .background(DividerLine),
            )
            Spacer(Modifier.height(24.dp))
            // 고정 2열 그리드(좌: 로맨틱·활발한·모던한 / 우: 힙한·조용한·차분한). 열마다 좌측 정렬되어
            // 열 안의 칩 왼쪽 끝이 나란히 맞는다(Figma node 1243-6988).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SCREEN_PADDING),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                for (column in 0..1) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CourseSaveUiState.TAG_PRESETS
                            .filterIndexed { index, _ -> index % 2 == column }
                            .forEach { mood ->
                                MoodTagChip(
                                    mood = mood,
                                    selected = mood in draftTags,
                                    onClick = { onToggle(mood) },
                                    showSelectionBorder = true,
                                )
                            }
                    }
                }
            }
        }
    }
}

/** 태그 추가 '+' 버튼 (node 1243:6175). */
@Composable
private fun AddTagButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(TAG_CHIP_HEIGHT)
            .clip(CircleShape)
            .background(Cream)
            .border(1.dp, Gray200, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_course_tag_add),
            contentDescription = stringResource(R.string.course_save_tag_add),
            modifier = Modifier.size(16.dp),
        )
    }
}

/**
 * 경로 미리보기 카드. 코스 순서대로 [places] 를 나열하고,
 * 좌측 거터에 순서 점(Pink-500)과 다음 점으로 이어지는 그라데이션 연결선을 그린다.
 */
@Composable
private fun CourseRoutePreview(places: List<PlaceUiModel>) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White)
            .border(1.dp, Gray200, shape)
            .padding(start = 24.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
    ) {
        places.forEachIndexed { index, place ->
            RoutePreviewRow(
                place = place,
                isLast = index == places.lastIndex,
            )
        }
    }
}

@Composable
private fun RoutePreviewRow(
    place: PlaceUiModel,
    isLast: Boolean,
) {
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        // 거터: 순서 점 + 다음 점으로 이어지는 연결선(위가 투명 → 아래가 Pink-500).
        Column(
            modifier = Modifier
                .width(ROUTE_DOT_SIZE)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(ROUTE_DOT_SIZE)
                    .clip(CircleShape)
                    .background(Pink500),
            )
            if (!isLast) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(Pink500.copy(alpha = 0f), Pink500),
                            ),
                        ),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 24.dp),
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.address,
                style = MaterialTheme.typography.bodySmall,
                color = Gray350,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val SCREEN_PADDING = 20.dp
private val SECTION_GAP = 24.dp
private val LABEL_GAP = 12.dp
private val TAG_CHIP_HEIGHT = 40.dp
private val ROUTE_DOT_SIZE = 11.dp

/**
 * 분위기별 태그 그라데이션(가로). 색상 토큰은 Color.kt 로, 코스 구성 화면의 추천 카드 그라데이션과 동일 값.
 */
private fun CourseMood.tagGradientColors(): List<Color> = when (this) {
    CourseMood.HIP -> listOf(CourseHipGradientStart, CourseHipGradientEnd)
    CourseMood.QUIET -> listOf(CourseQuietGradientStart, CourseQuietGradientEnd)
    CourseMood.ACTIVE -> listOf(CourseActiveGradientStart, CourseActiveGradientEnd)
    CourseMood.ROMANTIC -> listOf(CourseRomanticGradientStart, CourseRomanticGradientEnd)
    CourseMood.MODERN -> listOf(CourseModernGradientStart, CourseModernGradientEnd)
    CourseMood.CALM -> listOf(CourseCalmGradientStart, CourseCalmGradientEnd)
}

@Preview(name = "코스 저장", showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun CourseSaveScreenPreview() {
    val sample = listOf(
        PlaceUiModel(1, "뀌노이", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
        PlaceUiModel(2, "코이크", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
        PlaceUiModel(3, "121뤼말뒤페이", "서울 마포구 연남동 383-37", "카페", "연남", null, null, 37.56, 126.92),
        PlaceUiModel(4, "더바이브올스", "서울 마포구 연남동 383-37", "바", "연남", null, null, 37.56, 126.92),
    )
    TodaitTheme {
        CourseSaveContent(
            uiState = CourseSaveUiState(selectedTags = setOf(CourseMood.HIP, CourseMood.ROMANTIC)),
            places = sample,
            onNameChange = {},
            onMemoChange = {},
            onStartAddTag = {},
            onSave = {},
            onBack = {},
        )
    }
}
