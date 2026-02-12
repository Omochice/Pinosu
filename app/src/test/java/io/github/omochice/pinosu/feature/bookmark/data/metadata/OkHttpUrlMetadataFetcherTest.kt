package io.github.omochice.pinosu.feature.bookmark.data.metadata

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Unit tests for [OkHttpUrlMetadataFetcher] metadata fetching including og:image */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OkHttpUrlMetadataFetcherTest {

  private lateinit var okHttpClient: OkHttpClient
  private lateinit var fetcher: OkHttpUrlMetadataFetcher

  @Before
  fun setup() {
    okHttpClient = mockk()
    fetcher = OkHttpUrlMetadataFetcher(okHttpClient)
  }

  private fun mockHttpResponse(url: String, code: Int, body: String) {
    val call = mockk<Call>()
    val request = Request.Builder().url(url).build()
    val response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("OK")
            .body(body.toResponseBody())
            .build()
    every { okHttpClient.newCall(any()) } returns call
    every { call.execute() } returns response
  }

  @Test
  fun `fetchMetadata returns title and imageUrl from og tags`() = runTest {
    val html =
        """
        <html><head>
          <meta property="og:title" content="Test Article">
          <meta property="og:image" content="https://example.com/image.jpg">
        </head><body></body></html>
        """
            .trimIndent()
    val url = "https://example.com/page"
    mockHttpResponse(url, 200, html)

    val result = fetcher.fetchMetadata(url)

    assertTrue(result.isSuccess)
    val metadata = result.getOrNull()
    assertNotNull(metadata)
    assertEquals("Test Article", metadata!!.title)
    assertEquals("https://example.com/image.jpg", metadata.imageUrl)
  }

  @Test
  fun `fetchMetadata returns null imageUrl when og image is missing`() = runTest {
    val html =
        """
        <html><head>
          <meta property="og:title" content="No Image Article">
        </head><body></body></html>
        """
            .trimIndent()
    val url = "https://example.com/no-image"
    mockHttpResponse(url, 200, html)

    val result = fetcher.fetchMetadata(url)

    assertTrue(result.isSuccess)
    val metadata = result.getOrNull()
    assertNotNull(metadata)
    assertEquals("No Image Article", metadata!!.title)
    assertNull(metadata.imageUrl)
  }

  @Test
  fun `fetchMetadata returns null title and null imageUrl when no og tags present`() = runTest {
    val html = "<html><head></head><body></body></html>"
    val url = "https://example.com/empty"
    mockHttpResponse(url, 200, html)

    val result = fetcher.fetchMetadata(url)

    assertTrue(result.isSuccess)
    val metadata = result.getOrNull()
    assertNotNull(metadata)
    assertNull(metadata!!.title)
    assertNull(metadata.imageUrl)
  }

  @Test
  fun `fetchMetadata falls back to title tag when og title is missing`() = runTest {
    val html =
        """
        <html><head>
          <title>Fallback Title</title>
          <meta property="og:image" content="https://example.com/img.png">
        </head><body></body></html>
        """
            .trimIndent()
    val url = "https://example.com/fallback"
    mockHttpResponse(url, 200, html)

    val result = fetcher.fetchMetadata(url)

    assertTrue(result.isSuccess)
    val metadata = result.getOrNull()
    assertNotNull(metadata)
    assertEquals("Fallback Title", metadata!!.title)
    assertEquals("https://example.com/img.png", metadata.imageUrl)
  }

  @Test
  fun `fetchMetadata returns failure on HTTP error`() = runTest {
    val url = "https://example.com/error"
    mockHttpResponse(url, 500, "")

    val result = fetcher.fetchMetadata(url)

    assertTrue(result.isFailure)
  }

  @Test
  fun `fetchMetadata caches empty metadata and does not repeat HTTP call`() = runTest {
    val html = "<html><head></head><body></body></html>"
    val url = "https://example.com/no-og-tags"
    mockHttpResponse(url, 200, html)

    val firstResult = fetcher.fetchMetadata(url)
    val secondResult = fetcher.fetchMetadata(url)

    assertTrue(firstResult.isSuccess)
    assertTrue(secondResult.isSuccess)
    assertNull(firstResult.getOrNull()!!.title)
    assertNull(secondResult.getOrNull()!!.title)
    io.mockk.verify(exactly = 1) { okHttpClient.newCall(any()) }
  }
}
