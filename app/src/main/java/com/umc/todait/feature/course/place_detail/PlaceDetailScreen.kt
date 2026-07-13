package com.umc.todait.feature.course.place_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import com.umc.todait.ui.theme.DividerLine
import com.umc.todait.ui.theme.Gray100
import com.umc.todait.ui.theme.Gray200
import com.umc.todait.ui.theme.Gray400
import com.umc.todait.ui.theme.Gray450
import com.umc.todait.ui.theme.Gray800
import com.umc.todait.ui.theme.Gray900
import com.umc.todait.ui.theme.OpenGreen
import com.umc.todait.ui.theme.Pink800
import com.umc.todait.ui.theme.PlaceCardGradientEnd
import com.umc.todait.ui.theme.PlaceCardGradientStart
import com.umc.todait.ui.theme.Suit
import com.umc.todait.ui.theme.TodaitTheme
import com.umc.todait.ui.theme.White

/**
 * 장소 상세 화면(와이어프레임: 장소카드클릭_기본).
 *
 * 구성(위→아래): 상단 사진 캐러셀(n/N) → 장소명·주소 → 추천/해시태그 칩 →
 * 영업 상태(영업중·라스트오더) → 메뉴 → 내부 사진. 모두 Figma 시안에 맞춰 배치한다.
 *
 * 영업 상태·메뉴는 PlaceDetailDto 에 필드가 없어 데이터가 있을 때만 노출한다.
 * (모델 필드는 준비돼 있어 BE 스펙 확정 시 매핑만 채우면 된다. [PlaceDetailUiModel] 참고)
 *
 * @param onSeeAllPhotos 내부 사진 [전체보기] → 사진 그리드 화면([InteriorPhotosScreen])으로 이동.
 * @param onSeeAllMenu 메뉴 [전체보기] → 메뉴 전체보기 화면으로 이동. (TODO: 화면 추가 시 연결)
 */
@Composable
fun PlaceDetailScreen(
    onBack: () -> Unit,
    onSeeAllPhotos: () -> Unit,
    onSeeAllMenu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlaceDetailContent(
        state = uiState,
        onBack = onBack,
        onSeeAllPhotos = onSeeAllPhotos,
        onSeeAllMenu = onSeeAllMenu,
        onRetry = viewModel::loadPlaceDetail,
        modifier = modifier,
    )
}

@Composable
private fun PlaceDetailContent(
    state: PlaceDetailUiState,
    onBack: () -> Unit,
    onSeeAllPhotos: () -> Unit,
    onSeeAllMenu: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
    ) {
        when (val detail = state.detailState) {
            is PlaceDetailState.Loading -> LoadingIndicator()

            is PlaceDetailState.Error -> {
                // 에러 화면에서도 뒤로가기는 가능해야 한다.
                ErrorContent(
                    error = UiError(message = detail.message),
                    onRetry = onRetry,
                )
                OverlayBackButton(onBack = onBack)
            }

            is PlaceDetailState.Success -> PlaceDetailBody(
                place = detail.place,
                onBack = onBack,
                onSeeAllPhotos = onSeeAllPhotos,
                onSeeAllMenu = onSeeAllMenu,
            )
        }
    }
}

@Composable
private fun PlaceDetailBody(
    place: PlaceDetailUiModel,
    onBack: () -> Unit,
    onSeeAllPhotos: () -> Unit,
    onSeeAllMenu: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        PhotoCarousel(imageUrls = place.imageUrls, onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = place.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = place.address,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray400,
            )

            if (place.recommendReason != null || place.hashTags.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                TagChips(recommendReason = place.recommendReason, hashTags = place.hashTags)
            }

            if (place.openStatus != null) {
                Spacer(Modifier.height(10.dp))
                BusinessStatusRow(openStatus = place.openStatus, lastOrderText = place.lastOrderText)
            }

            if (place.menus.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                SectionDivider()
                Spacer(Modifier.height(16.dp))
                MenuSection(menus = place.menus, onSeeAll = onSeeAllMenu)
            }

            Spacer(Modifier.height(16.dp))
            SectionDivider()
            Spacer(Modifier.height(16.dp))
            InteriorPhotoSection(imageUrls = place.imageUrls, onSeeAll = onSeeAllPhotos)

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 상단 사진 캐러셀. 좌상단 뒤로가기 + 우하단 n/N 인디케이터를 얹는다. (내부 사진 화면과 공유) */
@Composable
internal fun PhotoCarousel(
    imageUrls: List<String>,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(257.dp),
    ) {
        if (imageUrls.isEmpty()) {
            // 사진이 없으면 브랜드 그라데이션 플레이스홀더.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(PlaceCardGradientStart, PlaceCardGradientEnd),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.place_detail_no_images),
                    fontSize = 14.sp,
                    color = White,
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { imageUrls.size })
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = imageUrls[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            // 우하단 n/N 인디케이터 (반투명 흰색 알약 + 흰색 텍스트, Figma 정합)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(White.copy(alpha = 0.6f))
                    .padding(horizontal = 15.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${imageUrls.size}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                )
            }
        }
        OverlayBackButton(onBack = onBack)
    }
}

/** 사진/에러 위에 얹는 반투명 원형 뒤로가기 버튼(좌상단). (내부 사진 화면과 공유) */
@Composable
internal fun OverlayBackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 20.dp, top = 52.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(Gray900.copy(alpha = 0.4f))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "뒤로가기",
            tint = White,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 추천 이유 + 분위기/음식 해시태그 칩들. 모두 흰 배경 + 얇은 테두리 + 핑크 텍스트(Figma 정합). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChips(recommendReason: String?, hashTags: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (recommendReason != null) {
            TagChip(text = recommendReason)
        }
        hashTags.forEach { tag ->
            TagChip(text = tag)
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(White)
            .border(0.5.dp, Gray200, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = Pink800,
        )
    }
}

/** 영업 상태 한 줄: "영업중"(초록) + 라스트 오더 안내(검정). */
@Composable
private fun BusinessStatusRow(openStatus: String, lastOrderText: String?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = openStatus,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = OpenGreen,
        )
        if (lastOrderText != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = lastOrderText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            )
        }
    }
}

/** 섹션 제목 + (선택) 전체보기 헤더. */
@Composable
private fun SectionHeader(title: String, onSeeAll: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gray800,
        )
        if (onSeeAll != null) {
            Text(
                text = stringResource(R.string.place_detail_see_all) + " >",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray450,
                modifier = Modifier.clickable(onClick = onSeeAll),
            )
        }
    }
}

/** 섹션 구분선(연한 회색). */
@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerLine),
    )
}

/** 메뉴 섹션. 헤더 + 가로 스크롤 메뉴 카드(120×140). */
@Composable
private fun MenuSection(menus: List<MenuUiItem>, onSeeAll: () -> Unit) {
    Column {
        SectionHeader(
            title = stringResource(R.string.place_detail_section_menu),
            onSeeAll = onSeeAll,
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(menus) { menu -> MenuCard(item = menu) }
        }
    }
}

@Composable
private fun MenuCard(item: MenuUiItem) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, Gray200, RoundedCornerShape(12.dp)),
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(87.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(87.dp)
                    .background(Gray100),
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = item.name,
            style = menuTextStyle(fontSize = 12.sp),
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.priceLabel,
            style = menuTextStyle(fontSize = 10.sp),
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

/**
 * 메뉴 카드 이름·가격 텍스트 스타일. Figma처럼 이름·가격이 촘촘히 붙도록
 * 줄 높이를 글자 크기에 맞추고(=여백 최소화) 폰트 상하 패딩을 제거한다.
 */
private fun menuTextStyle(fontSize: androidx.compose.ui.unit.TextUnit) = TextStyle(
    fontFamily = Suit,
    fontSize = fontSize,
    lineHeight = fontSize,
    fontWeight = FontWeight.Medium,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.Both,
    ),
)

/** 내부 사진 프리뷰 섹션. 헤더(제목 + 전체보기) + 가로 썸네일 리스트(104×104). */
@Composable
private fun InteriorPhotoSection(
    imageUrls: List<String>,
    onSeeAll: () -> Unit,
) {
    Column {
        SectionHeader(
            title = stringResource(R.string.place_detail_section_photos),
            onSeeAll = if (imageUrls.isNotEmpty()) onSeeAll else null,
        )
        Spacer(Modifier.height(12.dp))

        if (imageUrls.isEmpty()) {
            Text(
                text = stringResource(R.string.place_detail_no_images),
                fontSize = 12.sp,
                color = Gray450,
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(imageUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

// ---------- Preview ----------

private val previewPlace = PlaceDetailUiModel(
    placeId = 1,
    name = "애몽",
    categoryLabel = "카페 · 디저트",
    address = "서울특별시 마포구 연남로3길 13",
    phone = "02-123-4567",
    imageUrls = emptyList(),
    recommendReason = "현재 위치와 가까워요",
    hashTags = listOf("# 디저트", "# 낭만적인"),
    openStatus = "영업중",
    lastOrderText = "20:30에 라스트 오더",
    menus = listOf(
        MenuUiItem(name = "과일 소르베", priceLabel = "13000원", imageUrl = null),
        MenuUiItem(name = "생과일 파르페", priceLabel = "변동", imageUrl = null),
        MenuUiItem(name = "애몽 소다", priceLabel = "7500원", imageUrl = null),
    ),
)

@Preview(name = "장소 상세(사진 없음)", showBackground = true, heightDp = 900)
@Composable
private fun PlaceDetailContentPreview() {
    TodaitTheme {
        PlaceDetailContent(
            state = PlaceDetailUiState(PlaceDetailState.Success(previewPlace)),
            onBack = {},
            onSeeAllPhotos = {},
            onSeeAllMenu = {},
            onRetry = {},
        )
    }
}
