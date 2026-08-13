package com.umc.todait.feature.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.umc.todait.R
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.LoginHeadingPink
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 진행 중인 임시 코스가 있을 때 코스 만들기 진입에서 띄우는 확인 알림.
 *
 * 생김새는 [com.umc.todait.ui.component.CommonDialog] 를 따르되, 새로 만들기가 기존 코스를
 * 지운다는 경고 문구가 한 줄 더 들어가 높이가 다르다(그래서 CommonDialog 를 쓰지 않는다).
 *
 * - [onContinue] "이어서 하기": 기존 임시 코스를 그대로 이어서 쓴다(단계 화면으로 이동).
 * - [onStartNew] "새로 만들기": 기존 임시 코스를 포기(DELETE)한 뒤 새로 만든다.
 *
 * 되돌릴 수 없는 선택이라 바깥 탭/뒤로가기로는 닫히지 않는다 — 둘 중 하나를 골라야 한다.
 */
@Composable
fun CourseDraftResumeDialog(
    onContinue: () -> Unit,
    onStartNew: () -> Unit,
) {
    Dialog(onDismissRequest = { /* 선택 없이 닫을 수 없다. */ }) {
        Surface(
            modifier = Modifier.width(304.dp),
            shape = RoundedCornerShape(16.dp),
            color = White,
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.course_draft_resume_title),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.course_draft_resume_warning),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 12.sp,
                        // 데이터가 지워진다는 경고라 본문과 색으로 구분한다.
                        color = LoginHeadingPink,
                        textAlign = TextAlign.Center,
                    )
                }

                HorizontalDivider(color = DividerLine)

                Row(modifier = Modifier.height(55.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onStartNew() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.course_draft_resume_start_new),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )
                    }

                    VerticalDivider(color = DividerLine)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onContinue() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.course_draft_resume_continue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LoginHeadingPink,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "임시 코스 이어서 만들기", showBackground = true)
@Composable
private fun CourseDraftResumeDialogPreview() {
    TodaitTheme {
        CourseDraftResumeDialog(onContinue = {}, onStartNew = {})
    }
}
