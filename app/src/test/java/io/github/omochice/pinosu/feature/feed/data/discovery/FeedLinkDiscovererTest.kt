package io.github.omochice.pinosu.feature.feed.data.discovery

import io.github.omochice.pinosu.feature.feed.data.http.FeedFixtures
import io.github.omochice.pinosu.feature.feed.domain.model.FeedCandidate
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedLinkDiscovererTest {

  private fun discover(head: String, pageUrl: String = PAGE_URL): List<FeedCandidate> =
      FeedLinkDiscoverer.discover(FeedFixtures.html(head).byteInputStream(), "utf-8", pageUrl)

  @Test
  fun `RSS and Atom alternate links are found in document order with their titles`() {
    val candidates =
        discover(
            """
            <link rel="alternate" type="application/rss+xml" title="RSS" href="https://example.com/rss">
            <link rel="alternate" type="application/atom+xml" title="Atom" href="https://example.com/atom">
            """
                .trimIndent())

    assertEquals(
        listOf(
            FeedCandidate("https://example.com/rss", "RSS"),
            FeedCandidate("https://example.com/atom", "Atom"),
        ),
        candidates,
    )
  }

  @Test
  fun `rel and type are matched regardless of case`() {
    val candidates =
        discover(
            """<LINK REL="Alternate" TYPE="Application/RSS+XML" HREF="https://example.com/rss">""")

    assertEquals(listOf("https://example.com/rss"), candidates.map { it.url })
  }

  @Test
  fun `rel with several tokens matches when one of them is alternate`() {
    val candidates =
        discover(
            """<link rel="home  alternate" type="application/atom+xml" href="https://example.com/atom">""")

    assertEquals(listOf("https://example.com/atom"), candidates.map { it.url })
  }

  @Test
  fun `type with parameters still matches`() {
    val candidates =
        discover(
            """<link rel="alternate" type="application/rss+xml; charset=utf-8" href="https://example.com/rss">""")

    assertEquals(listOf("https://example.com/rss"), candidates.map { it.url })
  }

  @Test
  fun `links that are not feed alternates are ignored`() {
    val candidates =
        discover(
            """
            <link rel="stylesheet" type="text/css" href="/style.css">
            <link rel="alternate" type="text/html" hreflang="ja" href="/ja/">
            <link rel="alternateish" type="application/rss+xml" href="/not-a-token">
            <link rel="alternate" href="/no-type">
            """
                .trimIndent())

    assertEquals(emptyList(), candidates)
  }

  @Test
  fun `relative href is resolved against the page url`() {
    val candidates =
        discover(
            """<link rel="alternate" type="application/rss+xml" href="feed.xml">""",
            pageUrl = "https://www.example.com/blog/",
        )

    assertEquals(listOf("https://www.example.com/blog/feed.xml"), candidates.map { it.url })
  }

  @Test
  fun `missing or blank title gives null`() {
    val candidates =
        discover(
            """
            <link rel="alternate" type="application/rss+xml" href="/rss">
            <link rel="alternate" type="application/atom+xml" title=" " href="/atom">
            """
                .trimIndent())

    assertEquals(listOf(null, null), candidates.map { it.title })
  }

  @Test
  fun `blank href is skipped and duplicate urls are kept once`() {
    val candidates =
        discover(
            """
            <link rel="alternate" type="application/rss+xml" href="">
            <link rel="alternate" type="application/rss+xml" title="First" href="/rss">
            <link rel="alternate" type="application/rss+xml" title="Again" href="https://example.com/rss">
            """
                .trimIndent())

    assertEquals(listOf(FeedCandidate("https://example.com/rss", "First")), candidates)
  }

  companion object {
    private const val PAGE_URL = "https://example.com/"
  }
}
