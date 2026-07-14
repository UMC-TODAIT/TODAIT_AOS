package com.umc.todait

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodaitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 카카오맵 v2 SDK 초기화. 로그인과 동일한 네이티브 앱 키 사용.
        // (Kakao Developers 콘솔에서 앱에 'Kakao Map' 플랫폼 활성화 + 키 해시 등록 필요)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
