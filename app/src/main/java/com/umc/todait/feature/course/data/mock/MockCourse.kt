package com.umc.todait.feature.course.data.mock

import com.umc.todait.core.mock.MockImages
import com.umc.todait.feature.course.data.dto.AreaSummaryDto
import com.umc.todait.feature.course.data.dto.BasePlaceSummaryDto
import com.umc.todait.feature.course.data.dto.CourseDraftCreateResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftFoodCategorySaveResponseDto
import com.umc.todait.feature.course.data.dto.CourseDraftMoodTagSaveResponseDto
import com.umc.todait.feature.course.data.dto.FoodCategoryDto
import com.umc.todait.feature.course.data.dto.FoodCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.FoodCategorySummaryDto
import com.umc.todait.feature.course.data.dto.MoodTagDto
import com.umc.todait.feature.course.data.dto.MoodTagListResponseDto
import com.umc.todait.feature.course.data.dto.HotPlaceDto
import com.umc.todait.feature.course.data.dto.HotPlaceResultDto
import com.umc.todait.feature.course.data.dto.MoodTagSummaryDto
import com.umc.todait.feature.course.data.dto.PlaceCategoryListResponseDto
import com.umc.todait.feature.course.data.dto.PlaceCategoryResponseDto
import com.umc.todait.feature.course.data.dto.PlaceCategorySummaryDto
import com.umc.todait.feature.course.data.dto.PlaceDetailDto
import com.umc.todait.feature.course.data.dto.PlaceMenuDto
import com.umc.todait.feature.course.data.dto.PlaceSearchResultDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceDto
import com.umc.todait.feature.course.data.dto.RecommendedPlaceResultDto
import com.umc.todait.feature.course.data.dto.SearchPlaceDto

/**
 * MVP 시연용 스위치: true 면 코스 플로우의 Repository 가 서버 대신 [MockCourse] 를 반환한다.
 *
 * 코스 생성 플로우는 여러 Repository(검색·추천·상세·카테고리·임시코스)가 한 화면을 함께 채우므로
 * 플래그를 하나로 두고 **여기서만 켜고 끈다**. 실 API 연결 시 false 로 바꾸면 전체가 서버를 탄다.
 */
internal const val USE_COURSE_MOCK = true

/**
 * 코스 생성 플로우 Mock 데이터 (MVP 시연용).
 * 각 Repository 의 `USE_MOCK = true` 일 때 서버 대신 이 데이터를 반환한다.
 *
 * 기준 장소 설정 → 장소 상세 → 코스 구성 → 선택한 장소 → 코스 저장까지
 * BE 배포 없이 화면을 채운 상태로 확인할 수 있게 하는 것이 목적이다.
 *
 * 값은 **API 명세서(Notion) 최신본의 응답 예시 스키마**를 그대로 따른다. 그래서 실 API 를 붙일 때
 * (USE_MOCK=false) 화면 코드가 그대로 동작해야 정상이며, 어긋나면 매핑 버그다.
 *
 * 검증 포인트를 일부러 섞어 두었다:
 * - 검색 결과에 **내부 미등록 장소(placeId = null, detailAvailable = false)** 를 포함 → 상세 진입 차단·목록 key 확인
 * - 주변 핫플에 위치정보 없는 케이스(distanceMeters·isNearby = null) 포함
 * - 카테고리별 추천 카드의 matchedMoodTags 로 분위기 6종 색상을 모두 노출
 * - 장소 상세에 메뉴(가격 변동 포함)·내부 사진·영업중/라스트오더 포함
 *
 * 이미지 URL 은 피그마 예시 사진을 담은 [MockImages] 의 asset 경로를 쓴다. 서버 이미지처럼 Coil 이
 * 그대로 로드하므로 화면 코드는 손대지 않는다. 단 **메뉴 한 건은 imageUrl = null** 로 남겨
 * 사진 없는 메뉴의 플레이스홀더 렌더도 함께 확인할 수 있게 했다.
 */
object MockCourse {

    // ---------- 공통 요약 객체 ----------

    private val areaHongdae = AreaSummaryDto(areaId = 1, code = "HONGDAE", name = "홍대")
    private val areaYeonnam = AreaSummaryDto(areaId = 2, code = "YEONNAM", name = "연남")
    private val areaSeongsu = AreaSummaryDto(areaId = 3, code = "SEONGSU", name = "성수")

    private val categoryCafe = PlaceCategorySummaryDto(placeCategoryId = 1, code = "CAFE", name = "카페")
    private val categoryRestaurant = PlaceCategorySummaryDto(placeCategoryId = 2, code = "RESTAURANT", name = "식당")
    private val categoryActivity = PlaceCategorySummaryDto(placeCategoryId = 3, code = "ACTIVITY", name = "액티비티")
    private val categoryBar = PlaceCategorySummaryDto(placeCategoryId = 4, code = "BAR", name = "바")

    private val moodHip = MoodTagSummaryDto(moodTagId = 1, code = "HIP", name = "힙한")
    private val moodQuiet = MoodTagSummaryDto(moodTagId = 2, code = "QUIET", name = "조용한")
    private val moodActive = MoodTagSummaryDto(moodTagId = 3, code = "ACTIVE", name = "활발한")
    private val moodRomantic = MoodTagSummaryDto(moodTagId = 4, code = "ROMANTIC", name = "로맨틱")
    private val moodModern = MoodTagSummaryDto(moodTagId = 5, code = "MODERN", name = "모던한")
    private val moodCalm = MoodTagSummaryDto(moodTagId = 6, code = "CALM", name = "차분한")

    private val foodKorean = FoodCategorySummaryDto(foodCategoryId = 1, code = "KOREAN", name = "한식")
    private val foodJapanese = FoodCategorySummaryDto(foodCategoryId = 2, code = "JAPANESE", name = "일식")
    private val foodWestern = FoodCategorySummaryDto(foodCategoryId = 3, code = "WESTERN", name = "양식")
    private val foodChinese = FoodCategorySummaryDto(foodCategoryId = 4, code = "CHINESE", name = "중식")
    private val foodSnack = FoodCategorySummaryDto(foodCategoryId = 5, code = "SNACK", name = "분식")
    private val foodDessert = FoodCategorySummaryDto(foodCategoryId = 6, code = "DESSERT", name = "디저트")

    // 분위기 6종 전체 목록(코드 → 요약) — mood-tags 저장 mock 응답 구성에 사용.
    private val allMoods =
        listOf(moodHip, moodQuiet, moodActive, moodRomantic, moodModern, moodCalm)

    // 음식 6종 전체 목록(코드 → 요약) — food-categories 저장 mock 응답 구성에 사용.
    private val allFoods =
        listOf(foodKorean, foodJapanese, foodWestern, foodChinese, foodSnack, foodDessert)

    // ---------- 임시 코스 (POST /api/course-drafts) ----------

    val courseDraft = CourseDraftCreateResponseDto(
        courseDraftId = 15,
        draftStatus = "MOOD_SELECTING",
        expiresAt = null,
        createdAt = "2026-07-26T15:30:00",
    )

    // ---------- 기준 데이터 조회 (GET /api/mood-tags, GET /api/food-categories) ----------

    /** 분위기 태그 목록. moodTagId·code·name 은 API 명세 확정본과 동일하다. */
    val moodTags = MoodTagListResponseDto(
        moodTags = listOf(
            MoodTagDto(1, "HIP", "힙한", "트렌디하고 감각적인 분위기", 1),
            MoodTagDto(2, "QUIET", "조용한", "차분하고 대화하기 좋은 분위기", 2),
            MoodTagDto(3, "ACTIVE", "활발한", "밝고 에너지 있는 분위기", 3),
            MoodTagDto(4, "ROMANTIC", "로맨틱", "데이트에 어울리는 감성적인 분위기", 4),
            MoodTagDto(5, "MODERN", "모던한", "깔끔하고 세련된 분위기", 5),
            MoodTagDto(6, "CALM", "차분한", "편안하고 안정적인 분위기", 6),
        ),
    )

    /** 음식 카테고리 목록. foodCategoryId·code·name 은 API 명세 확정본과 동일하다. */
    val foodCategories = FoodCategoryListResponseDto(
        foodCategories = listOf(
            FoodCategoryDto(1, "KOREAN", "한식", "한식 음식 카테고리", 1),
            FoodCategoryDto(2, "JAPANESE", "일식", "일식 음식 카테고리", 2),
            FoodCategoryDto(3, "WESTERN", "양식", "양식 음식 카테고리", 3),
            FoodCategoryDto(4, "CHINESE", "중식", "중식 음식 카테고리", 4),
            FoodCategoryDto(5, "SNACK", "분식", "분식 음식 카테고리", 5),
            FoodCategoryDto(6, "DESSERT", "디저트", "디저트 음식 카테고리", 6),
        ),
    )

    // ---------- 분위기 태그 저장 (PUT /api/course-drafts/{id}/mood-tags) ----------

    /** [moodTagIds] 로 들어온 id 에 해당하는 요약을 그대로 돌려준다(서버가 저장 후 echo 하는 형태). */
    fun moodTagsSaveResult(moodTagIds: List<Long>) = CourseDraftMoodTagSaveResponseDto(
        courseDraftId = courseDraft.courseDraftId,
        draftStatus = "FOOD_SELECTING",
        moodTags = allMoods.filter { it.moodTagId in moodTagIds },
    )

    // ---------- 음식 카테고리 저장 (PUT /api/course-drafts/{id}/food-categories) ----------

    /** [foodCategoryIds] 로 들어온 id 에 해당하는 요약을 그대로 돌려준다. */
    fun foodCategoriesSaveResult(foodCategoryIds: List<Long>) = CourseDraftFoodCategorySaveResponseDto(
        courseDraftId = courseDraft.courseDraftId,
        draftStatus = "BASE_PLACE_SELECTING",
        foodCategories = allFoods.filter { it.foodCategoryId in foodCategoryIds },
    )

    // ---------- 카테고리 탭 (GET /api/place-categories) ----------

    val placeCategories = PlaceCategoryListResponseDto(
        placeCategories = listOf(
            PlaceCategoryResponseDto(1, "CAFE", "카페", "카페 및 디저트 장소", 1),
            PlaceCategoryResponseDto(2, "RESTAURANT", "식당", "식사 장소", 2),
            PlaceCategoryResponseDto(3, "ACTIVITY", "액티비티", "체험 및 활동 장소", 3),
            PlaceCategoryResponseDto(4, "BAR", "바", "술집 및 바 장소", 4),
        ),
    )

    // ---------- 주변 핫플 (GET /api/course-drafts/{id}/hot-places) ----------

    val hotPlaces = HotPlaceResultDto(
        recommendationLogId = 120,
        courseDraftId = courseDraft.courseDraftId,
        draftStatus = "BASE_PLACE_SELECTING",
        // 위치 권한 플로우 제외 범위라 앱이 좌표를 보내지 않는다 → 서버도 위치 미사용으로 응답.
        locationAvailable = false,
        places = listOf(
            hotPlace(
                placeId = 21,
                name = "애몽",
                address = "서울 마포구 연남로3길 13",
                area = areaYeonnam,
                category = categoryRestaurant,
                subCategory = "양식",
                rank = 1,
                matchedMoodCount = 2,
                reason = "선택한 분위기와 잘 어울려요.",
                imageUrl = MockImages.CAFE_MATCHA_LATTE,
            ),
            hotPlace(
                placeId = 22,
                name = "코이크",
                address = "서울 마포구 동교로39길 8",
                area = areaYeonnam,
                category = categoryCafe,
                subCategory = "디저트 카페",
                rank = 2,
                matchedMoodCount = 1,
                reason = "원하는 음식 취향과 잘 맞아요.",
                imageUrl = MockImages.CAFE_ICED_COFFEE,
            ),
            hotPlace(
                placeId = 23,
                name = "겸사서울",
                address = "서울 마포구 성미산로 184",
                area = areaHongdae,
                category = categoryCafe,
                subCategory = "브런치 카페",
                rank = 3,
                matchedMoodCount = 1,
                reason = "홍대 추천 장소예요.",
                imageUrl = MockImages.RESTAURANT_KOREAN,
            ),
            hotPlace(
                placeId = 24,
                name = "더바이브올스",
                address = "서울 성동구 연무장길 33",
                area = areaSeongsu,
                category = categoryBar,
                subCategory = "와인바",
                rank = 4,
                matchedMoodCount = 0,
                reason = "성수 추천 장소예요.",
                imageUrl = MockImages.BAR_COCKTAIL,
            ),
        ),
    )

    // ---------- 기준 장소 검색 (GET /api/places/search?query=) ----------

    /**
     * 검색 결과 mock. 명세대로 [query] 를 그대로 echo 하고 [PlaceSearchResultDto.resultCount] 를 맞춘다.
     *
     * 세 번째 결과는 **내부 DB 미등록 장소**(placeId = null)라 상세 진입이 막히고
     * 기준 장소 설정 시 externalPlaceId 로 전달돼야 하는 케이스다.
     */
    fun searchResult(query: String): PlaceSearchResultDto {
        val places = listOf(
            SearchPlaceDto(
                externalPlaceId = "1234567890",
                placeId = 21,
                name = "애몽",
                address = "서울 마포구 연남동 227-15",
                roadAddress = "서울 마포구 연남로3길 13",
                latitude = 37.561234,
                longitude = 126.923456,
                phone = "02-1234-5678",
                sourceUrl = "https://place.map.kakao.com/1234567890",
                area = areaYeonnam,
                category = categoryRestaurant,
                subCategory = "양식",
                isRegistered = true,
                imageUrl = MockImages.CAFE_MATCHA_LATTE,
                imageType = "PLACE_IMAGE",
                detailAvailable = true,
            ),
            SearchPlaceDto(
                externalPlaceId = "2345678901",
                placeId = 22,
                name = "코이크",
                address = "서울 마포구 연남동 390-70",
                roadAddress = "서울 마포구 동교로39길 8",
                latitude = 37.562100,
                longitude = 126.924500,
                phone = null,
                sourceUrl = "https://place.map.kakao.com/2345678901",
                area = areaYeonnam,
                category = categoryCafe,
                subCategory = "디저트 카페",
                isRegistered = true,
                imageUrl = MockImages.CAFE_ICED_COFFEE,
                imageType = "PLACE_IMAGE",
                detailAvailable = true,
            ),
            SearchPlaceDto(
                externalPlaceId = "9876543210",
                placeId = null,
                name = "연남 카페 어딘가",
                address = "서울 마포구 연남동 200-10",
                roadAddress = "서울 마포구 성미산로20길 10",
                latitude = 37.563000,
                longitude = 126.925300,
                phone = null,
                sourceUrl = "https://place.map.kakao.com/9876543210",
                area = areaYeonnam,
                category = categoryCafe,
                subCategory = "카페",
                isRegistered = false,
                // 내부 미등록 장소라 대표 사진이 없다 → 카테고리 기본 이미지가 내려오는 케이스.
                imageUrl = MockImages.SHOP_STOREFRONT,
                imageType = "CATEGORY_DEFAULT",
                detailAvailable = false,
            ),
        )
        return PlaceSearchResultDto(query = query, resultCount = places.size, places = places)
    }

    // ---------- 카테고리별 추천 (GET /api/course-drafts/{id}/recommended-places) ----------

    /**
     * 카테고리 탭별 추천 카드 mock. [placeCategoryCode] 에 해당하는 카드를 반환한다.
     * matchedMoodTags 를 카테고리마다 다르게 담아 분위기 6종 카드 색상을 모두 확인할 수 있다.
     *
     * 빈 상태(Empty) 화면을 확인하려면 아래 map 에서 해당 코드의 리스트를 비우면 된다.
     */
    fun recommendedPlaces(placeCategoryCode: String): RecommendedPlaceResultDto {
        val category = when (placeCategoryCode) {
            "RESTAURANT" -> categoryRestaurant
            "ACTIVITY" -> categoryActivity
            "BAR" -> categoryBar
            else -> categoryCafe
        }
        val places = when (placeCategoryCode) {
            "RESTAURANT" -> listOf(
                recommendedPlace(31, "쥬노이", "서울 마포구 신촌로 42-5", areaHongdae, category, "한식", 1, listOf(moodActive), 380, MockImages.RESTAURANT_KOREAN),
                recommendedPlace(32, "이밥", "서울 마포구 연희로 33", areaYeonnam, category, "한식", 2, listOf(moodCalm), 640, MockImages.SHOP_STOREFRONT),
            )

            "ACTIVITY" -> listOf(
                recommendedPlace(41, "라임아트 서울", "서울 마포구 북촌로 10", areaHongdae, category, "전시", 1, listOf(moodModern), 720, MockImages.ACTIVITY_WORKSHOP),
                recommendedPlace(42, "성수 방탈출", "서울 성동구 아차산로 17", areaSeongsu, category, "방탈출", 2, listOf(moodActive, moodHip), 1400, MockImages.SHOP_STOREFRONT),
            )

            "BAR" -> listOf(
                recommendedPlace(51, "더바이브올스", "서울 성동구 연무장길 33", areaSeongsu, category, "와인바", 1, listOf(moodRomantic), 900, MockImages.BAR_COCKTAIL),
                recommendedPlace(52, "연남 이자카야", "서울 마포구 연남로 25", areaYeonnam, category, "이자카야", 2, listOf(moodHip), 1100, MockImages.RESTAURANT_KOREAN),
            )

            else -> listOf(
                recommendedPlace(
                    61, "Everyday HappyBirthDay", "서울 마포구 연희로 33", areaYeonnam, category, "디저트 카페",
                    1, listOf(moodRomantic, moodCalm), 420, MockImages.CAFE_MATCHA_LATTE,
                ),
                recommendedPlace(62, "코이크", "서울 마포구 동교로39길 8", areaYeonnam, category, "디저트 카페", 2, listOf(moodQuiet), 560, MockImages.CAFE_ICED_COFFEE),
                recommendedPlace(63, "겸사서울", "서울 마포구 성미산로 184", areaHongdae, category, "브런치 카페", 3, listOf(moodModern), 980, MockImages.RESTAURANT_KOREAN),
            )
        }
        return RecommendedPlaceResultDto(
            recommendationLogId = 310,
            courseDraftId = courseDraft.courseDraftId,
            draftStatus = "PLACE_SELECTING",
            placeCategory = category,
            basePlace = BasePlaceSummaryDto(placeId = 21, name = "애몽", area = areaYeonnam),
            appliedRelaxationLevel = 1,
            places = places,
        )
    }

    // ---------- 장소 상세 (GET /api/places/{placeId}) ----------

    /** 장소 상세 mock. 어떤 [placeId] 로 들어와도 같은 상세를 돌려주되 id 만 맞춰준다. */
    fun placeDetail(placeId: Long) = PlaceDetailDto(
        placeId = placeId,
        name = "애몽",
        address = "서울 마포구 연남동 227-15",
        roadAddress = "서울 마포구 연남로3길 13",
        latitude = 37.561234,
        longitude = 126.923456,
        phone = "02-1234-5678",
        subCategory = "디저트 카페",
        defaultImageUrl = MockImages.CAFE_MATCHA_LATTE,
        businessStatus = "OPEN",
        lastOrderTime = "20:30",
        placeCategory = categoryCafe,
        primaryFoodCategory = foodDessert,
        foodCategories = listOf(foodDessert, foodWestern),
        moodTags = listOf(moodRomantic, moodCalm),
        // 상단 캐러셀용 대표 사진. 2장 이상이라 "n / m" 인디케이터·스와이프가 함께 확인된다.
        imageUrls = listOf(
            MockImages.CAFE_MATCHA_LATTE,
            MockImages.CAFE_ICED_COFFEE,
            MockImages.RESTAURANT_KOREAN,
        ),
        // 내부 사진 섹션(2열 메이슨리)용. 홀·짝 컬럼이 모두 채워지도록 짝수로 둔다.
        interiorImageUrls = listOf(
            MockImages.SHOP_STOREFRONT,
            MockImages.CAFE_ICED_COFFEE,
            MockImages.CAFE_MATCHA_LATTE,
            MockImages.RESTAURANT_KOREAN,
            MockImages.ACTIVITY_WORKSHOP,
            MockImages.BAR_COCKTAIL,
        ),
        menus = listOf(
            PlaceMenuDto(placeMenuId = 1, name = "과일 소르베", price = 13000, imageUrl = MockImages.CAFE_MATCHA_LATTE),
            // price = null → 화면에 "변동" 으로 표시된다. 사진 없는 메뉴(imageUrl = null) 케이스도 겸한다.
            PlaceMenuDto(placeMenuId = 2, name = "생과일 파르페", price = null, imageUrl = null),
            PlaceMenuDto(placeMenuId = 3, name = "애몽 소다", price = 7500, imageUrl = MockImages.CAFE_ICED_COFFEE),
        ),
        defaultRecommendReason = "감성적인 분위기와 디저트가 잘 어울리는 장소입니다.",
    )

    // ---------- 생성 헬퍼 ----------

    private fun hotPlace(
        placeId: Long,
        name: String,
        address: String,
        area: AreaSummaryDto,
        category: PlaceCategorySummaryDto,
        subCategory: String,
        rank: Int,
        matchedMoodCount: Int,
        reason: String,
        imageUrl: String,
    ) = HotPlaceDto(
        placeId = placeId,
        name = name,
        address = address,
        roadAddress = address,
        latitude = 37.561234 + rank * 0.001,
        longitude = 126.923456 + rank * 0.001,
        area = area,
        category = category,
        subCategory = subCategory,
        imageUrl = imageUrl,
        rank = rank,
        // locationAvailable = false 인 응답이라 거리 관련 필드는 명세대로 null 이다.
        distanceMeters = null,
        isNearby = null,
        matchedMoodCount = matchedMoodCount,
        matchedFoodCount = null,
        recommendationReason = reason,
        detailAvailable = true,
    )

    private fun recommendedPlace(
        placeId: Long,
        name: String,
        address: String,
        area: AreaSummaryDto,
        category: PlaceCategorySummaryDto,
        subCategory: String,
        rank: Int,
        moodTags: List<MoodTagSummaryDto>,
        distanceMeters: Int,
        imageUrl: String,
    ) = RecommendedPlaceDto(
        placeId = placeId,
        name = name,
        address = address,
        roadAddress = address,
        latitude = 37.561234 + rank * 0.001,
        longitude = 126.923456 + rank * 0.001,
        area = area,
        category = category,
        subCategory = subCategory,
        imageUrl = imageUrl,
        rank = rank,
        distanceMeters = distanceMeters,
        matchedMoodCount = moodTags.size,
        matchedMoodTags = moodTags,
        matchedFoodCount = 1,
        internalScore = 20 - rank,
        recommendationReasons = listOf(
            "선택한 ${moodTags.joinToString("·") { it.name }} 분위기와 잘 어울려요.",
            "기준 장소 바로 근처에 있어요.",
        ),
        alreadySelected = false,
        detailAvailable = true,
    )
}
