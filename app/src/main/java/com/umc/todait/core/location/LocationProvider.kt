package com.umc.todait.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** 요청 시점 사용자 위·경도. 추천 API 의 latitude/longitude 쿼리 파라미터로 전달한다. */
data class Coordinate(
    val latitude: Double,
    val longitude: Double,
)

/**
 * FusedLocationProviderClient 래퍼. "요청 시점 현재 위치" 1회 조회 전용.
 *
 * - 지속 추적(requestLocationUpdates)이 아니라 getCurrentLocation() 을 사용한다.
 * - getCurrentLocation() 이 null 이면 캐시된 lastLocation 으로 폴백한다.
 * - 권한이 없거나 조회에 실패하면 null 을 반환한다. (명세상 위·경도는 선택 파라미터라
 *   null 이어도 추천 조회 자체는 가능하다.)
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    /** 위치 권한(정밀 또는 대략)이 허용돼 있는지. */
    fun hasLocationPermission(): Boolean =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
            isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    /**
     * 현재 위치를 1회 조회한다. 권한 없음/기기 위치 꺼짐/조회 실패 시 null.
     * COARSE 권한만 허용된 경우에도 동작하도록 BALANCED 우선순위를 사용한다(추천 용도로 충분).
     */
    @SuppressLint("MissingPermission") // hasLocationPermission() 으로 선검사
    suspend fun getCurrentLocation(): Coordinate? {
        if (!hasLocationPermission()) return null
        return try {
            val location = client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token,
            ).await() ?: client.lastLocation.await()
            location?.let { Coordinate(latitude = it.latitude, longitude = it.longitude) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
