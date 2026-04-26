package io.github.omochice.pinosu.core.nip.nipb0

import org.junit.Assert.assertEquals
import org.junit.Test

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
