package com.umc.todait.feature.home.coursedetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.umc.todait.R
import com.umc.todait.feature.home.MOOD_DECORATIONS
import com.umc.todait.feature.home.MOOD_GRADIENTS
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.component.CourseSaveDialog
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.CourseCalmGradientEnd
import com.umc.todait.ui.theme.CourseCalmGradientStart
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Error
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 홈 "오늘의 추천 코스" 카드 탭 → 진입하는 추천 코스 상세 화면(#55, feature/home 소유).
 * 지니 담당 저장 코스 상세(feature/saved.CourseDetailScreen)와는 별도 화면이지만,
 * 저장 확인/완료 알럿은 티아가 만든 [CommonDialog]/[CourseSaveDialog](ui/component, #43)를 그대로 재사용한다.
 */
@Composable
fun RecommendedCourseDetailScreen(
    onBack: () -> Unit,
    onNavigateToPlaceDetail: (Long) -> Unit,
    onNavigateToSavedCourses: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecommendedCourseDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RecommendedCourseDetailEffect.NavigateToSavedCourses -> onNavigateToSavedCourses()
            }
        }
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

    RecommendedCourseDetailContent(
        uiState = uiState,
        onBack = onBack,
        onPlaceClick = onNavigateToPlaceDetail,
        onSaveClick = viewModel::onSaveClick,
        onRetry = viewModel::loadDetail,
        modifier = modifier,
    )
}

@Composable
private fun RecommendedCourseDetailContent(
    uiState: RecommendedCourseDetailUiState,
    onBack: () -> Unit,
    onPlaceClick: (Long) -> Unit,
    onSaveClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(Cream)) {
        CourseDetailTopBar(title = uiState.title, onBack = onBack)

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            uiState.loadError != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.course_detail_load_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500,
                    )
                    OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.course_detail_retry))
                    }
                }
            }

            else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                RecommendedCourseMap(
                    places = uiState.places,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.course_detail_place_count, uiState.placeCount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Gray900,
                        modifier = Modifier.weight(1f),
                    )
                    SaveCoursePillButton(onClick = onSaveClick, enabled = !uiState.isSaving)
                }

                if (uiState.hashtags.isNotEmpty()) {
                    Text(
                        text = uiState.hashtags.joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }

                if (uiState.saveError != null) {
                    Text(
                        text = uiState.saveError,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                }

                val (cardGradientStart, cardGradientEnd) = MOOD_GRADIENTS[uiState.moodTagCode]
                    ?: (CourseCalmGradientStart to CourseCalmGradientEnd)
                val cardDecorationRes = MOOD_DECORATIONS[uiState.moodTagCode] ?: R.drawable.ic_mood_calm
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    uiState.places.forEach { place ->
                        CourseDetailPlaceRow(
                            place = place,
                            isLast = place == uiState.places.last(),
                            gradient = listOf(cardGradientStart, cardGradientEnd),
                            decorationRes = cardDecorationRes,
                            // 원본 장소가 삭제 등으로 placeId 가 없으면(스냅샷만 존재) 상세 이동 비활성.
                            onClick = place.placeId?.let { placeId -> { onPlaceClick(placeId) } },
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CourseDetailTopBar(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Cream)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(White)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_back_button),
                    contentDescription = stringResource(R.string.course_detail_back_content_description),
                )
            }
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )
        }
        HorizontalDivider(color = DividerLine)
    }
}

@Composable
private fun SaveCoursePillButton(onClick: () -> Unit, enabled: Boolean) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Pink900)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(
            text = stringResource(R.string.course_detail_save_button),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = White,
        )
    }
}

/**
 * 코스 장소 순번 핀을 찍은 카카오맵.
 * 핀 아이콘은 [com.umc.todait.feature.course.compose.CourseMap]과 동일하게 임시 아이콘을 쓴다
 * (번호 배지 아이콘은 디자인 확정 후 교체 — 코스 구성 화면 쪽과 동일한 TODO. feature 간 참조 금지라
 * CourseMap을 직접 재사용하지 않고 동일 패턴으로 별도 구현한다).
 */
@Composable
private fun RecommendedCourseMap(places: List<CourseDetailPlaceUiModel>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(error: Exception?) {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                        }
                    },
                )
            }
        },
    )

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.finish()
        }
    }

    LaunchedEffect(kakaoMap, places) {
        val map = kakaoMap ?: return@LaunchedEffect
        val labelLayer = map.labelManager?.layer
        labelLayer?.removeAll()
        val styles = map.labelManager?.addLabelStyles(
            LabelStyles.from(LabelStyle.from(R.drawable.ic_place_deco_cloud)),
        )
        places.forEach { place ->
            labelLayer?.addLabel(
                LabelOptions.from(LatLng.from(place.latitude, place.longitude)).setStyles(styles),
            )
        }
        val center = places.firstOrNull()
        val target = if (center != null) LatLng.from(center.latitude, center.longitude) else DEFAULT_CENTER
        map.moveCamera(CameraUpdateFactory.newCenterPosition(target, DEFAULT_ZOOM))
    }
}

private val DEFAULT_CENTER: LatLng = LatLng.from(37.5563, 126.9236)
private const val DEFAULT_ZOOM = 15
private val PLACE_ROW_HEIGHT = 88.dp
private val PLACE_THUMBNAIL_WIDTH = 96.dp

@Composable
private fun CourseDetailPlaceRow(
    place: CourseDetailPlaceUiModel,
    isLast: Boolean,
    gradient: List<Color>,
    @DrawableRes decorationRes: Int,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 8.dp),
    ) {
        Column(
            modifier = Modifier.width(28.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Pink900),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = place.visitOrder.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = White,
                )
            }
            if (!isLast) {
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(Pink900.copy(alpha = 0.3f)),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(PLACE_ROW_HEIGHT)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = onClick != null, onClick = onClick ?: {})
                .background(Brush.horizontalGradient(gradient)),
        ) {
            Image(
                painter = painterResource(decorationRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
                    .size(48.dp)
                    .alpha(0.9f),
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaceThumbnail(imageUrl = place.imageUrl)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                ) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceThumbnail(imageUrl: String?) {
    // 부모 Box가 카드 모서리를 이미 clip 하므로 여기서는 따로 clip/모서리를 주지 않는다(왼쪽을 꽉 채움).
    val modifier = Modifier.fillMaxHeight().width(PLACE_THUMBNAIL_WIDTH)
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier.background(White.copy(alpha = 0.4f)))
    }
}

@Preview(showBackground = true, name = "추천 코스 상세", widthDp = 393, heightDp = 852)
@Composable
private fun RecommendedCourseDetailScreenPreview() {
    val sample = RecommendedCourseDetailUiState(
        isLoading = false,
        title = "연남 데이트 코스",
        hashtags = listOf("#낭만적인", "#베이커리카페"),
        moodTagCode = "ROMANTIC",
        places = listOf(
            CourseDetailPlaceUiModel(101, 21, 1, "더 파이브올스", "서울 마포구 와우산로13길 40", null, 37.5521, 126.9214),
            CourseDetailPlaceUiModel(102, 22, 2, "연남 카페", "서울 마포구 동교로 241", null, 37.5612, 126.9248),
            CourseDetailPlaceUiModel(103, 23, 3, "연남 공방", "서울 마포구 성미산로 152", null, 37.5623, 126.9259),
            CourseDetailPlaceUiModel(104, 24, 4, "연남 와인바", "서울 마포구 동교로38길 27", null, 37.5631, 126.9272),
        ),
    )
    TodaitTheme {
        RecommendedCourseDetailContent(
            uiState = sample,
            onBack = {},
            onPlaceClick = {},
            onSaveClick = {},
            onRetry = {},
        )
    }
}
