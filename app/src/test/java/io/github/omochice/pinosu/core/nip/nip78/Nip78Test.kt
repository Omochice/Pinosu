package io.github.omochice.pinosu.core.nip.nip78

import kotlin.test.Test
import kotlin.test.assertEquals

class Nip78Test {

  @Test
  fun `KIND_APP_DATA equals the NIP-78 kind`() {
    assertEquals("30078", Nip78.KIND_APP_DATA.toString())
  }

  @Test
  fun `Tag IDENTIFIER equals d`() {
    assertEquals("d", Nip78.Tag.IDENTIFIER)
  }
}
