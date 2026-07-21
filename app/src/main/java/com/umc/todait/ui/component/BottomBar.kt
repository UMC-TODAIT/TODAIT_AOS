package com.umc.todait.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umc.todait.navigation.BottomTab
import com.umc.todait.ui.theme.Gray100
import com.umc.todait.ui.theme.Gray500
import com.umc.todait.ui.theme.Pink800

@Composable
fun BottomBar(
    currentRoute: String?,
    onTabClick: (BottomTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(40.dp)
                )
                .background(
                    Color.White,
                    RoundedCornerShape(40.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->

                val selected =
                    currentRoute == tab.route

                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .weight(1f)
                        .background(
                            if(selected)
                                Gray100
                            else
                                Color.Transparent,
                            RoundedCornerShape(40.dp)
                        )
                        .clickable {
                            onTabClick(tab)
                        },
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Icon(
                            painter = painterResource(tab.iconRes),
                            contentDescription = null,
                            tint = if(selected) Pink800 else Gray500,
                            modifier = Modifier.size(24.dp)
                        )

                        Text(
                            text = tab.label,
                            fontSize = 12.sp,
                            color =
                                if(selected)
                                    Pink800
                                else
                                    Gray500
                        )
                    }
                }
            }
        }
    }
}