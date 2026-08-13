package com.umc.todait.feature.course

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.window.DialogProperties
import com.umc.todait.R
import com.umc.todait.ui.theme.AlertWarningRed
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Pink900
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
 *
 * 시안(393dp 기준 304x142)을 픽셀로 고정하지 않고, 좌우 여백과 최대 폭만 잡아 화면 폭에 맞춰
 * 늘어나게 한다. 높이도 내용에 따라 잡히므로 글자 크기 설정이 커져도 문구가 잘리지 않는다.
 */
@Composable
fun CourseDraftResumeDialog(
    onContinue: () -> Unit,
    onStartNew: () -> Unit,
) {
    Dialog(
        onDismissRequest = { /* 선택 없이 닫을 수 없다. */ },
        // 시스템 기본 폭 대신 화면 폭 기준으로 직접 잡는다.
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 44.dp)
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(12.dp),
            color = White,
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        // 경고 문구가 한 줄로 들어가야 해서 좌우 여백은 최소로 둔다.
                        .padding(horizontal = 8.dp, vertical = 17.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.course_draft_resume_title),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.course_draft_resume_warning),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Medium,
                        // 데이터가 지워진다는 경고라 본문과 색으로 구분한다.
                        color = AlertWarningRed,
                        textAlign = TextAlign.Center,
                    )
                }

                HorizontalDivider(color = DividerLine)

                // 세로 구분선이 버튼 높이를 따라가도록 행 높이를 내용 기준으로 잡는다.
                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .fillMaxHeight()
                            .clickable { onContinue() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.course_draft_resume_continue),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray200,
                        )
                    }

                    VerticalDivider(color = DividerLine)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .fillMaxHeight()
                            .clickable { onStartNew() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.course_draft_resume_start_new),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Pink900,
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
