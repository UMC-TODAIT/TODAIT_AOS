package com.umc.todait

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodaitApplication : Application() {
    /* key 생성되면 주석 풀고 이 코드 사용하세요
    override fun onCreate() {
        super.onCreate()
        // 카카오 SDK 초기화 — 로그인/지도 모두 동일한 네이티브 앱 키를 사용한다.
        // (Kakao Developers 콘솔에서 카카오 로그인 활성화 + 플랫폼(Android) 키 해시 등록 필요)
        val kakaoKey = BuildConfig.KAKAO_NATIVE_APP_KEY
        if (kakaoKey.isNotBlank()) {
            KakaoSdk.init(this, kakaoKey)                          // 로그인(v2-user)
            KakaoMapSdk.init(this, kakaoKey)                      // 지도(v2)
        }
        // 키가 비어 있으면(로컬 미설정) 초기화를 건너뛴다 — 앱 부팅은 정상, 소셜/지도 기능만 비활성.
    }
}
*/

    //key 발급 전에 임시로 사용하는 코드입니다.
    override fun onCreate() {
        super.onCreate()

        // 카카오맵 키가 있을 때만 SDK 초기화
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotEmpty()) {
            KakaoMapSdk.init(
                this,
                BuildConfig.KAKAO_NATIVE_APP_KEY
            )
        }
    }
}