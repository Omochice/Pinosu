package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.feature.feed.data.http.FeedFixtures
import io.github.omochice.pinosu.feature.feed.data.http.HttpDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.HttpStatusException
import io.github.omochice.pinosu.feature.feed.data.http.HttpsDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.OkHttpStub
import io.github.omochice.pinosu.feature.feed.data.parser.JsoupFeedParser
import io.github.omochice.pinosu.feature.feed.data.parser.UnsupportedFeedFormatException
import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class HttpFeedRepositoryTest {

  private val http = OkHttpStub()
  private val repository =
      HttpFeedRepository(HttpsDocumentFetcher(HttpDocumentFetcher(http.client)), JsoupFeedParser())

  @Test
  fun `feed response is parsed`() = runTest {
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val feed = repository.fetchFeed(FEED_URL).getOrThrow()

    assertEquals(FEED_URL, feed.url)
    assertEquals("Example Blog", feed.title)
    assertEquals(listOf("First post"), feed.items.map { it.title })
  }

  @Test
  fun `feed reached after a redirect keeps the given url and resolves links against the final one`() =
      runTest {
        http.respond(
            FEED_URL,
            FeedFixtures.RSS,
            FeedFixtures.RSS_CONTENT_TYPE,
            finalUrl = "https://cdn.example.com/feeds/main.xml",
        )

        val feed = repository.fetchFeed(FEED_URL).getOrThrow()

        assertEquals(FEED_URL, feed.url)
        assertEquals(FEED_URL, feed.items.single().feedUrl)
        assertEquals("https://cdn.example.com/feeds/posts/1", feed.items.single().link)
      }

  @Test
  fun `stored http feed url is fetched over https and keeps the stored url as identity`() =
      runTest {
        http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

        val feed = repository.fetchFeed("http://example.com/feed.xml").getOrThrow()

        assertEquals("http://example.com/feed.xml", feed.url)
        assertEquals(listOf("https"), http.requests.map { it.url.scheme })
      }

  @Test
  fun `stored http feed url whose https counterpart fails is reported as http not supported`() =
      runTest {
        http.fail(FEED_URL, IOException("connection refused"))

        val error = repository.fetchFeed("http://example.com/feed.xml").exceptionOrNull()

        assertIs<HttpFeedNotSupportedException>(error)
      }

  @Test
  fun `html response fails as not a feed`() = runTest {
    http.respond(FEED_URL, FeedFixtures.html(""))

    val error = repository.fetchFeed(FEED_URL).exceptionOrNull()

    assertIs<UnsupportedFeedFormatException>(error)
  }

  @Test
  fun `non-2xx response fails`() = runTest {
    http.respond(FEED_URL, "Gone", code = 410)

    val error = repository.fetchFeed(FEED_URL).exceptionOrNull()

    assertIs<HttpStatusException>(error)
  }

  companion object {
    private const val FEED_URL = "https://example.com/feed.xml"
  }
}
