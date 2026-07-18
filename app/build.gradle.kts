import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// local.properties에서 키를 읽어 BuildConfig로 주입
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.umc.todait"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.umc.todait"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String", "BASE_URL",
            localProperties.getProperty("BASE_URL") ?: "\"https://api.todait.example.com/\""
        )
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] =
            localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
        // KakaoMapSdk.init(context, key) 용. 로그인과 동일한 네이티브 앱 키를 코드에서 참조한다.
        buildConfigField(
            "String", "KAKAO_NATIVE_APP_KEY",
            "\"${localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""}\""
        )
        // 구글 로그인 serverClientId. 백엔드가 발급한 Web Client ID(공개값, 시크릿 아님).
        // local.properties 에 GOOGLE_WEB_CLIENT_ID 가 있으면 그 값을, 없으면 백엔드가 공유한 기본값을 사용.
        buildConfigField(
            "String", "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID")
                ?: "218676067662-pmt9jkjc0pqv87c09jcd07ngeaq0j48d.apps.googleusercontent.com"}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AndroidX Core / Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Location (현재 위치 1회 조회)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)

    // Map (Kakao Map SDK v2)
    implementation(libs.kakao.map)

    // Social login (카카오 로그인 + 구글 Credential Manager)
    implementation(libs.kakao.user)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)

    // Drag & drop 순서 변경 (선택한 장소 리스트)
    implementation(libs.reorderable)

    // Image
    implementation(libs.coil.compose)

    // Local storage
    implementation(libs.androidx.datastore.preferences)

    // Test
    testImplementation(libs.junit)
}
