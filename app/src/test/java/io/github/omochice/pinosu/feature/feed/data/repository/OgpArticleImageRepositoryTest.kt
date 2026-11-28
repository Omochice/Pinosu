package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.metadata.UrlMetadata
import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class OgpArticleImageRepositoryTest {

  private val fetcher = mockk<UrlMetadataFetcher>()
  private val repository = OgpArticleImageRepository(fetcher)

  private fun ogImage(imageUrl: String?) {
    coEvery { fetcher.fetchMetadata(LINK) } returns
        Result.success(UrlMetadata(title = "Article", imageUrl = imageUrl))
  }

  @Test
  fun `og image of the article page is the image`() = runTest {
    ogImage("https://example.com/og.png")

    assertEquals("https://example.com/og.png", repository.findImage(LINK))
  }

  @Test
  fun `http og image is kept`() = runTest {
    ogImage("http://example.com/og.png")

    assertEquals("http://example.com/og.png", repository.findImage(LINK))
  }

  @Test
  fun `og image with a scheme other than http and https is rejected`() = runTest {
    listOf(
            "javascript:alert(1)",
            "data:image/png;base64,AAAA",
            "intent://scan/#Intent;scheme=zxing;end",
            "file:///sdcard/a.png",
            "ftp://example.com/a.png")
        .forEach { imageUrl ->
          ogImage(imageUrl)

          assertNull(repository.findImage(LINK), "$imageUrl should be rejected")
        }
  }

  @Test
  fun `page without an og image has no image`() = runTest {
    ogImage(null)

    assertNull(repository.findImage(LINK))
  }

  @Test
  fun `page that cannot be fetched has no image`() = runTest {
    coEvery { fetcher.fetchMetadata(LINK) } returns Result.failure(IOException("timeout"))

    assertNull(repository.findImage(LINK))
  }

  companion object {
    private const val LINK = "https://example.com/article"
  }
}
