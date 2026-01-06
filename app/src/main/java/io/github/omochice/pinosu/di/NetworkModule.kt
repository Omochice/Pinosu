package io.github.omochice.pinosu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

/**
 * Network DI module
 *
 * Provides network-related dependencies such as OkHttpClient.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  /**
   * Provides OkHttpClient instance
   *
   * Singleton instance shared across the application for HTTP requests.
   */
  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
  }
}
