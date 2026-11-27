package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.metadata.UrlMetadata
import io.github.omochice.pinosu.core.metadata.UrlMetadataFetcher
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageDataStoreFile
import io.github.omochice.pinosu.feature.feed.data.local.ArticleImageStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest

class OgpArticleImageRepositoryTest {

  /** Clock the test moves forward to make stored answers older */
  private class MovableClock(var now: Instant) : Clock() {
    override fun instant(): Instant = now

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId): Clock = this
  }

  private val fetcher = mockk<UrlMetadataFetcher>()
  private val storeFile = ArticleImageDataStoreFile()
  private val clock = MovableClock(Instant.parse("2026-01-01T00:00:00Z"))

  @AfterTest fun tearDown() = runBlocking { storeFile.delete() }

  /** A repository of a fresh run of the app, which knows only what earlier runs stored */
  private suspend fun repository() =
      OgpArticleImageRepository(fetcher, ArticleImageStore(storeFile.open()), clock)

  private fun ogImage(imageUrl: String?, link: String = LINK) {
    coEvery { fetcher.fetchMetadata(link) } returns
        Result.success(UrlMetadata(title = "Article", imageUrl = imageUrl))
  }

  @Test
  fun `og image of the article page is the image`() = runTest {
    ogImage("https://example.com/og.png")

    assertEquals("https://example.com/og.png", repository().findImage(LINK))
  }

  @Test
  fun `http og image is kept`() = runTest {
    ogImage("http://example.com/og.png")

    assertEquals("http://example.com/og.png", repository().findImage(LINK))
  }

  @Test
  fun `og image with a scheme other than http and https is rejected`() = runTest {
    val repository = repository()
    listOf(
            "javascript:alert(1)",
            "data:image/png;base64,AAAA",
            "intent://scan/#Intent;scheme=zxing;end",
            "file:///sdcard/a.png",
            "ftp://example.com/a.png")
        .forEachIndexed { n, imageUrl ->
          val link = "$LINK/$n"
          ogImage(imageUrl, link)

          assertNull(repository.findImage(link), "$imageUrl should be rejected")
        }
  }

  @Test
  fun `page without an og image has no image`() = runTest {
    ogImage(null)

    assertNull(repository().findImage(LINK))
  }

  @Test
  fun `page that cannot be fetched has no image`() = runTest {
    coEvery { fetcher.fetchMetadata(LINK) } returns Result.failure(IOException("timeout"))

    assertNull(repository().findImage(LINK))
  }

  @Test
  fun `an image found once is reused by a later run of the app without fetching the page`() =
      runTest {
        ogImage(IMAGE)
        repository().findImage(LINK)
        clock.now += Duration.ofDays(6)

        val image = repository().findImage(LINK)

        assertEquals(IMAGE, image)
        coVerify(exactly = 1) { fetcher.fetchMetadata(LINK) }
      }

  @Test
  fun `a page found to have no image is not fetched again by a later run`() = runTest {
    ogImage(null)
    repository().findImage(LINK)

    val image = repository().findImage(LINK)

    assertNull(image)
    coVerify(exactly = 1) { fetcher.fetchMetadata(LINK) }
  }

  @Test
  fun `a page that could not be fetched is fetched again by a later run`() = runTest {
    coEvery { fetcher.fetchMetadata(LINK) } returns Result.failure(IOException("timeout"))
    repository().findImage(LINK)
    ogImage(IMAGE)

    val image = repository().findImage(LINK)

    assertEquals(IMAGE, image)
    coVerify(exactly = 2) { fetcher.fetchMetadata(LINK) }
  }

  @Test
  fun `a stored answer is looked up again once it is seven days old`() = runTest {
    ogImage(null)
    repository().findImage(LINK)
    clock.now += OgpArticleImageRepository.LOOKUP_INTERVAL
    ogImage(IMAGE)

    val image = repository().findImage(LINK)

    assertEquals(IMAGE, image)
    coVerify(exactly = 2) { fetcher.fetchMetadata(LINK) }
  }

  @Test
  fun `an old answer is still given when the page cannot be read again`() = runTest {
    ogImage(IMAGE)
    repository().findImage(LINK)
    clock.now += OgpArticleImageRepository.LOOKUP_INTERVAL
    coEvery { fetcher.fetchMetadata(LINK) } returns Result.failure(IOException("timeout"))

    val image = repository().findImage(LINK)

    assertEquals(IMAGE, image)
    coVerify(exactly = 2) { fetcher.fetchMetadata(LINK) }
  }

  @Test
  fun `an old answer kept after a failed lookup is looked up again by the next run`() = runTest {
    ogImage(null)
    repository().findImage(LINK)
    clock.now += OgpArticleImageRepository.LOOKUP_INTERVAL
    coEvery { fetcher.fetchMetadata(LINK) } returns Result.failure(IOException("timeout"))
    repository().findImage(LINK)
    ogImage(IMAGE)

    val image = repository().findImage(LINK)

    assertEquals(IMAGE, image)
    coVerify(exactly = 3) { fetcher.fetchMetadata(LINK) }
  }

  companion object {
    private const val LINK = "https://example.com/article"
    private const val IMAGE = "https://example.com/og.png"
  }
}
