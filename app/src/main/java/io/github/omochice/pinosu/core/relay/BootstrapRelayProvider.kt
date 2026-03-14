package io.github.omochice.pinosu.core.relay

/**
 * Provider for bootstrap relay configurations used when fetching NIP-65 relay list metadata.
 *
 * Returns the default relay combined with any user-configured additional relays.
 */
interface BootstrapRelayProvider {

  /**
   * Get the list of bootstrap relay configurations.
   *
   * @return List of relay configurations including the default and user-configured relays
   */
  fun getBootstrapRelays(): List<RelayConfig>
}
