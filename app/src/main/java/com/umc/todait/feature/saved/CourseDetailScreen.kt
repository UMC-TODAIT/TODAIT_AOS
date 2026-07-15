package com.umc.todait.feature.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.umc.todait.R
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.TermsText
import com.umc.todait.ui.theme.TextPlaceholder

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun CourseDetailScreenPreview() {
    CourseDetailScreen(
        navController = rememberNavController(),
        courseId = recentCourses.first().id
    )
}
@Composable
fun CourseDetailScreen(
    navController: NavController,
    courseId: Long,
    modifier: Modifier = Modifier
) {

    val course =
        (recentCourses + popularCourses)
            .firstOrNull { it.id == courseId }

    if (course == null) {
        Text("코스를 찾을 수 없습니다.")
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DetailHeader(navController)

            SummaryCard(course)

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                val places = coursePlaces[course.id] ?: emptyList()

                itemsIndexed(places) { index, place ->

                    PlaceCard(
                        place = place,
                        number = index,
                        backgroundImage = course.backgroundImage
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
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
    }
}

@Composable
private fun DetailHeader(
    navController: NavController
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

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
                    .clickable {
                        navController.popBackStack()
                    }
            )

            Text(
                text = "코스 상세 정보",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCard(
    course: CourseUiModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(246.dp),
        shape = RectangleShape
    ) {
        Box {
            Image(
                painter = painterResource(course.backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = course.title,
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = course.date,
                                color = Color.White,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Image(
                                painter = painterResource(course.moodTag),
                                contentDescription = null,
                                modifier = Modifier.height(13.dp)
                            )

                            Spacer(modifier = Modifier.width(9.dp))

                            Image(
                                painter = painterResource(course.foodTag),
                                contentDescription = null,
                                modifier = Modifier.height(13.dp)
                            )
                        }
                    }

                    Image(
                        painter = painterResource(course.topImage),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 80.dp)
                            .size(44.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                MemoSection()
            }
        }
    }
}

@Composable
fun MemoSection() {
    var memo by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isEditing) 136.dp else 89.dp)
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
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
                            isEditing = true
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
                            isEditing = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_save_memo),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
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
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                BasicTextField(
                    value = memo,
                    onValueChange = {
                        memo = it
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
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 14.sp
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (memo.isEmpty()) {
                                Text(
                                    text = "",
                                    color = Gray600,
                                    fontSize = 14.sp
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