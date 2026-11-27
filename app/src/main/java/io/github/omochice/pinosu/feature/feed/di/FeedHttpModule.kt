package io.github.omochice.pinosu.feature.feed.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.omochice.pinosu.feature.feed.data.http.FeedHttp
import io.github.omochice.pinosu.feature.feed.data.http.StaleCacheOnNetworkFailure
import java.io.File
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient

/** Hilt module for the HTTP client that subscribed feeds are downloaded with */
@Module
@InstallIn(SingletonComponent::class)
object FeedHttpModule {

  private const val CACHE_DIRECTORY = "feed_http_cache"
  private const val CACHE_BYTES = 20L * 1024 * 1024

  /** @return Disk cache of downloaded feeds, in the app's cache directory */
  @Provides
  @Singleton
  @FeedHttp
  fun provideFeedHttpCache(@ApplicationContext context: Context): Cache =
      Cache(File(context.cacheDir, CACHE_DIRECTORY), CACHE_BYTES)

  /**
   * @param client Client shared with the rest of the app, whose connections and settings are reused
   * @param cache Cache the downloaded feeds are kept in
   * @return Client that downloads feeds through [cache], falling back to a stale cached feed when
   *   the network cannot be reached
   */
  @Provides
  @Singleton
  @FeedHttp
  fun provideFeedHttpClient(client: OkHttpClient, @FeedHttp cache: Cache): OkHttpClient =
      client.newBuilder().cache(cache).addInterceptor(StaleCacheOnNetworkFailure()).build()
}
