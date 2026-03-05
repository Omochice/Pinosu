package io.github.omochice.pinosu.feature.auth.data

import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [RelayListProvider] implementation backed by cached relay list in [LocalAuthDataSource]
 *
 * Falls back to [DEFAULT_RELAY_URL] when no cached relays are available.
 */
@Singleton
class CachedRelayListProvider
@Inject
constructor(private val localAuthDataSource: LocalAuthDataSource) : RelayListProvider {

  override suspend fun getRelays(): List<RelayConfig> {
    return localAuthDataSource.getRelayList()?.takeIf { it.isNotEmpty() }
        ?: listOf(RelayConfig(url = DEFAULT_RELAY_URL))
  }

  companion object {
    private const val DEFAULT_RELAY_URL = "wss://yabu.me"
  }
}
