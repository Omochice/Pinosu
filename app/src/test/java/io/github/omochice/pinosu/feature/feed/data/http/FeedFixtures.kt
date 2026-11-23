package io.github.omochice.pinosu.feature.feed.data.http

/** Documents served by [OkHttpStub] in feed fetching tests */
internal object FeedFixtures {

  const val RSS_CONTENT_TYPE = "application/rss+xml; charset=utf-8"

  /** An RSS 2.0 feed titled "Example Blog" with one item whose link is relative */
  val RSS =
      """
      <?xml version="1.0" encoding="utf-8"?>
      <rss version="2.0">
        <channel>
          <title>Example Blog</title>
          <link>https://example.com/</link>
          <item>
            <title>First post</title>
            <link>posts/1</link>
          </item>
        </channel>
      </rss>
      """
          .trimIndent()

  /** An HTML page whose head holds the given markup */
  fun html(head: String): String =
      """
      <!DOCTYPE html>
      <html>
        <head>
          <title>Example</title>
          $head
        </head>
        <body><p>Hello</p></body>
      </html>
      """
          .trimIndent()
}
