package io.github.omochice.pinosu.core.nip.nip65

import kotlin.test.Test
import kotlin.test.assertEquals

class Nip65TagTest {

  @Test
  fun `RELAY equals r`() {
    assertEquals("r", Nip65.Tag.RELAY)
  }
}
