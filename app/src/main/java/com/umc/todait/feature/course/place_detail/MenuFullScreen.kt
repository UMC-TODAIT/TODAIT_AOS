package com.umc.todait.feature.course.place_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.umc.todait.R
import com.umc.todait.core.network.UiError
import com.umc.todait.ui.component.ErrorContent
import com.umc.todait.ui.component.LoadingIndicator
import com.umc.todait.ui.theme.Cream
import com.umc.todait.ui.theme.Gray100
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Suit
import com.umc.todait.ui.theme.TodaitTheme

/**
 * 메뉴 전체보기 화면(와이어프레임: 장소카드클릭_메뉴전체보기).
 *
 * 상단은 장소 상세와 동일한 사진 캐러셀([PhotoCarousel])을 재사용하고,
 * 그 아래 "← 메뉴" 헤더 + 가로형 메뉴 카드(356×83, 좌측 이름·가격 / 우측 썸네일)를 세로로 나열한다.
 *
 * 데이터는 장소 상세와 같은 [PlaceDetailViewModel] 을 재사용한다.
 * (메뉴 화면 라우트도 placeId 인자를 갖고 있어 동일하게 조회된다.)
 */
@Composable
fun MenuFullScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        when (val detail = uiState.detailState) {
            is PlaceDetailState.Loading -> LoadingIndicator()

            is PlaceDetailState.Error -> {
                ErrorContent(
                    error = UiError(message = detail.message),
                    onRetry = viewModel::loadPlaceDetail,
                )
                OverlayBackButton(onBack = onBack)
            }

            is PlaceDetailState.Success -> MenuFullBody(
                imageUrls = detail.place.imageUrls,
                menus = detail.place.menus,
                onBack = onBack,
            )
        }
    }
}

@Composable
private fun MenuFullBody(
    imageUrls: List<String>,
    menus: List<MenuUiItem>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // 상단 히어로 캐러셀(장소 상세와 동일).
        PhotoCarousel(imageUrls = imageUrls, onBack = onBack)

        // "← 메뉴" 서브 헤더.
        Row(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Gray800,
                modifier = Modifier
                    .size(28.dp)
                    .clickable(onClick = onBack),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.place_detail_section_menu),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
        }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            menus.forEach { menu -> MenuRowCard(item = menu) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** 가로형 메뉴 카드: 좌측 이름·가격, 우측 썸네일(106dp). 높이 83dp. */
@Composable
private fun MenuRowCard(item: MenuUiItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(83.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Gray200, RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = item.name,
                style = menuRowTextStyle(fontSize = 14.sp, weight = FontWeight.SemiBold),
                color = Gray800,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.priceLabel,
                style = menuRowTextStyle(fontSize = 12.sp, weight = FontWeight.Medium),
                color = Gray800,
            )
        }
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(106.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .width(106.dp)
                    .fillMaxHeight()
                    .background(Gray100),
            )
        }
    }
}

/** 이름·가격 텍스트 스타일. Figma처럼 6dp 간격이 정확히 유지되도록 폰트 상하 패딩을 제거한다. */
private fun menuRowTextStyle(fontSize: TextUnit, weight: FontWeight) = TextStyle(
    fontFamily = Suit,
    fontSize = fontSize,
    lineHeight = fontSize,
    fontWeight = weight,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

// ---------- Preview ----------

private val previewMenus = listOf(
    MenuUiItem(name = "과일 소르베", priceLabel = "13000원", imageUrl = null),
    MenuUiItem(name = "생과일 파르페", priceLabel = "변동", imageUrl = null),
    MenuUiItem(name = "애몽 소다", priceLabel = "13000원", imageUrl = null),
    MenuUiItem(name = "다도 상차림", priceLabel = "변동", imageUrl = null),
    MenuUiItem(name = "머핀 아라모드", priceLabel = "10800원", imageUrl = null),
)

@Preview(name = "메뉴 전체보기", showBackground = true, heightDp = 900)
@Composable
private fun MenuFullBodyPreview() {
    TodaitTheme {
        MenuFullBody(
            imageUrls = emptyList(),
            menus = previewMenus,
            onBack = {},
        )
    }
}
