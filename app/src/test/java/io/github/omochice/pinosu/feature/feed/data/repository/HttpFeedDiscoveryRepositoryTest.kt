package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.feature.feed.data.http.FeedFixtures
import io.github.omochice.pinosu.feature.feed.data.http.HttpDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.HttpStatusException
import io.github.omochice.pinosu.feature.feed.data.http.HttpsDocumentFetcher
import io.github.omochice.pinosu.feature.feed.data.http.OkHttpStub
import io.github.omochice.pinosu.feature.feed.data.parser.JsoupFeedParser
import io.github.omochice.pinosu.feature.feed.data.parser.UnsupportedFeedFormatException
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import io.github.omochice.pinosu.feature.feed.domain.model.FeedResolution
import io.github.omochice.pinosu.feature.feed.domain.model.error.HttpFeedNotSupportedException
import io.github.omochice.pinosu.feature.feed.domain.model.error.NoFeedFoundException
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class HttpFeedDiscoveryRepositoryTest {

  private val http = OkHttpStub()
  private val repository =
      HttpFeedDiscoveryRepository(
          HttpsDocumentFetcher(HttpDocumentFetcher(http.client)),
          JsoupFeedParser(),
      )

  @Test
  fun `feed url resolves directly to the parsed feed`() = runTest {
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val resolution = assertIs<FeedResolution.Direct>(repository.resolve(FEED_URL).getOrThrow())

    assertEquals(FEED_URL, resolution.feedUrl)
    assertEquals(FEED_URL, resolution.feed.url)
    assertEquals("Example Blog", resolution.feed.title)
  }

  @Test
  fun `feed served as text html is still recognized as a feed`() = runTest {
    http.respond(FEED_URL, FeedFixtures.RSS, "text/html")

    val resolution = repository.resolve(FEED_URL).getOrThrow()

    assertIs<FeedResolution.Direct>(resolution)
  }

  @Test
  fun `page with one feed link follows it and resolves to that feed`() = runTest {
    http.respond(
        PAGE_URL,
        FeedFixtures.html(
            """<link rel="alternate" type="application/rss+xml" href="https://example.com/feed.xml">"""),
    )
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val resolution = assertIs<FeedResolution.Direct>(repository.resolve(PAGE_URL).getOrThrow())

    assertEquals(FEED_URL, resolution.feedUrl)
    assertEquals("Example Blog", resolution.feed.title)
  }

  @Test
  fun `page with two feed links gives both as candidates with their titles`() = runTest {
    http.respond(
        PAGE_URL,
        FeedFixtures.html(
            """
            <link rel="alternate" type="application/rss+xml" title="Posts" href="/posts.rss">
            <link rel="alternate" type="application/atom+xml" title="Comments" href="/comments.atom">
            """
                .trimIndent()),
    )

    val resolution = repository.resolve(PAGE_URL).getOrThrow()

    assertEquals(
        FeedResolution.Candidates(
            listOf(
                FeedCandidate("https://example.com/posts.rss", "Posts"),
                FeedCandidate("https://example.com/comments.atom", "Comments"),
            )),
        resolution,
    )
    assertEquals(1, http.requests.size)
  }

  @Test
  fun `page without feed links fails as no feed found`() = runTest {
    http.respond(PAGE_URL, FeedFixtures.html("""<link rel="stylesheet" href="/style.css">"""))

    val error = repository.resolve(PAGE_URL).exceptionOrNull()

    assertIs<NoFeedFoundException>(error)
    assertEquals(PAGE_URL, error.pageUrl)
  }

  @Test
  fun `relative feed link is resolved against the url reached after redirects`() = runTest {
    http.respond(
        "https://example.com/",
        FeedFixtures.html(
            """
            <link rel="alternate" type="application/rss+xml" href="feed.xml">
            <link rel="alternate" type="application/atom+xml" href="atom.xml">
            """
                .trimIndent()),
        finalUrl = "https://www.example.com/blog/",
    )

    val resolution =
        assertIs<FeedResolution.Candidates>(repository.resolve("https://example.com/").getOrThrow())

    assertEquals(
        listOf("https://www.example.com/blog/feed.xml", "https://www.example.com/blog/atom.xml"),
        resolution.candidates.map { it.url },
    )
  }

  @Test
  fun `feed reached after a redirect keeps the requested url and resolves links against the final one`() =
      runTest {
        http.respond(
            FEED_URL,
            FeedFixtures.RSS,
            FeedFixtures.RSS_CONTENT_TYPE,
            finalUrl = "https://cdn.example.com/feeds/main.xml",
        )

        val resolution = assertIs<FeedResolution.Direct>(repository.resolve(FEED_URL).getOrThrow())

        assertEquals(FEED_URL, resolution.feedUrl)
        assertEquals(FEED_URL, resolution.feed.url)
        assertEquals(FEED_URL, resolution.feed.items.single().feedUrl)
        assertEquals("https://cdn.example.com/feeds/posts/1", resolution.feed.items.single().link)
      }

  @Test
  fun `http input is retried as https`() = runTest {
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val resolution =
        assertIs<FeedResolution.Direct>(
            repository.resolve("http://example.com/feed.xml").getOrThrow())

    assertEquals(FEED_URL, resolution.feedUrl)
    assertEquals(listOf("https"), http.requests.map { it.url.scheme })
  }

  @Test
  fun `http input whose https counterpart fails is reported as http not supported`() = runTest {
    http.fail(FEED_URL, IOException("connection refused"))

    val error = repository.resolve("http://example.com/feed.xml").exceptionOrNull()

    assertIs<HttpFeedNotSupportedException>(error)
  }

  @Test
  fun `input without a scheme is fetched over https`() = runTest {
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val resolution =
        assertIs<FeedResolution.Direct>(repository.resolve("example.com/feed.xml").getOrThrow())

    assertEquals(FEED_URL, resolution.feedUrl)
  }

  @Test
  fun `single http feed link on a page is followed over https`() = runTest {
    http.respond(
        PAGE_URL,
        FeedFixtures.html(
            """<link rel="alternate" type="application/rss+xml" href="http://example.com/feed.xml">"""),
    )
    http.respond(FEED_URL, FeedFixtures.RSS, FeedFixtures.RSS_CONTENT_TYPE)

    val resolution = assertIs<FeedResolution.Direct>(repository.resolve(PAGE_URL).getOrThrow())

    assertEquals(FEED_URL, resolution.feedUrl)
    assertEquals(listOf("https", "https"), http.requests.map { it.url.scheme })
  }

  @Test
  fun `single feed link that leads to another page fails without following further`() = runTest {
    val linkToNext =
        """<link rel="alternate" type="application/rss+xml" href="https://example.com/next">"""
    http.respond(PAGE_URL, FeedFixtures.html(linkToNext))
    http.respond("https://example.com/next", FeedFixtures.html(linkToNext))

    val error = repository.resolve(PAGE_URL).exceptionOrNull()

    assertIs<UnsupportedFeedFormatException>(error)
    assertEquals(2, http.requests.size)
  }

  @Test
  fun `non-2xx response fails`() = runTest {
    http.respond(PAGE_URL, "Server Error", code = 500)

    val error = repository.resolve(PAGE_URL).exceptionOrNull()

    assertIs<HttpStatusException>(error)
  }

  @Test
  fun `IO exception fails with that exception`() = runTest {
    val ioError = IOException("timeout")
    http.fail(PAGE_URL, ioError)

    val error = repository.resolve(PAGE_URL).exceptionOrNull()

    assertSame(ioError, error)
  }

  companion object {
    private const val PAGE_URL = "https://example.com/blog"
    private const val FEED_URL = "https://example.com/feed.xml"
  }
}
