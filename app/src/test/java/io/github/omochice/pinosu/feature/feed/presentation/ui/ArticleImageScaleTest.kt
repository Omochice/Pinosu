package io.github.omochice.pinosu.feature.feed.presentation.ui

import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArticleImageScaleTest {

  @Test
  fun `a photo narrower than the screen is laid out at its aspect ratio`() {
    assertEquals(2f, articleImageAspectRatio(Size(400f, 200f)))
  }

  @Test
  fun `a photo wider than the screen is laid out at its aspect ratio`() {
    assertEquals(0.5f, articleImageAspectRatio(Size(2000f, 4000f)))
  }

  @Test
  fun `an icon keeps its own size`() {
    assertNull(articleImageAspectRatio(Size(32f, 32f)))
  }

  @Test
  fun `a tracking pixel keeps its own size`() {
    assertNull(articleImageAspectRatio(Size(1f, 1f)))
  }

  @Test
  fun `an image of unknown size keeps its own size`() {
    assertNull(articleImageAspectRatio(Size.Unspecified))
  }

  @Test
  fun `an image without height keeps its own size`() {
    assertNull(articleImageAspectRatio(Size(400f, 0f)))
  }
}
