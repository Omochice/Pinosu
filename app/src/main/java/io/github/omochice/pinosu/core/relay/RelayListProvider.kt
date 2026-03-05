package io.github.omochice.pinosu.core.relay

/**
 * Provider for relay list used in Nostr relay queries
 *
 * Abstracts relay list retrieval so that core layer classes can obtain relay configurations without
 * depending on feature-layer data sources.
 */
interface RelayListProvider {

  /**
   * Retrieve relay list for queries, with a default fallback
   *
   * @return List of relay configurations, never empty
   */
  suspend fun getRelays(): List<RelayConfig>
}
