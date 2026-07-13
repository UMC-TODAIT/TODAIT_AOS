package com.umc.todait.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

/**
 * accessToken/refreshToken을 로컬(DataStore Preferences)에 저장·조회하는 공용 저장소.
 * 화면/ViewModel은 이 클래스를 통해서만 토큰에 접근하고, DataStore를 직접 다루지 않는다.
 */
@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val accessTokenFlow: Flow<String?> = context.authDataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val refreshTokenFlow: Flow<String?> = context.authDataStore.data.map { it[Keys.REFRESH_TOKEN] }

    suspend fun getAccessToken(): String? = accessTokenFlow.first()

    suspend fun getRefreshToken(): String? = refreshTokenFlow.first()

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun updateAccessToken(accessToken: String) {
        context.authDataStore.edit { prefs -> prefs[Keys.ACCESS_TOKEN] = accessToken }
    }

    suspend fun clearTokens() {
        context.authDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
        }
    }
}
