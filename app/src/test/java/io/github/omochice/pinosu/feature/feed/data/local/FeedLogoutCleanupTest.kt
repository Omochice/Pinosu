package io.github.omochice.pinosu.feature.feed.data.local

import io.github.omochice.pinosu.feature.feed.data.http.HttpDocumentFetcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Cache
import okhttp3.OkHttpClient

class FeedLogoutCleanupTest {

  private val server = MockWebServer().apply { start() }
  private val cacheDir: File = Files.createTempDirectory("feed-cache").toFile()
  private val cache = Cache(cacheDir, 1024L * 1024)
  private val storeFile = ArticleImageDataStoreFile()

  @AfterTest
  fun tearDown() = runBlocking {
    server.close()
    cache.close()
    cacheDir.deleteRecursively()
    storeFile.delete()
  }

  @Test
  fun `the downloaded feeds and the article images are deleted`() = runTest {
    val url = server.url("/feed.xml").toString()
    server.enqueue(
        MockResponse.Builder().addHeader("Cache-Control", "max-age=3600").body("<rss/>").build())
    HttpDocumentFetcher(OkHttpClient.Builder().cache(cache).build()).fetch(url).getOrThrow()
    val store = ArticleImageStore(storeFile.open())
    store.save(LINK, ArticleImageLookup(IMAGE, 1L))

    FeedLogoutCleanup(cache, store).cleanUp()

    assertEquals(emptyList(), cache.urls().asSequence().toList())
    assertNull(store.find(LINK))
  }

  @Test
  fun `the article images are deleted even when the feeds cannot be`() = runTest {
    val brokenCache = mockk<Cache> { every { evictAll() } throws IOException("disk error") }
    val store = ArticleImageStore(storeFile.open())
    store.save(LINK, ArticleImageLookup(IMAGE, 1L))

    FeedLogoutCleanup(brokenCache, store).cleanUp()

    assertNull(store.find(LINK))
  }

  @Test
  fun `the feeds are deleted even when the article images cannot be`() = runTest {
    val brokenStore =
        mockk<ArticleImageStore> { coEvery { clear() } throws IOException("disk error") }
    val spiedCache = mockk<Cache>(relaxed = true)

    FeedLogoutCleanup(spiedCache, brokenStore).cleanUp()

    coVerify(exactly = 1) { spiedCache.evictAll() }
  }

  private companion object {
    const val LINK = "https://example.com/article"
    const val IMAGE = "https://example.com/og.png"
  }
}
