package io.github.omochice.pinosu.core.nip.nip89

import org.junit.Assert.assertEquals
import org.junit.Test

class Nip89Test {

  @Test
  fun `clientTag returns client tag with Pinosu as client name`() {
    val tag = Nip89.clientTag()

    assertEquals(listOf("client", "Pinosu"), tag)
  }

  @Test
  fun `Tag CLIENT equals client`() {
    assertEquals("client", Nip89.Tag.CLIENT)
  }
}
