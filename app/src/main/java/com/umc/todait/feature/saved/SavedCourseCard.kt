package com.umc.todait.feature.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R

@Composable
fun SavedCourseCard(
    course: CourseUiModel
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(270.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Image(
                painter = painterResource(course.backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(course.topImage),
                    contentDescription = null,
                    modifier = Modifier.size(33.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = course.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = course.date,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(course.moodTag),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(7.dp))

                    Image(
                        painter = painterResource(course.foodTag),
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    val visiblePlaces = course.places.take(3)

                    visiblePlaces.forEachIndexed { index, place ->

                        Text(
                            text = place,
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        if (index != visiblePlaces.lastIndex) {
                            Image(
                                painter = painterResource(R.drawable.ic_saved_courses_dot),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (course.places.size > 3) {
                        Image(
                            painter = painterResource(R.drawable.ic_saved_courses_dot),
                            contentDescription = null,
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text = "외 ${course.places.size - 3}개 장소",
                            color = Color.White,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
