package com.umc.todait.feature.course.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.umc.todait.R
import com.umc.todait.feature.course.base_place.PlaceUiModel

/**
 * 코스 구성하기 상단 카카오맵(v2).
 *
 * View 기반 [MapView] 를 [AndroidView] 로 감싸고, 지도 준비 후 기준 장소/선택 장소를 라벨(핀)로 찍는다.
 * 카메라는 기준 장소(없으면 첫 선택 장소, 그마저 없으면 기본 좌표) 기준으로 이동한다.
 *
 * ⚠️ 실제 렌더링은 기기(에뮬레이터)에서만 확인 가능. 라벨 아이콘은 [R.drawable.ic_place_deco_1]
 * 을 임시로 사용하며, 기준/선택 구분 아이콘·번호 라벨은 디자인 확정 후 교체한다.(TODO)
 */
@Composable
fun CourseMap(
    basePlace: PlaceUiModel?,
    selectedPlaces: List<PlaceUiModel>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    var kakaoMap by remember { mutableStateOf<KakaoMap?>(null) }

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

    // 지도 준비 + 데이터 변경 시 라벨/카메라 갱신.
    LaunchedEffect(kakaoMap, basePlace, selectedPlaces) {
        val map = kakaoMap ?: return@LaunchedEffect
        val labelLayer = map.labelManager?.layer
        labelLayer?.removeAll()

        val styles = map.labelManager?.addLabelStyles(
            LabelStyles.from(LabelStyle.from(R.drawable.ic_place_deco_1)),
        )

        val pins = buildList {
            basePlace?.let { add(it) }
            addAll(selectedPlaces)
        }
        pins.forEach { place ->
            labelLayer?.addLabel(
                LabelOptions.from(LatLng.from(place.latitude, place.longitude))
                    .setStyles(styles),
            )
        }

        val center = basePlace ?: selectedPlaces.firstOrNull()
        val target = if (center != null) {
            LatLng.from(center.latitude, center.longitude)
        } else {
            DEFAULT_CENTER
        }
        map.moveCamera(CameraUpdateFactory.newCenterPosition(target, DEFAULT_ZOOM))
    }
}

// 기본 카메라 위치(홍대 인근) + 줌 레벨. 기준/선택 장소가 아직 없을 때 사용.
private val DEFAULT_CENTER: LatLng = LatLng.from(37.5563, 126.9236)
private const val DEFAULT_ZOOM = 15
