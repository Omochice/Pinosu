package io.github.omochice.pinosu.feature.shareintent.domain.usecase

import android.content.Intent
import io.github.omochice.pinosu.feature.shareintent.domain.model.SharedContent
import org.junit.Assert.*
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
}
