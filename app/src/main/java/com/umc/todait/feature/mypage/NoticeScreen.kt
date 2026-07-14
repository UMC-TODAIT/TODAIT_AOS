package com.umc.todait.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.umc.todait.R
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray800

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NoticeScreenPreview() {
    NoticeScreen(
        navController = rememberNavController()
    )
}

@Composable
fun NoticeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Image(
                painter = painterResource(R.drawable.shape_button_back),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .clickable {
                        navController.popBackStack()
                    }
            )

            Text(
                text = "공지사항 및 고객센터",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Image(
            painter = painterResource(R.drawable.divider_my_page_1),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}