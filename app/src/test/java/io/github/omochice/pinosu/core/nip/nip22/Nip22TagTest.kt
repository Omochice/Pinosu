package io.github.omochice.pinosu.core.nip.nip22

import org.junit.Assert.assertEquals
import org.junit.Test

class Nip22TagTest {

  @Test
  fun `ADDRESS equals a`() {
    assertEquals("a", Nip22.Tag.ADDRESS)
  }

  @Test
  fun `EVENT equals e`() {
    assertEquals("e", Nip22.Tag.EVENT)
  }

  @Test
  fun `KIND equals k`() {
    assertEquals("k", Nip22.Tag.KIND)
  }

  @Test
  fun `PUBKEY equals p`() {
    assertEquals("p", Nip22.Tag.PUBKEY)
  }

  @Test
  fun `ADDRESS_ROOT equals A`() {
    assertEquals("A", Nip22.Tag.ADDRESS_ROOT)
  }

  @Test
  fun `EVENT_ROOT equals E`() {
    assertEquals("E", Nip22.Tag.EVENT_ROOT)
  }

  @Test
  fun `KIND_ROOT equals K`() {
    assertEquals("K", Nip22.Tag.KIND_ROOT)
  }

  @Test
  fun `PUBKEY_ROOT equals P`() {
    assertEquals("P", Nip22.Tag.PUBKEY_ROOT)
  }
}
