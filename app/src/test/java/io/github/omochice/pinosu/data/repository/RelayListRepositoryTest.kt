package io.github.omochice.pinosu.data.repository

import io.github.omochice.pinosu.data.model.NostrEvent
import io.github.omochice.pinosu.data.relay.Nip65RelayListParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for NIP-65 relay list parsing
 *
 * Tests the parsing of kind:10002 events to extract relay configurations according to NIP-65
 * specification.
 */
class RelayListRepositoryTest {

  private fun createRelayListEvent(
      pubkey: String = "testpubkey",
      tags: List<List<String>> = emptyList()
  ): NostrEvent {
    return NostrEvent(
        id = "test-event-id",
        pubkey = pubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = Nip65RelayListParser.KIND_RELAY_LIST,
        tags = tags,
        content = "")
  }

  @Test
  fun `parseRelayListFromEvent with read-write relay should return RelayConfig with both flags true`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertTrue("read should be true", result[0].read)
    assertTrue("write should be true", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with read-only relay should return RelayConfig with read true and write false`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "read")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertTrue("read should be true", result[0].read)
    assertFalse("write should be false", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with write-only relay should return RelayConfig with read false and write true`() {
    val event = createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "write")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
    assertFalse("read should be false", result[0].read)
    assertTrue("write should be true", result[0].write)
  }

  @Test
  fun `parseRelayListFromEvent with multiple relays should return all relay configs`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("r", "wss://relay1.example.com"),
                    listOf("r", "wss://relay2.example.com", "read"),
                    listOf("r", "wss://relay3.example.com", "write")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 3 relays", 3, result.size)

    val relay1 = result.find { it.url == "wss://relay1.example.com" }
    assertTrue("relay1 should be read-write", relay1?.read == true && relay1.write)

    val relay2 = result.find { it.url == "wss://relay2.example.com" }
    assertTrue("relay2 should be read-only", relay2?.read == true && relay2.write == false)

    val relay3 = result.find { it.url == "wss://relay3.example.com" }
    assertTrue("relay3 should be write-only", relay3?.read == false && relay3.write == true)
  }

  @Test
  fun `parseRelayListFromEvent with empty tags should return empty list`() {
    val event = createRelayListEvent(tags = emptyList())

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertTrue("Should return empty list", result.isEmpty())
  }

  @Test
  fun `parseRelayListFromEvent should ignore non-r tags`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("e", "event-id"),
                    listOf("p", "pubkey"),
                    listOf("r", "wss://relay.example.com"),
                    listOf("d", "identifier")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent should ignore r tags without URL`() {
    val event =
        createRelayListEvent(tags = listOf(listOf("r"), listOf("r", "wss://relay.example.com")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertEquals("URL should match", "wss://relay.example.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent should ignore invalid URLs`() {
    val event =
        createRelayListEvent(
            tags =
                listOf(
                    listOf("r", "not-a-valid-url"),
                    listOf("r", "wss://valid.relay.com"),
                    listOf("r", "http://http-relay.com"),
                    listOf("r", "")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 valid relay", 1, result.size)
    assertEquals("URL should be wss://valid.relay.com", "wss://valid.relay.com", result[0].url)
  }

  @Test
  fun `parseRelayListFromEvent with unknown marker should default to read-write`() {
    val event =
        createRelayListEvent(tags = listOf(listOf("r", "wss://relay.example.com", "unknown")))

    val result = Nip65RelayListParser.parseRelayListFromEvent(event)

    assertEquals("Should have 1 relay", 1, result.size)
    assertTrue("read should be true for unknown marker", result[0].read)
    assertTrue("write should be true for unknown marker", result[0].write)
  }
}

/**
 * Unit tests for RelayListRepositoryImpl
 *
 * Tests the relay connectivity filtering and caching behavior.
 */
class RelayListRepositoryImplTest {

  private lateinit var relayPool: io.github.omochice.pinosu.data.relay.RelayPool
  private lateinit var localAuthDataSource: io.github.omochice.pinosu.data.local.LocalAuthDataSource
  private lateinit var repository: RelayListRepository

  private val testPubkey = "npub1" + "a".repeat(59)
  private val testHexPubkey = "3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"

  @org.junit.Before
  fun setup() {
    relayPool = io.mockk.mockk(relaxed = true)
    localAuthDataSource = io.mockk.mockk(relaxed = true)
    repository = RelayListRepositoryImpl(relayPool, localAuthDataSource)
  }

  private fun createRelayListEvent(tags: List<List<String>>): NostrEvent {
    return NostrEvent(
        id = "test-event-id",
        pubkey = testHexPubkey,
        createdAt = System.currentTimeMillis() / 1000,
        kind = Nip65RelayListParser.KIND_RELAY_LIST,
        tags = tags,
        content = "")
  }

  @org.junit.Test
  fun `fetchAndCacheUserRelays should only cache connectable relays`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(
                listOf("r", "wss://connectable1.example.com"),
                listOf("r", "wss://not-connectable.example.com"),
                listOf("r", "wss://connectable2.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://connectable1.example.com", any())
        } returns true
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://not-connectable.example.com", any())
        } returns false
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://connectable2.example.com", any())
        } returns true

        val result = repository.fetchAndCacheUserRelays(testPubkey)

        assertTrue("Should be successful", result.isSuccess)
        val relays = result.getOrNull()!!
        assertEquals("Should have 2 connectable relays", 2, relays.size)
        assertTrue(
            "Should contain connectable1",
            relays.any { it.url == "wss://connectable1.example.com" })
        assertTrue(
            "Should contain connectable2",
            relays.any { it.url == "wss://connectable2.example.com" })
        assertFalse(
            "Should not contain non-connectable",
            relays.any { it.url == "wss://not-connectable.example.com" })
      }

  @org.junit.Test
  fun `fetchAndCacheUserRelays should limit to 5 relays`() =
      kotlinx.coroutines.test.runTest {
        val tags = (1..8).map { i -> listOf("r", "wss://relay$i.example.com") }
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns true

        val result = repository.fetchAndCacheUserRelays(testPubkey)

        assertTrue("Should be successful", result.isSuccess)
        val relays = result.getOrNull()!!
        assertEquals("Should have max 5 relays", 5, relays.size)
      }

  @org.junit.Test
  fun `fetchAndCacheUserRelays should prioritize read-write relays`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(
                listOf("r", "wss://read-only1.example.com", "read"),
                listOf("r", "wss://read-write1.example.com"),
                listOf("r", "wss://write-only1.example.com", "write"),
                listOf("r", "wss://read-write2.example.com"),
                listOf("r", "wss://read-only2.example.com", "read"),
                listOf("r", "wss://read-write3.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns true

        val result = repository.fetchAndCacheUserRelays(testPubkey)

        assertTrue("Should be successful", result.isSuccess)
        val relays = result.getOrNull()!!
        assertEquals("Should have 5 relays", 5, relays.size)

        val readWriteCount = relays.count { it.read && it.write }
        assertEquals("All 3 read-write relays should be included", 3, readWriteCount)
      }

  @org.junit.Test
  fun `fetchAndCacheUserRelays should return empty list when all relays are unreachable`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(
                listOf("r", "wss://relay1.example.com"),
                listOf("r", "wss://relay2.example.com"),
                listOf("r", "wss://relay3.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns false

        val result = repository.fetchAndCacheUserRelays(testPubkey)

        assertTrue("Should be successful", result.isSuccess)
        val relays = result.getOrNull()!!
        assertTrue("Should return empty list", relays.isEmpty())
      }

  @org.junit.Test
  fun `fetchAndCacheUserRelays should not save when no connectable relays`() =
      kotlinx.coroutines.test.runTest {
        val tags = listOf(listOf("r", "wss://relay1.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns false

        repository.fetchAndCacheUserRelays(testPubkey)

        io.mockk.coVerify(exactly = 0) { localAuthDataSource.saveRelayList(any()) }
      }

  @org.junit.Test
  fun `streamConnectableRelays should emit cumulative list as relays become connectable`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(
                listOf("r", "wss://relay1.example.com"),
                listOf("r", "wss://relay2.example.com"),
                listOf("r", "wss://relay3.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://relay1.example.com", any())
        } returns true
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://relay2.example.com", any())
        } returns true
        io.mockk.coEvery {
          relayPool.checkRelayConnectivity("wss://relay3.example.com", any())
        } returns true

        val emissions = mutableListOf<List<io.github.omochice.pinosu.data.relay.RelayConfig>>()
        repository.streamConnectableRelays(testPubkey).collect { emissions.add(it) }

        assertTrue("Should have emissions", emissions.isNotEmpty())
        val finalEmission = emissions.last()
        assertEquals("Final emission should have 3 relays", 3, finalEmission.size)
      }

  @org.junit.Test
  fun `streamConnectableRelays should limit to 5 relays max`() =
      kotlinx.coroutines.test.runTest {
        val tags = (1..8).map { i -> listOf("r", "wss://relay$i.example.com") }
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns true

        val emissions = mutableListOf<List<io.github.omochice.pinosu.data.relay.RelayConfig>>()
        repository.streamConnectableRelays(testPubkey).collect { emissions.add(it) }

        val finalEmission = emissions.last()
        assertEquals("Should have max 5 relays", 5, finalEmission.size)
      }

  @org.junit.Test
  fun `streamConnectableRelays should emit empty list when no connectable relays`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(listOf("r", "wss://relay1.example.com"), listOf("r", "wss://relay2.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns false

        val emissions = mutableListOf<List<io.github.omochice.pinosu.data.relay.RelayConfig>>()
        repository.streamConnectableRelays(testPubkey).collect { emissions.add(it) }

        assertTrue("Should complete", true)
        if (emissions.isNotEmpty()) {
          assertTrue("Last emission should be empty", emissions.last().isEmpty())
        }
      }

  @org.junit.Test
  fun `streamConnectableRelays should emit empty when NIP-65 event not found`() =
      kotlinx.coroutines.test.runTest {
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns emptyList()

        val emissions = mutableListOf<List<io.github.omochice.pinosu.data.relay.RelayConfig>>()
        repository.streamConnectableRelays(testPubkey).collect { emissions.add(it) }

        assertTrue("Should complete", true)
        if (emissions.isNotEmpty()) {
          assertTrue("Should emit empty list", emissions.last().isEmpty())
        }
      }

  @org.junit.Test
  fun `streamConnectableRelays should prioritize read-write relays`() =
      kotlinx.coroutines.test.runTest {
        val tags =
            listOf(
                listOf("r", "wss://read-only1.example.com", "read"),
                listOf("r", "wss://read-write1.example.com"),
                listOf("r", "wss://write-only1.example.com", "write"),
                listOf("r", "wss://read-write2.example.com"),
                listOf("r", "wss://read-only2.example.com", "read"),
                listOf("r", "wss://read-write3.example.com"))
        val event = createRelayListEvent(tags)
        io.mockk.coEvery { relayPool.subscribeWithTimeout(any(), any(), any()) } returns
            listOf(event)
        io.mockk.coEvery { relayPool.checkRelayConnectivity(any(), any()) } returns true

        val emissions = mutableListOf<List<io.github.omochice.pinosu.data.relay.RelayConfig>>()
        repository.streamConnectableRelays(testPubkey).collect { emissions.add(it) }

        val finalEmission = emissions.last()
        assertEquals("Should have 5 relays", 5, finalEmission.size)
        val readWriteCount = finalEmission.count { it.read && it.write }
        assertEquals("All 3 read-write relays should be included", 3, readWriteCount)
      }
}
