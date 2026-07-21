package com.umc.todait.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.umc.todait.R
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink600
import com.umc.todait.ui.theme.White
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun CourseSaveDialog(
    onMoveClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onSkipClick
    ) {
        Surface(
            modifier = Modifier
                .width(332.dp)
                .height(460.dp),
            shape = RoundedCornerShape(12.dp),
            color = Cream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cream),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "코스가 저장되었어요",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "저장한 코스는 저장된코스에서 확인할 수 있어요",
                    fontSize = 12.sp,
                    color = Gray400
                )

                Spacer(modifier = Modifier.height(30.dp))

                CourseSaveDecoration()

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onMoveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Pink600
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "저장된 코스로 이동하기",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "건너뛰기",
                    color = Gray400,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        onSkipClick()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CourseSaveDecoration() {

    ConstraintLayout(
        modifier = Modifier
            .width(180.dp)
            .height(170.dp)
    ) {
        val (
            hip,
            romantic,
            symbol,
            orange,
            yellow,
            mint,
            pink
        ) = createRefs()

        Image(
            painter = painterResource(R.drawable.ic_mood_hip),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .constrainAs(hip) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(symbol.start, margin = (-70).dp)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_mood_romantic),
            contentDescription = null,
            modifier = Modifier
                .size(34.dp)
                .constrainAs(romantic) {
                    top.linkTo(hip.bottom, margin = 2.dp)
                    start.linkTo(parent.start, margin = 18.dp)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_todait_symbol),
            contentDescription = null,
            modifier = Modifier
                .size(82.dp)
                .constrainAs(symbol) {
                    top.linkTo(romantic.bottom, margin = 2.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_course_dialog_1),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .constrainAs(orange) {
                    start.linkTo(symbol.end, margin = 30.dp)
                    top.linkTo(symbol.top, margin = 8.dp)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_course_dialog_2),
            contentDescription = null,
            modifier = Modifier
                .size(44.dp)
                .constrainAs(yellow) {
                    end.linkTo(symbol.start, margin = 2.dp)
                    top.linkTo(symbol.bottom, margin = (-10).dp)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_course_dialog_3),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .constrainAs(mint) {
                    start.linkTo(symbol.end, margin = 4.dp)
                    top.linkTo(symbol.bottom, margin = 6.dp)
                }
        )

        Image(
            painter = painterResource(R.drawable.ic_course_dialog_4),
            contentDescription = null,
            modifier = Modifier
                .size(14.dp)
                .constrainAs(pink) {
                    start.linkTo(mint.end, margin = 8.dp)
                    top.linkTo(mint.top, margin = 2.dp)
                }
        )
    }
}