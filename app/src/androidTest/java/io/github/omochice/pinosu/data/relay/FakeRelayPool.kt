package io.github.omochice.pinosu.data.relay

import io.github.omochice.pinosu.data.model.NostrEvent

/**
 * Fake implementation of RelayPool for testing
 *
 * Allows configuring return values and recording method calls for verification in tests.
 */
class FakeRelayPool : RelayPool {

  /** Events to return from subscribeWithTimeout */
  var eventsToReturn: List<NostrEvent> = emptyList()

  /** Result to return from publishEvent */
  var publishResult: Result<PublishResult> =
      Result.success(
          PublishResult(eventId = "", successfulRelays = emptyList(), failedRelays = emptyList()))

  /** Recorded calls to subscribeWithTimeout */
  val subscribeCallArgs = mutableListOf<SubscribeCall>()

  /** Recorded calls to publishEvent */
  val publishCallArgs = mutableListOf<PublishCall>()

  data class SubscribeCall(val relays: List<RelayConfig>, val filter: String, val timeoutMs: Long)

  data class PublishCall(
      val relays: List<RelayConfig>,
      val signedEventJson: String,
      val timeoutMs: Long
  )

  override suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filter: String,
      timeoutMs: Long
  ): List<NostrEvent> {
    subscribeCallArgs.add(SubscribeCall(relays, filter, timeoutMs))
    return eventsToReturn
  }

  override suspend fun publishEvent(
      relays: List<RelayConfig>,
      signedEventJson: String,
      timeoutMs: Long
  ): Result<PublishResult> {
    publishCallArgs.add(PublishCall(relays, signedEventJson, timeoutMs))
    return publishResult
  }

  /** Resets all configured values and recorded calls */
  fun reset() {
    eventsToReturn = emptyList()
    publishResult =
        Result.success(
            PublishResult(eventId = "", successfulRelays = emptyList(), failedRelays = emptyList()))
    subscribeCallArgs.clear()
    publishCallArgs.clear()
  }
}
