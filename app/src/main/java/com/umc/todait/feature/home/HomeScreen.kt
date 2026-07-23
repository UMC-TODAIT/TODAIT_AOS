package com.umc.todait.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.CourseHipGradientEnd
import com.umc.todait.ui.theme.CourseHipGradientStart
import com.umc.todait.ui.theme.CourseRomanticGradientEnd
import com.umc.todait.ui.theme.CourseRomanticGradientStart
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.HomePlaceGreenEnd
import com.umc.todait.ui.theme.HomePlaceGreenStart
import com.umc.todait.ui.theme.HomePlaceMintEnd
import com.umc.todait.ui.theme.HomePlaceMintStart
import com.umc.todait.ui.theme.Pink400
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 홈 화면(라우트 진입점). ViewModel의 상태를 구독한다.
 * "오늘의 추천 코스" 카드 탭 → 코스 상세, 상단 알림/프로필 아이콘은 각각 상위(NavHost)로 위임한다.
 */
@Composable
fun HomeScreen(
    onCourseClick: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onCourseClick = onCourseClick,
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onRetryCourses = viewModel::loadRecommendedCourses,
        onRetryPlaces = viewModel::loadRecommendedPlaces,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onCourseClick: (Long) -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRetryCourses: () -> Unit,
    onRetryPlaces: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .verticalScroll(rememberScrollState()),
    ) {
        HomeTopBar(onNotificationClick = onNotificationClick, onProfileClick = onProfileClick)

        Spacer(Modifier.height(25.dp))

        Text(
            text = stringResource(R.string.home_greeting),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray900,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(28.dp))
        SectionHeader(
            iconRes = R.drawable.ic_saved_courses_fire,
            title = stringResource(R.string.home_section_courses_title),
            subtitle = stringResource(R.string.home_section_courses_subtitle),
        )
        Spacer(Modifier.height(16.dp))
        HomeCoursesSection(coursesState = uiState.coursesState, onCourseClick = onCourseClick, onRetry = onRetryCourses)

        Spacer(Modifier.height(32.dp))
        SectionHeader(
            icon = { DiamondIcon() },
            title = stringResource(R.string.home_section_places_title),
            subtitle = stringResource(R.string.home_section_places_subtitle),
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HomePlacesSection(placesState = uiState.placesState, onRetry = onRetryPlaces)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HomeTopBar(onNotificationClick: () -> Unit, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_todait_logo),
            contentDescription = null,
            modifier = Modifier.size(30.dp),
        )
        Spacer(Modifier.width(6.dp))
        Image(
            painter = painterResource(R.drawable.ic_todait_wordmark),
            contentDescription = null,
            modifier = Modifier.height(23.dp),
        )
        Spacer(Modifier.weight(1f))
        NotificationIconButton(onClick = onNotificationClick)
        Spacer(Modifier.width(10.dp))
        HeaderIconButton(
            iconRes = R.drawable.ic_my_page_profile,
            contentDescription = stringResource(R.string.home_profile_content_description),
            onClick = onProfileClick,
        )
    }
}

/**
 * 알림 아이콘(종) + 핑크 알림 표시(오른쪽 위).
 * 지금은 표시가 항상 노출된다(피그마 기준). 안읽음 알림이 있을 때만 노출할지는 디자인 확인 후 조건부로 바꾼다.
 */
@Composable
private fun NotificationIconButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_home_notification),
            contentDescription = stringResource(R.string.home_notification_content_description),
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-1).dp, y = 1.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Pink400),
        )
    }
}

@Composable
private fun HeaderIconButton(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Gray600)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    iconRes: Int? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                icon?.invoke()
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
        )
    }
}

/** "취향 기반 추천 장소" 섹션 타이틀 아이콘. 마름모.svg는 rect+rotate라 벡터 에셋 대신 도형으로 재현. */
@Composable
private fun DiamondIcon() {
    Box(
        modifier = Modifier
            .size(14.dp)
            .rotate(45f)
            .clip(RoundedCornerShape(3.dp))
            .background(Pink800),
    )
}

/** "오늘의 추천 코스" 섹션. Loading/Empty/Error 는 카드와 같은 높이(243dp)의 자리로 표시해 레이아웃이 튀지 않게 한다. */
@Composable
private fun HomeCoursesSection(coursesState: CoursesState, onCourseClick: (Long) -> Unit, onRetry: () -> Unit) {
    when (coursesState) {
        is CoursesState.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(243.dp)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is CoursesState.Empty -> Text(
            text = coursesState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        is CoursesState.Error -> Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = coursesState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )
            OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                Text("다시 시도")
            }
        }

        is CoursesState.Success -> LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValuesHorizontal24,
        ) {
            items(coursesState.courses) { course ->
                CourseCard(course = course, onClick = { onCourseClick(course.courseId) })
            }
        }
    }
}

@Composable
private fun CourseCard(course: CourseCardUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(222.dp)
            .height(243.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
    ) {
        CourseThumbnail(
            imageUrl = course.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .background(
                    Brush.verticalGradient(listOf(course.gradientStart, course.gradientEnd)),
                ),
        ) {
            Image(
                painter = painterResource(course.decorationRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(81.dp)
                    .offset(x = 5.dp, y = 15.dp), // 밑을 살짝 잘리게(카드 clip 으로 컷)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = course.hashtags.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    color = White,
                )
            }
        }
    }
}

/**
 * 코스 카드 상단 사진. URL이 없으면(대표 이미지 미등록 등) 반투명 플레이스홀더를 보여준다.
 * weight(1f)는 ColumnScope 에서만 쓸 수 있어 호출부(Column) 에서 modifier 로 전달받는다.
 */
@Composable
private fun CourseThumbnail(imageUrl: String?, modifier: Modifier = Modifier) {
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

@Composable
private fun HomePlacesSection(placesState: HomePlacesState, onRetry: () -> Unit) {
    when (placesState) {
        is HomePlacesState.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is HomePlacesState.Empty -> Text(
            text = placesState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
        )

        is HomePlacesState.Error -> Column {
            Text(
                text = placesState.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray500,
            )
            OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                Text("다시 시도")
            }
        }

        // 카드 배경색은 디자인상 초록/민트가 순서대로 번갈아 나온다(카테고리 규칙 미확정 — 확정되면 교체).
        is HomePlacesState.Success -> placesState.places.forEachIndexed { index, place ->
            val isGreen = index % 2 == 0
            PlaceCard(
                place = place,
                gradient = if (isGreen) {
                    listOf(HomePlaceGreenStart, HomePlaceGreenEnd)
                } else {
                    listOf(HomePlaceMintStart, HomePlaceMintEnd)
                },
                // 문양 위치·크기는 카드(도형)별로 따로 조절한다 — 아래 offset/scale 값만 바꾸면 각각 움직인다.
                decoration = if (isGreen) {
                    PlaceDecoration(
                        res = R.drawable.ic_home_place_deco_semicircle, // 반원 72×34
                        offsetX = (-10).dp, offsetY = (-15).dp, scale = 0.8f,
                    )
                } else {
                    PlaceDecoration(
                        res = R.drawable.ic_home_place_deco_arch, // 아치 53×64
                        offsetX = (-10).dp, offsetY = (-15).dp, scale = 0.8f,
                    )
                },
                onSaveClick = { /* TODO: 코스 저장 API 연동(동작 확정 필요) */ },
            )
        }
    }
}

/** 장소 카드 배경 장식 도형 + 위치/크기(카드별로 따로 조절). offset 은 오른쪽 아래(BottomEnd) 기준, scale 은 비율 유지. */
private data class PlaceDecoration(
    @DrawableRes val res: Int,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val scale: Float = 1f,
)

@Composable
private fun PlaceCard(
    place: RecommendedPlaceUiModel,
    gradient: List<Color>,
    decoration: PlaceDecoration,
    onSaveClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(gradient)),
    ) {
        // 크기는 벡터 원본(피그마 export)에 scale(비율 유지)만 적용 — size(단일값)는 정사각형이 돼 찌그러지므로 쓰지 않는다.
        Image(
            painter = painterResource(decoration.res),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = decoration.offsetX, y = decoration.offsetY)
                .scale(decoration.scale),
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceThumbnail(imageUrl = place.imageUrl)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(11.dp),
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = place.address,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = White,
                )
                // 태그는 카드 아래쪽에 배치
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(White)
                        .padding(horizontal = 8.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = place.recommendReason,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Pink900,
                    )
                }
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_home_place_add),
            contentDescription = stringResource(R.string.home_place_save_content_description),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(17.dp)
                .clickable(onClick = onSaveClick),
        )
    }
}

/**
 * 장소 카드 썸네일 — 카드 왼쪽을 꽉 채운다(카드 높이 전체, 고정 폭).
 * 왼쪽 둥근 모서리는 부모 카드의 clip 이 처리하므로 여기서 따로 clip 하지 않는다.
 * 이미지 URL이 없으면(추천 API 미연동 등) 반투명 플레이스홀더를 보여준다.
 */
@Composable
private fun PlaceThumbnail(imageUrl: String?) {
    val modifier = Modifier
        .fillMaxHeight()
        .width(118.dp)
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

private val PaddingValuesHorizontal24 = PaddingValues(horizontal = 24.dp)

@Preview(showBackground = true, name = "기본")
@Composable
private fun HomeScreenDefaultPreview() {
    TodaitTheme {
        HomeContent(
            uiState = HomeUiState(
                coursesState = CoursesState.Success(
                    listOf(
                        CourseCardUiModel(
                            1L, "연남 데이트 코스", listOf("#낭만적인", "#베이커리카페"),
                            CourseRomanticGradientStart,
                            CourseRomanticGradientEnd,
                            null,
                            R.drawable.ic_mood_romantic,
                        ),
                        CourseCardUiModel(
                            2L, "홍대 데이트 코스", listOf("#힙한", "#팝업"),
                            CourseHipGradientStart,
                            CourseHipGradientEnd,
                            null,
                            R.drawable.ic_mood_hip,
                        ),
                    ),
                ),
                placesState = HomePlacesState.Success(
                    listOf(
                        RecommendedPlaceUiModel(1L, "반지공방 지니움", "서울 마포구 연남동 383-37", null, "현재 위치와 가까워요"),
                        RecommendedPlaceUiModel(2L, "별마당 도서관", "서울 강남구 영동대로 513", null, "연남 추천 장소예요"),
                    ),
                ),
            ),
            onCourseClick = {}, onNotificationClick = {}, onProfileClick = {}, onRetryCourses = {}, onRetryPlaces = {},
        )
    }
}

@Preview(showBackground = true, name = "장소 로딩")
@Composable
private fun HomeScreenLoadingPreview() {
    TodaitTheme {
        HomeContent(
            uiState = HomeUiState(placesState = HomePlacesState.Loading),
            onCourseClick = {}, onNotificationClick = {}, onProfileClick = {}, onRetryCourses = {}, onRetryPlaces = {},
        )
    }
}
