package com.umc.todait

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodaitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 카카오 SDK 초기화 — 로그인/지도 모두 동일한 네이티브 앱 키를 사용한다.
        // (Kakao Developers 콘솔에서 카카오 로그인 활성화 + 플랫폼(Android) 키 해시 등록 필요)
        val kakaoKey = BuildConfig.KAKAO_NATIVE_APP_KEY
        if (kakaoKey.isNotEmpty()) {
            KakaoSdk.init(this, kakaoKey) // 로그인(v2-user) — 순수 코틀린이라 에뮬레이터 ABI 영향 없음

            // 카카오맵 네이티브 엔진(libK3fAndroid.so)이 ARM 전용이라 x86_64 에뮬레이터에서
            // dlopen 이 실패해 앱 부팅 자체가 죽는다(UnsatisfiedLinkError). 실기기/ARM 에뮬레이터에서는
            // 정상 동작하므로, x86_64 에뮬 개발 편의를 위해 실패 시 지도만 비활성화하고 앱은 계속 띄운다.
            try {
                KakaoMapSdk.init(this, kakaoKey) // 지도(v2)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "카카오맵 네이티브 라이브러리 로드 실패(x86_64 에뮬레이터 등 ARM 미지원 환경) — 지도 기능만 비활성화됩니다.", e)
            }
        }
        // 키가 비어 있으면(로컬 미설정) 초기화를 건너뛴다 — 앱 부팅은 정상, 소셜/지도 기능만 비활성.
    }

    private companion object {
        const val TAG = "TodaitApplication"
    }
}
