package com.umc.todait.feature.course.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.umc.todait.R
import kotlin.math.roundToInt

/**
 * 코스 구성 지도([CourseMap])에 찍는 마커 비트맵.
 *
 * 카카오맵 v2 의 라벨 아이콘은 drawable 리소스 또는 [Bitmap] 만 받는다. 순번 숫자가 들어가는
 * 장소 핀은 리소스로 만들 수 없어 [placePinBitmap] 으로 그린다.
 *
 * 치수는 Figma "코스구성하기_드래그수정"(node 534-13891) 실측값이다.
 * - 기준 장소(1번): 32dp 흰 원 + Pink-900 1.5dp 테두리 + Pink-900 숫자
 * - 나머지 장소: 28dp Pink-900 채운 원 + 흰 숫자
 */

/** Figma Pink-900. 핀 테두리/채움/숫자, 현재 위치 마커 코어 색. */
private const val PIN_ACCENT = 0xFFED9896.toInt()

// 기준 장소 핀(흰 원 + 테두리). 테두리가 원 위에 가운데 정렬로 그려져 절반이 밖으로 나온다.
private const val BASE_PIN_DIAMETER_DP = 32f
private const val BASE_PIN_STROKE_DP = 1.5f

// 담은 장소 핀(채운 원).
private const val PLACE_PIN_DIAMETER_DP = 28f

/** 두 핀 모두 숫자는 SUIT SemiBold 18. */
private const val PIN_NUMBER_SIZE_DP = 18f

// 현재 위치 마커의 원본 크기(ic_map_current_location 의 viewport).
private const val CURRENT_LOCATION_WIDTH_DP = 42f
private const val CURRENT_LOCATION_HEIGHT_DP = 49f

/**
 * 현재 위치 마커에서 "실제 좌표"에 해당하는 지점의 세로 위치 비율.
 * 원 중심이 이미지 위에서 28.1196/49.0196 지점에 있어 라벨 앵커를 여기에 맞춰야
 * 콘(삼각형) 높이만큼 마커가 위로 밀리지 않는다.
 */
const val CURRENT_LOCATION_ANCHOR_Y = 28.1196f / 49.0196f

/**
 * 코스 순번이 적힌 장소 핀. [number] 는 1부터 시작한다.
 *
 * [isBase] 면 기준 장소 스타일(흰 원 + Pink-900 테두리 + Pink-900 숫자),
 * 아니면 담은 장소 스타일(Pink-900 채운 원 + 흰 숫자)로 그린다.
 */
fun placePinBitmap(context: Context, number: Int, isBase: Boolean): Bitmap {
    val density = context.resources.displayMetrics.density
    // 기준 장소 핀만 테두리가 원 밖으로 0.75dp 걸쳐 나와 비트맵을 그만큼 크게 잡는다.
    val sizeDp = if (isBase) BASE_PIN_DIAMETER_DP + BASE_PIN_STROKE_DP else PLACE_PIN_DIAMETER_DP
    val sizePx = (sizeDp * density).roundToInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val center = sizePx / 2f
    val diameterDp = if (isBase) BASE_PIN_DIAMETER_DP else PLACE_PIN_DIAMETER_DP
    val radius = diameterDp * density / 2f

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = if (isBase) Color.WHITE else PIN_ACCENT
    }
    canvas.drawCircle(center, center, radius, fillPaint)

    if (isBase) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = BASE_PIN_STROKE_DP * density
            color = PIN_ACCENT
        }
        canvas.drawCircle(center, center, radius, strokePaint)
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isBase) PIN_ACCENT else Color.WHITE
        textAlign = Paint.Align.CENTER
        // 지도 아이콘이라 사용자 글꼴 배율(sp)을 따르지 않고 dp 로 고정한다.
        textSize = PIN_NUMBER_SIZE_DP * density
        typeface = ResourcesCompat.getFont(context, R.font.suit_semibold)
    }
    // drawText 의 y 는 baseline 이라, 글자 중앙이 원 중앙에 오도록 보정한다.
    val metrics = textPaint.fontMetrics
    val baseline = center - (metrics.ascent + metrics.descent) / 2f
    canvas.drawText(number.toString(), center, baseline, textPaint)

    return bitmap
}

/** 현재 위치 마커(분홍 코어 + 흰 링 + 핑크 글로우 + 방향 콘). */
fun currentLocationBitmap(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val drawable = checkNotNull(
        ContextCompat.getDrawable(context, R.drawable.ic_map_current_location),
    ) { "ic_map_current_location 을 찾을 수 없습니다." }
    return drawable.toBitmap(
        width = (CURRENT_LOCATION_WIDTH_DP * density).roundToInt(),
        height = (CURRENT_LOCATION_HEIGHT_DP * density).roundToInt(),
    )
}
