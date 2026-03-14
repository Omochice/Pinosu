package io.github.omochice.pinosu.feature.auth.data

import io.github.omochice.pinosu.core.nip.nip65.Nip65RelayListFetcherImpl
import io.github.omochice.pinosu.core.relay.RelayConfig
import io.github.omochice.pinosu.core.relay.RelayListProvider
import io.github.omochice.pinosu.feature.auth.data.local.LocalAuthDataSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [RelayListProvider] implementation backed by cached relay list in [LocalAuthDataSource]
 *
 * Falls back to [Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS] when no cached relays are
 * available.
 */
@Singleton
class CachedRelayListProvider
@Inject
constructor(private val localAuthDataSource: LocalAuthDataSource) : RelayListProvider {

  override suspend fun getRelays(): List<RelayConfig> {
    val relays = localAuthDataSource.getRelayList()
    return if (relays.isNullOrEmpty()) {
      Nip65RelayListFetcherImpl.DEFAULT_BOOTSTRAP_RELAY_URLS.map { RelayConfig(url = it) }
    } else {
      relays
    }
  }
}
