package com.umc.todait.feature.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.R
import androidx.compose.runtime.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.rememberScrollState
import com.umc.todait.ui.theme.Gray600
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.PlaceNumber

@Composable
fun PlaceCard(
    place: PlaceUiModel,
    number: Int,
    backgroundImage: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Color.White,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (place.isStartPlace) {
                            Image(
                                painter = painterResource(R.drawable.ic_todait_symbol),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = number.toString(),
                                color = PlaceNumber,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = place.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = place.address,
                            fontSize = 12.sp,
                            color = Color.Black
                        )

                    }

                }
                Spacer(modifier = Modifier.height(18.dp))

                Image(
                    painter = painterResource(R.drawable.divider_course_detail_line),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                PlaceMemoSection(place)
            }
        }
    }
}

@Composable
private fun PlaceMemoSection(place: PlaceUiModel) {

    var memo by remember {
        mutableStateOf(place.memo)
    }

    var isEditing by remember {
        mutableStateOf(false)
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_memo),
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(6.dp)
            )

            Text(
                text = "내 메모",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
        }

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isEditing) {
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .width(261.dp)
                        .height(50.dp)
                        .background(
                            Color.White.copy(alpha = 0.7f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = place.memo,
                            color = Gray600,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Image(
                    painter = painterResource(R.drawable.ic_write_memo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            isEditing = true
                        }
                )

            } else {

                BasicTextField(
                    value = memo,
                    onValueChange = {
                        memo = it
                    },
                    modifier = Modifier
                        .width(261.dp)
                        .height(60.dp)
                        .background(
                            Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(
                            horizontal = 14.dp
                        ),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = 14.sp
                    ),

                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
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

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            place.memo = memo
                            isEditing = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_white_circle_button),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_save_memo),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}