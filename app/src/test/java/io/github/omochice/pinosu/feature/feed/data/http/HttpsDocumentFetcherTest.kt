package io.github.omochice.pinosu.feature.feed.data.http

import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class HttpsDocumentFetcherTest {

  private val http = OkHttpStub()
  private val fetcher = HttpsDocumentFetcher(HttpDocumentFetcher(http.client))

  @Test
  fun `https url is fetched as is`() = runTest {
    http.respond("https://example.com/feed", "<html></html>")

    val document = fetcher.fetch("https://example.com/feed").getOrThrow()

    assertEquals("https://example.com/feed", document.requestedUrl)
  }

  @Test
  fun `http url is fetched over https instead`() = runTest {
    http.respond("https://example.com/feed", "<html></html>")

    val document = fetcher.fetch("http://example.com/feed").getOrThrow()

    assertEquals("https://example.com/feed", document.requestedUrl)
    assertEquals(listOf("https"), http.requests.map { it.url.scheme })
  }

  @Test
  fun `http scheme is recognized regardless of case`() = runTest {
    http.respond("https://example.com/feed", "<html></html>")

    val document = fetcher.fetch("HTTP://example.com/feed").getOrThrow()

    assertEquals("https://example.com/feed", document.requestedUrl)
  }

  @Test
  fun `http url fails as not supported when https fails too`() = runTest {
    val ioError = IOException("connection refused")
    http.fail("https://example.com/feed", ioError)

    val error = fetcher.fetch("http://example.com/feed").exceptionOrNull()

    assertIs<HttpFeedNotSupportedException>(error)
    assertEquals("http://example.com/feed", error.url)
    assertSame(ioError, error.cause)
  }

  @Test
  fun `http url fails as not supported when https answers with an error status`() = runTest {
    http.respond("https://example.com/feed", "Not Found", code = 404)

    val error = fetcher.fetch("http://example.com/feed").exceptionOrNull()

    assertIs<HttpFeedNotSupportedException>(error)
    assertIs<HttpStatusException>(error.cause)
  }

  @Test
  fun `https url failure is reported as is`() = runTest {
    http.respond("https://example.com/feed", "Not Found", code = 404)

    val error = fetcher.fetch("https://example.com/feed").exceptionOrNull()

    assertIs<HttpStatusException>(error)
  }

  @Test
  fun `input without a scheme is fetched over https`() = runTest {
    http.respond("https://example.com/feed", "<html></html>")

    val document = fetcher.fetch("  example.com/feed  ").getOrThrow()

    assertEquals("https://example.com/feed", document.requestedUrl)
  }

  @Test
  fun `host with a port but no scheme is fetched over https`() = runTest {
    http.respond("https://example.com:8443/feed", "<html></html>")

    val document = fetcher.fetch("example.com:8443/feed").getOrThrow()

    assertEquals("https://example.com:8443/feed", document.requestedUrl)
  }

  @Test
  fun `url with another scheme fails without being rewritten`() = runTest {
    val result = fetcher.fetch("ftp://example.com/feed")

    assertIs<IllegalArgumentException>(result.exceptionOrNull())
  }
}
