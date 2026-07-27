# 📄 TODAIT_AOS 컨벤션 문서

> 팀원 3명이 한 달 동안 충돌 없이 협업하기 위한 최소한의 규칙입니다.
> 규칙 변경은 팀 전원 합의 후 이 문서를 먼저 수정합니다.

---

## 1. 브랜치 네이밍 규칙

**GitHub Flow** 기반으로 운영합니다. `main`은 항상 빌드 가능한 상태를 유지합니다.

```
<타입>/#<이슈번호>-<간단한-설명(kebab-case)>
```

| 타입 | 용도 | 예시 |
| --- | --- | --- |
| `feat` | 기능 개발 | `feat/#12-login-screen` |
| `fix` | 버그 수정 | `fix/#31-crash-on-home` |
| `refactor` | 리팩토링 (동작 변경 없음) | `refactor/#40-course-viewmodel` |
| `chore` | 빌드/설정/의존성 | `chore/#3-add-hilt` |
| `docs` | 문서 | `docs/#5-update-readme` |

- 브랜치는 **반드시 이슈 생성 후** 이슈 번호를 붙여 생성합니다.
- `main`에 직접 push 금지 (브랜치 보호 규칙 설정).

## 2. 커밋 메시지 규칙

```
<타입>: <제목 (한글, 50자 이내, 마침표 없음)>

<본문 (선택) — 무엇을, 왜 변경했는지>
```

| 타입 | 설명 | 예시 |
| --- | --- | --- |
| `feat` | 새 기능 | `feat: 분위기 선택 화면 UI 구현` |
| `fix` | 버그 수정 | `fix: 코스 저장 중복 요청 방지` |
| `refactor` | 리팩토링 | `refactor: safeApiCall 에러 매핑 분리` |
| `style` | 포맷팅, 세미콜론 등 로직 무관 | `style: ktlint 포맷 적용` |
| `chore` | 설정/의존성/기타 | `chore: Kakao Map SDK 의존성 추가` |
| `docs` | 문서 | `docs: 컨벤션 문서에 PR 규칙 추가` |
| `test` | 테스트 코드 | `test: MoodSelectViewModel 단위 테스트 추가` |

- 커밋은 **작업 단위로 잘게** 나눕니다. (한 커밋 = 한 가지 의도)
- 이슈 연결이 필요하면 본문 마지막에 `#이슈번호`를 적습니다.

## 3. PR 규칙

- **PR 단위**: 이슈 1개 = 브랜치 1개 = PR 1개. 500라인 이상이면 분리 권장.
- **제목**: 커밋 규칙과 동일한 형식 — `feat: 기준 장소 설정 화면 구현 (#12)`
- **본문 필수 항목** (템플릿 사용)
  - 작업 내용 요약
  - 스크린샷/영상 (UI 변경 시 필수)
  - 리뷰어에게 궁금한 점 / 논의 필요 사항
- **리뷰어 지정**: 나머지 팀원 2명 모두 지정, **최소 1명 approve** 시 머지 가능.
  - 공통 모듈(`core`, `di`, `navigation`, `ui/theme`) 변경은 **2명 모두 approve** 필요.
- **머지 방식**: `Squash and merge` (커밋 히스토리를 PR 단위로 유지)
- **머지 조건**: ① approve 충족 ② 빌드/CI 통과 ③ 컨플릭트 해결 완료
- 리뷰는 **24시간 이내** 응답을 원칙으로 합니다. 급한 경우 카톡으로 호출.

## 4. 코드 네이밍 규칙

[Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html)을 기본으로 합니다.

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스 / interface / object | PascalCase | `CourseComposeViewModel` |
| Composable 함수 | PascalCase (명사형) | `PlaceCard()`, `MoodTagChip()` |
| 일반 함수 / 변수 | camelCase | `fetchNearbyPlaces()`, `selectedMoods` |
| 상수 (`const val`, companion) | UPPER_SNAKE_CASE | `MAX_MOOD_COUNT` |
| Boolean | is/has/can 접두사 | `isLoading`, `hasCoordinate` |
| StateFlow 백킹 필드 | `_` 접두사 | `_uiState` / `uiState` |
| 파일명 | 클래스명과 동일, 화면은 `~Screen.kt` | `BasePlaceScreen.kt` |
| 리소스(문자열 등) | snake_case, 화면 접두사 | `home_greeting_title` |
| 패키지 | 소문자, 언더스코어 없음 | `feature.course` |

- 함수는 **동사로 시작** (`load~`, `save~`, `toggle~`), UiEvent는 `on~` (`onMoodClick`).
- 매직 넘버 금지 — 의미 있는 상수로 추출.
- 사용하지 않는 import/코드는 머지 전 제거.

### 4.1 Drawable 리소스

Android `res/drawable`은 **하위 폴더를 만들 수 없습니다(flat 구조).** feature별 폴더 분류 대신 **파일명 접두사로 그룹핑**합니다.

**폴더 (밀도 한정자만 사용)**

| 종류 | 위치 |
| --- | --- |
| 벡터 (`VectorDrawable`, `.xml`) | `drawable/` |
| 래스터 (png/jpg) | `drawable-nodpi/` |
| 런처 아이콘 전용 | `mipmap-*` |

**파일명**: `<타입>_<도메인>_<설명>` — 전부 snake_case (소문자·숫자·`_`만)

| 타입 접두사 | 용도 | 예시 |
| --- | --- | --- |
| `ic_` | 아이콘 (벡터 우선) | `ic_place_pin` |
| `img_` | 사진 / 일러스트 / 장식 | `img_course_empty` |
| `bg_` | 배경 / 그라데이션 | `bg_home_gradient` |
| `shape_` | shape drawable (버튼·카드 배경) | `shape_button_primary` |
| `selector_` | 상태 selector | `selector_tab_icon` |
| `divider_` | 구분선 | `divider_list` |

- **도메인** = feature 이름 (`place`, `course`, `login`, `home`, `saved`, `mypage`). 2개 이상 화면에서 공통으로 쓰면 `common`.
- **브랜드 자산**은 도메인 대신 `todait` 사용: `ic_todait_logo`, `ic_todait_wordmark`.
- 같은 성격의 여러 개는 접미사 숫자로: `ic_place_deco_1`, `_2`, `_3`.
- 기존 파일(`ic_google.jpg`, `ic_kakao.png`)은 **소급 강제 아님**. 팀 합의 시 `ic_login_google` / `ic_login_kakao`로 일괄 rename(참조 코드 동반 수정).

## 5. 패키지 구조 규칙

```
com.umc.todait
├── core        # 앱 전역 공통 (base, network, util) — 특정 화면 의존 금지
├── di          # Hilt 모듈만 위치
├── navigation  # 라우트 정의(Screen), NavHost, 탭바
├── ui          # theme(디자인 시스템) + component(2개 이상 화면에서 쓰는 공통 컴포저블)
└── feature     # 화면 단위 패키지 (auth / home / course / saved / mypage)
```

- **feature 내부 기본 구성**
  ```
  feature/course
  ├── mood/          # 화면이 여러 개인 feature는 하위 패키지로 분리
  │   ├── MoodSelectScreen.kt
  │   ├── MoodSelectViewModel.kt
  │   └── MoodSelectUiState.kt
  └── data/          # 해당 feature 전용 DTO, Service, Repository
  ```
- **의존 방향**: `feature → core / ui / navigation` 단방향. feature 간 직접 참조 금지 (필요 시 core로 승격).
- 한 화면에서만 쓰는 컴포저블은 해당 feature에, **2개 이상 화면에서 쓰이면 `ui/component`로 이동**.
- DTO는 `data` 패키지 안에서만 사용하고 화면에는 UiModel로 매핑해 전달하는 것을 권장.

## 6. API 응답 / JSON 규약

API 명세서(Notion)의 **"공통 API 오류/빈 상태 처리"** 문서가 기준입니다. 아래는 앱에서 지켜야 할 요약입니다.

### 6.1 공통 응답 구조

성공·실패 모두 `isSuccess` / `code` / `message` / `result` 네 필드를 사용합니다. (`core/network/BaseResponse.kt`)

| 구분 | isSuccess | code 예시 | result |
| --- | --- | --- | --- |
| 성공 | `true` | `COMMON200`, `COURSE201`, `PLACE200`, `RECOMMENDATION200` | 데이터 객체 |
| 실패 | `false` | `AUTH401`, `COURSE404`, `PLACE_SEARCH400`, `KAKAO_API502` | **항상 `null`** |

- **실패는 HTTP 4xx/5xx로 내려옵니다.** HTTP Status와 body의 `code`를 **함께** 확인합니다 — 같은 403이라도 `AUTH403`(토큰 만료 → 재발급)과 `COURSE403`(소유권 없음 → 안내)의 처리가 다릅니다.
- `COMMON401` / `COMMON403`은 사용하지 않습니다. 인증은 `AUTH401`(인증 정보 없음) / `AUTH403`(토큰 무효·만료)입니다.
- 429·502(외부 API 한도/연결 실패)와 500은 재시도 안내 대상입니다.

### 6.2 필드 표기

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| JSON 필드명 (Request/Response) | **camelCase** | `courseDraftId`, `placeCategoryCode`, `createdAt` |
| DB 컬럼 | snake_case | `course_draft_id`, `created_at` |
| Boolean | 상태가 드러나는 이름, 문자열 금지 | `isRegistered`, `hasNext`, `detailAvailable` |
| 날짜·시간 | ISO-8601 문자열 | `"2026-07-25T15:30:00"` |

- DB 컬럼명과 JSON 필드명이 항상 같지는 않습니다. (예: `course_draft.status` → `draftStatus`)
- **DTO의 모든 프로퍼티에 `@SerializedName`을 명시합니다.** R8(난독화)을 켜면 어노테이션 없는 필드는 파싱이 깨집니다.

### 6.3 null과 빈 배열

- 목록 필드는 데이터가 없어도 **빈 배열 `[]`** 로 내려옵니다. `null`이 아닙니다.
- **빈 배열은 에러 화면이 아니라 빈 상태(Empty) 화면으로 처리합니다.**
- 단건 상세 리소스가 없으면 404 + 도메인 코드(`PLACE404` 등)로 내려옵니다.
- 선택적 단일 값은 `null`이 올 수 있습니다. (`memo`, `roadAddress`, `distanceMeters`, 가격 변동 메뉴의 `price` 등)
