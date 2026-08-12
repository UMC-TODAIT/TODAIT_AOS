package com.umc.todait.feature.course.compose

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.umc.todait.core.location.Coordinate
import com.umc.todait.feature.course.base_place.PlaceUiModel

/**
 * 코스 구성하기 상단 카카오맵(v2).
 *
 * View 기반 [MapView] 를 [AndroidView] 로 감싸고, 지도 준비 후 마커를 찍는다.
 * (Figma "코스구성하기_드래그수정" node 534-13891)
 *
 * - **현재 위치**: 분홍 코어 + 흰 링 마커. [currentLocation] 이 null 이면(권한 없음/조회 실패) 생략한다.
 * - **코스 장소**: [places] 를 코스 동선 순서대로 1, 2, 3, … 번 핀으로 그린다.
 *   그중 기준 장소([basePlaceKey] 와 key 가 같은 장소)만 흰 원 + 분홍 테두리이고 나머지는 분홍으로 채운다.
 *   기준 장소도 드래그로 자리를 옮길 수 있으므로 "1번 = 기준 장소"라고 가정하지 않는다.
 *
 * 카메라는 코스 장소들이 모두 보이도록 맞춘다. 현재 위치는 코스와 임의로 멀 수 있어
 * 카메라 계산에서 제외하고 마커만 찍는다(코스가 하나도 없을 때만 현재 위치로 이동).
 *
 * ⚠️ 실제 렌더링은 기기(에뮬레이터)에서만 확인 가능하다.
 */
@Composable
fun CourseMap(
    places: List<PlaceUiModel>,
    basePlaceKey: String?,
    currentLocation: Coordinate?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }
    val fitPaddingPx = with(LocalDensity.current) { FIT_PADDING.roundToPx() }

    // 라벨 스타일 캐시(핀 종류별 1개).
    //
    // 카카오맵은 라벨 스타일을 styleId 로 네이티브에 등록해 두고 라벨이 그 id 를 참조하는 구조다.
    // styleId 는 LabelStyles.hashCode() 로 만들어지는데 여기에 아이콘 Bitmap 의 identity hash 가
    // 섞인다. 그래서 갱신할 때마다 비트맵을 새로 그리면 내용이 같아도 매번 "다른 스타일"로
    // 새로 등록되고, layer.removeAll() 은 라벨만 지우지 등록된 스타일은 해제하지 않는다.
    // 장소를 하나씩 담을 때마다 (핀 수)개씩 누적 등록돼(1+2+3+…) 얼마 못 가 새 핀이 그려지지 않는다.
    // → 핀 종류별로 스타일을 한 번만 등록해 재사용한다. 지도가 새로 준비되면 캐시도 비운다.
    val labelStyleCache = remember(kakaoMap) { mutableMapOf<String, LabelStyles>() }

    // MapView 생성 + 지도 준비 콜백. 준비되면 KakaoMap 을 상태로 올린다.
    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(error: Exception?) {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(map: KakaoMap) {
                            kakaoMap = map
                        }
                    },
                )
            }
        },
    )

    // 지도 lifecycle 연동(resume/pause/finish).
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.finish()
        }
    }

    // 지도 준비 + 데이터 변경 시 마커/카메라 갱신.
    LaunchedEffect(kakaoMap, places, basePlaceKey, currentLocation) {
        val map = kakaoMap ?: return@LaunchedEffect
        val labelManager = map.labelManager ?: return@LaunchedEffect
        val layer = labelManager.layer ?: return@LaunchedEffect
        layer.removeAll()

        // 이미 등록해 둔 스타일이면 재사용하고, 없을 때만 비트맵을 그려 새로 등록한다.
        fun labelStyles(cacheKey: String, anchorY: Float, icon: () -> Bitmap) =
            labelStyleCache[cacheKey] ?: labelManager.addLabelStyles(
                LabelStyles.from(
                    LabelStyle.from(icon())
                        .setAnchorPoint(0.5f, anchorY)
                        // 비트맵을 이미 화면 밀도(px)로 그려두므로 SDK 가 다시 배율을 곱하면 안 된다.
                        .setApplyDpScale(false),
                ),
            )?.also { labelStyleCache[cacheKey] = it }

        currentLocation?.let { location ->
            // 마커 안 "원 중심"이 실제 좌표에 오도록 앵커를 내린다.
            val styles = labelStyles(CACHE_KEY_CURRENT_LOCATION, CURRENT_LOCATION_ANCHOR_Y) {
                currentLocationBitmap(context)
            } ?: return@let
            layer.addLabel(
                LabelOptions.from(LatLng.from(location.latitude, location.longitude))
                    .setStyles(styles)
                    // 번호 핀이 항상 현재 위치 마커 위에 오도록 rank 를 가장 낮게 준다.
                    .setRank(0),
            )
        }

        places.forEachIndexed { index, place ->
            val number = index + 1
            // 기준 장소만 흰 원 + 테두리, 나머지는 채운 원. (Figma node 534-13891)
            // 순번이 아니라 key 로 판단한다 — 기준 장소가 1번이 아닐 수 있다.
            val isBase = basePlaceKey != null && place.key == basePlaceKey
            val styles = labelStyles("pin:$number:$isBase", anchorY = 0.5f) {
                placePinBitmap(context, number, isBase)
            } ?: return@forEachIndexed
            layer.addLabel(
                LabelOptions.from(LatLng.from(place.latitude, place.longitude))
                    .setStyles(styles)
                    .setRank(number.toLong()),
            )
        }

        val coursePoints = places.map { LatLng.from(it.latitude, it.longitude) }
        val cameraUpdate = when {
            coursePoints.size >= 2 ->
                CameraUpdateFactory.fitMapPoints(coursePoints.toTypedArray(), fitPaddingPx)

            coursePoints.size == 1 ->
                CameraUpdateFactory.newCenterPosition(coursePoints.first(), DEFAULT_ZOOM)

            currentLocation != null -> CameraUpdateFactory.newCenterPosition(
                LatLng.from(currentLocation.latitude, currentLocation.longitude),
                DEFAULT_ZOOM,
            )

            else -> CameraUpdateFactory.newCenterPosition(DEFAULT_CENTER, DEFAULT_ZOOM)
        }
        map.moveCamera(cameraUpdate)
    }
}

// 기본 카메라 위치(홍대 인근) + 줌 레벨. 기준/선택 장소도 현재 위치도 없을 때 사용.
private val DEFAULT_CENTER: LatLng = LatLng.from(37.5563, 126.9236)
private const val DEFAULT_ZOOM = 15

// 여러 장소를 한 화면에 맞출 때 지도 가장자리에 남길 여백. 핀이 잘리지 않을 정도.
private val FIT_PADDING = 48.dp

// 현재 위치 마커의 라벨 스타일 캐시 키. 장소 핀은 "pin:{순번}:{기준장소여부}" 를 쓴다.
private const val CACHE_KEY_CURRENT_LOCATION = "current_location"
