package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class UrlBoundaryTest {

  private fun trimmed(text: String): String {
    val range = GREEDY_URL.find(text)!!.range
    return text.substring(trimmedUrlRange(text, range))
  }

  @Test
  fun `drops trailing sentence punctuation`() {
    assertEquals("https://example.com/a", trimmed("read https://example.com/a, thanks"))
  }

  @Test
  fun `drops a closing bracket that has no opening within the match`() {
    assertEquals("https://example.com/a", trimmed("see (https://example.com/a) now"))
  }

  @Test
  fun `keeps a closing bracket that is balanced within the match`() {
    val url = "https://en.wikipedia.org/wiki/Foo_(bar)"
    assertEquals(url, trimmed(url))
  }

  @Test
  fun `drops multiple trailing characters`() {
    assertEquals("https://example.com/a", trimmed("(https://example.com/a)."))
  }

  @Test
  fun `keeps a URL with no trailing punctuation unchanged`() {
    val url = "https://example.com/path"
    assertEquals(url, trimmed(url))
  }

  companion object {
    private val GREEDY_URL = """https?://\S+""".toRegex(RegexOption.IGNORE_CASE)
  }
}
