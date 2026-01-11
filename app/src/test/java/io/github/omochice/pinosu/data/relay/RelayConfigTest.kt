package io.github.omochice.pinosu.data.relay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

  @Test
  fun testEquality_SameValues() {
    val config1 = RelayConfig(url = "wss://relay.example.com", read = true, write = true)
    val config2 = RelayConfig(url = "wss://relay.example.com", read = true, write = true)

    assertEquals("Configs with same values should be equal", config1, config2)
    assertEquals("Hash codes should match", config1.hashCode(), config2.hashCode())
  }

  @Test
  fun testEquality_DifferentUrl() {
    val config1 = RelayConfig(url = "wss://relay1.example.com")
    val config2 = RelayConfig(url = "wss://relay2.example.com")

    assertNotEquals("Configs with different URLs should not be equal", config1, config2)
  }

  @Test
  fun testEquality_DifferentReadWrite() {
    val config1 = RelayConfig(url = "wss://relay.example.com", read = true, write = true)
    val config2 = RelayConfig(url = "wss://relay.example.com", read = false, write = true)

    assertNotEquals("Configs with different read values should not be equal", config1, config2)
  }

  @Test
  fun testCopy() {
    val original = RelayConfig(url = "wss://relay.example.com", read = true, write = true)
    val copied = original.copy(write = false)

    assertEquals("URL should be preserved", "wss://relay.example.com", copied.url)
    assertTrue("read should be preserved", copied.read)
    assertFalse("write should be updated", copied.write)
  }
}
