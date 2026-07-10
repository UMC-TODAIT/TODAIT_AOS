package com.umc.todait.feature.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SavedCoursesScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFBF3))
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "저장된 코스",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF222222)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(R.drawable.divider_my_page_1),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {

                item {

                    Spacer(modifier = Modifier.height(24.dp))

                    CourseSection(
                        title = "최근 저장된 코스",
                        description = "투데잇님이 최근 저장하신 코스에요."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recentCourses) { course ->
                            SavedCourseCard(course)
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    CourseSection(
                        title = "많이 이용한 코스",
                        description = "투데잇님이 많이 이용한 코스에요."
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(popularCourses) { course ->
                            SavedCourseCard(course)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Image(
                        painter = painterResource(R.drawable.divider_terms),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "이용약관",
                        color = Color(0xFFB3B3B3),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

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
fun CourseSection(
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painterResource(R.drawable.ic_saved_courses_fire),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF222222)
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = description,
        color = Color(0xFF888888),
        fontSize = 14.sp
    )
}
