package com.umc.todait.core.mock

/**
 * MVP 시연용 Mock 사진 경로 모음.
 *
 * 피그마 "컴포넌트" 페이지(node 426-1042)의 예시 사진을 내려받아 `app/src/main/assets/mock/` 에
 * 넣어 두고, 각 feature 의 Mock 데이터가 서버 이미지 URL 자리에 이 경로를 채운다.
 *
 * Coil 이 `file:///android_asset/...` URI 를 그대로 로드하므로 화면 코드(AsyncImage)는 손댈 필요가 없다.
 * `android.resource://` 대신 asset 경로를 쓰는 이유는 applicationId 에 의존하지 않기 때문이다.
 *
 * 실 API 연동 시(USE_MOCK=false) 서버가 http(s) URL 을 내려주므로 이 상수들은 쓰이지 않는다.
 */
object MockImages {

    private const val BASE = "file:///android_asset/mock"

    /** 어두운 조명의 칵테일 4잔 — 바·와인바·이자카야용. */
    const val BAR_COCKTAIL = "$BASE/bar_cocktail.jpg"

    /** 목공방에서 의자를 만드는 모습 — 공방·전시 등 액티비티용. */
    const val ACTIVITY_WORKSHOP = "$BASE/activity_workshop.jpg"

    /** 벚꽃 얹은 말차 라떼 — 디저트·감성 카페용. */
    const val CAFE_MATCHA_LATTE = "$BASE/cafe_matcha_latte.jpg"

    /** 트레이에 놓인 아이스 아메리카노 — 일반 카페·브런치용. */
    const val CAFE_ICED_COFFEE = "$BASE/cafe_iced_coffee.jpg"

    /** 김밥·우동 한 상 — 식당·브런치 등 음식 사진용. */
    const val RESTAURANT_KOREAN = "$BASE/restaurant_korean.jpg"

    /** 골목 상점 외관 — 외관 사진이 어울리는 장소·카테고리 기본 이미지용. */
    const val SHOP_STOREFRONT = "$BASE/shop_storefront.jpg"
}
