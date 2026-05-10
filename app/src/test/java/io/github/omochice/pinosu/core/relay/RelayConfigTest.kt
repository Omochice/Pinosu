package io.github.omochice.pinosu.core.relay

import kotlin.test.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/** Unit tests for [RelayConfig] data class */
class RelayConfigTest {

  @Test
  fun `constructor with all parameters should set values correctly`() {
    val config = RelayConfig(url = "wss://relay.example.com", read = true, write = false)

    assertEquals("URL should match", "wss://relay.example.com", config.url)
    assertTrue("read should be true", config.read)
    assertFalse("write should be false", config.write)
  }

  @Test
  fun `constructor without optional parameters should use default values`() {
    val config = RelayConfig(url = "wss://relay.example.com")

    assertEquals("URL should match", "wss://relay.example.com", config.url)
    assertTrue("read should default to true", config.read)
    assertTrue("write should default to true", config.write)
  }
}
