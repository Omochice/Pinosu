package io.github.omochice.pinosu.feature.feed.data.repository

import io.github.omochice.pinosu.core.model.NostrEvent
import io.github.omochice.pinosu.core.nip.nip78.Nip78
import io.github.omochice.pinosu.core.nip.nip89.ClientTagRepository
import io.github.omochice.pinosu.core.relay.FakeRelayPool
import io.github.omochice.pinosu.core.relay.PublishResult
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.core.relay.RelayPool
import io.github.omochice.pinosu.core.relay.RelayQueryResult
import io.github.omochice.pinosu.core.relay.RelayTermination
import io.github.omochice.pinosu.feature.feed.data.codec.FeedSubscriptionListCodec
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscription
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionList
import io.github.omochice.pinosu.feature.feed.domain.model.FeedSubscriptionLoadResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class RelayFeedSubscriptionRepositoryTest {

  private lateinit var relayPool: FakeRelayPool
  private lateinit var relayListProvider: RelayListProvider
  private lateinit var clientTagEnabled: MutableStateFlow<Boolean>
  private lateinit var repository: RelayFeedSubscriptionRepository

  @BeforeTest
  fun setup() {
    relayPool = FakeRelayPool()
    relayListProvider = mockk()
    coEvery { relayListProvider.getUserRelays() } returns listOf(RELAY_A, RELAY_B)
    clientTagEnabled = MutableStateFlow(false)
    val clientTagRepository = mockk<ClientTagRepository>()
    every { clientTagRepository.clientTagEnabledFlow } returns clientTagEnabled
    repository =
        RelayFeedSubscriptionRepository(
            relayPool = relayPool,
            relayListProvider = relayListProvider,
            clientTagRepository = clientTagRepository,
            codec = FeedSubscriptionListCodec(),
            clock = Clock.fixed(Instant.ofEpochSecond(NOW), ZoneOffset.UTC),
        )
  }

  @Test
  fun `load without user relays is unavailable and queries nothing`() = runTest {
    coEvery { relayListProvider.getUserRelays() } returns emptyList()

    val result = repository.loadSubscriptionList(AUTHOR)

    assertEquals(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.NO_USER_RELAYS),
        result)
    assertTrue(relayPool.queryCallArgs.isEmpty())
  }

  @Test
  fun `load queries only the write relays when any relay is marked write`() = runTest {
    val readOnly = RelayConfig(url = "wss://read.example.com", read = true, write = false)
    val writeOnly = RelayConfig(url = "wss://write.example.com", read = false, write = true)
    coEvery { relayListProvider.getUserRelays() } returns listOf(readOnly, writeOnly)

    repository.loadSubscriptionList(AUTHOR)

    assertEquals(listOf(writeOnly), relayPool.queryCallArgs.single().relays)
  }

  @Test
  fun `load queries every user relay when none is marked write`() = runTest {
    val first = RelayConfig(url = "wss://first.example.com", read = true, write = false)
    val second = RelayConfig(url = "wss://second.example.com", read = true, write = false)
    coEvery { relayListProvider.getUserRelays() } returns listOf(first, second)

    repository.loadSubscriptionList(AUTHOR)

    assertEquals(listOf(first, second), relayPool.queryCallArgs.single().relays)
  }

  @Test
  fun `load sends one filter for the author's Pinosu subscription list`() = runTest {
    repository.loadSubscriptionList(AUTHOR)

    val call = relayPool.queryCallArgs.single()
    assertEquals(
        Json.parseToJsonElement(
            """{"kinds":[30078],"authors":["$AUTHOR"],"#d":["pinosu/rss-subscriptions"]}"""),
        Json.parseToJsonElement(call.filters.single()))
    assertEquals(RelayPool.PER_RELAY_TIMEOUT_MS, call.timeoutMs)
  }

  @Test
  fun `load returns the newest list across relays`() = runTest {
    answer(RELAY_A, RelayTermination.Eose, listEvent(listOf(FEED_A), createdAt = NOW - 100))
    answer(RELAY_B, RelayTermination.Eose, listEvent(listOf(FEED_B), createdAt = NOW - 10))

    val result = repository.loadSubscriptionList(AUTHOR)

    assertEquals(
        FeedSubscriptionLoadResult.Found(FeedSubscriptionList(feeds(FEED_B), NOW - 10)), result)
  }

  @Test
  fun `load ignores newer events that are not the author's subscription list`() = runTest {
    answer(
        RELAY_A,
        RelayTermination.Eose,
        listEvent(listOf(FEED_A), createdAt = NOW - 100),
        listEvent(listOf(FEED_B), createdAt = NOW - 1, pubkey = OTHER_AUTHOR),
        listEvent(listOf(FEED_B), createdAt = NOW - 1, identifier = "other-app/settings"),
    )

    val result = repository.loadSubscriptionList(AUTHOR)

    assertEquals(
        FeedSubscriptionLoadResult.Found(FeedSubscriptionList(feeds(FEED_A), NOW - 100)), result)
  }

  @Test
  fun `load counts a list received from a relay that later timed out`() = runTest {
    answer(RELAY_A, RelayTermination.TimedOut, listEvent(listOf(FEED_A), createdAt = NOW))
    answer(RELAY_B, RelayTermination.Failed("connection reset"))

    val result = repository.loadSubscriptionList(AUTHOR)

    assertEquals(FeedSubscriptionLoadResult.Found(FeedSubscriptionList(feeds(FEED_A), NOW)), result)
  }

  @Test
  fun `load is absent when every relay answers EOSE without a list`() = runTest {
    assertEquals(FeedSubscriptionLoadResult.Absent, repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is absent when the only events returned are foreign`() = runTest {
    answer(RELAY_A, RelayTermination.Eose, listEvent(listOf(FEED_A), pubkey = OTHER_AUTHOR))

    assertEquals(FeedSubscriptionLoadResult.Absent, repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is absent when one relay answers EOSE and another refuses`() = runTest {
    answer(RELAY_A, RelayTermination.Eose)
    answer(RELAY_B, RelayTermination.Closed("auth-required: sign in first"))

    assertEquals(FeedSubscriptionLoadResult.Absent, repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is unavailable for access when every relay refuses`() = runTest {
    answer(RELAY_A, RelayTermination.Closed("auth-required: sign in first"))
    answer(RELAY_B, RelayTermination.Closed("restricted: members only"))

    assertEquals(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.ACCESS_DENIED),
        repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is unreachable when every relay fails or times out`() = runTest {
    answer(RELAY_A, RelayTermination.Failed("connection refused"))
    answer(RELAY_B, RelayTermination.TimedOut)

    assertEquals(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.UNREACHABLE),
        repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is unreachable when refusals are mixed with failures`() = runTest {
    answer(RELAY_A, RelayTermination.Closed("auth-required: sign in first"))
    answer(RELAY_B, RelayTermination.TimedOut)

    assertEquals(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.UNREACHABLE),
        repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `load is unreachable when relays close the query for reasons other than access`() = runTest {
    answer(RELAY_A, RelayTermination.Closed("error: shutting down"))
    answer(RELAY_B, RelayTermination.Closed("rate-limited: slow down"))

    assertEquals(
        FeedSubscriptionLoadResult.Unavailable(FeedSubscriptionLoadResult.Reason.UNREACHABLE),
        repository.loadSubscriptionList(AUTHOR))
  }

  @Test
  fun `create builds the list event dated now when there is no previous list`() {
    val event = repository.createSubscriptionListEvent(AUTHOR, feeds(FEED_A, FEED_B), null)

    assertEquals(NOW, event.createdAt)
    assertEquals(Nip78.KIND_APP_DATA, event.kind)
    assertEquals(AUTHOR, event.pubkey)
    assertEquals(
        listOf(
            listOf("d", "pinosu/rss-subscriptions"),
            listOf("url", FEED_A),
            listOf("url", FEED_B),
        ),
        event.tags)
  }

  @Test
  fun `create dates the event after a previous list that is not older than now`() {
    val previous = FeedSubscriptionList(feeds(FEED_A), createdAt = NOW + 30)

    val event = repository.createSubscriptionListEvent(AUTHOR, feeds(FEED_B), previous)

    assertEquals(NOW + 31, event.createdAt)
  }

  @Test
  fun `create adds the client tag when it is enabled`() {
    clientTagEnabled.value = true

    val event = repository.createSubscriptionListEvent(AUTHOR, feeds(FEED_A), null)

    assertEquals(listOf("client", "Pinosu"), event.tags.last())
  }

  @Test
  fun `publish sends the signed event to the user's relays through the relay pool`() = runTest {
    val published = PublishResult("event-id", listOf(RELAY_A.url), emptyList())
    relayPool.publishResult = Result.success(published)

    val result = repository.publishSubscriptionList(SIGNED_JSON)

    assertEquals(Result.success(published), result)
    val call = relayPool.publishCallArgs.single()
    assertEquals(listOf(RELAY_A, RELAY_B), call.relays)
    assertEquals(SIGNED_JSON, call.signedEventJson)
    assertEquals(RelayPool.PER_RELAY_TIMEOUT_MS, call.timeoutMs)
  }

  @Test
  fun `publish without user relays fails and publishes nothing`() = runTest {
    coEvery { relayListProvider.getUserRelays() } returns emptyList()

    val result = repository.publishSubscriptionList(SIGNED_JSON)

    assertTrue(result.isFailure)
    assertTrue(relayPool.publishCallArgs.isEmpty())
  }

  private fun answer(relay: RelayConfig, termination: RelayTermination, vararg events: NostrEvent) {
    relayPool.queryResults[relay.url] = RelayQueryResult(relay.url, events.toList(), termination)
  }

  private fun listEvent(
      feedUrls: List<String>,
      createdAt: Long = NOW,
      pubkey: String = AUTHOR,
      identifier: String = "pinosu/rss-subscriptions",
  ): NostrEvent =
      NostrEvent(
          id = "$pubkey-$identifier-$createdAt",
          pubkey = pubkey,
          createdAt = createdAt,
          kind = Nip78.KIND_APP_DATA,
          tags = listOf(listOf("d", identifier)) + feedUrls.map { listOf("url", it) },
          content = "",
          sig = "sig")

  private fun feeds(vararg feedUrls: String): List<FeedSubscription> =
      feedUrls.map(::FeedSubscription)

  private companion object {
    val AUTHOR = "a".repeat(64)
    val OTHER_AUTHOR = "b".repeat(64)
    const val NOW = 1_700_000_000L
    const val FEED_A = "https://a.example.com/feed.xml"
    const val FEED_B = "https://b.example.com/atom.xml"
    const val SIGNED_JSON = """{"id":"event-id"}"""
    val RELAY_A = RelayConfig(url = "wss://a.example.com")
    val RELAY_B = RelayConfig(url = "wss://b.example.com")
  }
}
