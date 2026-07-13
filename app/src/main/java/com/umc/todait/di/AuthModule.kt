package com.umc.todait.di

import com.umc.todait.feature.auth.data.service.AuthService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * auth feature 전용 Hilt 모듈.
 * Repository/ViewModel 은 @Inject 생성자로 자동 주입되므로 별도 등록이 필요 없다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)
}
