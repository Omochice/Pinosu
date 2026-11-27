package io.github.omochice.pinosu.feature.feed.data.http

import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Cache
import okhttp3.OkHttpClient

class HttpDocumentFetcherCacheTest {

  private val server = MockWebServer().apply { start() }
  private val cacheDir: File = Files.createTempDirectory("feed-cache").toFile()
  private val client = OkHttpClient.Builder().cache(Cache(cacheDir, CACHE_BYTES)).build()
  private val fetcher = HttpDocumentFetcher(client)
  private val url = server.url("/feed.xml").toString()

  @AfterTest
  fun tearDown() {
    server.close()
    client.cache?.close()
    cacheDir.deleteRecursively()
  }

  @Test
  fun `a fresh response read through the size cap is served from the cache the next time`() =
      runTest {
        server.enqueue(feed().addHeader("Cache-Control", "max-age=3600").build())

        fetcher.fetch(url).getOrThrow()
        val second = fetcher.fetch(url).getOrThrow()

        assertEquals(1, server.requestCount)
        assertEquals(BODY, second.openBody().readBytes().decodeToString())
      }

  @Test
  fun `a fresh chunked response is served from the cache the next time`() = runTest {
    server.enqueue(
        MockResponse.Builder()
            .addHeader("Content-Type", "application/rss+xml")
            .addHeader("Cache-Control", "max-age=3600")
            .chunkedBody(BODY, maxChunkSize = 8)
            .build())

    fetcher.fetch(url).getOrThrow()
    val second = fetcher.fetch(url).getOrThrow()

    assertEquals(1, server.requestCount)
    assertEquals(BODY, second.openBody().readBytes().decodeToString())
  }

  @Test
  fun `a response with only Last-Modified is taken as fresh for a tenth of its age`() = runTest {
    server.enqueue(
        feed()
            .addHeader("Date", httpDate(NOW))
            .addHeader("Last-Modified", httpDate(NOW.minus(Duration.ofDays(10))))
            .build())

    fetcher.fetch(url).getOrThrow()
    fetcher.fetch(url).getOrThrow()

    assertEquals(1, server.requestCount)
  }

  @Test
  fun `a response with only Last-Modified for a url with a query is asked for again`() = runTest {
    val queried = server.url("/feed.xml?format=rss").toString()
    repeat(2) {
      server.enqueue(
          feed()
              .addHeader("Date", httpDate(NOW))
              .addHeader("Last-Modified", httpDate(NOW.minus(Duration.ofDays(10))))
              .build())
    }

    fetcher.fetch(queried).getOrThrow()
    fetcher.fetch(queried).getOrThrow()

    assertEquals(2, server.requestCount)
  }

  private fun feed(): MockResponse.Builder =
      MockResponse.Builder().addHeader("Content-Type", "application/rss+xml").body(BODY)

  private fun httpDate(instant: Instant): String =
      DateTimeFormatter.RFC_1123_DATE_TIME.format(instant.atOffset(ZoneOffset.UTC))

  private companion object {
    const val CACHE_BYTES = 1024L * 1024
    val NOW: Instant = Instant.now()
    const val BODY = "<rss version=\"2.0\"><channel><title>Cached</title></channel></rss>"
  }
}
