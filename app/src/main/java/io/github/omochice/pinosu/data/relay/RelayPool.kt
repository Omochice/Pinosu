package io.github.omochice.pinosu.data.relay

import io.github.omochice.pinosu.data.model.NostrEvent

/**
 * Interface for managing multiple relay connections
 *
 * Queries multiple relays in parallel and merges/deduplicates results.
 */
interface RelayPool {

  /**
   * Subscribe to events from multiple relays with timeout
   *
   * Connects to all specified relays in parallel, collects events, deduplicates by event ID, and
   * returns the merged result.
   *
   * @param relays List of relay configurations to query
   * @param filter Nostr filter as JSON string
   * @param timeoutMs Timeout in milliseconds for each relay
   * @return List of deduplicated events from all relays
   */
  suspend fun subscribeWithTimeout(
      relays: List<RelayConfig>,
      filter: String,
      timeoutMs: Long
  ): List<NostrEvent>
}
