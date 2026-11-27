package io.github.omochice.pinosu.feature.feed.di

import io.github.omochice.pinosu.core.di.NetworkModule
import io.github.omochice.pinosu.feature.feed.data.http.StaleCacheOnNetworkFailure
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import okhttp3.Cache

class FeedHttpModuleTest {

  private val cacheDir = Files.createTempDirectory("feed-cache").toFile()
  private val cache = Cache(cacheDir, 1024L * 1024)

  @AfterTest
  fun tearDown() {
    cache.close()
    cacheDir.deleteRecursively()
  }

  @Test
  fun `feeds are downloaded through the cache while the shared client stays uncached`() {
    val shared = NetworkModule.provideOkHttpClient()

    val feedClient = FeedHttpModule.provideFeedHttpClient(shared, cache)

    assertSame(cache, feedClient.cache)
    assertNull(shared.cache)
  }

  @Test
  fun `only the feed client falls back to a stale cached feed when offline`() {
    val shared = NetworkModule.provideOkHttpClient()

    val feedClient = FeedHttpModule.provideFeedHttpClient(shared, cache)

    assertTrue(feedClient.interceptors.any { it is StaleCacheOnNetworkFailure })
    assertTrue(shared.interceptors.none { it is StaleCacheOnNetworkFailure })
  }

  @Test
  fun `the feed client keeps the timeouts and connections of the shared client`() {
    val shared = NetworkModule.provideOkHttpClient()

    val feedClient = FeedHttpModule.provideFeedHttpClient(shared, cache)

    assertEquals(shared.connectTimeoutMillis, feedClient.connectTimeoutMillis)
    assertEquals(shared.readTimeoutMillis, feedClient.readTimeoutMillis)
    assertSame(shared.connectionPool, feedClient.connectionPool)
  }
}
