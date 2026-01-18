package io.github.omochice.pinosu.data.relay

/**
 * Result of publishing an event to relays
 *
 * @property eventId The ID of the published event
 * @property successfulRelays List of relay URLs that accepted the event
 * @property failedRelays List of pairs (relay URL, failure reason) for relays that rejected
 */
data class PublishResult(
    val eventId: String,
    val successfulRelays: List<String>,
    val failedRelays: List<Pair<String, String>>
)
