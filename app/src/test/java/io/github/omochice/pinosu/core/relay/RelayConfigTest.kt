package io.github.omochice.pinosu.core.relay

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Unit tests for [RelayConfig] data class */
class RelayConfigTest {

  @Test
  fun `constructor with all parameters should set values correctly`() {
    val config = RelayConfig(url = "wss://relay.example.com", read = true, write = false)

    assertEquals("wss://relay.example.com", config.url, "URL should match")
    assertTrue(config.read, "read should be true")
    assertFalse(config.write, "write should be false")
  }

  @Test
  fun `constructor without optional parameters should use default values`() {
    val config = RelayConfig(url = "wss://relay.example.com")

    assertEquals("wss://relay.example.com", config.url, "URL should match")
    assertTrue(config.read, "read should default to true")
    assertTrue(config.write, "write should default to true")
  }
}
