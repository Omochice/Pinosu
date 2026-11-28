package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebLinkTest {

  @Test
  fun `http and https links in any letter case are web links`() {
    listOf("http://example.com/a", "https://example.com/a", "HTTPS://example.com/a").forEach {
      assertTrue(isWebLink(it), it)
    }
  }

  @Test
  fun `links that would launch another app or run script are not web links`() {
    listOf(
            "intent://scan/#Intent;scheme=zxing;end",
            "nostrsigner:{}",
            "javascript:alert(1)",
            "mailto:someone@example.com",
            "file:///sdcard/a",
            "/relative/path",
            "")
        .forEach { assertFalse(isWebLink(it), it) }
  }

  @Test
  fun `a web link is handed to the browser`() {
    val opened = mutableListOf<String>()

    val result = openWebLink(RecordingUriHandler(opened), "https://example.com/a")

    assertTrue(result)
    assertEquals(listOf("https://example.com/a"), opened)
  }

  @Test
  fun `a link that is not a web link never reaches the browser`() {
    val opened = mutableListOf<String>()

    val result = openWebLink(RecordingUriHandler(opened), "intent://scan/#Intent;end")

    assertFalse(result)
    assertTrue(opened.isEmpty())
  }

  @Test
  fun `a web link that no app can open is reported as not opened`() {
    val failing =
        object : UriHandler {
          override fun openUri(uri: String) = throw IllegalArgumentException("no activity")
        }

    assertFalse(openWebLink(failing, "https://example.com/a"))
  }

  private class RecordingUriHandler(private val opened: MutableList<String>) : UriHandler {
    override fun openUri(uri: String) {
      opened += uri
    }
  }
}
