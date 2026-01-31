package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import android.content.Intent
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Test class for ExtractSharedContentUseCase
 *
 * Verifies intent classification: null/invalid intents return null, ACTION_SEND text/plain intents
 * with URLs produce SharedContent(url=...), and non-URL text produces SharedContent(comment=...).
 */
@RunWith(RobolectricTestRunner::class)
class ExtractSharedContentUseCaseTest {

  private lateinit var useCase: ExtractSharedContentUseCase

  @Before
  fun setup() {
    useCase = ExtractSharedContentUseCaseImpl()
  }

  @Test
  fun `null intent returns null`() {
    val result = useCase(null)
    assertNull(result)
  }

  @Test
  fun `ACTION_VIEW intent returns null`() {
    val intent =
        Intent(Intent.ACTION_VIEW).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND with image MIME type returns null`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(Intent.EXTRA_TEXT, "https://example.com")
        }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND text plain with null EXTRA_TEXT returns null`() {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND text plain with empty string returns null`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "")
        }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND text plain with whitespace only returns null`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "   ")
        }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND text plain with https URL returns SharedContent with url`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "https://example.com/article")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://example.com/article", comment = null), result)
  }

  @Test
  fun `ACTION_SEND text plain with http URL returns SharedContent with url`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "http://example.com/page")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "http://example.com/page", comment = null), result)
  }

  @Test
  fun `ACTION_SEND text plain with uppercase HTTPS URL returns SharedContent with url`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "HTTPS://EXAMPLE.COM/PATH")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "HTTPS://EXAMPLE.COM/PATH", comment = null), result)
  }

  @Test
  fun `ACTION_SEND text plain with non-URL text returns SharedContent with comment`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "This is a great article about Nostr")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = null, comment = "This is a great article about Nostr"), result)
  }

  @Test
  fun `ACTION_SEND without MIME type returns null`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, "https://example.com") }

    val result = useCase(intent)
    assertNull(result)
  }

  @Test
  fun `ACTION_SEND text plain with CharSequence EXTRA_TEXT extracts URL`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, StringBuilder("https://example.com/charseq") as CharSequence)
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://example.com/charseq", comment = null), result)
  }

  @Test
  fun `ACTION_SEND with charset-parameterized MIME type extracts URL`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain; charset=UTF-8"
          putExtra(Intent.EXTRA_TEXT, "https://example.com/charset")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://example.com/charset", comment = null), result)
  }

  @Test
  fun `URL after text extracts both url and comment`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "Check out https://example.com")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://example.com", comment = "Check out"), result)
  }

  @Test
  fun `URL before text extracts both url and comment`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "https://example.com is great")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://example.com", comment = "is great"), result)
  }

  @Test
  fun `URL in middle of text extracts both url and comment`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "See https://example.com/path for details")
        }

    val result = useCase(intent)
    assertEquals(
        SharedContent(url = "https://example.com/path", comment = "See for details"), result)
  }

  @Test
  fun `multiple URLs extracts first URL only`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "https://a.com and https://b.com")
        }

    val result = useCase(intent)
    assertEquals(SharedContent(url = "https://a.com", comment = "and https://b.com"), result)
  }

  @Test
  fun `URL with query params in text extracts full URL`() {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"
          putExtra(Intent.EXTRA_TEXT, "Check https://example.com/p?q=1&r=2 out")
        }

    val result = useCase(intent)
    assertEquals(
        SharedContent(url = "https://example.com/p?q=1&r=2", comment = "Check out"), result)
  }
}
