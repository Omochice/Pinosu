package io.github.omochice.pinosu.core.relay

/**
 * Provider for bootstrap relay configurations used when fetching NIP-65 relay list metadata.
 *
 * Returns user-configured relays when available, otherwise falls back to default bootstrap relays.
 */
interface BootstrapRelayProvider {

  /**
   * Get the list of bootstrap relay configurations.
   *
   * @return List of user-configured relay configurations, or defaults if none are configured
   */
  fun getBootstrapRelays(): List<RelayConfig>
}
