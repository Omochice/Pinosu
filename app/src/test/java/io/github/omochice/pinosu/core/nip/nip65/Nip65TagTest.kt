package io.github.omochice.pinosu.core.nip.nip65

import org.junit.Assert.assertEquals
import org.junit.Test

class Nip65TagTest {

  @Test
  fun `RELAY equals r`() {
    assertEquals("r", Nip65.Tag.RELAY)
  }
}
