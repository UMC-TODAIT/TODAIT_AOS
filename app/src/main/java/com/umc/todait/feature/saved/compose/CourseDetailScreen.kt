package com.umc.todait.feature.saved.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.feature.home.MOOD_DECORATIONS
import com.umc.todait.feature.home.MOOD_GRADIENTS
import com.umc.todait.feature.saved.PlaceUiModel
import com.umc.todait.ui.component.CommonDialog
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink900
import com.umc.todait.ui.theme.Suit
import com.umc.todait.ui.theme.TextPlaceholder
import com.umc.todait.ui.theme.White
import com.umc.todait.navigation.Screen
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray500
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CourseDetailScreenPreview() {
    CourseDetailScreen(
        navController = rememberNavController(),
        courseId = 1L
    )
}
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: Long,
    modifier: Modifier = Modifier
) {
    val viewModel: CourseDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        viewModel.getCourseDetail(courseId)
    }

    when {
        uiState.isLoading -> {
            LoadingIndicator()
        }

        uiState.error != null -> {
            ErrorContent(
                error = uiState.error!!,
                onRetry = {
                    viewModel.getCourseDetail(courseId)
                },
                onDismiss = {
                    viewModel.clearError()
                }
            )
        }
        else -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Cream)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    DetailHeader(
                        navController = navController,
                        onBackClick = {
                            if (isEditing) {
                                showExitDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )

                    SummaryCard(
                        uiState = uiState,
                        isEditing = isEditing,
                        onEditingChange = {
                            isEditing = it
                        },
                        onSaveMemo = { memo ->
                            viewModel.updateCourseMemo(
                                courseId = courseId,
                                memo = memo
                            )
                            isEditing = false
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "저장된 코스",
                            fontFamily = Suit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            lineHeight = 22.sp,
                            color = Gray800
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "장소를 눌러 상세 정보를 확인해보세요!",
                            fontFamily = Suit,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                            color = Gray500
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val gradient = MOOD_GRADIENTS[uiState.moodTagCode]
                            ?: MOOD_GRADIENTS["CALM"]!!

                        val cardDecorationRes =
                            MOOD_DECORATIONS[uiState.moodTagCode]
                                ?: MOOD_DECORATIONS["CALM"]!!

                        Column {
                            uiState.places.forEachIndexed { index, place ->
                                CourseDetailPlaceRow(
                                    place = place,
                                    number = index + 1,
                                    isLast = index == uiState.places.lastIndex,
                                    gradient = listOf(
                                        gradient.first,
                                        gradient.second
                                    ),
                                    decorationRes = cardDecorationRes,
                                    onClick = {
                                        navController.navigate(
                                            Screen.PlaceDetail.createRoute(place.placeId)
                                        )
                                    }
                                )

                                if (index != uiState.places.lastIndex) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .fillMaxHeight(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(1.dp)
                                                    .fillMaxHeight()
                                                    .background(Gray200)
                                            )
                                        }

                                        Spacer(
                                            modifier = Modifier.width(12.dp)
                                        )

                                        Spacer(
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(200.dp))
                    }
                }

                Image(
                    painter = painterResource(R.drawable.ic_saved_courses_gradient),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 80.dp),
                    contentScale = ContentScale.FillBounds
                )

                if (showExitDialog) {
                    CommonDialog(
                        title = "수정 중인 메모가 있습니다.\n저장하지 않고 나가시겠습니까?",
                        confirmText = "나가기",
                        cancelText = "계속 수정하기",
                        onConfirm = {
                            showExitDialog = false
                            navController.popBackStack()
                        },
                        onDismiss = {
                            showExitDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    navController: NavController,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(11.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_button),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .clickable {
                        onBackClick()
                    }
            )

            Text(
                text = "코스 상세 정보",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                fontFamily = Suit,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 20.sp,
                color = Gray800
            )
        }

        Spacer(Modifier.height(11.dp))
    }
}

@Composable
private fun SummaryCard(
    uiState: CourseDetailUiState,
    isEditing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    onSaveMemo: (String) -> Unit
) {
    val backgroundImage = getMoodBackground(uiState.moodTagCode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isEditing) 294.dp else 246.dp),
        shape = RectangleShape
    ){
        Box {
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(
                            getMoodIcon(uiState.moodTagCode)
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 26.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.date,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        CourseTag(
                            text = uiState.moodTag ?: ""
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        CourseTag(
                            text = uiState.placeTag ?: ""
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                MemoSection(
                    memo = uiState.memo,
                    isEditing = isEditing,
                    onEditingChange = onEditingChange,
                    onSaveMemo = onSaveMemo
                )
            }
        }
    }
}

@Composable
fun MemoSection(
    memo: String,
    isEditing: Boolean,
    onEditingChange: (Boolean) -> Unit,
    onSaveMemo: (String) -> Unit
) {
    var editMemo by remember(memo) {
        mutableStateOf(memo)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isEditing) 142.dp else 89.dp)
            .background(
                Color.White.copy(alpha = 0.7f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp, vertical = 1.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_memo),
                    contentDescription = null,
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "코스 메모",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    )
                )
            }

            if (!isEditing) {
                Image(
                    painter = painterResource(R.drawable.ic_write_memo),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(40.dp)
                        .clickable {
                            onEditingChange(true)
                        }
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 38.dp)
                        .size(40.dp)
                        .background(
                            Color.White,
                            CircleShape
                        )
                        .clickable {
                            onSaveMemo(editMemo)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_saved_course_check),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (!isEditing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 38.dp)
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .height(60.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = if (memo.isBlank()) "" else memo,
                            color = TextPlaceholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            } else {
                BasicTextField(
                    value = editMemo,
                    onValueChange = {
                        editMemo = it
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 38.dp)
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .height(60.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (editMemo.isEmpty()) {
                                Text(
                                    text = "",
                                    color = Gray600,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}
private fun getMoodBackground(
    code: String?
): Int {
    return when(code) {
        "ROMANTIC" -> R.drawable.bg_saved_courses_romantic
        "QUIET" -> R.drawable.bg_saved_courses_quiet
        "MODERN" -> R.drawable.bg_saved_courses_modern
        "HIP" -> R.drawable.bg_saved_courses_hip
        "ACTIVE" -> R.drawable.bg_saved_courses_active
        "CALM" -> R.drawable.bg_saved_courses_calm
        else -> R.drawable.bg_saved_courses_romantic
    }
}

private fun getMoodIcon(
    code: String?
): Int {
    return when(code) {
        "ROMANTIC" -> R.drawable.ic_mood_romantic
        "QUIET" -> R.drawable.ic_mood_quiet
        "MODERN" -> R.drawable.ic_mood_modern
        "HIP" -> R.drawable.ic_mood_hip
        "ACTIVE" -> R.drawable.ic_mood_active
        "CALM" -> R.drawable.ic_mood_calm
        else -> R.drawable.ic_mood_romantic
    }
}

@Composable
private fun CourseDetailPlaceRow(
    place: PlaceUiModel,
    number: Int,
    isLast: Boolean,
    gradient: List<Color>,
    @DrawableRes decorationRes: Int,
    onClick: (() -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (number == 1) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Gray200,
                            shape = CircleShape,
                        ),
                )

                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Gray200),
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Gray200),
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        color = Pink900,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number.toString(),
                    fontFamily = Suit,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    color = White,
                )
            }

            if (isLast) {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Gray200),
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = Gray200,
                            shape = CircleShape,
                        ),
                )
            } else {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(Gray200),
                )

                Canvas(
                    modifier = Modifier
                        .width(12.dp)
                        .height(7.dp),
                ) {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, size.height)
                        close()
                    }

                    drawPath(
                        path = path,
                        color = Gray200,
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(96.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(
                    enabled = onClick != null,
                    onClick = onClick ?: {},
                )
                .background(
                    Brush.horizontalGradient(gradient),
                ),
        ) {
            Image(
                painter = painterResource(decorationRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 8.dp,
                        bottom = 8.dp,
                    )
                    .size(30.dp)
                    .alpha(0.9f),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                PlaceThumbnail(
                    imageUrl = place.imageUrl,
                )

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

                    Spacer(
                        modifier = Modifier.height(4.dp),
                    )

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
    val thumbnailModifier = Modifier
        .fillMaxHeight()
        .width(96.dp)

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = thumbnailModifier,
        )
    } else {
        Box(
            modifier = thumbnailModifier
                .background(White.copy(alpha = 0.4f))
        )
    }
}