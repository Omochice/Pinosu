package io.github.omochice.pinosu.core.relay

import io.github.omochice.pinosu.core.model.NostrEvent

/**
 * Outcome of querying a single relay
 *
 * @property relayUrl URL of the relay that was queried
 * @property events Events received from the relay before [termination], deduplicated by event ID
 * @property termination How the query against this relay ended
 */
data class RelayQueryResult(
    val relayUrl: String,
    val events: List<NostrEvent>,
    val termination: RelayTermination,
)
