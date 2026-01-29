package io.github.omochice.pinosu.core.relay

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.omochice.pinosu.data.model.NostrEvent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FakeRelayPoolTest {

  private lateinit var fakeRelayPool: FakeRelayPool

  @Before
  fun setup() {
    fakeRelayPool = FakeRelayPool()
  }

  @Test
  fun subscribeWithTimeout_returnsConfiguredEvents() = runTest {
    val event = createTestEvent()
    fakeRelayPool.eventsToReturn = listOf(event)

    val result =
        fakeRelayPool.subscribeWithTimeout(
            relays = listOf(RelayConfig("wss://relay.example.com")),
            filter = """{"kinds":[1]}""",
            timeoutMs = 1000)

    assertEquals(listOf(event), result)
  }

  @Test
  fun subscribeWithTimeout_recordsCallArguments() = runTest {
    val relays =
        listOf(RelayConfig("wss://relay1.example.com"), RelayConfig("wss://relay2.example.com"))
    val filter = """{"kinds":[1],"limit":10}"""
    val timeoutMs = 5000L

    fakeRelayPool.subscribeWithTimeout(relays, filter, timeoutMs)

    assertEquals(1, fakeRelayPool.subscribeCallArgs.size)
    val call = fakeRelayPool.subscribeCallArgs.first()
    assertEquals(relays, call.relays)
    assertEquals(filter, call.filter)
    assertEquals(timeoutMs, call.timeoutMs)
  }

  @Test
  fun publishEvent_returnsConfiguredResult() = runTest {
    val expectedResult =
        PublishResult(
            eventId = "abc123",
            successfulRelays = listOf("wss://relay.example.com"),
            failedRelays = emptyList())
    fakeRelayPool.publishResult = Result.success(expectedResult)

    val result =
        fakeRelayPool.publishEvent(
            relays = listOf(RelayConfig("wss://relay.example.com", write = true)),
            signedEventJson = """{"id":"abc123"}""",
            timeoutMs = 1000)

    assertTrue(result.isSuccess)
    assertEquals(expectedResult, result.getOrNull())
  }

  @Test
  fun publishEvent_recordsCallArguments() = runTest {
    val relays = listOf(RelayConfig("wss://relay.example.com", write = true))
    val signedEventJson = """{"id":"test","pubkey":"pk","content":"test"}"""
    val timeoutMs = 3000L

    fakeRelayPool.publishEvent(relays, signedEventJson, timeoutMs)

    assertEquals(1, fakeRelayPool.publishCallArgs.size)
    val call = fakeRelayPool.publishCallArgs.first()
    assertEquals(relays, call.relays)
    assertEquals(signedEventJson, call.signedEventJson)
    assertEquals(timeoutMs, call.timeoutMs)
  }

  @Test
  fun publishEvent_returnsFailureWhenConfigured() = runTest {
    val exception = Exception("Network error")
    fakeRelayPool.publishResult = Result.failure(exception)

    val result =
        fakeRelayPool.publishEvent(
            relays = listOf(RelayConfig("wss://relay.example.com", write = true)),
            signedEventJson = """{"id":"abc123"}""",
            timeoutMs = 1000)

    assertTrue(result.isFailure)
    assertEquals("Network error", result.exceptionOrNull()?.message)
  }

  @Test
  fun reset_clearsAllState() = runTest {
    val event = createTestEvent()
    fakeRelayPool.eventsToReturn = listOf(event)
    fakeRelayPool.publishResult =
        Result.success(
            PublishResult(
                eventId = "test",
                successfulRelays = listOf("wss://relay.example.com"),
                failedRelays = emptyList()))
    fakeRelayPool.subscribeWithTimeout(emptyList(), "{}", 1000)
    fakeRelayPool.publishEvent(emptyList(), "{}", 1000)

    fakeRelayPool.reset()

    assertTrue(fakeRelayPool.eventsToReturn.isEmpty())
    assertTrue(fakeRelayPool.subscribeCallArgs.isEmpty())
    assertTrue(fakeRelayPool.publishCallArgs.isEmpty())
  }

  private fun createTestEvent(): NostrEvent {
    return NostrEvent(
        id = "test-event-id",
        pubkey = "test-pubkey",
        createdAt = System.currentTimeMillis() / 1000,
        kind = 1,
        tags = emptyList(),
        content = "Test content")
  }
}
