package io.github.omochice.pinosu.data.relay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [RelayConfig] data class */
class RelayConfigTest {

  @Test
  fun testConstructorWithAllParameters() {
    val config = RelayConfig(url = "wss://relay.example.com", read = true, write = false)

    assertEquals("URL should match", "wss://relay.example.com", config.url)
    assertTrue("read should be true", config.read)
    assertFalse("write should be false", config.write)
  }

  @Test
  fun testDefaultValues() {
    val config = RelayConfig(url = "wss://relay.example.com")

    assertEquals("URL should match", "wss://relay.example.com", config.url)
    assertTrue("read should default to true", config.read)
    assertTrue("write should default to true", config.write)
  }
}
