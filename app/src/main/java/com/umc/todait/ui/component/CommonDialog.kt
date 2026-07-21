package com.umc.todait.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.LoginHeadingPink
import com.umc.todait.ui.theme.White
@Composable
fun CommonDialog(
    title: String,
    confirmText: String = "확인",
    cancelText: String = "취소",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
){
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .width(304.dp)
                .height(122.dp),
            shape = RoundedCornerShape(16.dp),
            color = White
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }

                HorizontalDivider(
                    color = DividerLine
                )

                Row(
                    modifier = Modifier.height(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cancelText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                    }
                    VerticalDivider(
                        color = DividerLine
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoginHeadingPink
                        )
                    }
                }
            }
        }
    }
}