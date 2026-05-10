package io.github.omochice.pinosu.core.nip.nipb0

import kotlin.test.Test
import kotlin.test.assertEquals

class NipB0TagTest {

  @Test
  fun `IDENTIFIER equals d`() {
    assertEquals("d", NipB0.Tag.IDENTIFIER)
  }

  @Test
  fun `CATEGORY equals t`() {
    assertEquals("t", NipB0.Tag.CATEGORY)
  }

  @Test
  fun `URL equals r`() {
    assertEquals("r", NipB0.Tag.URL)
  }

  @Test
  fun `TITLE equals title`() {
    assertEquals("title", NipB0.Tag.TITLE)
  }
}
