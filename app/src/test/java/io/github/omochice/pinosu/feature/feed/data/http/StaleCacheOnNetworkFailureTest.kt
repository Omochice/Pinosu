package io.github.omochice.pinosu.feature.feed.data.http

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient

class StaleCacheOnNetworkFailureTest {

  private val server = MockWebServer().apply { start() }
  private val cacheDir: File = Files.createTempDirectory("feed-cache").toFile()
  private val cache = Cache(cacheDir, CACHE_BYTES)
  private val client =
      OkHttpClient.Builder().cache(cache).addInterceptor(StaleCacheOnNetworkFailure()).build()
  private val fetcher = HttpDocumentFetcher(client)
  private val url = server.url("/feed.xml").toString()

  @AfterTest
  fun tearDown() {
    server.close()
    cache.close()
    cacheDir.deleteRecursively()
  }

  @Test
  fun `a stale cached feed is served when the server cannot be reached`() = runTest {
    server.enqueue(feed("max-age=0").addHeader("ETag", "\"v1\"").build())
    fetcher.fetch(url).getOrThrow()
    server.close()

    val document = fetcher.fetch(url).getOrThrow()

    assertEquals(BODY, document.openBody().readBytes().decodeToString())
  }

  @Test
  fun `a stale cached feed is served when a revalidation cannot reach the server`() = runTest {
    server.enqueue(feed("max-age=60").build())
    fetcher.fetch(url).getOrThrow()
    server.close()

    val document = fetcher.fetch(url, MAX_AGE_0).getOrThrow()

    assertEquals(BODY, document.openBody().readBytes().decodeToString())
  }

  @Test
  fun `a feed with nothing cached fails with the network error when unreachable`() = runTest {
    server.close()

    val error = fetcher.fetch(url).exceptionOrNull()

    assertIs<IOException>(error)
  }

  @Test
  fun `a cached feed sent with no-cache is not served when unreachable`() = runTest {
    server.enqueue(feed("no-cache").addHeader("ETag", "\"v1\"").build())
    fetcher.fetch(url).getOrThrow()
    assertEquals(listOf(url), cache.urls().asSequence().toList(), "the response is cached")
    server.close()

    val error = fetcher.fetch(url).exceptionOrNull()

    assertIs<IOException>(error)
  }

  @Test
  fun `a stale cached feed sent with must-revalidate is not served when unreachable`() = runTest {
    server.enqueue(feed("max-age=0, must-revalidate").addHeader("ETag", "\"v1\"").build())
    fetcher.fetch(url).getOrThrow()
    assertEquals(listOf(url), cache.urls().asSequence().toList(), "the response is cached")
    server.close()

    val error = fetcher.fetch(url).exceptionOrNull()

    assertIs<IOException>(error)
  }

  @Test
  fun `a server error is reported rather than answered from the cache`() = runTest {
    server.enqueue(feed("max-age=0").build())
    server.enqueue(MockResponse.Builder().code(503).build())
    fetcher.fetch(url).getOrThrow()

    val error = fetcher.fetch(url).exceptionOrNull()

    assertIs<HttpStatusException>(error)
    assertEquals(503, error.code)
  }

  @Test
  fun `only-if-cached with nothing cached is answered with 504 by OkHttp`() = runTest {
    val plain = HttpDocumentFetcher(OkHttpClient.Builder().cache(cache).build())

    val error = plain.fetch(url, CacheControl.FORCE_CACHE).exceptionOrNull()

    assertIs<HttpStatusException>(error)
    assertEquals(504, error.code)
    assertEquals(0, server.requestCount)
  }

  private fun feed(cacheControl: String): MockResponse.Builder =
      MockResponse.Builder()
          .addHeader("Content-Type", "application/rss+xml")
          .addHeader("Cache-Control", cacheControl)
          .body(BODY)

  private companion object {
    const val CACHE_BYTES = 1024L * 1024
    const val BODY = "<rss version=\"2.0\"><channel><title>Cached</title></channel></rss>"
    val MAX_AGE_0: CacheControl = CacheControl.Builder().maxAge(0, TimeUnit.SECONDS).build()
  }
}
