package io.github.omochice.pinosu.feature.feed.data.http

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

class HttpDocumentFetcherTest {

  private val http = OkHttpStub()
  private val fetcher = HttpDocumentFetcher(http.client)

  @Test
  fun `successful response gives the body, the charset, and the requested and final urls`() =
      runTest {
        http.respond(PAGE_URL, "<html></html>", finalUrl = "https://www.example.com/blog/")

        val document = fetcher.fetch(PAGE_URL).getOrThrow()

        assertEquals(PAGE_URL, document.requestedUrl)
        assertEquals("https://www.example.com/blog/", document.finalUrl)
        assertContentEquals("<html></html>".toByteArray(), document.openBody().readBytes())
        assertEquals("utf-8", document.charset?.lowercase())
      }

  @Test
  fun `request identifies the app and prefers feed types, then html`() = runTest {
    http.respond(PAGE_URL, "<html></html>")

    fetcher.fetch(PAGE_URL)

    val request = http.requests.single()
    assertEquals("Pinosu/1.0 (Android)", request.header("User-Agent"))
    val accept = assertNotNull(request.header("Accept"))
    val feedTypeIndex = accept.indexOf("application/rss+xml")
    val htmlIndex = accept.indexOf("text/html")
    assertTrue(feedTypeIndex >= 0 && accept.contains("application/atom+xml"), accept)
    assertTrue(htmlIndex > feedTypeIndex, accept)
  }

  @Test
  fun `non-2xx response fails with the status code`() = runTest {
    http.respond(PAGE_URL, "Not Found", code = 404)

    val error = fetcher.fetch(PAGE_URL).exceptionOrNull()

    assertIs<HttpStatusException>(error)
    assertEquals(404, error.code)
  }

  @Test
  fun `body declared larger than the limit fails`() = runTest {
    val oversized = ByteArray(HttpDocumentFetcher.MAX_BODY_BYTES.toInt() + 1)
    http.respondWith(PAGE_URL, oversized.toResponseBody("text/html".toMediaType()))

    val error = fetcher.fetch(PAGE_URL).exceptionOrNull()

    assertIs<ResponseTooLargeException>(error)
  }

  @Test
  fun `body of unknown length that exceeds the limit fails`() = runTest {
    val source = Buffer().write(ByteArray(HttpDocumentFetcher.MAX_BODY_BYTES.toInt() + 1))
    http.respondWith(PAGE_URL, source.asResponseBody("text/html".toMediaType(), -1L))

    val error = fetcher.fetch(PAGE_URL).exceptionOrNull()

    assertIs<ResponseTooLargeException>(error)
  }

  @Test
  fun `body of unknown length exactly at the limit succeeds`() = runTest {
    val source = Buffer().write(ByteArray(HttpDocumentFetcher.MAX_BODY_BYTES.toInt()))
    http.respondWith(PAGE_URL, source.asResponseBody("text/html".toMediaType(), -1L))

    val document = fetcher.fetch(PAGE_URL).getOrThrow()

    assertEquals(HttpDocumentFetcher.MAX_BODY_BYTES.toInt(), document.openBody().readBytes().size)
  }

  @Test
  fun `IO exception fails with that exception`() = runTest {
    val ioError = IOException("connection reset")
    http.fail(PAGE_URL, ioError)

    val error = fetcher.fetch(PAGE_URL).exceptionOrNull()

    assertSame(ioError, error)
  }

  @Test
  fun `url with an unsupported scheme fails`() = runTest {
    val result = fetcher.fetch("ftp://example.com/feed.xml")

    assertIs<IllegalArgumentException>(result.exceptionOrNull())
  }

  companion object {
    private const val PAGE_URL = "https://example.com/blog"
  }
}
